package pro.glideim.ui.group

import pro.glideim.sdk.api.group.GroupMemberBean
import pro.glideim.sdk.api.user.UserInfoBean

data class GroupMemberViewData(
    val memberInfo: GroupMemberBean,
    val userInfo: UserInfoBean
)