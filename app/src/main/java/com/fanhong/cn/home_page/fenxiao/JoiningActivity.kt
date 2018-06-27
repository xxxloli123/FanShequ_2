package com.fanhong.cn.home_page.fenxiao

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.amap.api.location.AMapLocationClient
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.StringUtils
import com.fanhong.cn.tools.ToastUtil
import kotlinx.android.synthetic.main.activity_joining.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x

class JoiningActivity : AppCompatActivity() {

    private var mSharedPref:SharedPreferences?=null
    private var client:AMapLocationClient?=null
    private var isinput = true  //拦截非用户输入的更改，防止死锁

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joining)
        initView()
    }

    private fun initView() {
//        client = AMapLocationClient(this)
        img_back.setOnClickListener {
            finish()
        }
        tv_title.text = "填写资料"

        bank_card.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isinput = if(isinput){
                    var str = StringUtils.addChar(bank_card.text.toString(),' ')
                    bank_card.setText(str)
                    bank_card.setSelection(str.length)
                    false
                }else{
                    true
                }
            }

        })

        tv_post_information.setOnClickListener {
            submitData()
        }
    }

    private fun submitData(){
        if(fenxiao_name.text.trim().isEmpty()){
            ToastUtil.showToastS("请输入姓名")
            return
        }
        if (!isPhoneNumber(fenxiao_phone.text.toString())){
            ToastUtil.showToastS("请输入正确的电话号码")
        }
        if(hetong_address.text.trim().isEmpty()){
            ToastUtil.showToastS("请输入地址")
            return
        }
        if(!isBankCardNumber(bank_card.text.toString().replace(" ",""))){
            ToastUtil.showToastS("请输入正确的银行卡号")
            return
        }
        mSharedPref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        var params = RequestParams(App.CMD)
        params.addBodyParameter("cmd","67")
        params.addBodyParameter("uid",mSharedPref!!.getString(App.PrefNames.USERID,""))
        params.addBodyParameter("name", fenxiao_name.text.toString())
        params.addBodyParameter("tel", fenxiao_phone.text.toString())
        params.addBodyParameter("dizhi", hetong_address.text.toString())
        params.addBodyParameter("kahao", bank_card.text.toString())
        params.addBodyParameter("ssyh", bank_type.text.toString())
        x.http().post(params,object :Callback.CommonCallback<String>{
            override fun onFinished() {
            }

            override fun onSuccess(result: String?) {
                if (JsonSyncUtils.getJsonValue(result!!,"cw") == "0"){
                    val intent = Intent(this@JoiningActivity,PostSuccessActivity::class.java)
                    intent.putExtra("from", "Express")
                    startActivity(intent)
                }else{
                    ToastUtil.showToastS("提交失败，请重试")
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                ToastUtil.showToastS("提交失败，请重试")
            }

        })
    }
    private fun isPhoneNumber(number:String):Boolean{
        if(StringUtils.validPhoneNum("2",number)){
            return true
        }
        return false
    }
    private fun isBankCardNumber(card:String):Boolean{
        if(card.trim().length in 16..18){
            return true
        }
        return false
    }
}
