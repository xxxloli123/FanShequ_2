package com.fanhong.cn.user_page

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.fanhong.cn.R
import kotlinx.android.synthetic.main.activity_score_cash.*
import kotlinx.android.synthetic.main.activity_top.*

class ScoreCashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_cash)
        tv_title.text = "获取积分"
        img_back.setOnClickListener { finish() }
    }
}
