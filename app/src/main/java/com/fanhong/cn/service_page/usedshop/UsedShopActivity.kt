package com.fanhong.cn.service_page.usedshop

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.ToastUtil
import kotlinx.android.synthetic.main.activity_used_shop.*

class UsedShopActivity : AppCompatActivity() {

    var mSharedPref: SharedPreferences? = null
    var f1: SelectgoodsFragment? = null
    var f2: AddgoodsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_used_shop)
        mSharedPref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        initViews()
    }

    private fun initViews() {
        used_back_btn.setOnClickListener {
            finish()
        }
        showFragment1()
        radio_used_shop.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radio_used_first -> {
                    showFragment1()
                }
                R.id.radio_used_second -> {
                    if (isLogin()) {
                        showFragment2()
                    } else {
                        ToastUtil.showToastS("请登录！")
                        radio_used_first.isChecked = true //setCheck(1)
                    }
                }
            }
        }
        if (isLogin()) {
            used_select_mine.visibility = View.VISIBLE
            used_select_mine.setOnClickListener {
                startActivity(Intent(this, MypostgoodsActivity::class.java))
            }
        } else {
            used_select_mine.visibility = View.GONE
        }
    }

    private fun showFragment1() {
        //开启事务，fragment的控制是由事务来实现的
        val transaction = supportFragmentManager.beginTransaction()
        if (f1 == null) {
            f1 = SelectgoodsFragment()
        }
        transaction.replace(R.id.used_body_relative, f1)
        transaction.commit()
    }

    private fun showFragment2() {
        val transaction = supportFragmentManager.beginTransaction()
        if (f2 == null) {
            f2 = AddgoodsFragment()
        }
        transaction.replace(R.id.used_body_relative, f2)
        transaction.commit()
    }

    private fun isLogin(): Boolean {
        return mSharedPref!!.getString(App.PrefNames.USERID, "-1") != "-1"
    }

    //提供方法设置radiobutton
    fun setCheck(i:Int) {
//        when(i){
            radio_used_shop.check(R.id.radio_used_first)
//            1->radio_used_shop.check(R.id.radio_used_second)
//        }
    }
}
