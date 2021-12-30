package pro.glideim.ui

import SessionsFragment
import android.content.Context
import android.content.Intent
import androidx.core.view.get
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import pro.glideim.R
import pro.glideim.base.BaseActivity
import pro.glideim.base.BaseFragment
import pro.glideim.sdk.GlideIM
import pro.glideim.ui.contacts.ContactsFragment
import pro.glideim.ui.profile.ProfileFragment

class MainActivity : BaseActivity() {

    private val mBnvNav by lazy { findViewById<BottomNavigationView>(R.id.bnv_nav) }
    private val mViewPager by lazy { findViewById<ViewPager2>(R.id.view_pager) }
    private val mFragments = mutableListOf<BaseFragment>()

    override val layoutResId = R.layout.activity_main

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, MainActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun initView() {
        initLayout()
    }

    private fun initLayout() {
        mFragments.add(SessionsFragment())
        mFragments.add(ContactsFragment())
        mFragments.add(ProfileFragment())
        mViewPager.offscreenPageLimit = 2
        mViewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = mFragments.size

            override fun createFragment(position: Int) = mFragments[position]
        }
        mViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mBnvNav.menu[position].isChecked = true
            }
        })
//        mBnvNav.backgroundTintList = null

        val menu = mapOf(
            Pair(R.id.it_messages, 0),
            Pair(R.id.it_contacts, 1),
            Pair(R.id.it_profile, 2),
        )

        mBnvNav.setOnItemSelectedListener {
            mViewPager.currentItem = menu.getOrDefault(it.itemId, 0)
            true
        }

//        val badge = mBnvNav.getOrCreateBadge(R.id.it_messages)
//        badge.isVisible = true
//        badge.number = 4
    }

    override fun onDestroy() {
        super.onDestroy()
        GlideIM.getAccount().imClient.disconnect()
    }
}