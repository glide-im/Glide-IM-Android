package pro.glideim.sdk.entity;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;
import pro.glideim.sdk.GlideIM;
import pro.glideim.sdk.SilentObserver;
import pro.glideim.sdk.api.Response;
import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.msg.GetGroupMessageStateDto;
import pro.glideim.sdk.api.msg.GetGroupMsgHistoryDto;
import pro.glideim.sdk.api.msg.GetSessionDto;
import pro.glideim.sdk.api.msg.GroupMessageBean;
import pro.glideim.sdk.api.msg.MsgApi;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.utils.RxUtils;
import pro.glideim.sdk.utils.SLogger;

public class IMSessionList {

    public static final String TAG = IMSessionList.class.getSimpleName();

    private final TreeMap<SessionTag, IMSession> sessionMap = new TreeMap<>();
    private final List<IMSession> tempNewSession = new ArrayList<>();
    private final List<IMSession> tempUpdateSession = new ArrayList<>();
    private SessionUpdateListener sessionUpdateListener;

    public void setSessionUpdateListener(SessionUpdateListener sessionUpdateListener) {
        this.sessionUpdateListener = sessionUpdateListener;
    }

    public IMSession getSession(int type, long id) {
        SessionTag sessionTag = SessionTag.get(type, id);
        return getOrCreateSession(sessionTag);
    }

    public boolean contain(int type, long id) {
        return sessionMap.containsKey(SessionTag.get(type, id));
    }

    public boolean contain(SessionTag t) {
        return sessionMap.containsKey(t);
    }

    private synchronized void addOrUpdateSession(IMSession... ses) {

        tempNewSession.clear();
        tempUpdateSession.clear();

        for (IMSession se : ses) {
            long oldUpdateAt = se.tag.updateAt;
            se.tag.updateAt = se.updateAt;
            IMSession s = getSession(se.tag);
            if (s == null) {
                s = se;
                tempNewSession.add(s);
                putSession(s);
                SLogger.d(TAG, "session add:" + s.toString());
            } else {
                if (oldUpdateAt != s.tag.updateAt) {
                    tempUpdateSession.add(s);
                }
                sessionMap.remove(s.tag);
                putSession(s.update(s));
                SLogger.d(TAG, "session update:" + s.toString());
            }
        }
        if (sessionUpdateListener != null) {
            if (!tempUpdateSession.isEmpty()) {
                SLogger.d(TAG, "updates session count:" + tempUpdateSession.size());
                sessionUpdateListener.onUpdate(tempUpdateSession.toArray(new IMSession[]{}));
            }
            if (!tempNewSession.isEmpty()) {
                SLogger.d(TAG, "add session count:" + tempNewSession.size());
                sessionUpdateListener.onNewSession(tempNewSession.toArray(new IMSession[]{}));
            }
        }
    }

    void onNewMessage(IMMessage message) {
        IMSession session = getSession(message.tag);
        session.onNewMessage(message);
    }

    private void putSession(IMSession session) {
        session.setOnUpdateListener(this::addOrUpdateSession);
        session.setIMSessionList(this);
        sessionMap.put(session.tag, session);
    }

    private IMSession getSession(SessionTag tag) {
        return sessionMap.get(tag);
    }

    private IMSession getOrCreateSession(SessionTag tag) {
        IMSession session = sessionMap.get(tag);
        if (session == null) {
            session = IMSession.create(tag.id, tag.type, this);
            if (tag.type == 2) {
                GroupInfoBean tempGroupInfo = GlideIM.getTempGroupInfo(tag.id);
                if (tempGroupInfo != null) {
                    session.setInfo(tempGroupInfo);
                } else {
                    MsgApi.API.getGroupMessageState(new GetGroupMessageStateDto(tag.id))
                            .map(RxUtils.bodyConverter())
                            .map(stateBean -> {
                                IMSession imSession = IMSession.fromGroupState(stateBean);
                                addOrUpdateSession(imSession);
                                return imSession;
                            })
                            .compose(RxUtils.silentScheduler())
                            .subscribe(new SilentObserver<>());
                }
            } else if (tag.type == 1) {
                UserInfoBean tempUserInfo = GlideIM.getTempUserInfo(tag.id);
                if (tempUserInfo != null) {
                    session.setInfo(tempUserInfo);
                } else {
                    MsgApi.API.getSession(new GetSessionDto(tag.id))
                            .map(RxUtils.bodyConverter())
                            .map(sessionBean -> {
                                IMSession imSession = IMSession.fromSessionBean(GlideIM.getInstance().getMyUID(), sessionBean);
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

    public Observable<IMSession> getSession(long id, int type) {
        return Observable.just(getOrCreateSession(SessionTag.get(type, id)));
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
                .map(sessions -> true);
    }

    private Observable<IMSession> getSessionList() {

        Observable<IMSession> groupSession = MsgApi.API.getAllGroupMessageState()
                .map(RxUtils.bodyConverter())
                .flatMap(Observable::fromIterable)
                .map(IMSession::fromGroupState)
                .flatMap((Function<IMSession, ObservableSource<IMSession>>) session ->
                        GlideIM.getGroupInfo(session.to).map(session::setInfo)
                );

        Observable<IMSession> chatSession = MsgApi.API.getRecentSession()
                .map(RxUtils.bodyConverter())
                .flatMap(Observable::fromIterable)
                .map(sessionBean -> IMSession.fromSessionBean(GlideIM.getInstance().getMyUID(), sessionBean))
                .flatMap((Function<IMSession, ObservableSource<IMSession>>) session -> session.getHistory(0)
                        .toObservable()
                        .zipWith(Observable.just(session), (messages, session1) -> session1)
                )
                .flatMap((Function<IMSession, ObservableSource<IMSession>>) imSession ->
                        GlideIM.getUserInfo(imSession.to).map(imSession::setInfo)
                );
        return Observable.merge(groupSession, chatSession);
    }

    private Observable<IMSession> initRecentMessages() {

        Observable<IMMessage> chat = MsgApi.API.getRecentChatMessage()
                .map(RxUtils.bodyConverter())
                .flatMap(Observable::fromIterable)
                .map(IMMessage::fromMessage);

        List<Observable<Response<List<GroupMessageBean>>>> gob = new ArrayList<>();
        for (Long gid : GlideIM.getInstance().getAccount().getContactsGroup()) {
            GetGroupMsgHistoryDto d = new GetGroupMsgHistoryDto(gid);
            gob.add(MsgApi.API.getRecentGroupMessage(d));
        }
        Observable<IMMessage> group = Observable.merge(gob)
                .map(RxUtils.bodyConverter())
                .flatMap(Observable::fromIterable)
                .map(IMMessage::fromGroupMessage);

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
                                    IMSession session = IMSession.create(t, this);
                                    session.addMessages(messages);
                                    return session;
                                });
                    }
                });

    }

    static class SessionTag implements Comparable<SessionTag> {
        private static final Map<String, SessionTag> temp = new HashMap<>();
        int type;
        long id;
        long updateAt = 0;

        private SessionTag(int type, long id) {
            this.type = type;
            this.id = id;
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
            return "SessionTag{" +
                    "type=" + type +
                    ", id=" + id +
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
