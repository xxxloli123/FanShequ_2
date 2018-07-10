package com.fanhong.cn

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.TextView
import com.fanhong.cn.door_page.DoorFragment
import com.fanhong.cn.home_page.ChooseCellActivity
import com.fanhong.cn.home_page.HomeFragment
import com.fanhong.cn.home_page.HomeFragment2
import com.fanhong.cn.login_pages.LoginActivity
import com.fanhong.cn.tools.AppCacheManager
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import com.fanhong.cn.user_page.UserFragment
import kotlinx.android.synthetic.main.activity_home.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : AppCompatActivity() {

    companion object {
        val ACTION_LOGIN: Int = 21
        val ACTION_LOGIN_BY_COMMUNITY: Int = 23
        val ACTION_CHOOSE_BY_COMMUNITY: Int = 25
    }

    private val fragments: MutableList<Fragment> = ArrayList()

    private var apkPath = ""
    private var apkName = ""

    private var lastTab = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        initViews()

        val today = SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
        if (today != getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE).getString(App.PrefNames.UPDATEIGNORE, ""))
            checkUpdate()
    }

    private fun initViews() {
        tv_tab_home.visibility= View.VISIBLE
        onClicks(all_tab_home)
        fragments.add(HomeFragment2())
        fragments.add(DoorFragment())
        fragments.add(UserFragment())
        val pagerAdapter: FragmentPagerAdapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment = fragments[position]
            override fun getCount(): Int = fragments.size
        }
        home_viewpager.adapter = pagerAdapter
        home_viewpager.offscreenPageLimit = 2 //设置向左和向右都缓存 limit 个页面
        home_viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        if (tv_tab_home.visibility == View.GONE) return
                        onClicks(all_tab_home)
                    }
                    1 -> {
                        if (tv_tab_door.visibility == View.GONE) return
                        onClicks(all_tab_door)
                    }
                    2 -> {
                        if (tv_tab_my.visibility == View.GONE) return
                        onClicks(all_tab_my)
                    }
                }
            }
        })
    }

    private fun setTabView(o: Int) {
        val textViews: Array<View> = arrayOf(tv_tab_home, tv_tab_door, tv_tab_my)
        val imgs: Array<View> = arrayOf(img_tab_home, img_tab_door, img_tab_my)
        for (i in 0 until textViews.size) {
            if (i == o) {
                textViews[i].visibility = View.GONE
                imgs[i].setPadding(0, 16, 0, 16)
            } else {
                textViews[i].visibility = View.VISIBLE
                imgs[i].setPadding(0,22,0,6)
            }
        }
        lastTab = o
    }

    fun onClicks(v: View) {
        when (v.id) {
            R.id.all_tab_home -> {
                if (tv_tab_home.visibility == View.GONE) return
                setTabView(0)
                home_viewpager.currentItem = 0
                img_tab_home.setImageResource(R.mipmap.home_page_1)
                img_tab_door.setImageResource(R.mipmap.entrance_guard)
                img_tab_my.setImageResource(R.mipmap.mine)
            }
            R.id.all_tab_door -> {
                if (tv_tab_door.visibility == View.GONE) return
                setTabView(1)
                home_viewpager.currentItem = 1
                img_tab_home.setImageResource(R.mipmap.home_page)
                img_tab_door.setImageResource(R.mipmap.entrance_guard_1)
                img_tab_my.setImageResource(R.mipmap.mine)
            }
            R.id.all_tab_my -> {
                if (tv_tab_my.visibility == View.GONE) return
                setTabView(2)
                home_viewpager.currentItem = 2

                img_tab_home.setImageResource(R.mipmap.home_page)
                img_tab_door.setImageResource(R.mipmap.entrance_guard)
                img_tab_my.setImageResource(R.mipmap.mine_1)
            }
        }
    }

    private fun isLogged() = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE).getString(App.PrefNames.USERID, "-1") != "-1"
    private fun isSelectedCommunity() = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE).getString(App.PrefNames.GARDENID, "-1") != "-1"

    private var time1 = 0L
    private var time2 = 0L
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            time1 = System.currentTimeMillis()
            if (time1 - time2 > 2000) {
                ToastUtil.showToastL("再按一次退出程序")
                time2 = time1
            } else {
                //各种链接的注销写在这里

                finish()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun checkUpdate() {
        val param = RequestParams(App.UPDATE_CHECK)
        param.addParameter("id", 1)
        x.http().post(param, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                val targetCode = JsonSyncUtils.getJsonValue(result, "number").toInt()
                if (targetCode > BuildConfig.VERSION_CODE) {
                    val targetName = JsonSyncUtils.getJsonValue(result, "bbname")
                    val targetIntroduce = JsonSyncUtils.getJsonValue(result, "gxsm")
                    val layout = LinearLayout(this@HomeActivity)
                    layout.orientation = LinearLayout.VERTICAL
                    layout.setPadding(50, 2, 2, 2)
//                    val scrollView = ScrollView(this@HomeActivity)
                    val tv1 = TextView(this@HomeActivity)
                    tv1.text = "更新说明："
                    tv1.textSize = 15.0f
                    tv1.setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.text_3))
                    val tv2 = TextView(this@HomeActivity)
                    tv2.text = targetIntroduce
                    tv2.textSize = 15.0f
                    tv2.setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.skyblue))
                    layout.addView(tv1)
                    layout.addView(tv2)
                    AlertDialog.Builder(this@HomeActivity)
                            .setTitle("发现新版本：v$targetName").setView(layout)
                            .setPositiveButton("立即更新", { _, _ ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    val checkCallPhonePermission = ContextCompat.checkSelfPermission(this@HomeActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                                        apkName = targetName
                                        ActivityCompat.requestPermissions(this@HomeActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                                11)
                                        return@setPositiveButton
                                    }
                                }
                                startUpdating(targetName)
                            })
                            .setNegativeButton("暂不更新", { _, _ ->
                                val editor = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                                val date = SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
                                editor.putString(App.PrefNames.UPDATEIGNORE, date)
                                editor.apply()
                            })
                            .show()
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

    private fun startUpdating(targetName: String) {
        val manager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifycation: Notification = Notification()
        notifycation.icon = R.mipmap.ic_launcher
        notifycation.tickerText = "更新通知"
        notifycation.contentView = RemoteViews(this@HomeActivity.packageName, R.layout.softupdate_progress)

        var p = 0
        val param = RequestParams(App.APP_DOWNLOAD)
        param.saveFilePath = getApkPath(targetName)
        x.http().get(param, object : Callback.ProgressCallback<File> {
            override fun onWaiting() {
            }

            override fun onStarted() {
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onLoading(total: Long, current: Long, isDownloading: Boolean) {
                if (isDownloading) {
                    val progress = AppCacheManager.getFormatSize(current.toDouble())
                    val max = AppCacheManager.getFormatSize(total.toDouble())
                    val percent = (current * 100 / total).toInt()
                    Log.e("TestLog", "$percent%($progress/$max)")
                    if (percent > p) {
                        notifycation.contentView.setTextViewText(R.id.content_view_text1, progress + "/$max")
                        notifycation.contentView.setProgressBar(R.id.content_view_progress, 100, p, false)
                        manager.notify(0, notifycation)
                        p = percent
                    }
                }
            }

            override fun onSuccess(result: File?) {
                notifycation.contentView.setTextViewText(R.id.content_view_text1, "下载完成")
                notifycation.contentView.setProgressBar(R.id.content_view_progress, 100, 100, false)
                manager.notify(0, notifycation)

                val i = Intent(Intent.ACTION_VIEW)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                i.setDataAndType(Uri.fromFile(File(apkPath)), "application/vnd.android.package-archive")
                startActivity(i)
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                Log.e("TestLog", "onError" + ex.toString())
            }

            override fun onFinished() {
                manager.cancel(0)
            }
        })
    }

    private fun getApkPath(code: String): String {
        val basePath = Environment.getExternalStorageDirectory().path
        val file = File(basePath + "/FanShequ")
        if (!file.exists())
            file.mkdir()
        apkPath = "$basePath/FanShequ/FanShequ$code.apk"
        Log.e("TestLog", apkPath)
        return apkPath
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 11)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startUpdating(apkName)
            } else
                ToastUtil.showToastL("需要文件读写权限！")
    }

    /**
     * 用户登录方法
     */
    fun onLogin(v: View) {
        val i = Intent(this, LoginActivity::class.java)
        startActivityForResult(i, ACTION_LOGIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ACTION_LOGIN -> {
//                (fragments[4] as UserFragment).refreshUser()
            }
            ACTION_LOGIN_BY_COMMUNITY -> {
                if (resultCode == 11) {//11表示成功登陆
                    startActivityForResult(Intent(this, ChooseCellActivity::class.java), ACTION_CHOOSE_BY_COMMUNITY)
                }
            }
            ACTION_CHOOSE_BY_COMMUNITY -> {
                if (resultCode == 51) {//51表示选择了小区
                }
            }
        }
    }
}
