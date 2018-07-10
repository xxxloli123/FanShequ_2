package com.fanhong.cn.user_page

import android.app.AlertDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.http.callback.StringDialogCallback
import com.fanhong.cn.service_page.repair.moudle.Repairer
import com.fanhong.cn.tools.LogUtil
import com.fanhong.cn.tools.ToastUtil
import com.google.gson.Gson
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import me.leefeng.promptlibrary.PromptDialog
import org.json.JSONException
import org.json.JSONObject

class IntegralActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_integral)
        loadData()
    }

    private fun loadData() {
//        1035.查询积分(APP->平台)
//        cmd:数据类型
//        phone:当前APP用户电话号码
        val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        OkGo.post<String>(App.CMD)
                .tag(this)//
                .params("cmd", "1035")
                .params("phone", pref.getString(App.PrefNames.USERNAME, ""))
                .execute(object : StringDialogCallback(this) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGo body",response.body().toString())
                        try {
                            val json = JSONObject(response.body()!!.toString())
                            if (json.getString("cw") == "1"){
                                PromptDialog(this@IntegralActivity).showError("获取失败")
                                return
                            }
                        } catch (e: JSONException) {
                            LogUtil.e("JSONException",e.toString())
                            ToastUtil.showToastL("数据解析异常")
                            e.printStackTrace()
                        }
                    }
                    override fun onError(response: Response<String>) {
                        AlertDialog.Builder(this@IntegralActivity).setMessage("获取失败！ 是否重试?")
                                .setPositiveButton("确定") { _, _ ->
                                    loadData()
                                }.setNegativeButton("取消",null) .show()
                        Log.e("OkGoError",response.message())
                    }
                })
    }
}
