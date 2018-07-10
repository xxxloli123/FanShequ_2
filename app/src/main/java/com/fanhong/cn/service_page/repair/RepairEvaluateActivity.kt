package com.fanhong.cn.service_page.repair

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.http.callback.StringDialogCallback
import com.fanhong.cn.service_page.repair.moudle.RepairInfoM
import com.fanhong.cn.tools.LogUtil
import com.fanhong.cn.tools.ToastUtil
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import kotlinx.android.synthetic.main.activity_repair_evaluate.*
import me.leefeng.promptlibrary.PromptDialog
import org.json.JSONException
import org.json.JSONObject

class RepairEvaluateActivity : AppCompatActivity() {
    lateinit var repairInfo:RepairInfoM
    private var fraction=3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair_evaluate)
        tv_title.text="维修评价"
        if (intent.getSerializableExtra("ri") != null) {
            repairInfo = intent.getSerializableExtra("ri") as RepairInfoM
            initView()
        } else {
            Toast.makeText(this, "数据读取错误", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        tv_name.text=repairInfo.wxboy
        val sb=((Math.random() * 222)).toInt()
        tv_order_amount.text="已接业务：${sb}单"
        simpleRatingBar.setOnRatingChangeListener { p0, p1 ->
            fraction=p1.toInt()
            if (p1<1.5){
                tv_satisfaction.text="  不满意"
                tv_satisfaction.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(
                        R.mipmap.crying),null,null,null)
            }else if (p1<3.5){
                tv_satisfaction.text="  一般"
                tv_satisfaction.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(
                        R.mipmap.beam),null,null,null)
            }else {
            tv_satisfaction.text="   比较满意"
            tv_satisfaction.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(
                    R.mipmap.smiling),null,null,null)
        }
        }
    }

    @SuppressLint("SetTextI18n")
    fun onCLicks(v: View) {
        when (v.id) {
            R.id.img_back -> finish()
            R.id.img_call -> {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${repairInfo.wxphone}"))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            R.id.btn_submit -> submit()
//            R.id.ckb_fw -> {
//                if (ckb_fw)edt_content.setText(edt_content.text.toString()+"服务态度好  ")
//            }
//            R.id.ckb_fy -> edt_content.setText(edt_content.text.toString()+"风雨无阻  ")
//            R.id.ckb_hq -> edt_content.setText(edt_content.text.toString()+"人很和气  ")
//            R.id.ckb_sd -> edt_content.setText(edt_content.text.toString()+"速度很快  ")
        }
    }

    private fun submit() {
//        1045.确认维修完成打分(APP->平台)
//        cmd:数据类型
//        id:当前维修信息的id
//        fs:分数
//        pl:评论
        OkGo.post<String>(App.CMD)
                .tag(this)//
                .isMultipart(true)
                .params("cmd","1045")
                .params("id",repairInfo.id)
                .params("fs",fraction.toString())
                .params("pl",edt_content.text.toString())
                .execute(object : StringDialogCallback(this) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGobody", response.body().toString())
                        try {
                            val json = JSONObject(response.body()!!.toString())
                            if (json.getString("cw") == "1"){
                                PromptDialog(this@RepairEvaluateActivity).showError("系统错误")
                            }else AlertDialog.Builder(this@RepairEvaluateActivity)
                                    .setMessage("评价成功")
                                    .setPositiveButton("确定") { _, _ ->
                                        finish()
                                    }.show()
                        }catch (e: JSONException) {
                            LogUtil.e("JSONException",e.toString())
                            ToastUtil.showToastL("数据解析异常")
                            e.printStackTrace()
                        }
                    }

                    override fun onError(response: Response<String>) {
                        Log.e("OkGoError", response.exception.toString())
                        AlertDialog.Builder(this@RepairEvaluateActivity)
                                .setMessage("评价失败！ 是否重试?")
                                .setPositiveButton("确定") { _, _ ->
                                    submit()
                                }.setNegativeButton("取消",null) .show()
                    }
                })
    }
}
