package pro.glideim.sdk;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;
import pro.glideim.sdk.api.msg.AckOfflineMsgDto;
import pro.glideim.sdk.api.msg.MessageBean;
import pro.glideim.sdk.api.msg.MsgApi;
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

    public IMSession getSession(int type, long id) {
        SessionTag sessionTag = SessionTag.get(type, id);
        return getSession(sessionTag);
    }

    public IMSession getOrCreate(int type, long id) {
        SessionTag sessionTag = SessionTag.get(type, id);
        return getOrCreateSession(sessionTag);
    }

    private void putSession(IMSession session) {
        sessionMap.remove(session.tag);
        sessionMap.put(session.tag, session);
        session.setOnUpdateListener(s -> {
            if (sessionUpdateListener != null) {
                sessionUpdateListener.onUpdate(session);
            }
        });
        session.setIMSessionList(this);
        GlideIM.getDataStorage().storeSession(account.uid, session);
    }

    private IMSession getSession(SessionTag tag) {
        return sessionMap.get(tag);
    }

    public IMSession getOrCreateSession(SessionTag tag) {
        IMSession session = sessionMap.get(tag);
        if (session == null) {
            session = IMSession.create(account, tag.id, tag.type, this);
            addOrUpdateSession(session);
        }
        return session;
    }

    public List<IMSession> getSessions() {
        return new ArrayList<>(this.sessionMap.values());
    }

    public boolean contain(SessionTag t) {
        return sessionMap.containsKey(t);
    }

    public void setSessionUpdateListener(SessionUpdateListener sessionUpdateListener) {
        this.sessionUpdateListener = sessionUpdateListener;
    }

    synchronized void addOrUpdateSession(IMSession... ses) {

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
                putSession(s.merge(se));
                SLogger.d(TAG, "session update:" + s.toString());
            }
        }
    }

    void onNewMessage(IMMessage message) {
        IMSession session = getOrCreateSession(message.tag);
        message.session = session;
        session.onNewMessage(message);
    }

    public Observable<IMSession> loadSessionsList() {

        return Observable.merge(loadChatSession(), loadGroupSession())
                .flatMapSingle((Function<IMSession, SingleSource<IMSession>>) session ->
                        session.loadHistory(0).map(imMessages -> session)
                )
                .flatMap((Function<IMSession, ObservableSource<IMSession>>) IMSession::initInfo)
                .doOnNext(this::addOrUpdateSession)
                .doOnComplete(() ->
                        syncOfflineMsg()
                                .compose(RxUtils.silentScheduler())
                                .subscribe(new SilentObserver<>())
                );
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

    private Observable<IMSession> loadChatSession() {
        return MsgApi.API.getRecentSession()
                .map(RxUtils.bodyConverter())
                .flatMap(Observable::fromIterable)
                .map(stateBean -> {
                    long to = stateBean.getUid1() == account.uid ? stateBean.getUid2() : stateBean.getUid1();
                    SessionTag t = SessionTag.get(Constants.SESSION_TYPE_USER, to);
                    if (contain(t)) {
                        IMSession session = getSession(t);
                        session.update(stateBean);
                        return session;
                    }
                    return IMSession.create(account, stateBean);
                });
    }

    private Observable<IMSession> loadGroupSession() {
        return MsgApi.API.getAllGroupMessageState()
                .map(RxUtils.bodyConverter())
                .flatMap(Observable::fromIterable)
                .map(stateBean -> {
                    SessionTag t = SessionTag.get(Constants.SESSION_TYPE_GROUP, stateBean.getGid());
                    if (contain(t)) {
                        IMSession session = getSession(t);
                        session.update(stateBean);
                        return session;
                    }
                    return IMSession.create(account, stateBean);
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SessionTag that = (SessionTag) o;
            return type == that.type && id == that.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, id);
        }

        @Override
        public int compareTo(SessionTag o) {
            if (this.updateAt == o.updateAt && this.hashCode() == o.hashCode()) {
                return 0;
            }
            long l = this.updateAt - o.updateAt;
            if (l == 0) {
                return this.hashCode() - o.hashCode();
            }
            return (int) l;
        }


    }
}
