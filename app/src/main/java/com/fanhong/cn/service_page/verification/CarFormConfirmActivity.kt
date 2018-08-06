package com.fanhong.cn.service_page.verification

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Half.getSign
import android.util.Log
import android.widget.Toast
import com.alipay.sdk.app.PayTask
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.home_page.fenxiao.PostSuccessActivity
import com.fanhong.cn.tools.*
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.vondear.rxfeature.module.wechat.pay.WechatModel
import com.vondear.rxtool.interfaces.OnSuccessAndErrorListener
import kotlinx.android.synthetic.main.activity_car_form_confirm.*
import org.json.JSONException
import org.json.JSONObject
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class CarFormConfirmActivity : AppCompatActivity() {

    private var payWay: Int = 1
    private val alipayHandler = @SuppressLint("HandlerLeak") object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                9000//支付成功
                ->
                    //"alipay_trade_app_pay_response":{"timestamp":"","total_amount":"","out_trade_no":"","trade_no":""}
                    //结果：下单时间，总金额，商户订单号，支付订单号
                    //                    Toast.makeText(CarFormConfirmActivity.this, R.string.zhifu_success, Toast.LENGTH_SHORT).show();
                    submitOrder()
                6001//用户中途取消
                -> payOrderCancel()
                8000//正在处理中，支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态
                    , 4000//订单支付失败
                    , 5000//重复请求
                    , 6002//网络连接出错
                    , 6004//支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态
                -> payFailure()
                else//其他支付错误
                -> payFailure()
            }//                    Toast.makeText(CarFormConfirmActivity.this, R.string.zhifu_failed, Toast.LENGTH_SHORT).show();
            btn_commit.isEnabled = true
        }
    }

    private val myReceiver: BroadcastReceiver? = MyBroadcastReceiver()

    private inner class MyBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == App.PayConfig.WX_ACTION_RESULT) {
                val reCode = intent.getIntExtra("status", 1) //0成功，-1签名错误等异常，-2用户取消支付操作
                val msg = intent.getStringExtra("msg")
                when (reCode) {
                    0 -> submitOrder()
                    -1 -> payFailure()
                    -2 -> payOrderCancel()
                }
                this@CarFormConfirmActivity.unregisterReceiver(myReceiver)
                btn_commit.isEnabled = true
            }
        }
    }

    private var api: IWXAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_form_confirm)
        tv_title.text = "确认信息"
        img_back.setOnClickListener { finish() }
        tv_form_name.text = intent.getStringExtra("name")
        tv_form_phone.text = intent.getStringExtra("phone")
        tv_form_address.text = intent.getStringExtra("address")

        checkbox_zfb.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (checkbox_wx.isChecked) {
                    checkbox_wx.isChecked = false
                    payWay = 1
                }
            } else if (!checkbox_wx.isChecked)
                buttonView.isChecked = true
        }
        checkbox_wx.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (checkbox_zfb.isChecked) {
                    checkbox_zfb.isChecked = false
                    payWay = 2
                }
            } else if (!checkbox_zfb.isChecked)
                buttonView.isChecked = true
        }
        btn_commit.setOnClickListener {
            when (payWay) {
                1 -> {
                    btn_commit.isEnabled = false
                    val params = RequestParams(App.CMD)
                    params.addParameter("cmd", "77")
                    x.http().post(params, object : Callback.CommonCallback<String> {
                        override fun onSuccess(result: String) {
                            if (JsonSyncUtils.getJsonValue(result, "cw") == "0") {
                                App.PayConfig.alipay_RSA_PRIVATE = JsonSyncUtils.getJsonValue(result, "data")
                                if (!StringUtils.isEmpty(App.PayConfig.alipay_RSA_PRIVATE)) {
                                    alipay()
                                }
                            }
                        }

                        override fun onError(ex: Throwable, isOnCallback: Boolean) {
                            Toast.makeText(this@CarFormConfirmActivity, "启动支付失败，请重试", Toast.LENGTH_LONG).show()
                        }

                        override fun onCancelled(cex: Callback.CancelledException) {
                            Toast.makeText(this@CarFormConfirmActivity, "启动支付失败，请重试", Toast.LENGTH_LONG).show()
                        }

                        override fun onFinished() {

                        }
                    })
                }
                2 -> {
                    btn_commit.isEnabled = false
                    api = WXAPIFactory.createWXAPI(this, App.PayConfig.WX_APPID)
//                    weiXinPay()

                    WXPay2(getRandomString())
                }
            }
        }
        registerReceiver(myReceiver, IntentFilter(App.PayConfig.WX_ACTION_RESULT))
    }

    fun alipay() {
        val orderInfo = getOrderInfo()
        val runnable = Runnable {
            val payTask = PayTask(this@CarFormConfirmActivity)
            val payResult = payTask.payV2(orderInfo, true)//支付
            /**
             * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
             */
            val resultInfo = payResult["result"]// 同步返回需要验证的信息
            val resultStatus = payResult["resultStatus"]

            val msg = Message()
            msg.obj = resultInfo
            Log.e("alipaytest", "resultInfo:$resultInfo\nresultstatus:$resultStatus")
            msg.what = Integer.parseInt(resultStatus)
            alipayHandler.sendMessage(msg)
        }
        Thread(runnable).start()
    }
    private fun WXPay2(orderNum: String) {
//        WechatPayTools.wechatPayUnifyOrder(mContext,
//        WX_APP_ID, //微信分配的APP_ID
//        WX_PARTNER_ID, //微信分配的 PARTNER_ID (商户ID)
//        WX_PRIVATE_KEY, //微信分配的 PRIVATE_KEY (私钥)
//        new WechatModel(order_id, //订单ID (唯一)
//                        money, //价格
//                        name, //商品名称
//                        detail), //商品描述详情
//        new onRequestListener() {
//            @Override
//            public void onSuccess(String s) {}
//
//            @Override
//            public void onError(String s) {}
//    });

        WechatPayTools2.wechatPayUnifyOrder(this,
                App.PayConfig.WX_APPID,
                App.PayConfig.WX_MCH_ID,
                App.PayConfig.WX_PRIVATE_KEY,
                WechatModel(orderNum,"${(300 * 10 * 10)}","没有卵用","帆社区-代办年审"),
                object : OnSuccessAndErrorListener {
                    override fun onSuccess(p0: String?) {
                        ToastUtil.showToastL(p0!!)
                    }

                    override fun onError(p0: String?) {
                        ToastUtil.showToastL(p0!!)
                    }

                }
        )
    }

    //支付宝单号
    private fun getOrderInfo(): String {
        var orderInfo: String? = null
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val timeStr = sdf.format(Date())
        val keyValues = HashMap<String, String>()
        keyValues.put("app_id", App.PayConfig.alipay_APPID)
        keyValues.put("biz_content", getGoodsContent())
        keyValues.put("charset", "utf-8")
        keyValues.put("method", "alipay.trade.app.pay")
        keyValues.put("notify_url", App.PayConfig.alipay_SERVICE_CALLBACK)
        keyValues.put("sign_type", "RSA2")
        keyValues.put("timestamp", timeStr)
        keyValues.put("version", "1.0")

        orderInfo = buildOrderParam(keyValues)
        val sign = getSign(keyValues, App.PayConfig.alipay_RSA_PRIVATE, true)
        Log.e("TestLog", "sign = $sign")
        orderInfo += "&" + sign
        return orderInfo
    }

    /**
     * 这里填写订单信息
     *
     * @return
     */
    private fun getGoodsContent(): String {
        var content = "{"
        content += "\"subject\":\"" + "汽车年审" + "\""//商品的标题/交易标题/订单标题/订单关键字等。
        content += ",\"body\":\"" + "汽车年审支付" + "\""//商品的描述
        content += ",\"out_trade_no\":\"" + getRandomString() + "\""//商户网站唯一订单号
        content += ",\"timeout_express\":\"30m\""//订单超时时间
        content += ",\"total_amount\":\"" + 300.00 + "\""//订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]
//        content += ",\"total_amount\":\"" + 0.01 + "\"";//测试0.01
        content += ",\"product_code\":\"" + "QUICK_MSECURITY_PAY" + "\""//销售产品码，商家和支付宝签约的产品码，为固定值QUICK_MSECURITY_PAY
        return content + "}"
        //        "{" +"\"timeout_express\":\"30m\",\"product_code\":\"QUICK_MSECURITY_PAY\",\"total_amount\":\"0.01\",\"subject\":\"1\",\"body\":\"我是测试数据\",\"out_trade_no\":\"" + getOutTradeNo() + "\"}");
    }//生成15位的随机字符串

    private fun getRandomString(): String {
        var s = ""
        val cs = "1234567890"
        val random = Random()
        val j = cs.length
        for (i in 0..4) {
            s += cs[random.nextInt(j)]
        }
        s = (System.currentTimeMillis() / 1000).toString() + s
        return s
    }

    //将map字符串序列化
    private fun buildOrderParam(map: Map<String, String>): String {
        val keys = ArrayList(map.keys)

        val sb = StringBuilder()
        for (i in 0 until keys.size - 1) {
            val key = keys[i]
            val value = map[key]
            sb.append(buildKeyValue(key, value!!, true))
            sb.append("&")
        }

        val tailKey = keys[keys.size - 1]
        val tailValue = map[tailKey]
        sb.append(buildKeyValue(tailKey, tailValue!!, true))

        return sb.toString()
    }

    private fun buildKeyValue(key: String, value: String, isEncode: Boolean): String {
        val sb = StringBuilder()
        sb.append(key)
        sb.append("=")
        if (isEncode) {
            try {
                sb.append(URLEncoder.encode(value, "UTF-8"))
            } catch (e: UnsupportedEncodingException) {
                sb.append(value)
            }

        } else {
            sb.append(value)
        }
        return sb.toString()
    }

    private fun getSign(map: Map<String, String>, rsaKey: String, rsa2: Boolean): String {
        val keys = ArrayList(map.keys)
        // key排序
        Collections.sort(keys)

        val authInfo = StringBuilder()
        for (i in 0 until keys.size - 1) {
            val key = keys[i]
            val value = map[key]
            authInfo.append(buildKeyValue(key, value!!, false))
            authInfo.append("&")
        }

        val tailKey = keys[keys.size - 1]
        val tailValue = map[tailKey]
        authInfo.append(buildKeyValue(tailKey, tailValue!!, false))

        val oriSign = SignUtils.sign(authInfo.toString(), rsaKey, rsa2)
        var encodedSign = ""

        try {
            encodedSign = URLEncoder.encode(oriSign, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return "sign=" + encodedSign
    }

    private fun weiXinPay() {
        val orderNum = getRandomString()
        val params = RequestParams(App.PayConfig.WX_getOrderUrl1)
        params.addBodyParameter("body", "汽车代办年审消费")
//        params.addBodyParameter("total_fee", "300")
        params.addBodyParameter("total_fee", "0.01")
        params.addBodyParameter("ddh", orderNum)
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                try {
                    val json = JSONObject(result)
                    Log.i("xqWXPay", json.toString())
                    if (null != json && !json.has("retcode")) {
                        val req = PayReq()
                        req.appId = App.PayConfig.WX_APPID
                        req.partnerId = App.PayConfig.WX_MCH_ID
                        req.prepayId = json.getString("prepay_id")
                        req.nonceStr = json.getString("nonce_str")
                        req.timeStamp = json.getString("timestamp")
                        req.packageValue = "Sign=WXPay"
                        //                        String content = "appid=" + req.appId + "&noncestr=" + req.nonceStr + "&package=" +
                        //                                req.packageValue + "&partnerid=" + req.partnerId + "&prepayid=" + req.prepayId +
                        //                                "&timestamp" + req.timeStamp + "&key=" + ParameterConfig.WX_API_KEY;
                        req.sign = json.getString("xsign")
                        //                        req.sign = MD5Util.MD5Encode(content, "").toUpperCase();
                        Log.i("xqWXPay", " sign==>" + req.sign)


                        Toast.makeText(this@CarFormConfirmActivity, "正在调起支付...", Toast.LENGTH_SHORT).show()
                        api?.registerApp(App.PayConfig.WX_APPID)//注册到微信
                        api?.sendReq(req)//调起支付
                    } else {
                        Toast.makeText(this@CarFormConfirmActivity, "返回错误" + json.getString("return_msg"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onFinished() {
            }
        })
    }

    private fun payFailure() {
        val alert = AlertDialog.Builder(this)
                .setMessage("订单支付失败！")
                .setPositiveButton("确定"
                        , null)
                .create()
        alert.show()
    }

    private fun payOrderCancel() {
        AlertDialog.Builder(this)
                .setMessage("订单支付取消！")
                .setPositiveButton("确定"
                        , null)
                .create().show()
    }

    private fun submitOrder() {
        val param = RequestParams(App.CMD)
        param.addBodyParameter("cmd", "75")
        param.addBodyParameter("name", tv_form_name.text.toString())
        param.addBodyParameter("phone", tv_form_phone.text.toString())
        param.addBodyParameter("mapdz", tv_form_address.text.toString())
        x.http().post(param, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                if (JsonSyncUtils.getJsonValue(result, "cw") == "0") {
                    val intent = Intent(this@CarFormConfirmActivity, PostSuccessActivity::class.java)
                    intent.putExtra("from", "Verification")
                    startActivity(intent)
                } else {
                    Toast.makeText(this@CarFormConfirmActivity, "数据提交错误", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

            override fun onFinished() {
            }
        })
    }
}
