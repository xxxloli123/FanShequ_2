package com.fanhong.cn.service_page.repair

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.fanhong.cn.R
import com.fanhong.cn.moudle.RepairInfoM
import kotlinx.android.synthetic.main.activity_repair_evaluate.*

class RepairEvaluateActivity : AppCompatActivity() {
    lateinit var repairInfo:RepairInfoM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair_evaluate)
        tv_title.text="维修详情"
        if (intent.getSerializableExtra("ri") != null) {
            repairInfo = intent.getSerializableExtra("ri") as RepairInfoM
            initView()
        } else {
            Toast.makeText(this, "数据读取错误", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initView() {

    }

    fun onCLicks(v: View) {
        when (v.id) {
            R.id.img_back -> finish()

        }
    }
}
