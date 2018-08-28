package com.fanhong.cn.service_page.questionnaire

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.http.callback.StringDialogCallback
import com.fanhong.cn.tools.LogUtil
import com.fanhong.cn.tools.StringUtils
import com.fanhong.cn.tools.ToastUtil
import com.google.gson.Gson
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.vondear.rxtool.view.RxToast
import kotlinx.android.synthetic.main.activity_questionnaire.*
import me.leefeng.promptlibrary.PromptDialog
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class QuestionnaireActivity : AppCompatActivity(), QuestionnaireAdapter.Callqck {
    override fun click(v: View) {
//        ToastUtil.showToastL(questions[v.tag.toString().toInt()].fraction.toString())
    }

    private var questions = ArrayList<Question>()
    private lateinit var comments: ArrayList<CommentListAdapter.Comment>

    private var adapter: QuestionnaireAdapter? = null
    private var pref: SharedPreferences? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionnaire)
        pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        tv_title.text = pref!!.getString(App.PrefNames.GARDENNAME, "") + "物业 问卷调查"
        img_back.setOnClickListener { finish() }

        loadData2()
        loadData3()
        check()
    }

    private fun loadData2() {
        OkGo.post<String>(App.CMD)
                .tag(this)//
                .isMultipart(true)
                //        1071: 查询当前小区综合评分 以及各个项目的综合评分(app->平台)
                //        cmd:数据类型
                //        qid:小区id
                .params("cmd", "1071")
                .params("qid", pref!!.getString(App.PrefNames.GARDENID, ""))
//                .params("qid", "126")
                .execute(object : StringDialogCallback(this) {
                    @SuppressLint("SetTextI18n")
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGo 1071", response.body().toString())
                        try {
                            val json = JSONObject(response.body()!!.toString())
//                            count:小区综合评分
//                            a:第一题的综合评分
//                            b:第二题的综合评分
//                            c:第三题的综合评分
//                            d:第四题的综合评分
//                            e:第五题的综合评分
//                            my:满意度
                            tv_fraction.text = json.getString("count")
                            if (tv_fraction.text=="0.0"){
                                all_integrate.visibility=View.GONE
                                img_integrate.visibility=View.VISIBLE
                                return
                            }
//                            tv_satisfaction.text = (json.getDouble("my")*100).toInt().toString()+"%"
                            tv_satisfaction.text = json.getString("my")

                            srb_service.rating=(json.getDouble("a")).toFloat()
                            srb_fire_control.rating=(json.getDouble("b")).toFloat()
                            srb_environment.rating=(json.getDouble("c")).toFloat()

                            tv_service.text=json.getString("a")
                            tv_fire_control.text=json.getString("b")
                            tv_environment.text=json.getString("c")

                        } catch (e: JSONException) {
                            all_integrate.visibility=View.GONE
                            img_integrate.visibility=View.VISIBLE

                            LogUtil.e("JSONException", e.toString())
                            RxToast.error("数据解析异常")
                            e.printStackTrace()
                        }
                    }

                    override fun onError(response: Response<String>) {
                        Log.e("OkGoError", response.exception.toString())
                    }
                })
    }

    private fun loadData3() {
        OkGo.post<String>(App.CMD)
                .tag(this)//
                .isMultipart(true)
                //                1069：查询所有用户对物业的评价(app->平台)
                //                cmd: 数据类型
                //                qie;小区id
                .params("cmd", "1069")
                .params("qid", pref!!.getString(App.PrefNames.GARDENID, ""))
//                .params("qid", "126")
                .execute(object : StringDialogCallback(this) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGo 1069", response.body().toString())
                        try {
                            val arr = JSONArray(response.body()!!.toString())
                            if (arr.length() == 0) return
                            comments = ArrayList()
                            for (i in 0 until arr.length()) {
                                val q = Gson().fromJson(arr.getString(i), CommentListAdapter.Comment::class.java)
                                comments.add(q)
                            }
                            showComment()
                        } catch (e: JSONException) {
                            LogUtil.e("JSONException", e.toString())
                            ToastUtil.showToastL("数据解析异常")
                            e.printStackTrace()
                        }
                    }

                    override fun onError(response: Response<String>) {
                        Log.e("OkGoError", response.exception.toString())
                    }
                })
    }

    private  var adapter2: CommentListAdapter? = null

    private fun showComment() {
        if (adapter2==null){
            adapter2= CommentListAdapter(comments)
            rv_list_comment.layoutManager= LinearLayoutManager(this,
                    LinearLayoutManager.VERTICAL,false)
            rv_list_comment.adapter = adapter2
        }else adapter2!!.setNewData(comments)
    }

    private fun check() {
        OkGo.post<String>(App.CMD)
                .tag(this)//
                .isMultipart(true)
                //        1067 查询是否评价物业（app->平台）
                //        cmd:数据类型
                //        uid：当前用户ID
//                                      qid:小区id
                .params("cmd", "1067")
                .params("uid", pref!!.getString(App.PrefNames.USERID, "-1"))
                .params("qid", pref!!.getString(App.PrefNames.GARDENID, ""))
                .execute(object : StringDialogCallback(this) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGo 1067", response.body().toString())
                        if (response.body().toString().contains("\"state\":\"400\"")) {
                            loadData()
                        }
                    }

                    override fun onError(response: Response<String>) {
                        Log.e("OkGoError", response.exception.toString())
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
                        Log.e("OkGo 1063", response.body().toString())
                        try {
                            val json = JSONObject(response.body()!!.toString())
                            if (json.getString("cw") == "0") {
                                val arr = json.getJSONArray("data")
                                if (arr.length() == 0) return
                                all_questionnaire2.visibility = View.VISIBLE
                                for (i in 0 until arr.length()) {
                                    val q = Gson().fromJson(arr.getString(i), Question::class.java)
                                    questions.add(q)
                                }
                                initView()
                            } else {
                                PromptDialog(this@QuestionnaireActivity).showError("获取失败")
                                finish()
                            }
                        } catch (e: JSONException) {
                            LogUtil.e("JSONException", e.toString())
                            ToastUtil.showToastL("数据解析异常")
                            e.printStackTrace()
                        }
                    }

                    override fun onError(response: Response<String>) {
                        AlertDialog.Builder(this@QuestionnaireActivity).setMessage("获取失败！ 是否重试?")
                                .setPositiveButton("确定") { _, _ ->
                                    loadData()
                                }.setNegativeButton("取消", null).show()
                        Log.e("OkGoError", response.exception.toString())
                    }
                })
    }

    fun submit(v: View) {
//        if (StringUtils.isEmpty(edt_opinion.text.toString())) {
//            Toast.makeText(this, "请填写意见", Toast.LENGTH_SHORT).show()
//            return
//        }
        var fs = ""
        for (i in questions.indices) {
            fs += if (i == 0) questions[i].fraction.toString()
            else "," + questions[i].fraction
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
                .params("uid", pref!!.getString(App.PrefNames.USERID, "23"))
                .params("fs", fs)
                .params("qid", pref!!.getString(App.PrefNames.GARDENID, ""))
                .params("liuyan", edt_opinion.text.toString())
                .params("time", System.currentTimeMillis().toString())
                .execute(object : StringDialogCallback(this) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGo 1065", response.body().toString() + "body")
                        if (response.body().toString().contains("\"cw\":\"0\"")
                                || response.body().toString().contains("\"state\":\"200\"")) {
                            PromptDialog(this@QuestionnaireActivity).showSuccess("提交成功", true)
                            all_questionnaire2.visibility = View.GONE
                            loadData2()
                            loadData3()
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
