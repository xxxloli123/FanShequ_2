package com.fanhong.cn

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.util.Log
import com.simple.spiderman.SpiderMan
import com.vondear.rxtool.RxTool
import com.zhy.autolayout.config.AutoLayoutConifg
import com.lzy.okgo.OkGo
import io.rong.imlib.RongIMClient
import org.xutils.DbManager
import org.xutils.x
import java.io.File
import java.util.*
import com.zhy.http.okhttp.OkHttpUtils
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


/**
 * Created by Administrator on 2017/12/26.
 */

class App : Application() {
    companion object {
        val PREFERENCES_NAME = "mSettings"  //全局缓存统一名称
        val DB_NAME = "mSettings"  //全局数据库统一名称
        val WEB_SITE = "http://m.wuyebest.com"
        val CMD = "http://m.wuyebest.com/index.php/App/index"//数据接口统一访问路径
        val IMG = "http://m.wuyebest.com/index.php/App/index/upwygxwx"//
        val IMG2 = "http://m.wuyebest.com/index.php/App/index/iosnewgxwx"//
        val UPDATE_CHECK = "http://m.wuyebest.com/index.php/App/index/appnumber"//更新检查访问路径
        val APP_DOWNLOAD = "http://m.wuyebest.com/public/apk/FanShequ.apk"//app下载路径
        val HEAD_UPLOAD = "http://m.wuyebest.com/index.php/App/index/newupapp"//头像上传路径

        //开门禁所需访问的路径
        val OPEN_URL = "http://m.wuyebest.com/index.php/App/index/yjkm"
        //开门禁结果查询路径
        val CHECK_URL = "http://m.wuyebest.com/index.php/App/index/yjkmcx"

        var lastCodeMsgTime = 0L

        var old_msg_times: MutableSet<Long> = HashSet()

val  sqpath = /*Environment.getExternalStorageDirectory().path+*/"/data/data/com.fanhong.cn/database"
//        Log.e("TestLog",sqpath)
        var daoConfig: DbManager.DaoConfig = DbManager.DaoConfig()
                .setDbName(DB_NAME)
                .setDbDir(File(sqpath))
                .setDbVersion(1)
                .setDbOpenListener { db ->
                    // 开启WAL, 对写入加速提升巨大
                    db.database.enableWriteAheadLogging()
                }
                .setDbUpgradeListener { _, _, _ -> }
                .setTableCreateListener { _, table -> Log.i("JAVA", "onTableCreated：" + table.name) }
                .setAllowTransaction(true)
        var db = x.getDb(daoConfig)
    }

    private val TAG = this.javaClass.toString()

    /**
     *全局缓存数据键集
     */
    object PrefNames {
        val UPDATEIGNORE = "checkDate"//忽略更新日期 String
        val FIRST_START = "first_start"  //首次登录检测 Boolean
        val USERNAME = "Name"  //用户名 String
        val PASSOWRD = "Password"  //密码（加密前）String
        val NICKNAME = "Nick"//用户昵称 String
        val USERID = "userId"  //用户Id String
        val HEADIMG = "Logo"//用户头像地址 String
        val TOKEN = "token"//融云Token String
        val ISNOTIFY = "notifyMsg"//是否开启通知 Boolean
        val GARDENNAME = "gardenName" //小区名字 String
        val GARDENID = "gardenId" //小区id  String

        val LASTYEAR = "fx_last_year" //招商代理查询缓存最后一个年份
        val LASTMONTH = "fx_last_month" //                      月份
        val SHAREDRAFT = "gov_share_draft" //党功能分享草稿
    }

    /**
     * 支付相关参数
     */
    object PayConfig {
        /**
         * AliPay支付宝
         */
        //APPID
        val alipay_APPID = "2017082508372012"
        //商户收款账号
        val alipay_SELLER = "18725732573@139.com"
        // 支付宝公钥
        val alipay_RSA_PUBLIC = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApdHlve2U3JjPDeXv+30PkA5pCkwtdPOodAHF6qCXwcyeS7/BVtx9GVbZhdI+inOY2oJI4ll3METeGmeGw99962V7YkAJu7+r9SVpDdoXz1jo8zATq/vVi7mCRSxhsfPmJ3YZfZUSWOf/ECfrkh6t+LROvBIa8VHhyaoLp5/zbCyFhFdfyk4/EWee+McNxtnehVlMknvjm6rCQ1A2Eyy+NyryA/nShclJL6wr5l/N4tSaH5dsSBHexoGXswE+5JQ6J+GQ/hNpiU4bUWLVDRd5OsnqYsS4xSqmVVwGG4Ts3xO1/skNORAFQ7DYMti1U8uxu1z5tPUljqbpYCWB3N8BOQIDAQAB"
        // 支付宝私钥
        var alipay_RSA_PRIVATE = ""
        //后台回调通知地址(商城)
        val alipay_SERVICE_CALLBACK = "http://m.wuyebest.com/library/zhifubao/notify_url.php"
        //后台回调通知地址(车审)
        val alipay_SERVICE_CALLBACK1 = "http://m.wuyebest.com/library/zhifubao/notify.php"

        /**
         * 微信
         */

        // 开放平台登录https://open.weixin.qq.com的开发者中心获取APPID
//                    #define MXWechatAPPID       @"wxea49e10e35c4b1ea"
////                    开放平台登录https://open.weixin.qq.com的开发者中心获取 AppSecret。
//                    #define MXWechatAPPSecret   @"80891e400e0c3619df730ebef548a9e3"
//// 微信支付商户号
//                    #define MXWechatMCHID       @"1488497082"
//// 安全校验码（MD5）密钥，商户平台登录账户和密码登录http://pay.weixin.qq.com
//// 平台设置的“API密钥”，为了安全，请设置为以数字和字母组成的32字符串。
//                    #define MXWechatPartnerKey  @"qiangxu15123073170QIANGXU1234567"
        //appid 微信分配的app应用ID
        val WX_APPID = "wxea49e10e35c4b1ea"
        //商户号——微信分配的公众账号ID
        val WX_MCH_ID = "1488497082"
//        WX_PRIVATE_KEY
        val WX_PRIVATE_KEY = "qiangxu15123073170QIANGXU1234567"
        //支付回调广播
        val WX_ACTION_RESULT = "com.fanhong.cn.wxapi.PAY_RESULT"
        //服务器回调接口
        val WX_notifyUrl = ""// 用于微信支付成功的回调（后台已设置）
        //商城下单签名接口
        val WX_getOrderUrl = "http://m.wuyebest.com/public/newWeiPay/index.php"
        //车审下单签名接口
        val WX_getOrderUrl1 = "http://m.wuyebest.com/public/WeiPay/index.php"
    }

    override fun onCreate() {
        super.onCreate()
        x.Ext.init(this)

        OkGo.getInstance().init(this);
        val okHttpClient = OkHttpClient.Builder()
                //                .addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build()

        OkHttpUtils.initClient(okHttpClient)
        initSpiderMan()
        RxTool.init(this)
        //配置 默认使用的高度是设备的可用高度，也就是不包括状态栏和底部的操作栏的，
        // 如果你希望拿设备的物理高度进行百分比化：
//        可以在Application的onCreate方法中进行设置:
        AutoLayoutConifg.getInstance().useDeviceSize()
        /**
         * OnCreate 会被多个进程重入，这段保护代码，确保只有您需要使用 RongIMClient 的进程和 Push 进程执行了 init。
         * io.rong.push 为融云 push 进程名称，不可修改。
         */
        if (applicationInfo.packageName == getCurProcessName(applicationContext) || "io.rong.push" == getCurProcessName(applicationContext)) {
            RongIMClient.init(this)
        }
    }

    private fun initSpiderMan() {
        SpiderMan.init(this).setOnCrashListener { t, ex, model ->
                    Log.e(TAG, "李大娘==" + model.toString()) }
    }

    private fun getCurProcessName(context: Context): String? {

        val pid = android.os.Process.myPid()

        val activityManager = context
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for (appProcess in activityManager
                .runningAppProcesses) {

            if (appProcess.pid == pid) {
                return appProcess.processName
            }
        }
        return null
    }
}
