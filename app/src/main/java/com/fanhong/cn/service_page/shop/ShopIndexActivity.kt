package com.fanhong.cn.service_page.shop

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.login_pages.LoginActivity
import kotlinx.android.synthetic.main.activity_shop_index.*
import java.util.*

class ShopIndexActivity : AppCompatActivity() {
    private val icons = intArrayOf(/*R.mipmap.icon_rice, R.mipmap.icon_oil, R.mipmap.icon_nol,*/R.mipmap.winner ,R.mipmap.suo)
    private val tabNames = intArrayOf(/*R.string.dami, R.string.you, R.string.mian,*/ R.string.jiu,R.string.suo)
    private var itemWidth = 0
    private val fragments: MutableList<Fragment> = ArrayList()
    private val pagerAdapter: FragmentPagerAdapter = object : FragmentPagerAdapter(supportFragmentManager) {
        override fun getItem(position: Int): Fragment = fragments[position]
        override fun getCount(): Int = fragments.size
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_index)

        initViews()

    }

    private fun initViews() {
        img_back.setOnClickListener { finish() }
        btn_shopCar.setOnClickListener {
            if ("-1" == getUid()) {
                AlertDialog.Builder(this).setMessage("请先登录！").setPositiveButton("立即登录", { _, _ ->
                    startActivity(Intent(this@ShopIndexActivity, LoginActivity::class.java))
                }).setNegativeButton("取消", null).show()
                return@setOnClickListener
            }
            if (!isCarEmpty()) {
                val i = Intent(this, ShopCarActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(i)
            }
//            App.db.dropTable(GoodsCarTable::class.java)
        }
        tv_car_count.setOnClickListener { btn_shopCar.callOnClick() }

        shop_viewpager.adapter = pagerAdapter
        shop_viewpager.currentItem = 4
        shop_viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (positionOffset != 0f && positionOffsetPixels != 0) {
                    val lp: RelativeLayout.LayoutParams = header_line.layoutParams as RelativeLayout.LayoutParams
                    lp.leftMargin = positionOffsetPixels / icons.size + itemWidth * position
                    header_line.layoutParams = lp
                }
            }

            override fun onPageSelected(position: Int) {
            }
        })

        addPages()
    }

    override fun onResume() {
        super.onResume()
        checkCar()
    }

    private fun getUid(): String = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE).getString(App.PrefNames.USERID, "-1")
    private fun isCarEmpty() = null == App.db.selector(GoodsCarTable::class.java).where("c_uid", "=", getUid()).findFirst()
    private fun checkCar() {
        val existGoods = App.db.selector(GoodsCarTable::class.java).where("c_uid", "=", getUid()).findAll()
        val count = existGoods?.size ?: 0
        tv_car_count.text = count.toString()
        if (count != 0)
            tv_car_count.visibility = View.VISIBLE
        else tv_car_count.visibility = View.INVISIBLE
    }

    private fun addPages() {
        for (i in 0 until icons.size) {
            val tabItem: View = LayoutInflater.from(this).inflate(R.layout.shop_tab_header_item, null)
            val icon = tabItem.findViewById<ImageView>(R.id.ItemImage)
            val tabName = tabItem.findViewById<TextView>(R.id.ItemTitle)
            icon.setImageResource(icons[i])
            tabName.setText(tabNames[i])
            if (i == 0) {
                var width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                val height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                tabItem.measure(width, height)
                width = tabItem.measuredWidth

                val wm = this.windowManager
                val totalWidth = wm.defaultDisplay.width

                itemWidth = totalWidth / if (icons.size < 4) icons.size else 4
            }
            tabItem.minimumWidth = itemWidth
            tabItem.setOnClickListener { oper(i) }
            layout_headers.addView(tabItem)
            //1：米，2：油，3：面 4:酒 5：锁
            fragments.add(GoodsListFragment().setType(i + 1 /**/+ 3 /**/))//
        }
        pagerAdapter.notifyDataSetChanged()
        val lineP = header_line.layoutParams as RelativeLayout.LayoutParams
        lineP.width = itemWidth
        header_line.layoutParams = lineP
//        Log.e("TestLog", "itemWidth=$itemWidth,lineWidth=${header_line.layoutParams.width}")
    }

    private fun oper(i: Int) {
        shop_viewpager.currentItem = i
    }
}
