package com.fanhong.cn.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log

import com.fanhong.cn.App
import com.fanhong.cn.R
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory

/**
 * Created by Administrator on 2017/9/26.
 */

class WXPayEntryActivity : Activity(), IWXAPIEventHandler {

    private var api: IWXAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pay_result)

        api = WXAPIFactory.createWXAPI(this, App.PayConfig.WX_APPID)
        api!!.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        api!!.handleIntent(intent, this)
    }

    override fun onReq(baseReq: BaseReq) {

    }

    override fun onResp(baseResp: BaseResp) {
        //        Log.i("xqWXPay", "onPayFinish, errCode = " + baseResp.errCode);
        Log.e("TestLog", "onResp: baseResp = " + baseResp.toString())
        if (baseResp.type == ConstantsAPI.COMMAND_PAY_BY_WX) {
            //            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //            builder.setTitle("提示");
            //            builder.setMessage(getString(R.string.wx_pay_result)+"："+baseResp.errCode);
            //            builder.show();

            val intent = Intent()
            intent.action = App.PayConfig.WX_ACTION_RESULT
            intent.putExtra("status", baseResp.errCode)
            intent.putExtra("msg", baseResp.errStr)
            sendBroadcast(intent)
        }
        finish()
        //在需要处理支付结果的地方注册广播来接收消息
    }
}
