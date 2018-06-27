package com.fanhong.cn.user_page

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.fanhong.cn.BuildConfig
import com.fanhong.cn.R
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.activity_top.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        tv_title.text=getString(R.string.aboutour)
        img_back.setOnClickListener { finish() }

        tv_versionName.text="FanShequ v${BuildConfig.VERSION_NAME}"
    }
}
