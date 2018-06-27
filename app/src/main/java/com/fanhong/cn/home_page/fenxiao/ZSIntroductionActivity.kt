package com.fanhong.cn.home_page.fenxiao

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.DialogUtil
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import kotlinx.android.synthetic.main.activity_top.*
import kotlinx.android.synthetic.main.activity_zsintroduction.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x

class ZSIntroductionActivity : AppCompatActivity() {

    private var mSharedPref: SharedPreferences? = null
    private var y = 0f
    private var oldy = 0f
    private var flag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zsintroduction)
        mSharedPref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        initView()
    }

    private fun initView() {
        img_back.setOnClickListener {
            finish()
        }
        tv_title.text = "招募"
        scroll_introduction.setOnTouchListener { v, event ->
            when (event!!.action) {
                MotionEvent.ACTION_DOWN -> {
                    oldy = event.y
                    flag = true
                }
                MotionEvent.ACTION_UP -> {
                    y = event.y
                    var drag = y - oldy
                    if (drag < -100 || (drag > -10 && drag < 10)) {
                        if(flag){
                            Handler().post {
                                scroll_introduction.smoothScrollTo(0,iv_fenxiao2.top)
                            }
                            flag = false
                        }
                    }
                }
            }
            false
        }
        iv_fenxiao2.setOnClickListener {
            scroll_introduction.smoothScrollTo(0,tv_join_now.top)
        }
        tv_join_now.setOnClickListener {
            if(isLogined()){
                checkJoined(mSharedPref!!.getString(App.PrefNames.USERID,"-1"))
            }else{
                DialogUtil.showDialog(this,"login",100)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == 11){
//            startActivity(Intent(this,JoiningActivity::class.java))
            checkJoined( mSharedPref!!.getString(App.PrefNames.USERID,"-1"))
        }
    }

    private fun checkJoined(uid: String) {
        var params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "69")
        params.addBodyParameter("uid", uid)
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onFinished() {
            }

            override fun onSuccess(result: String?) {
                var data = JsonSyncUtils.getJsonValue(result!!, "data").toInt()
                when (data) {
                    0 -> startActivity(Intent(this@ZSIntroductionActivity, JoiningActivity::class.java))
                    1 -> startActivity(Intent(this@ZSIntroductionActivity, HaveJoinedActivity::class.java))
                    else -> ToastUtil.showToastS("登录状态异常")
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                ToastUtil.showToastS("登录状态异常")
            }

        })
    }

    private fun isLogined(): Boolean {
        return mSharedPref!!.getString(App.PrefNames.USERID, "-1") != "-1"
    }
}
