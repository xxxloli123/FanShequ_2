package com.fanhong.cn.user_page.shippingaddress

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import kotlinx.android.synthetic.main.activity_my_address.*
import kotlinx.android.synthetic.main.activity_top.*
import org.json.JSONException
import org.json.JSONObject
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x
import java.io.Serializable

class MyAddressActivity : AppCompatActivity() {

    private var status: Int = 0
    private var isNeedResult: Boolean = false
    private var mSharedPref: SharedPreferences? = null

    private var list: MutableList<AddressModel>? = ArrayList()
    private var adapter: AddressAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_address)
        mSharedPref = this.getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        initViews()
    }


    private fun initData() {
        list!!.clear()
        var params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "57")
        params.addBodyParameter("uid", mSharedPref!!.getString(App.PrefNames.USERID, "-1"))
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onFinished() {
            }

            override fun onSuccess(result: String?) {
                try {
                    var array = JSONObject(result).getJSONArray("data")
                    (0 until array.length())
                            .map { array.getJSONObject(it) }
                            .forEach {
                                var model = AddressModel()
                                model.adrid = it.optString("id")
                                model.cellId = it.optString("xid")
                                model.cellName = it.optString("xqname")
                                model.louId = it.optString("ldh")
                                model.louName = it.optString("ldname")
                                model.content = it.optString("dizhi")
                                model.address = it.optString("shdz")
                                model.name = it.optString("name")
                                model.phone = it.optString("dh")
                                model.isDefault = it.optInt("mr")
                                list!!.add(model)
                            }
                    adapter!!.notifyDataSetChanged()
                } catch (e: JSONException) {
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

        })
    }

    private fun initViews() {
        img_back.setOnClickListener { finish() }
        tv_title.setText(R.string.address)
        top_extra.visibility = View.VISIBLE
        top_extra.setText(R.string.control)
        top_extra.setOnClickListener {
            when (status) {
                0 -> {
                    top_extra.setText(R.string.finish)
                    adapter!!.controlable = true
                    adapter!!.notifyDataSetChanged()
                    status = 1
                }
                else -> {
                    top_extra.setText(R.string.control)
                    adapter!!.controlable = false
                    adapter!!.notifyDataSetChanged()
                    status = 0
                }
            }
        }
        add_new_address.setOnClickListener {
            startActivity(Intent(this@MyAddressActivity, AddAddressActivity::class.java))
        }
        adapter = AddressAdapter(this, list!!)
        adapter!!.setConclick(object : AddressAdapter.ControlClick {
            override fun delAddress(adrid: String, pos: Int) {
                AlertDialog.Builder(this@MyAddressActivity).setMessage(R.string.if_sure_delete)
                        .setPositiveButton("确认") { _, _ ->
                            var params = RequestParams(App.CMD)
                            params.addBodyParameter("cmd", "63")
                            params.addBodyParameter("id", adrid)
                            x.http().post(params, object : Callback.CommonCallback<String> {
                                override fun onFinished() {
                                }

                                override fun onSuccess(result: String?) {
                                    when (JsonSyncUtils.getJsonValue(result!!, "cw")) {
                                        "0" -> {
                                            list!!.removeAt(pos)
                                            adapter!!.notifyDataSetChanged()
                                            ToastUtil.showToastS("删除成功！")
                                        }
                                        else -> ToastUtil.showToastS("删除失败，请重试！")
                                    }
                                }

                                override fun onCancelled(cex: Callback.CancelledException?) {
                                }

                                override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                                }

                            })
                        }
                        .setNegativeButton("取消", null)
                        .show()
            }

            override fun edtAddress(model: AddressModel) {
                var intent = Intent(this@MyAddressActivity, EditAddressActivity::class.java)
                intent.putExtra("content", model)
                startActivity(intent)
            }

        })
        address_list.adapter = adapter

        isNeedResult = intent.getBooleanExtra("result", false)
        address_list.setOnItemClickListener { _, _, position, _ ->
            if (isNeedResult) {
                val addr = list!![position]
                val i = Intent()
                i.putExtra("addrId", addr.adrid)
                i.putExtra("addrName", addr.address)
                setResult(101, i)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initData()
    }

    class AddressModel : Serializable {
        internal var name: String? = null
        internal var phone: String? = null
        internal var address: String? = null
        internal var cellName: String? = null
        internal var louName: String? = null
        internal var content: String? = null
        internal var cellId: String? = null
        internal var louId: String? = null
        internal var isDefault: Int = 0
        internal var adrid: String? = null
        override fun toString(): String {
            return "AddressModel(name=$name, phone=$phone, address=$address, cellName=$cellName, louName=$louName, content=$content, cellId=$cellId, louId=$louId, isDefault=$isDefault, adrid=$adrid)"
        }
    }
}
