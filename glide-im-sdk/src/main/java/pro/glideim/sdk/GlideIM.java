package pro.glideim.sdk;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import pro.glideim.sdk.api.Response;
import pro.glideim.sdk.api.auth.AuthApi;
import pro.glideim.sdk.api.auth.AuthBean;
import pro.glideim.sdk.api.auth.AuthDto;
import pro.glideim.sdk.api.auth.LoginDto;
import pro.glideim.sdk.api.group.CreateGroupBean;
import pro.glideim.sdk.api.group.CreateGroupDto;
import pro.glideim.sdk.api.group.GetGroupInfoDto;
import pro.glideim.sdk.api.group.GroupApi;
import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.msg.AckOfflineMsgDto;
import pro.glideim.sdk.api.msg.GetChatHistoryDto;
import pro.glideim.sdk.api.msg.MessageBean;
import pro.glideim.sdk.api.msg.MessageIDBean;
import pro.glideim.sdk.api.msg.MsgApi;
import pro.glideim.sdk.api.msg.SessionBean;
import pro.glideim.sdk.api.user.GetUserInfoDto;
import pro.glideim.sdk.api.user.ProfileBean;
import pro.glideim.sdk.api.user.UserApi;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.entity.IMAccount;
import pro.glideim.sdk.entity.ContactsChangeListener;
import pro.glideim.sdk.entity.IMContacts;
import pro.glideim.sdk.entity.IMMessage;
import pro.glideim.sdk.entity.IMSession;
import pro.glideim.sdk.http.RetrofitManager;
import pro.glideim.sdk.im.ConnStateListener;
import pro.glideim.sdk.im.IMClient;
import pro.glideim.sdk.im.IMClientImpl;
import pro.glideim.sdk.protocol.Actions;
import pro.glideim.sdk.protocol.ChatMessage;
import pro.glideim.sdk.protocol.CommMessage;

public class GlideIM {

    private static final IMAccount S_IM_ACCOUNT = new IMAccount();
    private static final IMClient sIM = IMClientImpl.create();
    private static final Map<Long, UserInfoBean> sTempUserInfo = new HashMap<>();
    private static final Map<Long, GroupInfoBean> sTempGroupInfo = new HashMap<>();
    private static final Map<String, SessionBean> sTempSession = new HashMap<>();

    private static GlideIM sInstance;
    private final String wsUrl;
    private DataStorage dataStorage = new DefaultDataStoreImpl();
    private int device = 1;

    private GlideIM(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    public static GlideIM getInstance() {
        return sInstance;
    }

    public static void init(String wsUrl, String baseUrlApi) {
        RetrofitManager.init(baseUrlApi);
        sInstance = new GlideIM(wsUrl);
    }

    public static void init(String wsUrl, String baseUrlApi, ConnStateListener listener) {
        RetrofitManager.init(baseUrlApi);
        sIM.setConnStateListener(listener);
        sInstance = new GlideIM(wsUrl);
    }

    public static Observable<List<IMMessage>> subscribeChatMessageChanges(long to, int type) {
        return Observable.empty();
    }

    public static Observable<IMMessage> sendChatMessage(long to, int type, String content) {

        Observable<ChatMessage> creator = Observable.create(emitter -> {
            ChatMessage message = new ChatMessage();
            message.setMid(0);
            message.setContent(content);
            message.setFrom(getInstance().getMyUID());
            message.setTo(to);
            message.setType(type);
            message.setState(ChatMessage.STATE_INIT);
            message.setcTime(System.currentTimeMillis() / 1000);
            emitter.onNext(message);
            emitter.onComplete();
        });

        Observable<MessageIDBean> midRequest = MsgApi.API.getMessageID()
                .map(bodyConverter());

        return creator
                .flatMap((Function<ChatMessage, ObservableSource<ChatMessage>>) message -> {
                    Observable<ChatMessage> init = Observable.just(message);
                    Observable<ChatMessage> create = Observable.just(message)
                            .zipWith(midRequest, (m1, messageIDBean) -> {
                                m1.setState(ChatMessage.STATE_CREATED);
                                m1.setMid(messageIDBean.getMid());
                                return m1;
                            })
                            .flatMap((Function<ChatMessage, ObservableSource<ChatMessage>>) m2 -> {
                                Observable<ChatMessage> ob = sIM.sendChatMessage(m2);
                                return Observable.concat(Observable.just(m2), ob);
                            });
                    return Observable.concat(init, create);
                })
                .map(chatMessage -> {
                    IMMessage r = IMMessage.fromChatMessage(chatMessage);
                    IMSession session = S_IM_ACCOUNT.getIMSessionList().getSession(chatMessage.getType(), chatMessage.getTo());
                    switch (chatMessage.getState()) {
                        case ChatMessage.STATE_INIT:
                            break;
                        case ChatMessage.STATE_CREATED:
                            session.addMessage(r);
                            break;
                        case ChatMessage.STATE_SRV_RECEIVED:
                            session.onMessageSendSuccess(r);
                            break;
                        case ChatMessage.STATE_RCV_RECEIVED:
                            session.onMessageReceived(r);
                            break;
                    }
                    return r;
                });
    }

    public static Observable<ProfileBean> auth() {
        String token = getInstance().dataStorage.loadToken();
        if (token == null) {
            return Observable.error(new Exception("invalid token"));
        }
        return AuthApi.API.auth(new AuthDto(token, getInstance().device))
                .map(bodyConverter())
                .doOnNext(authBean -> S_IM_ACCOUNT.uid = authBean.getUid())
                .flatMap((Function<AuthBean, ObservableSource<Boolean>>) aBoolean -> authWs())
                .flatMap((Function<Boolean, ObservableSource<ProfileBean>>) aBoolean -> S_IM_ACCOUNT.initUserProfile());
    }

    private static Observable<Boolean> authWs() {
        AuthDto d = new AuthDto(getInstance().dataStorage.loadToken(), getInstance().device);
        return sIM.request(Actions.Cli.ACTION_USER_AUTH, AuthBean.class, false, d)
                .map(bodyConverterForWsMsg())
                .map(authBean -> {
                    return authBean.getUid() != 0;
                });
    }

    public static Observable<Boolean> login(String account, String password, int device) {
        return AuthApi.API.login(new LoginDto(account, password, device))
                .map(bodyConverter())
                .flatMap((Function<AuthBean, ObservableSource<Boolean>>) authBean -> {
                    getInstance().dataStorage.storeToken(authBean.getToken());
                    S_IM_ACCOUNT.uid = authBean.getUid();
                    return authWs();
                });
    }

    public static void onContactsChange(ContactsChangeListener listener) {
        S_IM_ACCOUNT.contactsChangeListener = listener;
    }

    public static Observable<List<MessageBean>> getOfflineMessage() {
        return MsgApi.API.getOfflineMsg()
                .map(bodyConverter())
                .doOnNext(messageBeans -> {
                    List<Long> mids = new ArrayList<>();
                    for (MessageBean b : messageBeans) {
                        mids.add(b.getMid());
                    }
                    MsgApi.API.ackOfflineMsg(new AckOfflineMsgDto(mids)).subscribe(new Observer<Response<Object>>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                        }

                        @Override
                        public void onNext(@NonNull Response<Object> objectResponse) {
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
                });
    }

    public static Observable<List<IMMessage>> getChatMessageHistory(long uid, long beforeMid) {

        return MsgApi.API.getChatMessageHistory(new GetChatHistoryDto(uid, beforeMid))
                .map(bodyConverter())
                .flatMap((Function<List<MessageBean>, ObservableSource<MessageBean>>) Observable::fromIterable)
                .map(IMMessage::fromMessage)
                .toList()
                .toObservable();
    }

    public static Observable<List<IMContacts>> getContacts() {
        return UserApi.API.getContactsList()
                .map(bodyConverter())
                .flatMap(Observable::fromIterable)
                .map(IMContacts::fromContactsBean)
                .toList()
                .doOnSuccess(S_IM_ACCOUNT::addContacts)
                .flatMapObservable((Function<List<IMContacts>, ObservableSource<List<IMContacts>>>) contacts ->
                        updateContactInfo()
                );
    }

    public static Observable<GroupInfoBean> getGroupInfo(long gid) {
        if (sTempGroupInfo.containsKey(gid)) {
            return Observable.just(sTempGroupInfo.get(gid));
        }
        return GroupApi.API.getGroupInfo(new GetGroupInfoDto(Collections.singletonList(gid)))
                .map(bodyConverter())
                .map(groupInfo -> {
                    GroupInfoBean g = groupInfo.get(0);
                    sTempGroupInfo.put(g.getGid(), g);
                    return g;
                });
    }

    public static Observable<List<GroupInfoBean>> getGroupInfo(List<Long> gid) {

        final List<GroupInfoBean> temped = new ArrayList<>();
        List<Long> filtered = new ArrayList<>();
        for (Long id : gid) {
            if (!sTempGroupInfo.containsKey(id)) {
                filtered.add(id);
            } else {
                temped.add(sTempGroupInfo.get(id));
            }
        }
        if (filtered.isEmpty()) {
            return Observable.just(temped);
        }
        Observable<Response<List<GroupInfoBean>>> s = GroupApi.API.getGroupInfo(new GetGroupInfoDto(filtered));
        return s.map(bodyConverter()).map(r -> {
            for (GroupInfoBean u : r) {
                sTempGroupInfo.put(u.getGid(), u);
            }
            temped.addAll(r);
            return r;
        });
    }

    public static Observable<UserInfoBean> getUserInfo(long uid) {
        if (sTempUserInfo.containsKey(uid)) {
            return Observable.just(sTempUserInfo.get(uid));
        }
        return UserApi.API.getUserInfo(new GetUserInfoDto(Collections.singletonList(uid)))
                .map(bodyConverter())
                .map(userInfoBeans -> {
                    UserInfoBean g = userInfoBeans.get(0);
                    sTempUserInfo.put(g.getUid(), g);
                    return g;
                });
    }

    public static Observable<List<UserInfoBean>> getUserInfo(List<Long> uid) {
        final List<UserInfoBean> temped = new ArrayList<>();
        List<Long> filtered = new ArrayList<>();
        for (Long id : uid) {
            if (!sTempUserInfo.containsKey(id)) {
                filtered.add(id);
            } else {
                temped.add(sTempUserInfo.get(id));
            }
        }
        if (filtered.isEmpty()) {
            return Observable.just(temped);
        }
        Observable<Response<List<UserInfoBean>>> s = UserApi.API.getUserInfo(new GetUserInfoDto(filtered));
        return s.map(bodyConverter()).map(r -> {
            for (UserInfoBean u : r) {
                sTempUserInfo.put(u.getUid(), u);
            }
            temped.addAll(r);
            return r;
        });
    }

    public static Observable<CreateGroupBean> createGroup(String name) {
        return GroupApi.API.createGroup(new CreateGroupDto(name))
                .map(bodyConverter());
    }

    public static Observable<List<IMContacts>> updateContactInfo() {
        Iterable<IMContacts> idList = S_IM_ACCOUNT.getContacts();
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
                    ob = getUserInfo(ids).map(S_IM_ACCOUNT::updateContacts).flatMap(Observable::fromIterable);
                    break;
                case 2:
                    ob = getGroupInfo(ids).map(S_IM_ACCOUNT::updateContactsGroup).flatMap(Observable::fromIterable);
                    break;
            }
            obs.add(ob);
        });

        return Observable.merge(obs).toList().map(s -> S_IM_ACCOUNT.getContacts()).toObservable();
    }

    private static <T> Function<Response<T>, T> bodyConverter() {
        return r -> {
            if (!r.success()) {
                throw new Exception(r.getCode() + "," + r.getMsg());
            }
            return r.getData();
        };
    }

    private static <T> Function<CommMessage<T>, T> bodyConverterForWsMsg() {
        return r -> {
            if (!r.success()) {
                throw new Exception("");
            }
            return r.getData();
        };
    }

    public IMAccount getAccount() {
        return S_IM_ACCOUNT;
    }

    public void setConnectionListener(ConnStateListener listener) {
        sIM.setConnStateListener(listener);
    }

    public Single<Boolean> connect() {
        return sIM.connect(this.wsUrl);
    }

    public void disconnect() {
        sIM.disconnect();
    }

    public void setDevice(int device) {
        this.device = device;
    }

    public Long getMyUID() {
        return S_IM_ACCOUNT.uid;
    }

    public DataStorage getDataStorage() {
        return dataStorage;
    }

    public void setDataStorage(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }
}
