package com.fanhong.cn.user_page

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.fanhong.cn.App
import com.fanhong.cn.BuildConfig
import com.fanhong.cn.R
import com.vondear.rxtool.view.RxToast
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.activity_top.*

class AboutActivity : AppCompatActivity() {


    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        tv_title.text=getString(R.string.aboutour)
        img_back.setOnClickListener { finish() }

        tv_versionName.text="FanShequ v${BuildConfig.VERSION_NAME}"

        pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)

    }

    private var e=0

    fun surprise(v:View){
        e++
        if (e==4){
            e=0
            egg.visibility=View.VISIBLE
            egg.text = (pref.getString(App.PrefNames.USERNAME, "看来是没有登录呢")
                    +"\n"+pref.getString(App.PrefNames.USERID, "")
                    +"\n"+pref.getString(App.PrefNames.GARDENID, "")
                    )
        }
    }
}
