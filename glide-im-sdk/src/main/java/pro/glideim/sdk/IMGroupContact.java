package pro.glideim.sdk;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import pro.glideim.sdk.api.group.GetGroupMemberDto;
import pro.glideim.sdk.api.group.GroupApi;
import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.group.GroupMemberBean;
import pro.glideim.sdk.api.user.ContactsBean;
import pro.glideim.sdk.utils.RxUtils;

public class IMGroupContact extends IMContact {

    private final IMAccount account;
    private final LinkedHashMap<Long, GroupMemberBean> members = new LinkedHashMap<>();
    private final IMContactList contactList;

    public IMGroupContact(IMAccount account, IMContactList list, ContactsBean contactsBean) {
        this.account = account;
        this.contactList = list;
        this.type = contactsBean.getType();
        this.id = contactsBean.getId();
        this.title = contactsBean.getRemark();
    }

    public Single<IMGroupContact> update() {
        Observable<List<GroupMemberBean>> getMembers =
                GroupApi.API.getGroupMember(new GetGroupMemberDto(id))
                        .map(RxUtils.bodyConverter())
                        .doOnNext(this::initGroupMember);
        Observable<GroupInfoBean> getInfo = GlideIM.getGroupInfo(id)
                .doOnNext(this::setDetail);

        return Observable.concat(getInfo, getMembers)
                .toList()
                .map(s -> this);
    }

    private void setDetail(GroupInfoBean groupInfoBean) {
        title = groupInfoBean.getName();
        avatar = groupInfoBean.getAvatar();
    }

    void removeMember(long uid) {
        members.remove(uid);
        contactList.onUpdate(this);
    }

    void addMember(long uid) {
        GroupMemberBean m = new GroupMemberBean();
        m.setUid(uid);
        m.setType(Constants.GROUP_MEMBER);
        members.put(uid, m);
        contactList.onUpdate(this);
    }

    private void initGroupMember(List<GroupMemberBean> groupMemberBeans) {
//        List<Long> uids = new ArrayList<>();
//        for (GroupMemberBean m : groupMemberBeans) {
//            uids.add(m.getUid());
//        }
//        GlideIM.getUserInfo(uids)
//                .compose(RxUtils.silentScheduler())
//                .subscribe(new SilentObserver<List<UserInfoBean>>() {
//                    @Override
//                    public void onNext(@NonNull List<UserInfoBean> userInfoBeans) {
//                        IMSession session = account.getIMSessionList().getSession(Constants.SESSION_TYPE_GROUP, id);
//                        if (session != null) {
//                            session.onDetailUpdated();
//                        }
//                    }
//                });
        for (GroupMemberBean m : groupMemberBeans) {
            members.put(m.getUid(), m);
        }

//        members.sort((o1, o2) -> {
//            if (o1.getType() < o2.getType()) {
//                return 1;
//            }
//            if (o1.getUid() == o2.getUid()) {
//                return 0;
//            }
//            long i = o1.getUid() - o2.getUid();
//            return (int) i;
//        });
    }

    public List<GroupMemberBean> getMembers() {
        return new ArrayList<>(members.values());
    }
}
