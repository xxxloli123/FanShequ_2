package com.fanhong.cn.service_page.express

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.ToastUtil
import kotlinx.android.synthetic.main.activity_express_home.*
import kotlinx.android.synthetic.main.activity_top.*
import kotlinx.android.synthetic.main.agree_sheets.*

class ExpressHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_express_home)
        tv_title.text = "代发快递"
        img_back.setOnClickListener { finish() }
        sheet_protocol.text = "代发快递须知"
        setOnClicks()
    }

    private fun setOnClicks() {
        send_expressage.setOnClickListener {
            if (isLogined()) {
                startActivity(Intent(this, SendExpressActivity::class.java))
            } else ToastUtil.showToastL("请先登录！")
        }
        check_expressage.setOnClickListener { startActivity(Intent(this, CheckExpressActivity::class.java)) }
        expressage_order.setOnClickListener { startActivity(Intent(this, ExpressOrderActivity::class.java)) }
        net_phone.setOnClickListener { startActivity(Intent(this, NetphoneActivity::class.java)) }
        sheet_protocol.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val inflater = LayoutInflater.from(this)
            val view = inflater.inflate(R.layout.layout_dialog_sheet, null)
            builder.setView(view)
            val alertDialog = builder.create()
            alertDialog.show()
            val agreeSheet = view.findViewById<View>(R.id.read_and_agree) as Button
            val textView = view.findViewById<View>(R.id.tv_content) as TextView
            textView.setText(R.string.daifaxuzhi)
            agreeSheet.setOnClickListener {
                alertDialog.dismiss()
                agree_sheet_protocol.isChecked = true
            }
        }
    }

    private fun isLogined(): Boolean = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE).getString(App.PrefNames.USERID, "-1") != "-1"

}