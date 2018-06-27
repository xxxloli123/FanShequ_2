package com.fanhong.cn.login_pages

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.fanhong.cn.R
import kotlinx.android.synthetic.main.activity_top.*

class AgreementSheetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agreement_sheet)
        tv_title.text = "帆社区用户协议"
        img_back.setOnClickListener {
            setResult(-1)
            finish()
        }
    }

    fun onAgree(v: View) {
        setResult(22)
        finish()
    }

    fun ondecline(v: View) {
        setResult(23)
        finish()
    }
}
