package pro.glideim.sdk;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Function;
import pro.glideim.sdk.api.auth.AuthApi;
import pro.glideim.sdk.api.auth.AuthBean;
import pro.glideim.sdk.api.auth.AuthDto;
import pro.glideim.sdk.api.group.GroupApi;
import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.group.JoinGroupDto;
import pro.glideim.sdk.api.user.ProfileBean;
import pro.glideim.sdk.api.user.UserApi;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.im.ConnStateListener;
import pro.glideim.sdk.im.IMClient;
import pro.glideim.sdk.im.IMClientImpl;
import pro.glideim.sdk.im.MessageListener;
import pro.glideim.sdk.protocol.Actions;
import pro.glideim.sdk.protocol.ChatMessage;
import pro.glideim.sdk.protocol.CommMessage;
import pro.glideim.sdk.utils.RxUtils;
import pro.glideim.sdk.utils.SLogger;
import pro.glideim.sdk.ws.WsClient;

public class IMAccount implements MessageListener {

    private static final String TAG = "IMAccount";

    private final IMSessionList sessionList = new IMSessionList(this);
    private final TreeMap<String, IMContacts> contactsMap = new TreeMap<>();
    private final int device = 1;
    private final List<String> servers = new ArrayList<>();
    public long uid;
    @Nullable
    private IMClient im;
    private ProfileBean profileBean = new ProfileBean();
    private boolean wsAuthed = false;
    private final ConnStateListener reLoginConnStateListener = new ConnStateListener() {
        @Override
        public void onStateChange(int state, String msg) {
            if (state == WsClient.STATE_OPENED && uid > 0) {
                authIMConnection().compose(RxUtils.silentScheduler())
                        .subscribe(new SilentObserver<>());
            } else {
                wsAuthed = false;
            }
        }
    };

    public IMAccount(long uid) {
        this.uid = uid;
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
        sessionList.init();
        return getOrInitConnectedIM().toObservable().map(c -> true);
    }

    public List<IMContacts> updateContacts(@NonNull List<UserInfoBean> userInfoBeans) {
        List<IMContacts> res = new ArrayList<>();
        for (UserInfoBean userInfoBean : userInfoBeans) {
            IMContacts c = contactsMap.get(1 + "_" + userInfoBean.getUid());
            if (c == null) {
                continue;
            }
            c.title = userInfoBean.getNickname();
            c.avatar = userInfoBean.getAvatar();
            c.id = userInfoBean.getUid();
            c.type = 1;
            res.add(c);
        }
        return res;
    }

    public IMSessionList getIMSessionList() {
        return sessionList;
    }

    public List<IMContacts> updateContactsGroup(@NonNull List<GroupInfoBean> groupInfoBeans) {
        List<IMContacts> res = new ArrayList<>();
        for (GroupInfoBean groupInfoBean : groupInfoBeans) {
            IMContacts c = contactsMap.get(2 + "_" + groupInfoBean.getGid());
            if (c == null) {
                continue;
            }
            c.title = groupInfoBean.getName();
            c.avatar = groupInfoBean.getAvatar();
            c.id = groupInfoBean.getGid();
            c.type = 2;
            res.add(c);
        }
        return res;
    }

    public void addContacts(@NonNull List<IMContacts> contacts) {
        for (IMContacts c : contacts) {
            addContacts(c);
        }
    }

    public ProfileBean getProfile() {
        return profileBean;
    }

    public Observable<ProfileBean> initUserProfile() {
        return UserApi.API.myProfile()
                .map(RxUtils.bodyConverter())
                .doOnNext(profileBean -> IMAccount.this.profileBean = profileBean);
    }

    public void addContacts(IMContacts c) {
        contactsMap.put(c.type + "_" + c.id, c);
    }

    public List<IMContacts> getTempContacts() {
        return new ArrayList<>(contactsMap.values());
    }

    public List<Long> getContactsGroup() {
        List<Long> g = new ArrayList<>();
        for (IMContacts contact : getTempContacts()) {
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

    public Observable<List<IMContacts>> getContacts() {
        return UserApi.API.getContactsList()
                .map(RxUtils.bodyConverter())
                .flatMap(Observable::fromIterable)
                .map(IMContacts::fromContactsBean)
                .toList()
                .doOnSuccess(this::addContacts)
                .flatMapObservable((Function<List<IMContacts>, ObservableSource<List<IMContacts>>>) contacts ->
                        updateContactInfo()
                );
    }

    public Observable<Boolean> joinGroup(long gid) {
        return GroupApi.API.joinGroup(new JoinGroupDto(gid))
                .map(RxUtils.bodyConverter())
                .map(o -> true);
    }

    public void logout() {
        GlideIM.getDataStorage().storeToken(uid, "");
        if (!wsAuthed) {
            return;
        }
        if (im == null) {
            return;
        }
        im.request(Actions.Cli.ACTION_API_LOGOUT, Object.class, false, "")
                .compose(RxUtils.silentScheduler())
                .subscribe(new SilentObserver<>());
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
                .toObservable()
                .flatMap((Function<IMClient, ObservableSource<CommMessage<AuthBean>>>) client ->
                        client.request(Actions.Cli.ACTION_API_USER_AUTH, AuthBean.class, false, d)
                ).map(RxUtils.bodyConverterForWsMsg())
                .doOnError(throwable -> {
                    if (throwable instanceof GlideException) {
                        GlideIM.getDataStorage().storeToken(uid, "");
                    }
                })
                .map(authBean -> {
                    wsAuthed = true;
                    return authBean.getUid() != 0;
                });
    }

    private Observable<List<IMContacts>> updateContactInfo() {
        Iterable<IMContacts> idList = getTempContacts();
        List<Observable<IMContacts>> obs = new ArrayList<>();

        Map<Integer, List<Long>> typeIdsMap = new HashMap<>();
        for (IMContacts contacts : idList) {
            if (!typeIdsMap.containsKey(contacts.type)) {
                typeIdsMap.put(contacts.type, new ArrayList<>());
            }
            //noinspection ConstantConditions
            typeIdsMap.get(contacts.type).add(contacts.id);
        }

        typeIdsMap.forEach((type, ids) -> {
            Observable<IMContacts> ob = Observable.empty();
            switch (type) {
                case 1:
                    ob = GlideIM.getUserInfo(ids).map(this::updateContacts).flatMap(Observable::fromIterable);
                    break;
                case 2:
                    ob = GlideIM.getGroupInfo(ids).map(this::updateContactsGroup).flatMap(Observable::fromIterable);
                    break;
            }
            obs.add(ob);
        });

        return Observable.merge(obs).toList().map(s -> this.getTempContacts()).toObservable();
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
                im.addConnStateListener(reLoginConnStateListener);
                emitter.onSuccess(im);
            });
            return create.flatMap(client -> client.connect().map(s -> client));
        }
    }

    @Override
    public void onNewMessage(ChatMessage m) {
        IMMessage ms = IMMessage.fromChatMessage(this, m);
        sessionList.onNewMessage(ms);
    }
}
