package com.fanhong.cn.user_page.shippingaddress

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.myviews.SpinerPopWindow
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import kotlinx.android.synthetic.main.activity_edit_address.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x

class EditAddressActivity : AppCompatActivity() {

    var mSharedPref: SharedPreferences? = null
    var cellId: String? = null
    var louId: String? = null
    var adrId: String? = null
    private var cellNames: MutableList<String>? = ArrayList()
    private var cellIds: MutableList<String>? = ArrayList()
    private var louNames: MutableList<String>? = ArrayList()
    private var louIds: MutableList<String>? = ArrayList()
    var checked: Int = -1
    var ssp: SpinerPopWindow<String>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_address)
        init()
    }

    private fun init() {
        img_back.setOnClickListener { createDialog() }
        tv_title.setText(R.string.edtaddress)
        top_extra.setText(R.string.save)
        top_extra.visibility = View.VISIBLE
        top_extra.setOnClickListener {
            editAddress()
        }

        var model = intent.getSerializableExtra("content") as MyAddressActivity.AddressModel
        change_name_edt.setText(model.name)
        change_phone_edt.setText(model.phone)
        change_address_choosecell.text = model.cellName
        change_address_chooselou.text = model.louName
        change_address_edt.setText(model.content)
        cellId = model.cellId
        louId = model.louId
        adrId = model.adrid
        checked = model.isDefault
        when (checked) {
            0 -> whether_default.isChecked = false
            1 -> {
                whether_default.isChecked = true
                whether_default.isEnabled = false
            }
        }
        whether_default.setOnCheckedChangeListener { _, isChecked ->
            checked = if (isChecked)
                1
            else
                0
        }

        change_address_choosecell.setOnClickListener {
            getCells()
        }
        change_address_chooselou.setOnClickListener {
            getLous()
        }

    }

    private fun getCells() {
        cellNames!!.clear()
        cellIds!!.clear()
        var params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "29")
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onFinished() {
            }

            override fun onSuccess(result: String?) {
                if (JsonSyncUtils.getJsonValue(result!!, "cw") == "0") {
                    var data = JsonSyncUtils.getJsonValue(result!!, "data")
                    cellNames = JsonSyncUtils.getStringList(data!!, "name")
                    cellIds = JsonSyncUtils.getStringList(data!!, "id")

                    ssp = SpinerPopWindow(this@EditAddressActivity, cellNames!!, "") { parent, view, position, id ->
                        change_address_choosecell.text = cellNames!![position]
                        cellId = cellIds!![position]
                        ssp!!.dismiss()
                        change_address_chooselou.setText(R.string.chooselou)
                        louId = ""
                    }
                    ssp!!.width = change_address_choosecell.width
                    ssp!!.showAsDropDown(change_address_choosecell, 0, 0)
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

        })
    }

    private fun getLous() {
        louNames!!.clear()
        louIds!!.clear()
        var params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "1001")
        params.addBodyParameter("xid", cellId)
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onFinished() {
            }

            override fun onSuccess(result: String?) {
                if (JsonSyncUtils.getJsonValue(result!!, "state") == "200") {
                    var data = JsonSyncUtils.getJsonValue(result!!, "data")
                    louNames = JsonSyncUtils.getStringList(data!!, "bname")
                    louIds = JsonSyncUtils.getStringList(data!!, "id")

                    ssp = SpinerPopWindow(this@EditAddressActivity, louNames!! , ""){ parent, view, position, id ->
                        change_address_chooselou.text = louNames!![position]
                        louId = louIds!![position]
                        ssp!!.dismiss()
                    }
                    ssp!!.width = change_address_chooselou.width
                    ssp!!.showAsDropDown(change_address_chooselou)
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

        })
    }

    private fun editAddress() {
        mSharedPref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        var params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "61")
        params.addBodyParameter("uid", mSharedPref!!.getString(App.PrefNames.USERID, "-1"))
        params.addBodyParameter("id", adrId)
        params.addBodyParameter("xid", cellId)
        params.addBodyParameter("ldh", louId)
        params.addParameter("mr", checked)
        params.addParameter("dizhi", change_address_edt.text.toString())
        params.addParameter("dh", change_phone_edt.text.toString())
        params.addParameter("name", change_name_edt.text.toString())
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onFinished() {
            }

            override fun onSuccess(result: String?) {
                when (JsonSyncUtils.getJsonValue(result!!, "cw")) {
                    "0" -> {
                        ToastUtil.showToastS("修改成功！")
                        this@EditAddressActivity.finish()
                    }
                    else -> ToastUtil.showToastS("修改失败，请重试！")
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                ToastUtil.showToastS("修改失败，请重试！")
            }

        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            createDialog()
        }
        return true
    }

    private fun createDialog() {
        AlertDialog.Builder(this)
                .setTitle("更改尚未保存")
                .setMessage("是否放弃修改？")
                .setPositiveButton("确认") { dialog, which -> this@EditAddressActivity.finish() }
                .setNegativeButton("取消", null)
                .show()
    }
}
