package com.fanhong.cn.home_page

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import kotlinx.android.synthetic.main.activity_star_manager.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.image.ImageOptions
import org.xutils.x

class StarManagerActivity : AppCompatActivity() {

    private var managerName = ""
    private var managerId = ""
    private var managerTel = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_star_manager)
        initData()
        initView()
    }

    private fun initData() {
        managerName = intent.getStringExtra("name")
        managerId = intent.getStringExtra("id")
        var params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "81")
        params.addBodyParameter("id", managerId)
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onFinished() {
            }

            override fun onSuccess(s: String?) {
                managerTel = JsonSyncUtils.getJsonValue(s!!, "tel")
                var description = JsonSyncUtils.getJsonValue(s!!, "describe")
                var imgUrl = JsonSyncUtils.getJsonValue(s!!, "img")
                var nearby = JsonSyncUtils.getJsonValue(s!!, "periphery")
                var price = JsonSyncUtils.getJsonValue(s!!, "price")
                if (imgUrl!! != "-1") {
                    val option = ImageOptions.Builder().setLoadingDrawableId(R.mipmap.img_default).setFailureDrawableId(R.mipmap.img_default).setUseMemCache(true).build()
                    x.image().bind(img_description, imgUrl, option)
                    img_description.visibility = View.VISIBLE
                }
                tv_description.text = Html.fromHtml(description)
                tv_nearby.text = Html.fromHtml(nearby)
                label_price.text = "物管费：${price}元/平方米"
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

        })
    }

    private fun initView() {
        tv_title.setText(R.string.wuyezhixing)
        img_back.setOnClickListener {
            finish()
        }
        img_description.visibility = View.GONE
    }
}
