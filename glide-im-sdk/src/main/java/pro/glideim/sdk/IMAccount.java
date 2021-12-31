package pro.glideim.sdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import pro.glideim.sdk.api.auth.AuthApi;
import pro.glideim.sdk.api.auth.AuthBean;
import pro.glideim.sdk.api.auth.AuthDto;
import pro.glideim.sdk.api.group.GroupApi;
import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.group.JoinGroupDto;
import pro.glideim.sdk.api.msg.MessageIDBean;
import pro.glideim.sdk.api.msg.MsgApi;
import pro.glideim.sdk.api.user.ProfileBean;
import pro.glideim.sdk.api.user.UserApi;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.im.IMClient;
import pro.glideim.sdk.im.IMClientImpl;
import pro.glideim.sdk.im.MessageListener;
import pro.glideim.sdk.protocol.Actions;
import pro.glideim.sdk.protocol.ChatMessage;
import pro.glideim.sdk.utils.RxUtils;
import pro.glideim.sdk.ws.WsClient;

public class IMAccount implements MessageListener {

    private final IMSessionList sessionList = new IMSessionList(this);
    private final TreeMap<String, IMContacts> contactsMap = new TreeMap<>();
    private final IMClient im;
    private final int device = 1;
    public long uid;
    public ContactsChangeListener contactsChangeListener;
    private ProfileBean profileBean;
    private final List<String> servers = new ArrayList<>();
    private boolean wsAuthed = false;

    public IMAccount(long uid) {
        this.uid = uid;
        this.im = IMClientImpl.create(servers.get(0));
        this.im.addConnStateListener((state, msg) -> {
            if (state == WsClient.STATE_OPENED) {
                authWs().compose(RxUtils.silentScheduler())
                        .subscribe(new SilentObserver<>());
            } else {
                wsAuthed = false;
            }
        });
    }

    public void setServers(List<String> servers){
        this.servers.addAll(servers);
    }

    public Observable<Boolean> initAccountAndConn() {
        String token = GlideIM.getDataStorage().loadToken(uid);
        if (token.isEmpty()) {
            return Observable.error(new Exception("invalid token"));
        }
        sessionList.init();
        return im.connect().flatMapObservable(aBoolean -> authWs());
    }

    public List<IMContacts> updateContacts(List<UserInfoBean> userInfoBeans) {
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

    public List<IMContacts> updateContactsGroup(List<GroupInfoBean> groupInfoBeans) {
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

    public void addContacts(List<IMContacts> contacts) {
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

    public Observable<ProfileBean> auth() {
        String token = GlideIM.getDataStorage().loadToken(uid);
        if (token == null) {
            return Observable.error(new Exception("invalid token"));
        }
        return AuthApi.API.auth(new AuthDto(token, device))
                .map(RxUtils.bodyConverter())
                .doOnNext(authBean -> {
                    this.uid = authBean.getUid();
                    this.servers.clear();
                    this.servers.addAll(authBean.getServers());
                })
                .flatMap((Function<AuthBean, ObservableSource<Boolean>>) aBoolean -> initAccountAndConn())
                .flatMap((Function<Boolean, ObservableSource<ProfileBean>>) aBoolean -> initUserProfile());
    }

    public Observable<IMMessage> sendChatMessage(long to, int type, String content) {

        Observable<ChatMessage> creator = Observable.create(emitter -> {
            ChatMessage message = new ChatMessage();
            message.setMid(0);
            message.setContent(content);
            message.setFrom(uid);
            message.setTo(to);
            message.setType(type);
            message.setState(ChatMessage.STATE_INIT);
            message.setcTime(System.currentTimeMillis() / 1000);
            emitter.onNext(message);
            emitter.onComplete();
        });

        Observable<MessageIDBean> midRequest = MsgApi.API.getMessageID()
                .map(RxUtils.bodyConverter());

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
                                Observable<ChatMessage> ob = im.sendChatMessage(m2);
                                return Observable.concat(Observable.just(m2), ob);
                            });
                    return Observable.concat(init, create);
                })
                .map(chatMessage -> {
                    IMMessage r = IMMessage.fromChatMessage(this, chatMessage);
                    IMSession session = getIMSessionList().getSession(chatMessage.getType(), chatMessage.getTo());
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

    public IMClient getIMClient() {
        return im;
    }

    public void setContactsChangeListener(ContactsChangeListener contactsChangeListener) {
        this.contactsChangeListener = contactsChangeListener;
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
        im.request(Actions.Cli.ACTION_API_LOGOUT, Object.class, false, "")
                .compose(RxUtils.silentScheduler())
                .subscribe(new SilentObserver<>());
    }

    private Observable<Boolean> authWs() {
        String token = GlideIM.getDataStorage().loadToken(uid);
        if (token.isEmpty()) {
            return Observable.error(new Exception("invalid token"));
        }
        AuthDto d = new AuthDto(token, device);
        return im.request(Actions.Cli.ACTION_API_USER_AUTH, AuthBean.class, false, d)
                .map(RxUtils.bodyConverterForWsMsg())
                .doOnError(throwable -> {
                    if (throwable instanceof GlideException) {
                        GlideIM.getDataStorage().storeToken(uid, "");
                    }
                })
                .map(authBean -> {
                    wsAuthed = true;
                    im.setMessageListener(this);
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

    @Override
    public void onNewMessage(ChatMessage m) {
        IMMessage ms = IMMessage.fromChatMessage(this, m);
        sessionList.onNewMessage(ms);
    }
}
