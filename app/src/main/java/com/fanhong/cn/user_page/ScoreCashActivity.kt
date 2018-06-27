package com.fanhong.cn.user_page

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.fanhong.cn.R
import kotlinx.android.synthetic.main.activity_top.*

class ScoreCashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_cash)
        tv_title.setText(R.string.score_cash)
        img_back.setOnClickListener { finish() }
    }
}
