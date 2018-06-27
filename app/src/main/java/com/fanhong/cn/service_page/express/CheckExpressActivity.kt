package com.fanhong.cn.service_page.express

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import kotlinx.android.synthetic.main.activity_check_express.*
import kotlinx.android.synthetic.main.activity_top.*
import org.json.JSONException
import org.json.JSONObject
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x
import java.util.ArrayList

class CheckExpressActivity : AppCompatActivity() {

    internal var host = "http://jisukdcx.market.alicloudapi.com"
    internal var path = "/express/query"
    internal var appcode = "62b004f79e4e48579d154afc59aadb5b"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_express)
        tv_title.text = "查快递"
        img_back.setOnClickListener { finish() }

        input_express_number.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(input_express_number.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                checkexpressage()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        img_search_express.setOnClickListener { checkexpressage() }
    }

    private fun checkexpressage() {
//        val params = RequestParams(host + path)
        val params = RequestParams(App.CMD)
//        params.addHeader("Authorization", "APPCODE " + appcode)
        params.addBodyParameter("number", input_express_number.text.toString())
        params.addBodyParameter("cmd", "1007")
//        params.addBodyParameter("type", "AUTO")    //自动识别快递类型
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                Log.i("CheckExpress", result)
                val status = JsonSyncUtils.getJsonValue(result, "status")
                if (status == "0") {
                    show_wuliu.text = JsonSyncUtils.getJsonValue(result, "result")
                } else {
                    show_wuliu.text = result
                }
                ruleText(result)
            }

            override fun onError(ex: Throwable, isOnCallback: Boolean) {
                show_wuliu.text = JsonSyncUtils.getJsonValue(ex.toString(), "result")
            }

            override fun onCancelled(cex: Callback.CancelledException) {
                show_wuliu.text = JsonSyncUtils.getJsonValue(cex.toString(), "result")
            }

            override fun onFinished() {

            }
        })
    }

    private fun ruleText(json: String) {
        val result = JsonSyncUtils.getJsonValue(json, "result")
        val number = JsonSyncUtils.getJsonValue(result, "number")
        val type = JsonSyncUtils.getJsonValue(result, "type")
        show_wuliu.text = "订单号：$number\n快递公司：$type\n物流信息："
        val list = getJsonList(result)
        Log.i("CheckExpress", "list.size==>" + list.size)
        for (i in list.indices) {
            val tv = TextView(this)
            tv.gravity = Gravity.CENTER_HORIZONTAL
            tv.text = list[i].time + "\n" + list[i].detail
            wuliu_layout.removeAllViews()
            wuliu_layout.addView(tv)
        }
    }

    private fun getJsonList(json: String): List<ListOrder> {
        val l = ArrayList<ListOrder>()
        try {
            val obj = JSONObject(json)
            val arr = obj.getJSONArray("list")
            (0 until arr.length())
                    .map { arr.getJSONObject(it) }
                    .mapTo(l) {
                        ListOrder(it.getString("time"),
                                it.getString("status"))
                    }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return l
    }

    internal inner class ListOrder(var time: String, var detail: String)
}
