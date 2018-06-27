package com.fanhong.cn.service_page.repair

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.fanhong.cn.HomeActivity
import com.fanhong.cn.R
import kotlinx.android.synthetic.main.activity_repair_success.*
import kotlinx.android.synthetic.main.activity_top.*

import org.xutils.view.annotation.ContentView
import org.xutils.view.annotation.Event
import org.xutils.view.annotation.ViewInject
import org.xutils.x

/**
 * Created by Administrator on 2017/8/21.
 */
class RepairSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair_success)
        tv_title.text = "提交成功"
        img_back.visibility = View.INVISIBLE
        btn_back.setOnClickListener { onBack() }
    }

    private fun onBack() {
        val i = Intent(this, HomeActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(i)
    }

    override fun onBackPressed() {
        onBack()
    }
}
