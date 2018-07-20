package com.fanhong.cn.service_page.questionnaire

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.http.callback.StringDialogCallback
import com.fanhong.cn.tools.LogUtil
import com.fanhong.cn.tools.StringUtils
import com.fanhong.cn.tools.ToastUtil
import com.google.gson.Gson
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import kotlinx.android.synthetic.main.activity_questionnaire.*
import me.leefeng.promptlibrary.PromptDialog
import org.json.JSONException
import org.json.JSONObject

class QuestionnaireActivity : AppCompatActivity(),QuestionnaireAdapter.Callqck {
    override fun click(v: View) {
//        ToastUtil.showToastL(questions[v.tag.toString().toInt()].fraction.toString())
    }

    private var questions = ArrayList<Question>()
    private var adapter: QuestionnaireAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionnaire)
        img_back.setOnClickListener { finish() }
        check()
        initView()
    }

    private fun check() {
        val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        OkGo.post<String>(App.CMD)
                .tag(this)//
                .isMultipart(true)
                            //        1067 查询是否评价物业（app->平台）
                            //        cmd:数据类型
                            //        uid：当前用户ID
                .params("cmd","1067")
                .params("uid",pref.getString(App.PrefNames.USERID, "-1"))
                .execute(object : StringDialogCallback(this) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGo 1067", response.body().toString())
                        if ( response.body().toString().contains("\"state\":\"400\"")) {
                            loadData()
                        }
                    }
                    override fun onError(response: Response<String>) {
                        Log.e("OkGoError", response.message())
                    }
                })
    }

    private fun initView() {
        adapter = QuestionnaireAdapter(questions, this)
        rv_questionnaire_list.layoutManager = LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false)
        rv_questionnaire_list.adapter = adapter
    }

    private fun loadData() {
//        1063 物业评价题目(app->平台)
//        cmd:数据类型
        OkGo.post<String>(App.CMD)
                .tag(this)//
                .params("cmd", "1063")
                .execute(object : StringDialogCallback(this) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGo 1063",response.body().toString())
                        try {
                            val json = JSONObject(response.body()!!.toString())
                            if (json.getString("cw") == "0"){
                                val arr = json.getJSONArray("data")
                                if (arr.length() == 0) return
                                scroll_view.visibility=View.VISIBLE
                                for (i in 0 until arr.length()) {
                                    val q= Gson().fromJson(arr.getString(i), Question::class.java)
                                    questions.add(q)
                                }
                                initView()
                            }else {
                                PromptDialog(this@QuestionnaireActivity).showError("获取失败")
                                finish()
                            }
                        } catch (e: JSONException) {
                            LogUtil.e("JSONException",e.toString())
                            ToastUtil.showToastL("数据解析异常")
                            e.printStackTrace()
                        }
                    }
                    override fun onError(response: Response<String>) {
                        AlertDialog.Builder(this@QuestionnaireActivity).setMessage("获取失败！ 是否重试?")
                                .setPositiveButton("确定") { _, _ ->
                                    loadData()
                                }.setNegativeButton("取消",null) .show()
                        Log.e("OkGoError",response.message())
                    }
                })
    }

    fun submit(v: View){
        if (StringUtils.isEmpty(edt_opinion.text.toString())) {
            Toast.makeText(this, "请填写意见", Toast.LENGTH_SHORT).show()
            return
        }
        val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        var fs=""
        for (i in questions.indices){
            fs += if (i==0) questions[i].fraction.toString()
            else ","+questions[i].fraction
        }
        OkGo.post<String>(App.CMD)
                .tag(this)//
                .isMultipart(true)
                    //        1065 用户评价(APP->平台)
                    //        cmd: 数据类型
                    //        uid:用户ID
                    //        fs:分数("1","2","3",);
                    //        qid:小区id
                    //        liuyan:用户意见建议
                .params("cmd", "1065")
                .params("uid", pref.getString(App.PrefNames.USERID, "-1"))
                .params("fs", fs)
                .params("qid", pref.getString(App.PrefNames.GARDENID, ""))
                .params("liuyan", edt_opinion.text.toString())
                .execute(object : StringDialogCallback(this) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGo 1065", response.body().toString()+"body")
                        if (response.body().toString().contains("\"cw\":\"0\"")
                                || response.body().toString().contains("\"state\":\"200\"")) {
                            PromptDialog(this@QuestionnaireActivity).showSuccess("提交成功", true)
                            Handler().postDelayed({ finish() }, 1500)
                        } else PromptDialog(this@QuestionnaireActivity).showError("提交失败")
                    }
                    override fun onError(response: Response<String>) {
                        Log.e("OkGoError", response.exception.toString())
                        AlertDialog.Builder(this@QuestionnaireActivity)
                                .setMessage("评价失败！ 是否重试?")
                                .setPositiveButton("确定") { _, _ ->
                                    submit(img_back)
                                }.setNegativeButton("取消", null).show()
                    }
                })
    }

}
