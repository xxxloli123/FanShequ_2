package com.fanhong.cn.user_page

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.service_page.shop.ShopIndexActivity
import com.fanhong.cn.tools.JsonSyncUtils
import kotlinx.android.synthetic.main.activity_score.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x

class ScoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)
        tv_title.text = "积分"
        img_back.setOnClickListener { finish() }
        tv_score.text = "0"
        initScore()

        score_get.setOnClickListener { startActivity(Intent(this, ShopIndexActivity::class.java)) }
        score_cash.setOnClickListener { startActivity(Intent(this, ScoreCashActivity::class.java)) }
    }

    private fun initScore() {
        val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val uid = pref.getString(App.PrefNames.USERID, "-1")
        val param = RequestParams(App.CMD)
        param.addBodyParameter("cmd", "1020")
        param.addBodyParameter("uid", uid)
        x.http().post(param, object : Callback.CommonCallback<String> {

            override fun onSuccess(result: String) {
                when (JsonSyncUtils.getState(result)) {
                    200 -> {
                        val score = JsonSyncUtils.getJsonValue(result, "jf")
                        runOnUiThread {
                            tv_score.text = score
                        }
                    }
                    400 -> {
                    }
                }
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

            override fun onFinished() {
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }
        })
    }
}
