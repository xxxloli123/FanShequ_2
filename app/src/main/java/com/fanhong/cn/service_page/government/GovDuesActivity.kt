package com.fanhong.cn.service_page.government

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.fanhong.cn.R
import kotlinx.android.synthetic.main.activity_top.*

class GovDuesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gov_dues)
        tv_title.text = "党费信息"
        img_back.setOnClickListener { finish() }
    }
}
