package com.fanhong.cn.login_pages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x

class LoginActivity : AppCompatActivity() {
    private var failCount = 0
    private var realCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tv_title.text = "用户登录"
        img_back.setOnClickListener { finish() }

        img_showCode.setOnClickListener {
            img_showCode.setImageBitmap(Code.instance.createBitmap())
            realCode = Code.instance.code.toLowerCase()
        }
        img_showCode.callOnClick()
    }

    fun onLogin(v: View) {
        val username = edt_username.text.toString().trim()
        if (!StringUtils.validPhoneNum("0", username)) {
            ToastUtil.showToastL("请输入正确的电话号码！")
            return
        }
        val pwd = edt_password.text.toString().trim()
        if (TextUtils.isEmpty(pwd)) {
            ToastUtil.showToastL("请输入密码！")
            return
        }
        if (failCount > 3) {
            val code = edt_code.text.toString().trim()
            if (TextUtils.isEmpty(code) || code.equals(realCode, false)) {
                ToastUtil.showToastL("验证码错误！")
                return
            }
        }
        btn_login.isEnabled = false
        btn_login.text = "正在登录..."
        val password = MD5Util.getEncryptString(pwd)
        val param = RequestParams(App.CMD)
        param.addParameter("cmd", 5)
        param.addParameter("name", username)
        param.addParameter("password", password)
        x.http().post(param, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                if (JsonSyncUtils.getJsonValue(result, "cw") == "0") {
                    ToastUtil.showToastL("登录成功！")
                    Log.e("testLog",result)
                    val id = JsonSyncUtils.getJsonValue(result, "id")
                    val name = JsonSyncUtils.getJsonValue(result, "name")
                    val token = JsonSyncUtils.getJsonValue(result, "token")
                    val user = JsonSyncUtils.getJsonValue(result, "user")
                    val logo = JsonSyncUtils.getJsonValue(result, "logo")
                    val editor = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                    editor.putString(App.PrefNames.USERID, id)
                    editor.putString(App.PrefNames.USERNAME, name)
                    editor.putString(App.PrefNames.PASSOWRD, pwd)//保存未加密的密码
                    editor.putString(App.PrefNames.TOKEN, token)
                    editor.putString(App.PrefNames.NICKNAME, user)
                    editor.putString(App.PrefNames.HEADIMG, logo)
                    editor.commit()
                    setResult(11)
                    finish()
                } else {
                    failCount++
                    if (failCount == 4) {
                        layout_code.visibility = View.VISIBLE
                    }
                    ToastUtil.showToastL("用户名或密码错误！")
                }
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                ToastUtil.showToastL("访问服务器失败，请检查网络连接")
                failCount++
                if (failCount == 4) {
                    layout_code.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onFinished() {
                btn_login.isEnabled = true
                btn_login.text = "登录"
            }
        })
    }

    fun onRegister(v: View) {
        val i = Intent(this, RegisterActivity::class.java)
        i.putExtra("type", "register")
        startActivityForResult(i, 21)
    }

    fun onForgetPwd(v: View) {
        val i = Intent(this, RegisterActivity::class.java)
        i.putExtra("type", "reset")
        startActivityForResult(i, 21)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 21) {
            edt_username.setText(data?.getStringExtra("username"))
            edt_password.setText(data?.getStringExtra("password"))
        }
    }
}
