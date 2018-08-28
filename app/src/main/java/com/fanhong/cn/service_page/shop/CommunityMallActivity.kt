package com.fanhong.cn.service_page.shop

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.http.callback.StringDialogCallback
import com.fanhong.cn.service_page.shop.entity.TabEntity
import com.fanhong.cn.tools.LogUtil
import com.fanhong.cn.tools.ScreenUtil
import com.fanhong.cn.tools.ToastUtil
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import kotlinx.android.synthetic.main.activity_community_mall.*
import me.leefeng.promptlibrary.PromptDialog
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList


class CommunityMallActivity : AppCompatActivity() {
    private val fragments: MutableList<Fragment> = ArrayList()
    private val mTabEntities = ArrayList<CustomTabEntity>()
    private val mTitles = arrayOf("全部", "智能门锁", "酒水")

    private val pagerAdapter: FragmentPagerAdapter = object : FragmentPagerAdapter(supportFragmentManager) {
        override fun getItem(position: Int): Fragment = fragments[position]
        override fun getCount(): Int = fragments.size
        override fun getPageTitle(position: Int): CharSequence {
            if (position >= 0 && position < fragments.size) {
                when (position) {
                    0 -> return "全部"
                    1 -> return "智能门锁"
                    2 -> return "精品酒水"
                }
            }
            return ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_mall)
        all_fillStatusBar.setPadding(0,ScreenUtil.getStatusBar(this),0,0)
        fragments.add(GoodsListFragment().setType(2333))//
        //1：米，2：油，3：面 4:酒 5：锁
        fragments.add(GoodsListFragment().setType(5))//
        fragments.add(GoodsListFragment().setType(4))//
        shop_viewpager.adapter = pagerAdapter
        shop_viewpager.offscreenPageLimit = 2 //设置向左和向右都缓存 limit 个页面
//        st_title2.setViewPager(shop_viewpager)
        for (i in mTitles.indices) {
            mTabEntities.add(TabEntity(mTitles[i]))
        }
        ctl_title.setTabData(mTabEntities)
        ctl_title.setOnTabSelectListener(object : OnTabSelectListener {
            //                       在选项卡重新选择
            override fun onTabReselect(position: Int) {}

            override fun onTabSelect(position: Int) {
                shop_viewpager.currentItem = position
            }
        })
        shop_viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                ctl_title.currentTab = position
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        shop_viewpager.currentItem = 0
    }

    fun onClicks(v: View){
        if (v==img_back){
            finish()
        }else{
            if (!isCarEmpty()) {
                val i = Intent(this, ShopCarActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(i)
            }
        }

    }
    private fun getUid(): String = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE).getString(App.PrefNames.USERID, "-1")
    private fun isCarEmpty() = null == App.db.selector(GoodsCarTable::class.java).where("c_uid", "=", getUid()).findFirst()

    private fun loadData() {
        OkGo.post<String>(App.CMD)
                .tag(this)//
//        1059 社区卖场(APP->平台)
//        cmd:数据类型
                .params("cmd", "1059")
                .execute(object : StringDialogCallback(this) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGo CommunityMall",response.body().toString())
                        try {
                            val json = JSONObject(response.body()!!.toString())
                            if (json.getString("cw") == "0"){
                                val arr = json.getJSONArray("data")
                                if (arr.length() == 0) return
                                for (i in 0 until arr.length()) {

                                }
                            }else PromptDialog(this@CommunityMallActivity).showError("获取失败")
                        } catch (e: JSONException) {
                            LogUtil.e("JSONException",e.toString())
                            ToastUtil.showToastL("数据解析异常")
                            e.printStackTrace()
                        }
                    }
                    override fun onError(response: Response<String>) {
                        AlertDialog.Builder(this@CommunityMallActivity).setMessage("获取失败！ 是否重试?")
                                .setPositiveButton("确定") { _, _ ->
                                }.setNegativeButton("取消",null) .show()
                        Log.e("OkGoError",response.exception.toString())
                    }
                })
    }

}
