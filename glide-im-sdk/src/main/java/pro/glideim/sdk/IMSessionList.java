package pro.glideim.sdk;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;
import pro.glideim.sdk.api.Response;
import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.msg.AckOfflineMsgDto;
import pro.glideim.sdk.api.msg.GetGroupMessageStateDto;
import pro.glideim.sdk.api.msg.GetGroupMsgHistoryDto;
import pro.glideim.sdk.api.msg.GetSessionDto;
import pro.glideim.sdk.api.msg.GroupMessageBean;
import pro.glideim.sdk.api.msg.MessageBean;
import pro.glideim.sdk.api.msg.MsgApi;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.utils.RxUtils;
import pro.glideim.sdk.utils.SLogger;

public class IMSessionList {

    public static final String TAG = IMSessionList.class.getSimpleName();

    private final TreeMap<SessionTag, IMSession> sessionMap = new TreeMap<>();
    private final IMAccount account;
    private SessionUpdateListener sessionUpdateListener;

    IMSessionList(IMAccount account) {
        this.account = account;
    }

    void init() {
        List<IMSession> imSessions = GlideIM.getDataStorage().loadSessions(account.uid);
        addOrUpdateSession(imSessions.toArray(new IMSession[]{}));
    }

    public void setSessionUpdateListener(SessionUpdateListener sessionUpdateListener) {
        this.sessionUpdateListener = sessionUpdateListener;
    }

    public IMSession getSession(int type, long id) {
        SessionTag sessionTag = SessionTag.get(type, id);
        return getOrCreateSession(sessionTag);
    }

    public boolean contain(SessionTag t) {
        return sessionMap.containsKey(t);
    }

    private synchronized void addOrUpdateSession(IMSession... ses) {

        for (IMSession se : ses) {
            long oldUpdateAt = se.tag.updateAt;
            se.tag.updateAt = se.updateAt;
            IMSession s = getSession(se.tag);
            if (s == null) {
                s = se;
                putSession(s);
                if (sessionUpdateListener != null) {
                    sessionUpdateListener.onNewSession(s);
                }
                SLogger.d(TAG, "session add:" + s.toString());
            } else {
                if (oldUpdateAt != s.tag.updateAt) {
                    if (sessionUpdateListener != null) {
                        sessionUpdateListener.onUpdate(s);
                    }
                }
                sessionMap.remove(s.tag);
                putSession(s.merge(se));
                SLogger.d(TAG, "session update:" + s.toString());
            }
        }
    }

    private void onSessionUpdate(IMSession session) {
        sessionMap.remove(session.tag);
        sessionMap.put(session.tag, session);
        if (sessionUpdateListener != null) {
            sessionUpdateListener.onUpdate(session);
        }
    }

    void onNewMessage(IMMessage message) {
        IMSession session = getOrCreateSession(message.tag);
        message.session = session;
        session.onNewMessage(message);
    }

    private void putSession(IMSession session) {
        session.setOnUpdateListener(this::onSessionUpdate);
        session.setIMSessionList(this);
        GlideIM.getDataStorage().storeSession(account.uid, session);
        sessionMap.put(session.tag, session);
    }

    private IMSession getSession(SessionTag tag) {
        return sessionMap.get(tag);
    }

    private IMSession getOrCreateSession(SessionTag tag) {
        IMSession session = sessionMap.get(tag);
        if (session == null) {
            session = IMSession.create(account, tag.id, tag.type, this);
            if (tag.type == 2) {
                GroupInfoBean tempGroupInfo = GlideIM.getDataStorage().loadTempGroupInfo(tag.id);
                if (tempGroupInfo != null) {
                    session.setInfo(tempGroupInfo);
                } else {
                    MsgApi.API.getGroupMessageState(new GetGroupMessageStateDto(tag.id))
                            .map(RxUtils.bodyConverter())
                            .map(stateBean -> {
                                IMSession imSession = IMSession.fromGroupState(account, stateBean);
                                addOrUpdateSession(imSession);
                                return imSession;
                            })
                            .compose(RxUtils.silentScheduler())
                            .subscribe(new SilentObserver<>());
                }
            } else if (tag.type == 1) {
                UserInfoBean tempUserInfo = GlideIM.getDataStorage().loadTempUserInfo(tag.id);
                if (tempUserInfo != null) {
                    session.setInfo(tempUserInfo);
                } else {
                    MsgApi.API.getSession(new GetSessionDto(tag.id))
                            .map(RxUtils.bodyConverter())
                            .map(sessionBean -> {
                                IMSession imSession = IMSession.fromSessionBean(account, sessionBean);
                                addOrUpdateSession(imSession);
                                return imSession;
                            })
                            .compose(RxUtils.silentScheduler())
                            .subscribe(new SilentObserver<>());
                }
            }
            addOrUpdateSession(session);
        }
        return session;
    }

    public List<IMSession> getSessions() {
        return new ArrayList<>(this.sessionMap.values());
    }

    public Single<Boolean> initSessionsList() {
        return getSessionList()
                .toList()
                //                .flatMap((Function<List<IMSession>, SingleSource<List<IMSession>>>) sessions ->
                //                        initRecentMessages()
                //                                .toList()
                //                                .map(s -> {
                //                                    sessions.addAll(s);
                //                                    return sessions;
                //                                })
                //                )
                .doOnSuccess(sessions -> addOrUpdateSession(sessions.toArray(new IMSession[]{})))
                .zipWith(syncOfflineMsg().toList(), (imSessions, objects) -> true);
    }

    private Observable<IMSession> getSessionList() {

        Observable<IMSession> groupSession = MsgApi.API.getAllGroupMessageState()
                .map(RxUtils.bodyConverter())
                .flatMap(Observable::fromIterable)
                .map(stateBean -> IMSession.fromGroupState(account, stateBean));

        Observable<IMSession> chatSession = MsgApi.API.getRecentSession()
                .map(RxUtils.bodyConverter())
                .flatMap(Observable::fromIterable)
                .map(sessionBean -> IMSession.fromSessionBean(account, sessionBean))
                .flatMap((Function<IMSession, ObservableSource<IMSession>>) session -> session.getHistory(0)
                        .toObservable()
                        .zipWith(Observable.just(session), (messages, session1) -> session1)
                );
        return Observable.merge(groupSession, chatSession)
                .flatMap((Function<IMSession, ObservableSource<IMSession>>) IMSession::initInfo);
    }

    Observable<Object> syncOfflineMsg() {
        return MsgApi.API.getOfflineMsg()
                .map(RxUtils.bodyConverter())
                .compose(RxUtils.silentScheduler())
                .flatMap((Function<List<MessageBean>, ObservableSource<MessageBean>>) Observable::fromIterable)
                .map(messageBean -> IMMessage.fromMessage(account, messageBean))
                .groupBy(imMessage -> imMessage.tag)
                .flatMapSingle((Function<GroupedObservable<SessionTag, IMMessage>, SingleSource<List<IMMessage>>>) g -> {
                    SessionTag tag = g.getKey();
                    return g.toList().doOnSuccess(imMessages ->
                            getOrCreateSession(tag).onOfflineMessage(imMessages)
                    );
                })
                .filter(imMessages -> !imMessages.isEmpty())
                .flatMap((Function<List<IMMessage>, ObservableSource<IMMessage>>) Observable::fromIterable)
                .map(IMMessage::getMid)
                .toList()
                .flatMapObservable(longs ->
                        longs.isEmpty()
                                ? Observable.just(true)
                                : MsgApi.API.ackOfflineMsg(new AckOfflineMsgDto(longs))
                );
    }

    private Observable<IMSession> initRecentMessages() {

        Observable<IMMessage> chat = MsgApi.API.getRecentChatMessage()
                .map(RxUtils.bodyConverter())
                .flatMap(Observable::fromIterable)
                .map(messageBean -> IMMessage.fromMessage(account, messageBean));

        List<Observable<Response<List<GroupMessageBean>>>> gob = new ArrayList<>();
        for (Long gid : GlideIM.getAccount().getContactsGroup()) {
            GetGroupMsgHistoryDto d = new GetGroupMsgHistoryDto(gid);
            gob.add(MsgApi.API.getRecentGroupMessage(d));
        }
        Observable<IMMessage> group = Observable.merge(gob)
                .map(RxUtils.bodyConverter())
                .flatMap(Observable::fromIterable)
                .map(messageBean -> IMMessage.fromGroupMessage(account, messageBean));

        return Observable.merge(chat, group)
                .groupBy(message -> message.tag)
                .flatMap((Function<GroupedObservable<SessionTag, IMMessage>, ObservableSource<IMSession>>) go -> {
                    SessionTag t = go.getKey();
                    if (contain(t)) {
                        IMSession s = getSession(go.getKey());
                        return go.toList()
                                .doOnSuccess(s::addMessages)
                                .toObservable()
                                .flatMap((Function<List<IMMessage>, ObservableSource<IMSession>>) messages ->
                                        Observable.empty()
                                );
                    } else {
                        return go.toList()
                                .toObservable()
                                .map(messages -> {
                                    IMSession session = IMSession.create(account, t, this);
                                    session.addMessages(messages);
                                    return session;
                                });
                    }
                });

    }

    static class SessionTag implements Comparable<SessionTag> {
        private static final Map<String, SessionTag> temp = new ConcurrentHashMap<>();
        String tag;
        int type;
        long id;
        long updateAt = 0;

        private SessionTag(int type, long id) {
            this.type = type;
            this.id = id;
            this.tag = type + "@" + id;
        }

        public static synchronized SessionTag get(int type, long id) {
            if (id == 0) {
                throw new IllegalArgumentException("id can not be zero");
            }
            String tag = type + "@" + id;
            if (temp.containsKey(tag)) {
                return temp.get(tag);
            }
            SessionTag r = new SessionTag(type, id);
            temp.put(tag, r);
            return r;
        }

        public int getType() {
            return type;
        }

        public long getId() {
            return id;
        }

        @NonNull
        @Override
        public String toString() {
            return "SessionTag{tag=" + tag +
                    ", updateAt=" + updateAt +
                    '}';
        }

        @Override
        public int compareTo(SessionTag o) {
            if (this.updateAt == o.updateAt) {
                return 0;
            }
            long l = this.updateAt - o.updateAt;
            return (int) l;
        }


    }
}
