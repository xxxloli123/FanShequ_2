package com.fanhong.cn.user_page


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.fanhong.cn.App
import com.fanhong.cn.HomeActivity
import com.fanhong.cn.R
import com.fanhong.cn.login_pages.LoginActivity
import com.fanhong.cn.service_page.repair.RepairInfoListActivity
import com.fanhong.cn.tools.ToastUtil
import kotlinx.android.synthetic.main.fragment_user.*
import org.xutils.common.Callback
import org.xutils.image.ImageOptions
import org.xutils.x


/**
 * A simple [Fragment] subclass.
 */
class UserFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        addClickListeners()
        all_fillStatusBar.setPadding(0,getStatusBar(),0,0)
        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * 获取状态栏高度
     * @return
     */
    fun  getStatusBar(): Int {
        /**
         * 获取状态栏高度
         */
        var statusBarHeight1 = -1
        //获取status_bar_height资源的ID
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight1
    }

    override fun onResume() {
        super.onResume()
        refreshUser()
    }

    private val listener = View.OnClickListener { v ->
        when (v.id) {
            R.id.all_account -> {//账号设置
                if (isLogged()) {
                    val intent = Intent(activity!!, AccountSetsActivity::class.java)
                    startActivity(intent)
                } else{
                    val i = Intent(activity!!, LoginActivity::class.java)
                    startActivityForResult(i, HomeActivity.ACTION_LOGIN)
                }
            }
            R.id.all_repair_info -> {//
                if (isLogged()) {
                    val intent = Intent(activity!!, RepairInfoListActivity::class.java)
                    startActivity(intent)
                } else ToastUtil.showToastL("请登录！")
            }
            R.id.all_integral -> {//我的积分
                if (isLogged()) {
                    val intent = Intent(activity!!, ScoreActivity::class.java)
                    startActivity(intent)
                } else ToastUtil.showToastL("请登录！")
            }
            R.id.news_notice -> {//消息通知
                val intent = Intent(activity!!, MessagesActivity::class.java)
                startActivity(intent)
            }
            R.id.my_order -> {//我的订单
                if (isLogged()) {
                    val intent = Intent(activity!!, OrderListActivity::class.java)
//                    val intent = Intent(activity!!, EvaluateActivity::class.java)
                    startActivity(intent)
                } else ToastUtil.showToastL("请登录！")
            }
            R.id.customer_hotline -> {//客服热线
                val phoneNumber = tv_hotline.text.toString().trim()
                //判断Android版本是否大于23
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val checkCallPhonePermission = ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CALL_PHONE)

                    if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.CALL_PHONE),
                                11)
                        return@OnClickListener
                    }
                }
                callDialog(phoneNumber)
            }
            R.id.img_setting -> {//通用设置
                startActivity(Intent(activity!!, BasicSettingsActivity::class.java))
            }
            R.id.about_us -> {//关于我们
                startActivity(Intent(activity!!, AboutActivity::class.java))
            }
            R.id.all_logout-> {//
                onLogout()
            }
        }
    }

    private fun isLogged(): Boolean {
        val pref = activity!!.getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        return pref.getString(App.PrefNames.USERID, "-1") != "-1"
    }

    private fun callDialog(phoneNumber: String) {
        AlertDialog.Builder(activity!!).setTitle("拨打电话")
                .setMessage(phoneNumber + "\n是否立即拨打？")
                .setPositiveButton("确认", DialogInterface.OnClickListener { _, _ ->
                    val intent = Intent(Intent.ACTION_CALL)
                    val data = Uri.parse("tel:" + phoneNumber)
                    intent.data = data
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                })
                .setNegativeButton("取消", null)
                .create().show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 11) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val phoneNumber = tv_hotline.text.toString().trim()
                callDialog(phoneNumber)
            } else
                ToastUtil.showToastL("需要通话权限！")
        }
    }

    private fun addClickListeners() {
        all_account.setOnClickListener(listener)
        all_repair_info.setOnClickListener(listener)
        news_notice.setOnClickListener(listener)
        my_order.setOnClickListener(listener)
        customer_hotline.setOnClickListener(listener)
        img_setting.setOnClickListener(listener)
        about_us.setOnClickListener(listener)
        all_integral.setOnClickListener(listener)
        all_logout.setOnClickListener(listener)
    }

    private fun refreshUser() {
        val pref = activity!!.getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val userId = pref.getString(App.PrefNames.USERID, "-1")
        if (userId != "-1") {
            mine_photo.isEnabled = false
            user_name.isEnabled = false
            val headImg = pref.getString(App.PrefNames.HEADIMG, "")
            Glide.with(this)
                    .load(headImg)
                    .into(mine_photo)
        } else {
            mine_photo.isEnabled = true
            user_name.isEnabled = true
        }

        var nickName = pref.getString(App.PrefNames.NICKNAME, "")

        if (null == nickName || nickName == "")
            nickName = pref.getString(App.PrefNames.USERNAME, getString(R.string.keylogin))
        else tv_phone.text=pref.getString(App.PrefNames.USERNAME, getString(R.string.keylogin))
        user_name.text = nickName
    }

    private fun onLogout() {
        val pref = activity!!.getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val id = pref.getString(App.PrefNames.USERID,"-1")
        if (id == "-1")
            ToastUtil.showToastL("当前未处于登录状态！")
        else AlertDialog.Builder(activity!!).setMessage("是否确定退出此账号？")
                .setPositiveButton("确定") { _, _ -> doLogout() }
                .setNegativeButton("取消", null).show()
    }

    @SuppressLint("ApplySharedPref")
    private fun doLogout() {
        tv_phone.text=""
        val editor = activity!!.getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
        editor.putString(App.PrefNames.USERID, "-1")
        editor.putString(App.PrefNames.USERNAME, null)
        editor.putString(App.PrefNames.PASSOWRD, null)
        editor.putString(App.PrefNames.GARDENID, "-1")
        editor.putString(App.PrefNames.GARDENNAME, null)
        editor.putString(App.PrefNames.TOKEN, null)
        editor.putString(App.PrefNames.NICKNAME, null)
        editor.putString(App.PrefNames.HEADIMG, null)
        editor.commit()
        refreshUser()
    }

}
