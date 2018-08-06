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
import android.support.animation.DynamicAnimation
import android.support.animation.SpringAnimation
import android.support.animation.SpringForce
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
import com.allenliu.versionchecklib.callback.APKDownloadListener
import com.allenliu.versionchecklib.v2.AllenVersionChecker
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder
import com.allenliu.versionchecklib.v2.builder.UIData
import com.fanhong.cn.door_page.DoorFragment
import com.fanhong.cn.home_page.ChooseCellActivity
import com.fanhong.cn.home_page.HomeFragment2
import com.fanhong.cn.login_pages.LoginActivity
import com.fanhong.cn.tools.AppCacheManager
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import com.fanhong.cn.user_page.UserFragment
import com.vondear.rxtool.view.RxToast
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

    private var lastTab = 0
    private var myBuilder: DownloadBuilder? = null

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
        setSpringAnimation(v)
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
                if (!isLogged())fragments[1].onResume()
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

    private fun setSpringAnimation(v: View?) {
        //Spring Force (弹簧 力)
//        val s=SpringForce(0F)
        //              ( ,参数是动画类型,float类型的被作用对象最终位置)
//        SpringAnimation(v,DynamicAnimation)
        val animX = SpringAnimation(v, SpringAnimation.SCALE_X, 0.5f)
        val animY = SpringAnimation(v, SpringAnimation.SCALE_Y, 0.5f)

        animX.spring.finalPosition = 0.5f

        animY.spring.finalPosition = 0.5f
        animX.start()
        animY.start()

        animX.addEndListener { animation, canceled, value, velocity ->
            if (animX.spring.finalPosition==0.5f){
                animX.spring.stiffness = SpringForce.STIFFNESS_LOW
                animX.spring.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                animX.spring.finalPosition = 1f
                animX.start()
            }
        }
        animY.addEndListener { animation, canceled, value, velocity ->
            if (animY.spring.finalPosition==0.5f){
                animY.spring.stiffness = SpringForce.STIFFNESS_LOW
                animY.spring.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                animY.spring.finalPosition = 1f
                animY.start()
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

                    myBuilder = AllenVersionChecker
                            .getInstance()
                            .downloadOnly(crateUIData("发现新版本：v$targetName",targetIntroduce))
                    myBuilder!!.apkDownloadListener = object :APKDownloadListener{
                        override fun onDownloading(progress: Int) {
                        }

                        override fun onDownloadFail() {
                        }

                        override fun onDownloadSuccess(file: File?) {
                        }
                    }
                    myBuilder!!.excuteMission(this@HomeActivity)
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

    private fun crateUIData(title: String, content: String): UIData {
        val uiData = UIData.create()
        uiData.title = title
        uiData.downloadUrl = "http://m.wuyebest.com/public/apk/FanShequ.apk"
        uiData.content = content
        return uiData
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
