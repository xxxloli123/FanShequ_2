package com.fanhong.cn.service_page.repair

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.fanhong.cn.R
import com.fanhong.cn.service_page.repair.moudle.RepairInfoM
import com.fanhong.cn.tools.DateUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_repair_info.*
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.fanhong.cn.App
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

class RepairInfoActivity : AppCompatActivity() {

    lateinit var repairInfo:RepairInfoM

    private var isManage: Boolean=false
    private var res= ArrayList<Repairer>()
    private var which=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair_info)
        tv_title.text="维修详情"
        if (intent.getSerializableExtra("ri") != null) {
            repairInfo = intent.getSerializableExtra("ri") as RepairInfoM
            isManage=intent.getBooleanExtra("manage",false)
            initView()
        } else {
            Toast.makeText(this, "数据读取错误", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadData() {
        OkGo.post<String>(App.CMD)
                .tag(this)//
                .params("cmd", "1049")
                .execute(object : StringDialogCallback(this) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGo body",response.body().toString())
                        try {
                            val json = JSONObject(response.body()!!.toString())
                            if (json.getString("cw") == "0"){
                                val arr = json.getJSONArray("data")
                                if (arr.length() == 0) return
                                res= ArrayList()
                                for (i in 0 until arr.length()) {
                                    val re=Gson().fromJson(arr.getString(i), Repairer::class.java)
                                    res.add(re)
                                }
                                selectRepairer()
                            }else PromptDialog(this@RepairInfoActivity).showError("获取失败")
                        } catch (e: JSONException) {
                            LogUtil.e("JSONException",e.toString())
                            ToastUtil.showToastL("数据解析异常")
                            e.printStackTrace()
                        }
                    }
                    override fun onError(response: Response<String>) {
                        AlertDialog.Builder(this@RepairInfoActivity).setMessage("获取失败！ 是否重试?")
                                .setPositiveButton("确定") { _, _ ->
                                    loadData()
                                }.setNegativeButton("取消",null) .show()
                        Log.e("OkGoError",response.exception.toString())
                    }
                })
    }

    private fun selectRepairer() {
        val strings=ArrayList<String>()
        for (i in 0 until  res.size){
            strings.add(res[i].wxname+"   "+res[i].phone)
        }
        val strArray = strings.toArray(arrayOfNulls<String>(strings.size))
        AlertDialog.Builder(this@RepairInfoActivity).setTitle("选择维修员")
                .setSingleChoiceItems(strArray,0) { _, which -> //which 哪一个
                    this@RepairInfoActivity.which=which
                }.setPositiveButton("确定") { _, _ ->
                    dispatch(res[which].phone)
                }.setNegativeButton("取消",null) .show()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        when(repairInfo.zt){
//            zt:状态：0 未修  1  正在处理 2 维修完成
            0->{
                if (isManage){
                    arl_process_info.visibility=View.GONE
                    tv_call.text="派单"
                    tv_call.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null)
                }else{
                    tv_handle_date.text=DateUtil.getDateToString(repairInfo.time.toLong(),"MM-dd")
                    tv_handle_time.text=DateUtil.getDateToString(repairInfo.time.toLong(),"HH:mm")
                }
            }
            1->{
                arl_handleing.visibility=View.VISIBLE
                if (repairInfo.time1!="null"){
                    tv_handle_date.text=DateUtil.getDateToString(repairInfo.time.toLong(),"MM-dd")
                    tv_handle_time.text=DateUtil.getDateToString(repairInfo.time.toLong(),"HH:mm")
                }
                img_handle.setImageResource(R.mipmap.dispose)
                tv_handleing_info.text="已经为您分配维修员，维修员${repairInfo.wxboy}(${repairInfo.wxphone})" +
                        "为您维修"
            }
            2->{
                arl_complete.visibility=View.VISIBLE
                tv_complete_date.text=DateUtil.getDateToString(repairInfo.time2.toLong(),"MM-dd")
                tv_complete_time.text=DateUtil.getDateToString(repairInfo.time2.toLong(),"HH:mm")
                img_handleing.setImageResource(R.mipmap.single)
                arl_handleing.visibility=View.VISIBLE
                tv_handleing_date.text=DateUtil.getDateToString(repairInfo.time1.toLong(),"MM-dd")
                tv_handle_time.text=DateUtil.getDateToString(repairInfo.time1.toLong(),"HH:mm")
                img_handle.setImageResource(R.mipmap.dispose)
                tv_handleing_info.text="已经为您分配维修员，维修员${repairInfo.wxboy}(${repairInfo.wxphone})" +
                        "为您维修"
            }
        }

        tv_device.text=repairInfo.men
        if (repairInfo.imgUrls!=null){
            for (i in repairInfo.imgUrls.indices){
                Log.e("imgUrl",repairInfo.imgUrls[i])
                when(i){
                    0-> Picasso.with(this).load(repairInfo.imgUrls[i]).resize(200,200).
                            centerCrop().into(img1)

                    1-> Picasso.with(this).load(repairInfo.imgUrls[i]).resize(200,200).
                            centerCrop().into(img2)

                    2-> Picasso.with(this).load(repairInfo.imgUrls[i]).resize(200,200).
                            centerCrop().into(img3)
                }
            }
        }
        tv_phone.text=repairInfo.lxphone
        tv_address.text=repairInfo.dizhi
        tv_number.text=repairInfo.time
        tv_date.text=DateUtil.getDateToString(repairInfo.time.toLong(),"yyyy-MM-dd")
        tv_remark.text=repairInfo.concent
    }

    fun onCLicks(v: View) {
        when (v.id) {
            R.id.img_back -> finish()
            R.id.arl_call -> {
                if (isManage&&repairInfo.zt==0){
                    loadData()
                }else{
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:023 6887 2002"))
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
        }
    }

    private fun dispatch(phone: String?) {
//        1051:工作派发(APP->平台)
//        id:当前订单ID
//        phone：维修人员电话号码
        OkGo.post<String>(App.CMD)
                .tag(this)//
                .params("cmd", "1051")
                .params("id", repairInfo.id)
                .params("phone", phone)
                .execute(object : StringDialogCallback(this) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGo body",response.body().toString())
                        try {
                            val json = JSONObject(response.body()!!.toString())
                            if (json.getString("state") == "200"){
                                AlertDialog.Builder(this@RepairInfoActivity).setMessage("派单成功")
                                        .setPositiveButton("确定"){ _, _ ->
                                            val  i=Intent()
                                            repairInfo.zt=1
                                            repairInfo.time1=System.currentTimeMillis().toString()
                                            repairInfo.wxphone=res[which].phone
                                            repairInfo.wxboy=res[which].wxname
                                            i.putExtra("back",repairInfo)
                                            setResult(25,i)
                                            finish()
                                        }
                                        .show()
                            }else{
                                AlertDialog.Builder(this@RepairInfoActivity).setMessage("派单失败！ 是否重试?")
                                        .setPositiveButton("确定") { _, _ ->
                                            dispatch(res[which].phone)
                                        }.setNegativeButton("取消",null) .show()
                            }
                        } catch (e: JSONException) {
                            LogUtil.e("JSONException",e.toString())
                            ToastUtil.showToastL("数据解析异常")
                            e.printStackTrace()
                        }
                    }
                    override fun onError(response: Response<String>) {
                        AlertDialog.Builder(this@RepairInfoActivity).setMessage("派单失败！ 是否重试?")
                                .setPositiveButton("确定") { _, _ ->
                                    dispatch(res[which].phone)
                                }.setNegativeButton("取消",null) .show()
                        Log.e("OkGoError",response.exception.toString())
                    }
                })
    }
}
