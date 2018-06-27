package com.fanhong.cn.service_page.government

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import com.fanhong.cn.R
import kotlinx.android.synthetic.main.activity_government_main.*
import kotlinx.android.synthetic.main.activity_top.*

class GovernMainActivity : AppCompatActivity() {

    var fragments: MutableList<Fragment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_government_main)
        tv_title.text = "分享"
        img_back.setOnClickListener { finish() }
        initFragments()
        var adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                return fragments!![position]
            }

            override fun getCount(): Int {
                return fragments!!.size
            }
        }
        party_viewpager.adapter = adapter
        party_viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when(position){
                    0->{
                        radioChecked(0)
                    }
                    1->{
                        radioChecked(1)
                    }
                    2->{
                        radioChecked(2)
                    }
                }
            }

        })
        bottom_group.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.radiogroup_fx->radioChecked(0)
                R.id.radiogroup_lt->radioChecked(1)
                R.id.radiogroup_wd->radioChecked(2)
            }
        }
    }

    private fun initFragments() {
        fragments = ArrayList()
        fragments!!.add(SharedFragment())
        fragments!!.add(ForumFragment())
        fragments!!.add(MineFragment())
    }

    fun radioChecked(position: Int) {
        bottom_fx.visibility = View.INVISIBLE
        bottom_lt.visibility = View.INVISIBLE
        bottom_wd.visibility = View.INVISIBLE
        when (position) {
            0 -> {
                tv_title.text = "分享"
                bottom_group.check(R.id.radiogroup_fx)
                bottom_fx.visibility = View.VISIBLE
                party_viewpager.currentItem = 0
            }
            1 -> {
                tv_title.text = "论坛"
                bottom_group.check(R.id.radiogroup_lt)
                bottom_lt.visibility = View.VISIBLE
                party_viewpager.currentItem = 1
            }
            2 -> {
                tv_title.text = "我的"
                bottom_group.check(R.id.radiogroup_wd)
                bottom_wd.visibility = View.VISIBLE
                party_viewpager.currentItem = 2
            }
        }
    }
}
