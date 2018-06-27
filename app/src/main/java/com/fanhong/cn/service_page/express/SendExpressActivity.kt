package com.fanhong.cn.service_page.express

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.AdapterView
import android.widget.Toast
import com.amap.api.location.AMapLocationClient
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.home_page.fenxiao.PostSuccessActivity
import com.fanhong.cn.myviews.SpinerPopWindow
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import kotlinx.android.synthetic.main.activity_send_express.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x
import java.util.*

class SendExpressActivity : AppCompatActivity() {

    private var client: AMapLocationClient? = null
    private var spw: SpinerPopWindow<String>? = null
    private var provinces: MutableList<String> = ArrayList()
    private var seProvince: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_express)
        tv_title.text = "寄快递"
        img_back.setOnClickListener { finish() }

        setOnClicks()

        client = AMapLocationClient(this)
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermission()
        } else {
            getLocation()
        }
    }

    private fun getLocation() {
        val location = client?.lastKnownLocation
        if (location != null && location.errorCode == 0) {
            val strings = location.address.split("靠近".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            send_express_address.setText(strings[0])
            seProvince = location.province
        }
    }

    private fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(applicationContext, "没有权限,请手动开启定位权限", Toast.LENGTH_SHORT).show()
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            ActivityCompat.requestPermissions(this@SendExpressActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE), 110)
        } else {
            getLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 110) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 获取到权限，作相应处理（调用定位SDK应当确保相关权限均被授权，否则可能引起定位失败）
                getLocation()
            } else {
                // 没有获取到权限，做特殊处理
                ToastUtil.showToastS("获取位置权限失败，请手动开启")
            }
        }
    }

    private fun setOnClicks() {
        choose_get_city.setOnClickListener {
            val params = RequestParams(App.CMD)
            params.addBodyParameter("cmd", "89")
            x.http().post(params, object : Callback.CommonCallback<String> {
                override fun onSuccess(result: String) {
                    if (JsonSyncUtils.getJsonValue(result, "cw") == "0") {
                        provinces = JsonSyncUtils.getStringList(JsonSyncUtils.getJsonValue(result, "data"), "province")
                        spw = SpinerPopWindow(this@SendExpressActivity, provinces, "") { _, _, position, _ ->
                            choose_get_city.text = provinces[position]
                            spw?.dismiss()
                        }
                        spw?.width = choose_get_city.width
                        spw?.showAsDropDown(choose_get_city, 0, 0)
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
        ll_express_time.setOnClickListener {
            val intent = Intent(this, ChooseExpressActivity::class.java)
            intent.putExtra("status", 1)
            startActivityForResult(intent, 0)
        }
        ll_express_type.setOnClickListener {
            val intent1 = Intent(this, ChooseExpressActivity::class.java)
            intent1.putExtra("status", 2)
            startActivityForResult(intent1, 0)
        }
        submit_send_express.setOnClickListener {
            when {
                TextUtils.isEmpty(send_express_address.text.toString()) -> Toast.makeText(this, "请填写寄件人地址", Toast.LENGTH_SHORT).show()
                TextUtils.isEmpty(send_express_name.text.toString()) -> Toast.makeText(this, "请填写寄件人姓名", Toast.LENGTH_SHORT).show()
                TextUtils.isEmpty(get_express_address.text.toString()) -> Toast.makeText(this, "请填写收件人地址", Toast.LENGTH_SHORT).show()
                TextUtils.isEmpty(get_express_name.text.toString()) -> Toast.makeText(this, "请填写收件人姓名", Toast.LENGTH_SHORT).show()
                else -> submitMessage()
            }
        }
    }

    private fun submitMessage() {
        val params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "73")
        params.addBodyParameter("uid", getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE).getString(App.PrefNames.USERID,"-1"))
        params.addBodyParameter("jsf", seProvince)
        params.addBodyParameter("jdizhi", send_express_address.text.toString())
        params.addBodyParameter("jmz", send_express_name.text.toString())
        params.addBodyParameter("ssf", choose_get_city.text.toString())
        params.addBodyParameter("sdizhi", get_express_address.text.toString())
        params.addBodyParameter("smz", get_express_name.text.toString())
        params.addBodyParameter("smtime", tv_ex_time.text.toString())
        params.addBodyParameter("kdlx", tv_ex_type.text.toString())
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onSuccess(s: String) {
                val cw = JsonSyncUtils.getJsonValue(s, "cw")
                if (cw == "0") {
                    val intent = Intent(this@SendExpressActivity, PostSuccessActivity::class.java)
                    intent.putExtra("fromExpress", true)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@SendExpressActivity, "提交失败，请重试", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(throwable: Throwable, b: Boolean) {

            }

            override fun onCancelled(e: Callback.CancelledException) {

            }

            override fun onFinished() {

            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == 46) {
            val type = data.getStringExtra("string")
            tv_ex_type.text = type
        } else if (resultCode == 45) {
            val time = data.getStringExtra("string")
            tv_ex_time.text = time
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
