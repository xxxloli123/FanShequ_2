package com.fanhong.cn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import cn.jpush.android.api.JPushInterface
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by Administrator on 2018/1/26.
 */
class MyJPushReciver : BroadcastReceiver() {
    private val TAG = "testLog"
    private var context: Context? = null
    override fun onReceive(context: Context, intent: Intent) {
        try {
            this.context = context
            val bundle = intent.extras
            Log.e(TAG, "[MyReceiver] onReceive - " + intent.action + ", extras: " + printBundle(bundle))

            when (intent.action) {
                JPushInterface.ACTION_REGISTRATION_ID -> {
                    val regId = bundle!!.getString(JPushInterface.EXTRA_REGISTRATION_ID)
                    Log.e(TAG, "[MyReceiver] 接收Registration Id : " + regId!!)
                }
                JPushInterface.ACTION_MESSAGE_RECEIVED -> {
                    Log.e(TAG, "[MyReceiver] 接收到推送下来的自定义消息: " + bundle!!.getString(JPushInterface.EXTRA_MESSAGE)!!)
                }
                JPushInterface.ACTION_NOTIFICATION_RECEIVED -> {
                    Log.e(TAG, "[MyReceiver] 接收到推送下来的通知")
                    val notifactionId = bundle!!.getInt(JPushInterface.EXTRA_NOTIFICATION_ID)
                    Log.e(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId)
                }
                JPushInterface.ACTION_NOTIFICATION_OPENED -> {
                    Log.e(TAG, "[MyReceiver] 用户点击打开了通知")
                }
                JPushInterface.ACTION_RICHPUSH_CALLBACK -> {
                    Log.e(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle!!.getString(JPushInterface.EXTRA_EXTRA)!!)
                    //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
                }
                JPushInterface.ACTION_CONNECTION_CHANGE -> {
                    val connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false)
                    Log.e(TAG, "[MyReceiver]" + intent.action + " connected state change to " + connected)
                }
                else -> {
                    Log.e(TAG, "[MyReceiver] Unhandled intent - " + intent.action!!)
                }
            }
        } catch (e: Exception) {

        }

    }

    // 打印所有的 intent extra 数据
    private fun printBundle(bundle: Bundle): String {
        val sb = StringBuilder()
        for (key in bundle.keySet()) {
            if (key == JPushInterface.EXTRA_NOTIFICATION_ID) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key))
            } else if (key == JPushInterface.EXTRA_CONNECTION_CHANGE) {
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key))
            } else if (key == JPushInterface.EXTRA_EXTRA) {
                if (TextUtils.isEmpty(bundle.getString(JPushInterface.EXTRA_EXTRA))) {
                    Log.e(TAG, "This message has no Extra data")
                    continue
                }

                try {
                    val json = JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA))
                    val it = json.keys()

                    while (it.hasNext()) {
                        val myKey = it.next().toString()
                        sb.append("\nkey:" + key + ", value: [" +
                                myKey + " - " + json.optString(myKey) + "]")
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Get message extra JSON error!")
                }

            } else {
                sb.append("\nkey:" + key + ", value:" + bundle.getString(key))
            }
        }
        return sb.toString()
    }
}