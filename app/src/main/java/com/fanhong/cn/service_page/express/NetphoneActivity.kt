package com.fanhong.cn.service_page.express

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import com.zhy.autolayout.utils.AutoUtils
import io.rong.imlib.statistics.UserData.phone
import kotlinx.android.synthetic.main.activity_netphone.*
import kotlinx.android.synthetic.main.activity_top.*
import kotlinx.android.synthetic.main.fragment_user.*
import org.json.JSONException
import org.json.JSONObject
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x
import java.util.*

class NetphoneActivity : AppCompatActivity() {

    private val list = ArrayList<NetphoneModel>()
    private var adapter: NetphoneAdapter? = null
    private var phone = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_netphone)
        tv_title.text = "网点电话"
        img_back.setOnClickListener { finish() }
        adapter = NetphoneAdapter(this, list)
        adapter?.setCallPhone { phone ->
            val builder = AlertDialog.Builder(this@NetphoneActivity)
            builder.setTitle("将要拨打" + phone)
            builder.setMessage("是否立即拨打？")
            builder.setPositiveButton("确认") { _, _ ->
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    val call = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    if (call != PackageManager.PERMISSION_GRANTED) {
                        this.phone = phone
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 11)
                        return@setPositiveButton
                    }
                }
                callNumber(phone)
            }
            builder.setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
            val alertDialog = builder.create()
            alertDialog.show()
        }
        net_phone_rec.adapter = adapter
        net_phone_rec.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        getData()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 11) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val phoneNumber = tv_hotline.text.toString().trim()
                callNumber(phone)
            } else
                ToastUtil.showToastL("需要通话权限！")
        }
    }
    private fun callNumber(phone: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_CALL
        intent.data = Uri.parse("tel:" + phone)
        //        if(ActivityCompat.checkSelfPermission(NetphoneActivity.this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
        //            return;
        //        }
        startActivity(intent)

    }

    private fun getData() {
        val params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "83")
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                if (JsonSyncUtils.getJsonValue(result, "cw").equals("0")) {
                    try {
                        val jsonArray = JSONObject(result).optJSONArray("kdtel")
                        (0 until jsonArray.length())
                                .map { jsonArray.optJSONObject(it) }
                                .mapTo(list) {
                                    val name = it.optString("name") + "："
                                    val phone = it.optString("tel")
                                    NetphoneModel(name, phone)
                                }
                        runOnUiThread { adapter?.notifyDataSetChanged() }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(ex: Throwable, isOnCallback: Boolean) {

            }

            override fun onCancelled(cex: Callback.CancelledException) {

            }

            override fun onFinished() {

            }
        })
    }

    private data class NetphoneModel(val name: String, val phone: String)
    private class NetphoneAdapter(val context: Context, val list: MutableList<NetphoneModel>) : RecyclerView.Adapter<NetphoneAdapter.viewHolder>() {
        private val inflater = LayoutInflater.from(context)

        internal var callPhone: CallPhone? = null
        fun setCallPhone(l: (phone: String) -> Unit) {
            callPhone = object : CallPhone {
                override fun callout(phone: String) {
                    l(phone)
                }
            }
        }

        interface CallPhone {
            fun callout(phone: String)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: viewHolder?, position: Int) {
            val model = list[position]
            holder?.name?.text = model.name
            holder?.phone?.text = model.phone
            holder?.phone?.paint?.flags = Paint.UNDERLINE_TEXT_FLAG
            holder?.phone?.setOnClickListener { callPhone?.callout(holder.phone.text.toString()) }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): viewHolder {
            val view = inflater.inflate(R.layout.telphone_item, parent, false)
            return viewHolder(view)
        }

        class viewHolder(view: View) : RecyclerView.ViewHolder(view) {
            internal var name: TextView = view.findViewById(R.id.net_name)
            internal var phone: TextView = view.findViewById(R.id.net_phone)

            init {
                AutoUtils.autoSize(itemView)
            }
        }
    }
}
