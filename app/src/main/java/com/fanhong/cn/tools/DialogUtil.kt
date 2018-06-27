package com.fanhong.cn.tools

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import com.fanhong.cn.home_page.ChooseCellActivity
import com.fanhong.cn.login_pages.LoginActivity

/**
 * Created by Administrator on 2018/1/31.
 */
object DialogUtil {
    fun showDialog(activity: Activity,action: Any, requestCode: Int) {
        val builder= AlertDialog.Builder(activity)
        when (action) {
            "login" -> {
                builder.setTitle("你还未登录")
                        .setMessage("是否立即登录？")
                        .setPositiveButton("确定"){
                            _, _ ->
                            activity.startActivityForResult(Intent(activity,LoginActivity::class.java),requestCode)}
                        .setNegativeButton("取消",null)
                        .show()
            }
            "chooseCell" -> {
                builder.setTitle("你还未选择小区")
                        .setMessage("是否立即去选择小区？")
                        .setPositiveButton("确定"){
                            _, _ ->
                            activity.startActivityForResult(Intent(activity, ChooseCellActivity::class.java), requestCode)}
                        .setNegativeButton("取消",null)
                        .show()
            }
        }
    }
}