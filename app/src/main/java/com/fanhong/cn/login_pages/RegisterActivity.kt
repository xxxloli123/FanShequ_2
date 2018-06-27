package com.fanhong.cn.login_pages

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.MD5Util
import com.fanhong.cn.tools.StringUtils
import com.fanhong.cn.tools.ToastUtil
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        initViews(intent.getStringExtra("type"))
    }

    private fun initViews(type: String?) {
        when (type) {
            "register" -> {
                tv_title.text = "用户注册"
                btn_register_commit.text = "注册"
                checkbox_agree.setOnCheckedChangeListener { buttonView, isChecked ->
                    btn_register_commit.isEnabled = isChecked
                }
                btn_register_commit.setOnClickListener { register() }
            }
            "reset" -> {
                tv_title.text = "重置密码"
                btn_register_commit.text = "确定"
                layout_agreement.visibility = View.GONE
                btn_register_commit.setOnClickListener { resetPwd() }
            }
        }
        img_back.setOnClickListener {
            setResult(-1)
            finish()
        }



        Thread(runnable).start()
    }

    private fun resetPwd() {
        val name = edt_username.text.toString().trim()
        if (!StringUtils.validPhoneNum("0", name)) {
            ToastUtil.showToastL("请输入正确的电话号码！")
            return
        }
        val code = edt_code.text.toString().trim()
        if (TextUtils.isEmpty(code)) {
            ToastUtil.showToastL("请输入验证码！")
            return
        }
        val pwd = edt_password.text.toString().trim()
        if (TextUtils.isEmpty(pwd)) {
            ToastUtil.showToastL("请输入密码")
            return
        }
        val password=MD5Util.getEncryptString(pwd)
        val param = RequestParams(App.CMD)
        param.addParameter("cmd", 7)
        param.addParameter("yzm", code)
        param.addParameter("name", name)
        param.addParameter("password", password)
        x.http().post(param, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                when (JsonSyncUtils.getJsonValue(result, "cw")) {
                    "0" -> {
                        ToastUtil.showToastL("修改成功！")
                        val i = Intent()
                        i.putExtra("username", name)
                        i.putExtra("password", pwd)
                        setResult(21, i)
                        finish()
                    }
                    "1" -> ToastUtil.showToastL("验证码错误！请重试")
                    "2" -> ToastUtil.showToastL("系统错误！请稍后重试")
                    "3" -> ToastUtil.showToastL("用户不存在！")
                }
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

            override fun onFinished() {
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }
        })
    }

    private fun register() {
        val name = edt_username.text.toString().trim()
        if (!StringUtils.validPhoneNum("0", name)) {
            ToastUtil.showToastL("请输入正确的电话号码！")
            return
        }
        val code = edt_code.text.toString().trim()
        if (TextUtils.isEmpty(code)) {
            ToastUtil.showToastL("请输入验证码！")
            return
        }
        val pwd = edt_password.text.toString().trim()
        if (TextUtils.isEmpty(pwd)) {
            ToastUtil.showToastL("请输入密码")
            return
        }
        val password=MD5Util.getEncryptString(pwd)
        val param = RequestParams(App.CMD)
        param.addParameter("cmd", 3)
        param.addParameter("yzm", code)
        param.addParameter("name", name)
        param.addParameter("password", password)
        x.http().post(param, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                when (JsonSyncUtils.getJsonValue(result, "cw")) {
                    "0" -> {
                        ToastUtil.showToastL("注册成功！")
                        val i = Intent()
                        i.putExtra("username", name)
                        i.putExtra("password", pwd)
                        setResult(21, i)
                        finish()
                    }
                    "1" -> ToastUtil.showToastL("验证码错误！")
                    "2" -> ToastUtil.showToastL("系统错误！请稍后重试")
                    "3" -> ToastUtil.showToastL("用户已注册！")
                }
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

            override fun onFinished() {
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }
        })
    }

    private val runnable = Runnable({
        var count: Int = ((System.currentTimeMillis() - App.lastCodeMsgTime) / 1000).toInt()
        runOnUiThread { btn_getcode.isEnabled = false }
        while (count < 30) {
            runOnUiThread { btn_getcode.text = "" + (30 - count) + "秒后可重试" }
            Thread.sleep(1000)
            count = ((System.currentTimeMillis() - App.lastCodeMsgTime) / 1000).toInt()
        }
        runOnUiThread {
            btn_getcode.text = "获取验证码"
            btn_getcode.isEnabled = true
        }
    })

    fun onGetCode(v: View) {
        val phoneNo = edt_username.text.toString().trim()
        if (StringUtils.validPhoneNum("0", phoneNo)) {
            App.lastCodeMsgTime = System.currentTimeMillis()
            Thread(runnable).start()
            val param = RequestParams(App.CMD)
            param.addParameter("cmd", 1)
            param.addParameter("name", phoneNo)
            x.http().post(param, object : Callback.CommonCallback<String> {
                override fun onCancelled(cex: Callback.CancelledException?) {
                }

                override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                    App.lastCodeMsgTime = 0L
                }

                override fun onFinished() {
                }

                override fun onSuccess(result: String?) {
                    if (JsonSyncUtils.getJsonValue(result ?: "", "cw") == "0")
                        ToastUtil.showToastL("短信验证码发送成功")
                }
            })
        } else {
            ToastUtil.showToastL("请输入正确的电话号码！")
        }
    }

    fun onReadAgreement(v: View) {
        startActivityForResult(Intent(this, AgreementSheetActivity::class.java), 22)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            22 -> {//Agree
                checkbox_agree.isChecked = true
            }
            23 -> {//Decline
                checkbox_agree.isChecked = false
            }
        }
    }
}
