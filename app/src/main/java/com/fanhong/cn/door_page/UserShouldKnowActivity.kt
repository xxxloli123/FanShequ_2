package com.fanhong.cn.door_page

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.fanhong.cn.R
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.view.annotation.Event

/**
 * Created by Administrator on 2017/9/4.
 */

class UserShouldKnowActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addkey_know)
        tv_title.text = "用户须知"
    }

    @Event(R.id.img_back, R.id.receive_xuzhi, R.id.refuse_xuzhi)
    private fun onClick(v: View) {
        when (v.id) {
            R.id.img_back -> goBack()
            R.id.receive_xuzhi -> goBack()
            R.id.refuse_xuzhi -> {
                val intent = Intent()
                intent.putExtra("ifreceive", false)
                setResult(17, intent)
                finish()
            }
        }
    }

    private fun goBack() {
        val intent = Intent()
        intent.putExtra("ifreceive", true)
        setResult(17, intent)
        finish()
    }
}
