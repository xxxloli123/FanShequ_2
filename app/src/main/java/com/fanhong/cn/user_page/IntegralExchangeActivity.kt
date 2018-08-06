package com.fanhong.cn.user_page

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.http.callback.StringDialogCallback
import com.fanhong.cn.tools.LogUtil
import com.fanhong.cn.tools.ToastUtil
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import kotlinx.android.synthetic.main.activity_integral_exchange.*
import kotlinx.android.synthetic.main.activity_top.*
import me.leefeng.promptlibrary.PromptDialog
import org.json.JSONException
import org.json.JSONObject

class IntegralExchangeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_integral_exchange)
        tv_title.text = "积分兑换"
        img_back.setOnClickListener { finish() }
    }

    fun exchange(v: View){
        if (edt_parameter.text.toString().isEmpty()){
            ToastUtil.showToastL("请输入银行卡卡号")
            return
        }
        val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        OkGo.post<String>(App.CMD)
                .tag(this)//
        //        1039.用户积分提现(APP->平台)
        //        cmd:数据类型
        //        uid:当前用户的ID
        //        card：银行卡号
        //        tname:当前用户姓名
                .params("cmd", "1039")
                .params("uid",pref.getString(App.PrefNames.USERID, "-1"))
                .params("card", edt_parameter.text.toString())
                .params("tname", pref.getString(App.PrefNames.USERNAME, ""))
                .execute(object : StringDialogCallback(this) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGo body",response.body().toString())
                        try {
                            val json = JSONObject(response.body()!!.toString())
                            if (json.getString("state") == "200"){
                                PromptDialog(this@IntegralExchangeActivity).showSuccess("提现交成功",true)
                                Handler().postDelayed({ finish() }, 1500)
                            } else {
                                PromptDialog(this@IntegralExchangeActivity).showError(json.getString("msg"))
                            }
                        } catch (e: JSONException) {
                            LogUtil.e("JSONException",e.toString())
                            ToastUtil.showToastL("数据解析异常")
                            e.printStackTrace()
                        }
                    }
                    override fun onError(response: Response<String>) {
                        AlertDialog.Builder(this@IntegralExchangeActivity).setMessage("提现失败！ 是否重试?")
                                .setPositiveButton("确定") { _, _ ->
                                    exchange(edt_parameter)
                                }.setNegativeButton("取消",null) .show()
                        Log.e("OkGoError",response.exception.toString())
                    }
                })
    }
}
