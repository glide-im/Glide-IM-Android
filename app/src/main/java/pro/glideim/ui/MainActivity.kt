package pro.glideim.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import pro.glideim.R
import pro.glideim.base.BaseActivity

class MainActivity : BaseActivity() {

    override val layoutResId = R.layout.activity_main

    companion object{
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, MainActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun initView() {


    }
}