package pro.glideim.sdk;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Function;
import pro.glideim.sdk.api.auth.AuthApi;
import pro.glideim.sdk.api.auth.AuthBean;
import pro.glideim.sdk.api.auth.AuthDto;
import pro.glideim.sdk.api.group.GroupApi;
import pro.glideim.sdk.api.group.JoinGroupDto;
import pro.glideim.sdk.api.user.ProfileBean;
import pro.glideim.sdk.api.user.UserApi;
import pro.glideim.sdk.im.ConnStateListener;
import pro.glideim.sdk.im.IMClient;
import pro.glideim.sdk.im.IMClientImpl;
import pro.glideim.sdk.im.MessageListener;
import pro.glideim.sdk.messages.Actions;
import pro.glideim.sdk.messages.ChatMessage;
import pro.glideim.sdk.messages.CommMessage;
import pro.glideim.sdk.messages.GroupMessage;
import pro.glideim.sdk.messages.GroupNotify;
import pro.glideim.sdk.messages.GroupNotifyMemberChanges;
import pro.glideim.sdk.push.NewContactsMessage;
import pro.glideim.sdk.utils.RxUtils;
import pro.glideim.sdk.utils.SLogger;
import pro.glideim.sdk.ws.WsClient;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class IMAccount implements MessageListener {

    private static final String TAG = "IMAccount";

    private final IMSessionList sessionList = new IMSessionList(this);
    private final IMContactList contactsList = new IMContactList(this);
    private final int device = 1;
    private final List<String> servers = new ArrayList<>();
    public long uid;
    @Nullable
    private IMClient im;
    private ProfileBean profileBean = new ProfileBean();
    private boolean wsAuthed = false;
    private IMMessageListener imMessageListener;
    private final ConnStateListener reLoginConnStateListener = (state, msg) -> {
        if (state == WsClient.STATE_OPENED && !wsAuthed && uid != 0) {
            SLogger.d(TAG, "re-auth due to reconnect...");
            authAccount()
                    .compose(RxUtils.silentScheduler())
                    .zipWith(authIMConnection(), (aBoolean, o) -> true)
                    .doOnError(throwable -> {
                        if (throwable instanceof GlideException) {
                            logout();
                            if (imMessageListener != null) {
                                imMessageListener.onTokenInvalid();
                            }
                        }
                    })
                    .zipWith(sessionList.syncOfflineMsg(), (aBoolean, o) -> true)
                    .subscribe(new SilentObserver<>());
        } else {
            wsAuthed = false;
        }
    };

    public IMAccount(long uid) {
        this.uid = uid;
    }

    public void setImMessageListener(IMMessageListener imMessageListener) {
        this.imMessageListener = imMessageListener;
    }

    public void setServers(List<String> servers) {
        this.servers.addAll(servers);
    }

    // when account just set uid, init im client and account info.
    public Observable<Boolean> initAccountAndConn() {
        String token = GlideIM.getDataStorage().loadToken(uid);
        if (token.isEmpty()) {
            return Observable.error(new Exception("invalid token"));
        }
        Observable<Boolean> initConn = getOrInitConnectedIM().toObservable().map(c -> true);
        Observable<Boolean> initSession = sessionList.init();
        return Observable.zip(initConn, initSession, (c, a) -> true);
    }

    public IMSessionList getIMSessionList() {
        return sessionList;
    }

    public ProfileBean getProfile() {
        return profileBean;
    }

    public Observable<ProfileBean> initUserProfile() {
        return UserApi.API.myProfile()
                .map(RxUtils.bodyConverter())
                .doOnNext(profileBean -> IMAccount.this.profileBean = profileBean);
    }

    public IMContactList getContactsList(){
        return contactsList;
    }

    public List<Long> getContactsGroup() {
        List<Long> g = new ArrayList<>();
        for (IMContact contact : contactsList.getAll()) {
            if (contact.type == 2) {
                g.add(contact.id);
            }
        }
        return g;
    }

    @Nullable
    public IMClient getIMClient() {
        return im;
    }

    public boolean isIMAvailable() {
        return im != null && im.isConnected();
    }

    public Single<List<IMContact>> getContacts() {
        return UserApi.API.getContactsList()
                .map(RxUtils.bodyConverter())
                .flatMap(Observable::fromIterable)
                .map(a -> IMContact.fromContactsBean(a, contactsList, this))
                .doOnNext(contactsList::addContacts)
                .flatMapSingle((Function<IMContact, SingleSource<? extends IMContact>>) IMContact::update)
                .toList();
    }

    public Observable<Boolean> joinGroup(long gid) {
        return GroupApi.API.joinGroup(new JoinGroupDto(gid))
                .map(RxUtils.bodyConverter())
                .map(o -> true);
    }

    public void logout() {

        String token = GlideIM.getDataStorage().loadToken(uid);
        if (!token.isEmpty()) {
            AuthApi.API.logout()
                    .compose(RxUtils.silentScheduler())
                    .doOnComplete(() -> GlideIM.getDataStorage().storeToken(uid, ""))
                    .subscribe(new SilentObserver<>());
        }

        if (!wsAuthed) {
            return;
        }
        if (im == null || !im.isConnected()) {
            return;
        }
        im.removeConnStateListener(reLoginConnStateListener);
        im.disconnect();
    }

    public Observable<String> auth() {
        return authAccount()
                .flatMap((Function<AuthBean, ObservableSource<IMClient>>) authBean ->
                        getOrInitConnectedIM().toObservable()
                )
                .flatMap((Function<IMClient, ObservableSource<Boolean>>) client ->
                        authIMConnection()
                )
                .flatMap((Function<Boolean, ObservableSource<?>>) aBoolean ->
                        initUserProfile()
                )
                .map(s -> "auth success");
    }

    public Observable<AuthBean> authAccount() {
        String token = GlideIM.getDataStorage().loadToken(uid);
        if (token == null) {
            return Observable.error(new Exception("invalid token"));
        }
        SLogger.d(TAG, "authToken: " + uid);
        return AuthApi.API.auth(new AuthDto(token, device))
                .map(RxUtils.bodyConverter())
                .doOnNext(authBean -> this.setServers(authBean.getServers()));
    }

    private Observable<Boolean> authIMConnection() {
        String token = GlideIM.getDataStorage().loadToken(uid);
        if (token.isEmpty()) {
            return Observable.error(new Exception("invalid token"));
        }
        AuthDto d = new AuthDto(token, device);
        SLogger.d(TAG, "auth im connection " + uid);
        return getOrInitConnectedIM()
                .doOnSuccess(client -> client.removeConnStateListener(reLoginConnStateListener))
                .toObservable()
                .flatMap((Function<IMClient, ObservableSource<CommMessage<AuthBean>>>) client ->
                        client.request(Actions.Cli.ACTION_API_USER_AUTH, AuthBean.class, false, d)
                ).map(RxUtils.bodyConverterForWsMsg())
                .doOnError(throwable -> {
                    wsAuthed = false;
                    if (throwable instanceof GlideException) {
                        GlideIM.getDataStorage().storeToken(uid, "");
                    }
                })
                .doOnNext(authBean -> {
                    assert im != null;
                    im.addConnStateListener(reLoginConnStateListener);
                })
                .map(authBean -> {
                    wsAuthed = true;
                    return authBean.getUid() != 0;
                });
    }

    public Single<Boolean> deleteContacts(int type, long id) {
        return Single.create(emitter -> {
            emitter.onSuccess(true);
        });
    }

    private Single<IMClient> getOrInitConnectedIM() {
        if (im != null) {
            if (im.isConnected()) {
                return Single.just(im);
            } else {
                return im.connect().map(s -> im);
            }
        } else {
            if (servers.isEmpty()) {
                return Single.error(new IllegalStateException("the chat server list is empty"));
            }
            Single<IMClient> create = Single.create(emitter -> {
                im = IMClientImpl.create(servers.get(0));
                im.setMessageListener(this);
                emitter.onSuccess(im);
            });
            return create.flatMap(client -> client.connect().map(s -> client));
        }
    }

    @Override
    public void onNewMessage(ChatMessage m) {
        IMMessage ms = IMMessage.fromChatMessage(this, m);
        storeMessage(ms);
        sessionList.onNewMessage(ms);
        if (imMessageListener != null) {
            try {
                imMessageListener.onNewMessage(ms);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onGroupMessage(GroupMessage m) {
        IMMessage ms = IMMessage.fromGroupMessage(this, m);
        if (!sessionList.onNewMessage(ms)) {
            // existed
            return;
        }
        storeMessage(ms);
        if (imMessageListener != null) {
            try {
                imMessageListener.onNewMessage(ms);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void storeMessage(IMMessage ms) {
        GlideIM.getDataStorage().storeMessage(ms);
    }

    @Override
    public void onControlMessage(CommMessage<Object> m) {
        switch (m.getAction()) {
            case Actions.Srv.ACTION_NOTIFY_ERROR:
                if (imMessageListener != null) {
                    imMessageListener.onNotify(m.getData().toString());
                }
                break;
            case Actions.Srv.ACTION_KICK_OUT:
                if (imMessageListener != null) {
                    imMessageListener.onKickOut();
                }
                break;
            case Actions.Srv.ACTION_NEW_CONTACT:
                if (imMessageListener != null) {
                    Type type = new TypeToken<CommMessage<NewContactsMessage>>() {
                    }.getType();
                    CommMessage<NewContactsMessage> s = m.deserialize(type);
                    onNewContacts(s.getData());
                }
                break;
            case Actions.Srv.ACTION_NOTIFY_GROUP:
                Type type = new TypeToken<CommMessage<GroupNotify<GroupNotifyMemberChanges>>>() {
                }.getType();
                CommMessage<GroupNotify<GroupNotifyMemberChanges>> s = m.deserialize(type);
                if (s == null) {
                    SLogger.e(TAG, new NullPointerException("the group notify is null"));
                    return;
                }
                GroupNotify<GroupNotifyMemberChanges> notify = s.getData();
                if (notify == null) {
                    SLogger.e(TAG, new NullPointerException("the group notify is null"));
                    return;
                }
                IMGroupContact group = contactsList.getGroup(notify.getGid());
                if (group == null) {
                    SLogger.e(TAG, "group does not exist");
                    return;
                }
                switch (((int) notify.getType())) {
                    case GroupNotify.TYPE_MEMBER_ADDED:
                        notify.getData().getUid().forEach(group::addMember);
                        break;
                    case GroupNotify.TYPE_MEMBER_REMOVED:
                        IMGroupContact g = contactsList.getGroup(notify.getGid());
                        if (g == null) {
                            SLogger.e(TAG, "the group contact is null");
                            return;
                        }
                        notify.getData().getUid().forEach(uid -> {
                            group.removeMember(uid);
                            if (uid == this.uid) {
                                contactsList.removeGroup(notify.getGid());
                                getIMSessionList().existGroupChat(notify.getGid(), notify);
                            }
                        });
                        break;
                    default:
                        SLogger.d(TAG, "unknown group notify:" + notify.getType());
                        break;
                }
                IMSession groupSes = getIMSessionList().getOrCreate(Constants.SESSION_TYPE_GROUP, notify.getGid());
                groupSes.onNotifyMessage(notify);
                break;
            default:
                SLogger.d(TAG, "unknown action:" + m.getAction());
                break;
        }
    }

    private void onNewContacts(NewContactsMessage co) {
        if (co == null) {
            SLogger.e(TAG, new IllegalStateException("the new contacts message is empty data"));
            return;
        }
        IMSession session = null;
        switch (co.getType()) {
            case Constants.SESSION_TYPE_GROUP:
            case Constants.SESSION_TYPE_USER:
                session = IMSession.create(this, co.getId(), co.getType());
                break;
        }
        if (session != null) {
            sessionList.addOrUpdateSession(session);
        }
        imMessageListener.onNewContact(co);
    }
}
