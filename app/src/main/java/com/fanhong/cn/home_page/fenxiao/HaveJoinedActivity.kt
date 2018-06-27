package com.fanhong.cn.home_page.fenxiao

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.AdapterView
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.myviews.SpinerPopWindow
import com.fanhong.cn.tools.JsonSyncUtils
import kotlinx.android.synthetic.main.activity_have_joined.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.common.Callback.CancelledException
import org.xutils.http.RequestParams
import org.xutils.x


class HaveJoinedActivity : AppCompatActivity() {

    private var mSharedPref: SharedPreferences? = null
    private var ssp: SpinerPopWindow<String>? = null

    private var years: MutableList<String>? = null
    private var months: MutableList<String>? = null
    private var year = "2018"
    private var month = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_have_joined)
        mSharedPref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        year = mSharedPref!!.getString(App.PrefNames.LASTYEAR, "2018")
        month = mSharedPref!!.getString(App.PrefNames.LASTMONTH, "1")
        init()
        getData(year,month)
    }

    private fun init() {
        img_back.setOnClickListener {
            finish()
        }
        tv_title.text = "招募统计"
        tv_year.text = year + "年"
        tv_month.text = month + "月"

        tv_this_month.text = tv_month.text

        tv_year.setOnClickListener {
            ssp = SpinerPopWindow(this@HaveJoinedActivity, getYears(), "年") { parent, view, position, id ->
                year = years!![position]
                getData(year,month)
                tv_year.text = year + "年"
                ssp!!.dismiss()
            }
            ssp!!.width = tv_year.width
            ssp!!.showAsDropDown(tv_year, 0, 1)
        }
        tv_month.setOnClickListener {
            ssp = SpinerPopWindow(this@HaveJoinedActivity, getMonth(), "月") { parent, view, position, id ->
                month = months!![position]
                getData(year,month)
                tv_month.text = month + "月"
                tv_this_month.text = month+"月"
                ssp!!.dismiss()
            }
            ssp!!.width = tv_month.width
            ssp!!.showAsDropDown(tv_month, 0, 1)
        }
    }

    private fun getYears(): MutableList<String> {
        years = ArrayList()
        for (i in 2017..2030) {
            years!!.add(i.toString())
        }
        return years!!
    }

    private fun getMonth(): MutableList<String> {
        months = ArrayList()
        for (i in 1..12) {
            months!!.add(i.toString())
        }
        return months!!
    }

    private fun getData(year: String, month: String) {
        val params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "137")
        params.addBodyParameter("uid", mSharedPref!!.getString(App.PrefNames.USERID,"-1"))
        params.addBodyParameter("times", year + "-" + month)
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                if (JsonSyncUtils.getJsonValue(result, "cmd") == "138") {
                    val htdoor = JsonSyncUtils.getJsonValue(result, "htdoor")
                    val sjdoor = JsonSyncUtils.getJsonValue(result, "sjdoor")
                    val zgold = JsonSyncUtils.getJsonValue(result, "zgold")
                    runOnUiThread {
//                        hNumber.setText(htdoor)
//                        tNumber.setText(sjdoor)
//                        mAmount.setText(zgold)
                        hetong_number.text = htdoor
                        true_number.text = sjdoor
                        money_amount.text = zgold
                    }
                }
            }

            override fun onError(ex: Throwable, isOnCallback: Boolean) {

            }

            override fun onCancelled(cex: CancelledException) {

            }

            override fun onFinished() {

            }
        })
    }

    override fun onPause() {
        var editor = mSharedPref!!.edit()
        editor.putString(App.PrefNames.LASTYEAR, year).apply()
        editor.putString(App.PrefNames.LASTMONTH, month).apply()
        super.onPause()
    }
}
