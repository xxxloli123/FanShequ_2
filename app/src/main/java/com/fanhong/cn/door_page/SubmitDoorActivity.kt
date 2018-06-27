package com.fanhong.cn.door_page

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.fanhong.cn.HomeActivity
import com.fanhong.cn.R
import kotlinx.android.synthetic.main.activity_submit_door.*
import kotlinx.android.synthetic.main.activity_top.*

class SubmitDoorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_door)
        initViews()
    }

    private fun initViews() {
        img_back.visibility = View.GONE
        tv_title.text = getString(R.string.postmessages)
        back_to_mainpage.setOnClickListener {
            startActivity(Intent(this,HomeActivity::class.java))
        }
    }
}
