package com.fanhong.cn.community_page

import android.graphics.drawable.AnimationDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import kotlinx.android.synthetic.main.activity_news_details.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x

class NewsDetailsActivity : AppCompatActivity() {

    private var newsId = ""
    private var anim: AnimationDrawable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_details)
        tv_title.text = getString(R.string.newsDetail)
        img_back.setOnClickListener { finish() }
        newsId = intent.getStringExtra("id")
        anim = img_news_details_bar.drawable as AnimationDrawable
        anim?.start()

        initData()
    }

    private fun initData() {
        val param = RequestParams(App.CMD)
        param.addBodyParameter("cmd", "51")
        param.addBodyParameter("id", newsId)
        x.http().post(param, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                if (JsonSyncUtils.getJsonValue(result, "cw") == "0") {
                    val data = JsonSyncUtils.getJsonValue(result, "data")
                    news_detail_title.text = JsonSyncUtils.getJsonValue(data, "bt")
                    news_detail_time.text = JsonSyncUtils.getJsonValue(data, "time")
                    news_detail_place.text = JsonSyncUtils.getJsonValue(data, "zz")
                    new_detail_content.text = JsonSyncUtils.getJsonValue(data, "nr")
                    news_details_bar.visibility = View.GONE
                    anim?.stop()
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

            override fun onFinished() {

            }
        })
    }
}
