package com.fanhong.cn.service_page.shop

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.alipay.sdk.app.PayTask
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.http.callback.StringDialogCallback
import com.fanhong.cn.login_pages.LoginActivity
import com.fanhong.cn.tools.*
import com.fanhong.cn.user_page.shippingaddress.MyAddressActivity
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.zhy.autolayout.utils.AutoUtils
import kotlinx.android.synthetic.main.activity_order_confirm.*
import kotlinx.android.synthetic.main.activity_top.*
import me.leefeng.promptlibrary.PromptDialog
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.xutils.common.Callback
import org.xutils.db.sqlite.WhereBuilder
import org.xutils.http.RequestParams
import org.xutils.image.ImageOptions
import org.xutils.view.annotation.ViewInject
import org.xutils.x
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class OrderConfirmActivity : AppCompatActivity() {
    private val goods: MutableList<GoodsCarTable> = ArrayList()
    private var adapter: GoodsAdapter? = null
    private var payWay: Int = 1 //1:支付宝 2:微信
    private var addrId = "-1"
    private var total = 0f
    private var from = ""
    private var orderTitle = ""
    private var orderDescription: String = ""

    private var orderTime = 0L
    private var orderNum=""
    lateinit var addr:MyAddressActivity.AddressModel
    lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirm)
        tv_title.text = "确认订单"
        pref = this.getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        img_back.setOnClickListener { finish() }

        adapter = GoodsAdapter(this, goods)
        lv_goods_pay.adapter = adapter
        from = intent.getStringExtra("from")
        when (from) {
            "goods" -> {
                val info = intent.getSerializableExtra("goods") as GoodsCarTable
                total = intent.getFloatExtra("total", 0.0f)
                total = (Math.round(total * 100)).toFloat() / 100
                tv_price.text = "￥$total"
                orderTitle = info.name
                orderDescription = info.content
                goods.clear()
                goods.add(info)
                adapter?.notifyDataSetChanged()
            }
            "car" -> {
                total = intent.getFloatExtra("total", 0.0f)
                tv_price.text = "￥$total"
                goods.clear()
                goods += App.db.selector(GoodsCarTable::class.java).where("c_uid", "=", getUid()).and("c_select", "=", true).findAll()
                orderTitle = "${goods[0].name}...等${goods.map { it.count }.sum()}件商品"
                for (i in goods)
                    orderDescription += "${i.name}×${i.count} "
                adapter?.notifyDataSetChanged()
            }
        }

        layout_choose_addr.setOnClickListener {
            val i = Intent(this, MyAddressActivity::class.java)
            i.putExtra("result", true)
            startActivityForResult(i, 101)
        }

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

        btn_commit.setOnClickListener { onCommit() }

        val intentFilter = IntentFilter()
        intentFilter.addAction(App.PayConfig.WX_ACTION_RESULT)
        registerReceiver(wxReciver, intentFilter)
    }

    override fun onResume() {
        super.onResume()
        btn_commit.isEnabled = true
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wxReciver)
    }

    private fun getUid(): String = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE).getString(App.PrefNames.USERID, "-1")

    private fun onCommit() {
        if (!isLoged()) {
            AlertDialog.Builder(this).setMessage("请先登录！").setPositiveButton("立即登录", { _, _ ->
                startActivity(Intent(this@OrderConfirmActivity, LoginActivity::class.java))
            }).setNegativeButton("取消", null).show()
            return
        }
        if (addrId == "-1") {
            ToastUtil.showToastL("请选择收货地址！")
            return
        }
        btn_commit.isEnabled = false
        val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val uid = pref.getString(App.PrefNames.USERID, "-1")
        val param = RequestParams(App.CMD)
        param.addBodyParameter("cmd", "1013")
        param.addBodyParameter("uid", uid)
        param.addBodyParameter("zjje", total.toString())
        param.addBodyParameter("ldh", addrId)
        param.addBodyParameter("goods", getJsonArray())

        layout_progressBar.visibility = View.VISIBLE
        x.http().post(param, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                when (JsonSyncUtils.getState(result)) {
                    200 -> {
                        Log.e("TestLog", "request:${param.toJSONString()}\nresult:$result")
//                        var t1 = Date(1483159625851)
                        val data = JsonSyncUtils.getJsonValue(result, "data")
                         orderNum = JsonSyncUtils.getJsonValue(data, "ddh")
                         orderTime = JsonSyncUtils.getJsonValue(data, "time").toLong()//订单生成时间
                        val targetTime = orderTime + 1000 * 60 * 15//预定最后支付时间为订单生成时间15分钟后
                        val residueTime = targetTime - System.currentTimeMillis()//剩余支付时间
                        if (residueTime > 1000 * 60) {
                            //仅在剩余支付时间大于一分钟的情况下才进行支付
                            when (payWay) {
                                1 -> aliPay(orderNum, residueTime)
                                2 -> WXPay(orderNum, targetTime)
                            }
                        } else {
                            ToastUtil.showToastL("订单已超时！请重新下单！")
                        }
                    }
                    400 -> {
                        ToastUtil.showToastL("订单获取失败！")
                    }
                }
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                ToastUtil.showToastL("连接服务器失败！请检查网络")
                btn_commit.isEnabled = true
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
                btn_commit.isEnabled = true
            }

            override fun onFinished() {
                layout_progressBar.visibility = View.GONE
            }
        })
        isHaveSuperior()
    }

    private fun isHaveSuperior() {
//        1037.查询是否有上家(APP->平台)
//        cmd:数据类型
//        uid:当前用户的ID
        val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        OkGo.post<String>(App.CMD)
                .tag(this)//
                .isMultipart(true)
                .params("cmd","1037")
                .params("uid",pref.getString(App.PrefNames.USERID, "-1"))
                .execute(object : StringDialogCallback(this) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGobody", response.body().toString())
                        try {
                            val json = JSONObject(response.body()!!.toString())
                            if (json.getString("cw") == "1"){
                                arl_recommend.visibility=View.VISIBLE
                            }
                        }catch (e: JSONException) {
                            LogUtil.e("JSONException",e.toString())
                            e.printStackTrace()
                        }
                    }

                    override fun onError(response: Response<String>) {
                        Log.e("OkGoError", response.message())
                    }
                })

    }

    private fun isLoged(): Boolean = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE).getString(App.PrefNames.USERID, "-1") != "-1"

    private fun getJsonArray(): String {
        var str = "["
        for (i in 0 until goods.size) {
            if (i != 0) str += ","
            str += "{\"id\":\"${goods[i].gid}\",\"count\":\"${goods[i].count}\"}"
        }
        return str + "]"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 101 && null !== data) {
            tv_chosen_address.text = data.getStringExtra("addrName")
            addrId = data.getStringExtra("addrId")
            addr = data.getSerializableExtra("addr") as MyAddressActivity.AddressModel
        }
    }

    fun onNullClick(v: View) {}
    private val alipayHandler = @SuppressLint("HandlerLeak") object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
            //支付宝
                9000//支付成功
                -> {
                    Log.e("TestLog", "payResult = ${msg.obj}")
                    val response = JsonSyncUtils.getJsonValue(msg.obj.toString(), "alipay_trade_app_pay_response")
                    paySuccess()
                    if (from == "car")//删除购物车已选择商品
                        App.db.delete(GoodsCarTable::class.java, WhereBuilder.b("c_select", "=", true).and("c_uid", "=", getUid()))
                }
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
            }
            btn_commit.isEnabled = true
        }
    }

    private fun aliPay(orderNum: String, residueTime: Long) {
        val param = RequestParams(App.CMD)
        param.addBodyParameter("cmd", "77")
        x.http().post(param, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                if (JsonSyncUtils.getJsonValue(result, "cw") == "0") {
                    App.PayConfig.alipay_RSA_PRIVATE = JsonSyncUtils.getJsonValue(result, "data")
                    if (!StringUtils.isEmpty(App.PayConfig.alipay_RSA_PRIVATE)) {
                        val orderInfo = getOrderInfo(orderNum, residueTime)//获取支付宝订单信息
                        val runnable = Runnable {
                            val payTask = PayTask(this@OrderConfirmActivity)
                            val payResult = payTask.payV2(orderInfo, true)//调起支付

                            /**
                             * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                             */
                            val resultInfo = payResult["result"]// 同步返回需要验证的信息
                            val resultStatus = payResult["resultStatus"]

                            val msg = Message()
                            msg.obj = resultInfo
                            Log.e("alipaytest", "resultInfo:$resultInfo\nresultstatus:$resultStatus")
                            msg.what = resultStatus?.toInt() ?: 0
                            alipayHandler.sendMessage(msg)
                        }
                        Thread(runnable).start()
                    }
                } else {
                    ToastUtil.showToastL("启动支付失败！")
                    btn_commit.isEnabled = true
                }
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                ToastUtil.showToastL("启动支付失败！请检查网络")
                btn_commit.isEnabled = true
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
                btn_commit.isEnabled = true
            }

            override fun onFinished() {
            }
        })
    }

    //支付宝单号
    fun getOrderInfo(orderNum: String, residueTime: Long): String {
        var orderInfo: String? = null
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val timeStr = sdf.format(Date())
        val keyValues = HashMap<String, String>()
        keyValues.put("app_id", App.PayConfig.alipay_APPID)
        keyValues.put("biz_content", getGoodsContent(orderNum, residueTime))
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

    private fun getGoodsContent(orderNum: String, residueTime: Long): String {
        val minutes = residueTime / 1000 / 60
        var content = "{"
        content += "\"subject\":\"$orderTitle\""//商品的标题/交易标题/订单标题/订单关键字等。
        content += ",\"body\":\"$orderDescription\""//商品的描述
        content += ",\"out_trade_no\":\"$orderNum\""//商户网站唯一订单号
        content += ",\"timeout_express\":\"${minutes}m\""//订单超时时间
//        content += ",\"goods_type\":\"1\""//商品主类型：0—虚拟类商品，1—实物类商品 注：虚拟类商品不支持使用花呗渠道
//        content += ",\"enable_pay_channels\":\"pcredit,balance,moneyFund,debitCardExpress\""//可用渠道，用户只能在指定渠道范围内支付
        content += ",\"total_amount\":\"$total\""//订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]
//        content += ",\"total_amount\":\"" + 0.01 + "\""
        content += ",\"product_code\":\"" + "QUICK_MSECURITY_PAY" + "\""//销售产品码，商家和支付宝签约的产品码，为固定值QUICK_MSECURITY_PAY
        return content + "}"
        //        "{" +"\"timeout_express\":\"30m\",\"product_code\":\"QUICK_MSECURITY_PAY\",\"total_amount\":\"0.01\",\"subject\":\"1\",\"body\":\"我是测试数据\",\"out_trade_no\":\"" + getOutTradeNo() + "\"}");
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

    private fun WXPay(orderNum: String, orderTime: Long) {
        val sdf = SimpleDateFormat("yyyyMMddHHmmss")
        val api = WXAPIFactory.createWXAPI(this, App.PayConfig.WX_APPID)
        val param = RequestParams(App.PayConfig.WX_getOrderUrl)
        param.addBodyParameter("body", orderTitle)
        param.addBodyParameter("total_fee", total.toString())
//        param.addBodyParameter("total_fee","0.01")
        param.addBodyParameter("ddh", orderNum)
        param.addBodyParameter("times", sdf.format(Date(orderTime)))
        x.http().post(param, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                Log.e("TestLog", "WX request = ${param.toJSONString()}\n\tresult = $result")
                if (JsonSyncUtils.getJsonValue(result, "result_code") == "SUCCESS") {
                    val req = PayReq()
                    req.appId = App.PayConfig.WX_APPID
                    req.partnerId = App.PayConfig.WX_MCH_ID
                    req.prepayId = JsonSyncUtils.getJsonValue(result, "prepay_id")
                    req.nonceStr = JsonSyncUtils.getJsonValue(result, "nonce_str")
                    req.timeStamp = JsonSyncUtils.getJsonValue(result, "timestamp")
                    req.packageValue = "Sign=WXPay"
                    req.sign = JsonSyncUtils.getJsonValue(result, "xsign")
                    val r1 = api.registerApp(App.PayConfig.WX_APPID)
//                    ToastUtil.showToastL("正在调起微信支付...")
                    val r = api.sendReq(req)
//                    Log.e("TestLog","registerResult = $r1 , sendReqResult = $r")
//                    btn_commit.isEnabled = true
                } else {
                    ToastUtil.showToastL("启动支付失败！")
                    btn_commit.isEnabled = true
                }
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                btn_commit.isEnabled = true
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
                btn_commit.isEnabled = true
            }

            override fun onFinished() {
            }
        })
    }

    private var wxReciver: BroadcastReceiver = WXPayReciver()

    private inner class WXPayReciver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == App.PayConfig.WX_ACTION_RESULT) {//微信支付回调
                val reCode = intent.getIntExtra("status", 1) //0成功，-1签名错误等异常，-2用户取消支付操作
                val msg = intent.getStringExtra("msg")
                when (reCode) {
                    0 -> {
                        paySuccess()
                        if (from == "car")//删除购物车已选择商品
                            App.db.delete(GoodsCarTable::class.java, WhereBuilder.b("c_select", "=", true).and("c_uid", "=", getUid()))
                    }
                    -1 -> payFailure()
                    -2 -> payOrderCancel()
                }
//                this@OrderConfirmActivity.unregisterReceiver(wxReciver)
                btn_commit.isEnabled = true
            }
        }
    }

    private fun paySuccess() {
        if (arl_recommend.visibility==View.VISIBLE){
            btn_commit.text="已支付"
            btn_commit.isEnabled=false
            PromptDialog(this).showSuccess("支付成功 填写推荐人可得积分哟")
        }else{
            val dialog = AlertDialog.Builder(this)
                    .setMessage("订单支付成功！")
                    .setPositiveButton("确定", null).create()
            dialog.setOnDismissListener { finish() }
            dialog.show()
        }
    }

    fun submitRecommend(v: View) {
        var string=""
        for (i in goods.indices){
            val params = HashMap<String, String>()
            params["ID"] = goods[i].gid
            params["ID2"] = goods[i].count.toString()
            if (i==0) string ="${goods[i].gid}:${goods[i].gid}" else
                string+=",${goods[i].gid}:${goods[i].gid}"
        }

//        1031.“添加三级分销（APP->平台）
//        cmd：数据类型
//        uid：下订单用户ID
//        time：下订单时间
//        zjje：支付金额
//        zffs：支付方式（1支付宝，2微信）
//        user：收货人姓名
//        dh：收货人手机号
//        ldh：详细地址
//        ddh：订单号
//        qid：小区ID号
//        goods: 商品ID和数量（ID:数量，ID2：数量）
//        phone:代理商电话号码
        OkGo.post<String>(App.CMD)
                .tag(this)//
                .params("cmd", "1031")
                .params("uid", pref.getString(App.PrefNames.USERID, "-1"))
                .params("time", orderTime.toString())
                .params("zjje", total.toString())
                .params("zffs", payWay.toString())
                .params("user", addr.name)
                .params("dh", addr.phone)
                .params("ldh", addr.address)
                .params("ddh", orderNum)
                .params("qid", pref.getString(App.PrefNames.GARDENID, ""))
                .params("goods", string)
                .params("phone", edt_phone.text.toString())
                .execute(object : StringDialogCallback(this) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGo body",response.body().toString())
                        try {
                            val json = JSONObject(response.body()!!.toString())
                            when(json.getInt("cw")){
                                0-> {
                                    addCommission()
                                }
                                1-> PromptDialog(this@OrderConfirmActivity).showError("系统错误")
                                2-> PromptDialog(this@OrderConfirmActivity).showError("电话号码错误")
                                3-> PromptDialog(this@OrderConfirmActivity).showError("没有这个代理商")
                            }
                        } catch (e: JSONException) {
                            LogUtil.e("JSONException",e.toString())
                            ToastUtil.showToastL("数据解析异常")
                            e.printStackTrace()
                        }
                    }
                    override fun onError(response: Response<String>) {
                        AlertDialog.Builder(this@OrderConfirmActivity).setMessage("填写推荐人失败！ 是否重试?")
                                .setPositiveButton("确定") { _, _ ->
                                    submitRecommend(btn_commit)
                                }.setNegativeButton("取消") { _, _ ->
                                }.show()
                        Log.e("OkGoError",response.exception.toString())
                    }
                })
    }

    private fun addCommission() {
//        1033.添加佣金(APP->平台)
//        cmd:数据类型
//        phone:当前APP用户的电话号码
//        ddh:当前订单号
        OkGo.post<String>(App.CMD)
                .tag(this)//
                .params("cmd", "1033")
                .params("phone", pref.getString(App.PrefNames.USERNAME, ""))
                .params("ddh", orderNum)
                .execute(object : StringDialogCallback(this) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGo body",response.body().toString())
                        try {
                            val json = JSONObject(response.body()!!.toString())
                            when(json.getInt("cw")){
                                0-> {
                                    PromptDialog(this@OrderConfirmActivity).showSuccess("填写推荐人成功")
                                    finish()
                                }
                                1-> PromptDialog(this@OrderConfirmActivity).showSuccess("添加失败")
                            }
                        } catch (e: JSONException) {
                            LogUtil.e("JSONException",e.toString())
                            ToastUtil.showToastL("数据解析异常")
                            e.printStackTrace()
                        }
                    }
                    override fun onError(response: Response<String>) {
                        AlertDialog.Builder(this@OrderConfirmActivity).setMessage("添加推荐人失败！ 是否重试?")
                                .setPositiveButton("确定") { _, _ ->
                                    submitRecommend(btn_commit)
                                }.setNegativeButton("取消") { _, _ ->
                                }.show()
                        Log.e("OkGoError",response.exception.toString())
                    }
                })
    }

    private fun payFailure() {
        AlertDialog.Builder(this)
                .setMessage("订单支付失败！")
                .setPositiveButton("确定", null)
                .create().show()
        btn_commit.isEnabled = true
    }

    private fun payOrderCancel() {
        AlertDialog.Builder(this)
                .setMessage("订单支付取消！")
                .setPositiveButton("确定", null)
                .create().show()
        btn_commit.isEnabled = true
    }

    private class GoodsAdapter(val context: Context, val list: MutableList<GoodsCarTable>) : BaseAdapter() {
        private val option = ImageOptions.Builder().setUseMemCache(true).setFailureDrawableId(R.mipmap.img_default).setFailureDrawableId(R.mipmap.img_default).build()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val holder: ViewHolder
            val view: View =
                    if (null == convertView) {
                        val view = LayoutInflater.from(context).inflate(R.layout.item_goods_confirm, parent, false)
                        holder = ViewHolder(view)
                        view.tag = holder
                        view
                    } else {
                        holder = convertView.tag as ViewHolder
                        convertView
                    }
            val data = list[position]
            x.image().bind(holder.ivLogo, data.logo, option)
            holder.tvTitle?.text = data.name
            holder.tvPrice?.text = "￥${data.price}"
            holder.tvCount?.text = "×${data.count}"
            return view
        }

        override fun getItem(position: Int): Any = list[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getCount(): Int = list.size

        private class ViewHolder(view: View) {
            @ViewInject(R.id.iv_goods_logo)
            var ivLogo: ImageView? = null
            @ViewInject(R.id.tv_goods_title)
            var tvTitle: TextView? = null
            @ViewInject(R.id.tv_goods_price)
            var tvPrice: TextView? = null
            @ViewInject(R.id.tv_goods_count)
            var tvCount: TextView? = null

            init {
                x.view().inject(this, view)
                AutoUtils.autoSize(view)
            }
        }
    }
}
