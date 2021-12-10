package pro.glideim.viewholder

import android.view.ViewGroup
import com.dengzii.adapter.AbsViewHolder
import com.dengzii.adapter.SuperAdapter

class EmptyViewHolder(v: ViewGroup) : AbsViewHolder<SuperAdapter.Empty>(v) {

    override fun onCreate(parent: ViewGroup) {

    }

    override fun onBindData(data: SuperAdapter.Empty, position: Int) {

    }
}