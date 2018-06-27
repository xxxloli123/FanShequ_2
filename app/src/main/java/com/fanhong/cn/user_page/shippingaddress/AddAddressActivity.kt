package com.fanhong.cn.user_page.shippingaddress

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.myviews.SpinerPopWindow
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.StringUtils
import com.fanhong.cn.tools.ToastUtil
import kotlinx.android.synthetic.main.activity_add_address.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x

class AddAddressActivity : AppCompatActivity() {

    private var checked = 0
    private var mSharedPref: SharedPreferences? = null

    private var cellNames: MutableList<String> = ArrayList()
    private var cellIds: MutableList<String> = ArrayList()
    private var louNames: MutableList<String> = ArrayList()
    private var louIds: MutableList<String> = ArrayList()
    private var ssp: SpinerPopWindow<String>? = null

    private var cellId: String? = null
    private var louId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_address)
        initViews()
    }

    private fun initViews() {
        img_back.setOnClickListener { finish() }
        tv_title.setText(R.string.addaddress)
        top_extra.setText(R.string.save)
        top_extra.visibility = View.VISIBLE

        top_extra.setOnClickListener {
            if (input_name_edt.text.trim().isEmpty()) {
                ToastUtil.showToastS("请输入姓名！")
                return@setOnClickListener
            }
            if (input_phone_edt.text.trim().isEmpty()) {
                ToastUtil.showToastS("请输入联系电话！")
                return@setOnClickListener
            }
            if (!StringUtils.validPhoneNum("2", input_phone_edt.text.toString().trim())) {
                ToastUtil.showToastS("请输入正确的电话号码！")
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(cellId)) {
                ToastUtil.showToastS("请选择小区！")
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(louId)) {
                ToastUtil.showToastS("请选择楼栋！")
                return@setOnClickListener
            }
            if (input_address_edt.text.trim().isEmpty()) {
                ToastUtil.showToastS("请输入详细地址！")
                return@setOnClickListener
            }
            addNewAddress()
        }

        setEnableds(false, false, false)

        address_choosecell.setOnClickListener {
            getCells()
        }
        address_chooselou.setOnClickListener {
            ssp = SpinerPopWindow(this@AddAddressActivity, louNames!!, ""){ parent, view, position, id ->
                address_chooselou.text = louNames!![position]
                louId = louIds!![position]
                setEnableds(true, true, false)
                ssp!!.dismiss()
            }
            ssp!!.width = address_chooselou.width
            ssp!!.showAsDropDown(address_chooselou, 0, 0)
        }

        input_address_edt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!s.trim().isEmpty()) {
                    setEnableds(true, true, true)
                } else {
                    setEnableds(true, true, false)
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
        whether_set_default.setOnCheckedChangeListener { _, isChecked ->
            checked = if (isChecked)
                1
            else
                0
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

                    ssp = SpinerPopWindow(this@AddAddressActivity, cellNames!!, "") { parent, view, position, id ->
                        address_choosecell.text = cellNames!![position]
                        cellId = cellIds!![position]
                        setEnableds(true, true, false)
                        ssp!!.dismiss()
                        address_chooselou.setText(R.string.chooselou)
                        louId = ""
                        getLous()
                    }
                    ssp!!.width = address_choosecell.width
                    ssp!!.showAsDropDown(address_choosecell, 0, 0)
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
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

        })
    }

    private fun addNewAddress() {
        mSharedPref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        var params = RequestParams(App.CMD)
        params.addParameter("cmd", "59")
        params.addParameter("uid", mSharedPref!!.getString(App.PrefNames.USERID, "-1"))
        params.addParameter("mr", checked)
        params.addParameter("xid", cellId)
        params.addParameter("ldh", louId)
        params.addParameter("dizhi", input_address_edt.text.toString())
        params.addParameter("dh", input_phone_edt.text.toString())
        params.addParameter("name", input_name_edt.text.toString())
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onFinished() {
            }

            override fun onSuccess(result: String?) {
                var cw = JsonSyncUtils.getJsonValue(result!!, "cw")
                when (cw) {
                    "0" -> {
                        ToastUtil.showToastS("添加成功！")
                        this@AddAddressActivity.finish()
                    }
                    else -> ToastUtil.showToastS("添加失败，请重试！")
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

        })
    }

    fun setEnableds(louClickable: Boolean, inputable: Boolean, checkable: Boolean) {
        address_chooselou.isEnabled = louClickable
        input_address_edt.isEnabled = inputable
        whether_set_default.isEnabled = checkable
    }
}
