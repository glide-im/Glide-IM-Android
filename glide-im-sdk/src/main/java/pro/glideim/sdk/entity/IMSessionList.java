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
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import pro.glideim.sdk.GlideIM;
import pro.glideim.sdk.api.Response;
import pro.glideim.sdk.api.msg.GetGroupMessageStateDto;
import pro.glideim.sdk.api.msg.GetGroupMsgHistoryDto;
import pro.glideim.sdk.api.msg.GetSessionDto;
import pro.glideim.sdk.api.msg.GroupMessageBean;
import pro.glideim.sdk.api.msg.MsgApi;
import pro.glideim.sdk.utils.Logger;
import pro.glideim.sdk.utils.RxUtils;

public class IMSessionList {

    public static final String TAG = IMSessionList.class.getSimpleName();

    private final TreeMap<SessionTag, IMSession> sessionMap = new TreeMap<>();

    private SessionUpdateListener sessionUpdateListener;

    public void setSessionUpdateListener(SessionUpdateListener sessionUpdateListener) {
        this.sessionUpdateListener = sessionUpdateListener;
    }

    public IMSession getSession(int type, long id) {
        SessionTag sessionTag = SessionTag.get(type, id);
        return getOrCreateSession(sessionTag);
    }

    public boolean containSession(int type, long id) {
        return sessionMap.containsKey(SessionTag.get(type, id));
    }

    private void updateSession(IMSession... ses) {

        List<IMSession> newSes = new ArrayList<>();
        for (IMSession se : ses) {
            se.tag.updateAt = se.updateAt;

            IMSession s = getSession(se.tag);
            if (s == null) {
                s = se;
                newSes.add(s);
                addSession(s);
                Logger.d(TAG, "session add:" + s.toString());
            } else {
                sessionMap.remove(s.tag);
                addSession(s.update(s));
                if (sessionUpdateListener != null) {
                    sessionUpdateListener.onUpdate(s);
                }
                Logger.d(TAG, "session update:" + s.toString());
            }
        }
        if (sessionUpdateListener != null && newSes.size() != 0) {
            sessionUpdateListener.onNewSession(newSes.toArray(new IMSession[]{}));
        }
    }


    void addMessage(IMMessage message) {
        IMSession session = getSession(message.getTargetType(), message.getTargetId());
        session.updateAt = message.getSendAt();
        session.addMessage(message);
    }

    private void addSession(IMSession session) {
        session.setOnUpdateListener(this::updateSession);
        session.setIMSessionList(this);
        sessionMap.put(session.tag, session);
    }

    private IMSession getSession(SessionTag tag) {
        return sessionMap.get(tag);
    }

    private IMSession getOrCreateSession(SessionTag tag) {
        IMSession session = sessionMap.get(tag);
        if (session == null) {
            session = IMSession.create(tag.id, tag.type);
            session.initTargetInfo();
            session.setIMSessionList(this);
            addSession(session);
        }
        return session;
    }

    public void setSessionRecentMessages(List<IMMessage> messages) {
        for (IMMessage message : messages) {
            SessionTag sessionTag = SessionTag.get(message.getTargetType(), message.getTargetId());
            getOrCreateSession(sessionTag).addMessage(message);
        }
    }

    public Observable<IMSession> getSession(long id, int type) {
        if (containSession(type, id)) {
            return Observable.just(getSession(type, id));
        }
        if (type == 2) {
            return MsgApi.API.getGroupMessageState(new GetGroupMessageStateDto(id))
                    .map(RxUtils.bodyConverter())
                    .map(stateBean -> {
                        IMSession imSession = IMSession.fromGroupState(stateBean);
                        updateSession(imSession);
                        return imSession;
                    });
        }
        return MsgApi.API.getSession(new GetSessionDto(id)).map(RxUtils.bodyConverter()).map(sessionBean -> {
            IMSession imSession = IMSession.fromSessionBean(GlideIM.getInstance().getMyUID(), sessionBean);
            updateSession(imSession);
            return imSession;
        });
    }

    public Observable<List<IMSession>> getSessionList() {

        Observable<IMSession> groupSession = MsgApi.API.getAllGroupMessageState()
                .map(RxUtils.bodyConverter())
                .flatMap(Observable::fromIterable)
                .map(IMSession::fromGroupState)
                .flatMap((Function<IMSession, ObservableSource<IMSession>>) session ->
                        GlideIM.getGroupInfo(session.to).map(session::setGroupInfo)
                );

        Observable<IMSession> chatSession = MsgApi.API.getRecentSession()
                .map(RxUtils.bodyConverter())
                .flatMap(Observable::fromIterable)
                .map(sessionBean -> IMSession.fromSessionBean(GlideIM.getInstance().getMyUID(), sessionBean))
                .flatMap((Function<IMSession, ObservableSource<IMSession>>) imSession ->
                        GlideIM.getUserInfo(imSession.to).map(imSession::setUserInfo)
                );
        return Observable.merge(groupSession, chatSession)
                .toList()
                .toObservable()
                .doOnNext(new Consumer<List<IMSession>>() {
                    @Override
                    public void accept(List<IMSession> sessions) throws Exception {
                        IMSessionList.this.updateSession(sessions.toArray(new IMSession[]{}));
                    }
                });
    }

    public Single<List<IMSession>> updateSessionList() {

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
                .toList()
                .doOnSuccess(this::setSessionRecentMessages)
                .map(messages -> new ArrayList<>(sessionMap.values()));
    }

    static class SessionTag implements Comparable<SessionTag> {
        private static final Map<String, SessionTag> temp = new HashMap<>();
        int type;
        long id;
        private long updateAt = 0;

        private SessionTag(int type, long id) {
            this.type = type;
            this.id = id;
        }

        public static synchronized SessionTag get(int type, long id) {
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
