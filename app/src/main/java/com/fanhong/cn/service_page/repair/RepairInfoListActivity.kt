package com.fanhong.cn.service_page.repair

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.adapter.RepairInfoAdapter
import com.fanhong.cn.moudle.RepairInfoM
import com.fanhong.cn.tools.ToastUtil
import com.google.gson.Gson
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import kotlinx.android.synthetic.main.activity_repair_info_list.*
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import android.view.MotionEvent
import android.os.SystemClock
import com.fanhong.cn.callback.StringDialogCallback
import com.fanhong.cn.tools.LogUtil
import com.tencent.mm.opensdk.utils.Log


class RepairInfoListActivity : AppCompatActivity(),RepairInfoAdapter.Callback {

//
//    private val CHANNELS = arrayOf("全部","待处理","处理中","已完成")
//    private val typeStrings = Arrays.asList(*CHANNELS)

//                 RI  RepairInfo
    private val allRI= ArrayList<RepairInfoM>()
    private val wait_handleRIs= ArrayList<RepairInfoM>()
    private val handleingRIs= ArrayList<RepairInfoM>()
    private val completeRIs= ArrayList<RepairInfoM>()
    private var adapter: RepairInfoAdapter? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair_info_list)
        tv_title.text="维修信息"
        loadData2()
    }

    private var isManage: Boolean=false

    private fun loadData2() {
        val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        OkGo.post<String>(App.CMD)
                .tag(this)//
                .params("cmd", "1041")
                .params("uid", pref.getString(App.PrefNames.USERID, ""))
                .execute(object : StringDialogCallback(this) {
                    override fun onSuccess(response: Response<String>) {
                        LogUtil.e("OkGo body",response.body().toString())
                        try {
                            val json = JSONObject(response.body()!!.toString())
                            if (json.getString("state") == "400"){
                                AlertDialog.Builder(this@RepairInfoListActivity).setMessage("获取失败！ 是否重试?")
                                        .setPositiveButton("确定") { dialog, _ ->
                                            loadData2()
                                        }.setNegativeButton("取消",null) .show()
                            }
                            val arr = json.getJSONArray("data")
                            if (arr.length() == 0) return
                            for (i in 0 until arr.length()) {
                                val ri=Gson().fromJson(arr.getString(i), RepairInfoM::class.java)
                                if (json.getString("state") == "201")isManage=true
                                if (arr.optJSONObject(i).getString("tupian") != "null" &&
                                        !arr.optJSONObject(i).getString("tupian").isEmpty()){
                                    val jsonArray= arr.optJSONObject(i).getJSONArray("tupian")
                                    ri.imgUrls= ArrayList()
                                    for (a in 0 until  jsonArray.length()){
                                        if (!jsonArray[a].toString().isEmpty()){
                                            if (jsonArray[a].toString().contains("hou"))
                                                ri.imgUrls.add((jsonArray[a].toString()+".jpg")
                                                    .replace("\\", ""))
                                            else ri.imgUrls.add((jsonArray[a].toString())
                                                    .replace("\\", ""))
                                        }
                                    }
                                }
                                allRI.add(ri)
                                when (ri.zt){
                                    0 -> wait_handleRIs.add(ri)
                                    1 -> handleingRIs.add(ri)
                                    2 -> completeRIs.add(ri)
                                }
                            }
                            showRIlist(allRI)
                        } catch (e: JSONException) {
                            LogUtil.e("JSONException",e.toString())
                            ToastUtil.showToastL("数据解析异常")
                            e.printStackTrace()
                        }
                    }
                    override fun onError(response: Response<String>) {

                        Log.e("OkGoError",response.message())
                    }
                })
    }

    private fun showRIlist(RI_list: ArrayList<RepairInfoM>) {
        if (adapter==null){
            adapter= RepairInfoAdapter(RI_list,this)
            rv_ri_list.layoutManager= LinearLayoutManager(this,
                    LinearLayoutManager.VERTICAL,false)
            rv_ri_list.adapter=adapter
        }
        else {
            adapter!!.refresh(RI_list)
            adapter!!.notifyDataSetChanged()
        }
        if (RI_list.size==0) ToastUtil.showToastL("没有数据哟")
    }

    override fun click(v: View) {
        val intent= Intent()
        intent.putExtra("manage",isManage)
        intent.putExtra("ri", v.getTag(R.id.repair_info)as Serializable)
        if (v.id!=R.id.btn_evaluate) {
            intent.setClass(this,RepairInfoActivity::class.java)
        }
        else {
            intent.setClass(this,RepairEvaluateActivity::class.java)
        }
        startActivityForResult(intent,2546)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data==null)return
        val selected:RepairInfoM? = data.getSerializableExtra("back") as RepairInfoM
        if (allRI.contains(selected))allRI.remove(selected)
        if (wait_handleRIs.contains(selected))wait_handleRIs.remove(selected)
        adapter!!.notifyDataSetChanged()
    }

    fun onCLicks(v: View) {
        when (v.id) {
            R.id.img_back -> finish()
            R.id.rbn_all -> {
                showRIlist(allRI)
            }

            R.id.rbn_wait_handle -> {
                showRIlist(wait_handleRIs)
            }

            R.id.rbn_handle_ing -> {
                showRIlist(handleingRIs)
            }

            R.id.rbn_complete -> {
                showRIlist(completeRIs)
            }
        }
    }

}

