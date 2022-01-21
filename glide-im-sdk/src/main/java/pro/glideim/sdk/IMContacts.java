package pro.glideim.sdk;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import pro.glideim.sdk.api.group.GetGroupMemberDto;
import pro.glideim.sdk.api.group.GroupApi;
import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.group.GroupMemberBean;
import pro.glideim.sdk.api.user.ContactsBean;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.utils.RxUtils;
import pro.glideim.sdk.utils.SLogger;

public class IMContacts {
    public String title;
    public String avatar;
    public long id;
    public int type;
    private List<GroupMemberBean> members;
    private IMAccount account;

    public static IMContacts fromContactsBean(ContactsBean contactsBean, IMAccount account) {
        IMContacts c = new IMContacts();
        c.type = contactsBean.getType();
        c.id = contactsBean.getId();
        c.title = contactsBean.getRemark();
        c.account = account;
        return c;
    }

    public Single<IMContacts> update() {
        switch (type) {
            case Constants.SESSION_TYPE_USER:
                return GlideIM.getUserInfo(id)
                        .doOnNext(this::setDetail)
                        .toList()
                        .map(userInfoBeans -> IMContacts.this);
            case Constants.SESSION_TYPE_GROUP:
                Observable<List<GroupMemberBean>> getMembers =
                        GroupApi.API.getGroupMember(new GetGroupMemberDto(id))
                                .map(RxUtils.bodyConverter())
                                .doOnNext(this::initGroupMember);
                Observable<GroupInfoBean> getInfo = GlideIM.getGroupInfo(id)
                        .doOnNext(this::setDetail);

                return Observable.concat(getInfo, getMembers)
                        .toList()
                        .map(s -> this);

            default:
                SLogger.d("IMContacts", "unknown contacts type " + type);
                return Single.just(this);
        }
    }

    private void initGroupMember(List<GroupMemberBean> groupMemberBeans) {
        List<Long> uids = new ArrayList<>();
        for (GroupMemberBean m : groupMemberBeans) {
            uids.add(m.getUid());
        }
        GlideIM.getUserInfo(uids)
                .compose(RxUtils.silentScheduler())
                .subscribe(new SilentObserver<List<UserInfoBean>>() {
                    @Override
                    public void onNext(@NonNull List<UserInfoBean> userInfoBeans) {
                        IMSession session = account.getIMSessionList().getSession(Constants.SESSION_TYPE_GROUP, id);
                        if (session != null) {
                            session.onDetailUpdated();
                        }
                    }
                });
        members = groupMemberBeans;
    }

    public List<GroupMemberBean> getMembers() {
        return members;
    }

    private void setDetail(UserInfoBean userInfoBean) {
        title = userInfoBean.getNickname();
        avatar = userInfoBean.getAvatar();
    }

    private void setDetail(GroupInfoBean groupInfoBean) {
        title = groupInfoBean.getName();
        avatar = groupInfoBean.getAvatar();
    }

    @Override
    public String toString() {
        return "IMContacts{" +
                "title='" + title + '\'' +
                ", avatar='" + avatar + '\'' +
                ", id=" + id +
                ", type=" + type +
                '}';
    }
}
