package pro.glideim.sdk;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;
import pro.glideim.sdk.api.msg.AckOfflineMsgDto;
import pro.glideim.sdk.api.msg.MessageBean;
import pro.glideim.sdk.api.msg.MsgApi;
import pro.glideim.sdk.messages.GroupNotify;
import pro.glideim.sdk.messages.GroupNotifyMemberChanges;
import pro.glideim.sdk.utils.RxUtils;
import pro.glideim.sdk.utils.SLogger;

public class IMSessionList {

    public static final String TAG = IMSessionList.class.getSimpleName();

    private final LinkedHashMap<SessionTag, IMSession> sessionMap = new LinkedHashMap<>();
    private final IMAccount account;
    private SessionUpdateListener sessionUpdateListener;
    private ReentrantLock lock = new ReentrantLock();

    IMSessionList(IMAccount account) {
        this.account = account;
    }

    Observable<Boolean> init() {
        return Observable.create(emitter -> {
            List<IMSession> imSessions = GlideIM.getDataStorage().loadSessions(account.uid);
            addOrUpdateSession(imSessions.toArray(new IMSession[]{}));

            for (IMSession ses : imSessions) {
                List<IMMessage> messages = GlideIM.getDataStorage().loadMessage(account.uid, ses.type, ses.to);
                ses.addHistoryMessage(messages);
            }
            emitter.onNext(true);
            emitter.onComplete();
        });
    }

    public void existGroupChat(long id, GroupNotify<GroupNotifyMemberChanges> n) {
        IMSession session = getSession(Constants.SESSION_TYPE_GROUP, id);
        if (session != null) {
            session.onNotifyMessage(n);
            addOrUpdateSession(session);
        }
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
        lock.lock();
        sessionMap.remove(session.tag);
        sessionMap.put(session.tag, session);
        lock.unlock();
        session.addSessionUpdateListener(s -> {
            if (sessionUpdateListener != null) {
                sessionUpdateListener.onUpdate(session);
            }
        });
        GlideIM.getDataStorage().storeSession(account.uid, session);
    }

    private IMSession getSession(SessionTag tag) {
        lock.lock();
        IMSession s = sessionMap.get(tag);
        lock.unlock();
        return s;
    }

    public IMSession getOrCreateSession(SessionTag tag) {
        IMSession session = getSession(tag);
        if (session == null) {
            session = IMSession.create(account, tag.id, tag.type);
            if (session.createAt == session.updateAt) {
                session.syncStatus();
            }
            addOrUpdateSession(session);
        }
        return session;
    }

    public List<IMSession> getSessions() {
        lock.lock();
        Collection<IMSession> values = this.sessionMap.values();
        lock.unlock();
        return new ArrayList<>(values);
    }

    public boolean contain(SessionTag t) {
        lock.lock();
        boolean b = sessionMap.containsKey(t);
        lock.unlock();
        return b;
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
                SLogger.d(TAG, "session add:" + s);
            } else {
                if (oldUpdateAt != s.tag.updateAt) {
                    if (sessionUpdateListener != null) {
                        sessionUpdateListener.onUpdate(s);
                    }
                }
                putSession(s.merge(se));
                SLogger.d(TAG, "session update:" + s);
            }
        }
    }

    boolean onNewMessage(IMMessage message) {
        IMSession session = getOrCreateSession(message.tag);
        if (session.disabled()) {
            return false;
        }
        return session.onNewMessage(message);
    }

    public int getUnread() {
        int unread = 0;
        for (IMSession session : getSessions()) {
            unread += session.unread;
        }
        return unread;
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
