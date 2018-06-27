package com.fanhong.cn.service_page.repair

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.fanhong.cn.R
import com.fanhong.cn.moudle.RepairInfoM
import com.fanhong.cn.tools.DateUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_repair_info.*
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.fanhong.cn.tools.ToastUtil
import io.rong.imlib.statistics.UserData.phone

class RepairInfoActivity : AppCompatActivity() {

    lateinit var repairInfo:RepairInfoM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair_info)
        tv_title.text="维修详情"
        if (intent.getSerializableExtra("ri") != null) {
            repairInfo = intent.getSerializableExtra("ri") as RepairInfoM
            initView()
        } else {
            Toast.makeText(this, "数据读取错误", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        when(repairInfo.zt){
//            zt:状态：0 未修  1  正在处理 2 维修完成
            0->{
                tv_handle_date.text=DateUtil.getDateToString(repairInfo.time.toLong(),"MM-dd")
                tv_handle_time.text=DateUtil.getDateToString(repairInfo.time.toLong(),"HH:mm")
            }
            1->{
                arl_handleing.visibility=View.VISIBLE
                tv_handleing_date.text=DateUtil.getDateToString(repairInfo.time1.toLong(),"MM-dd")
                tv_handle_time.text=DateUtil.getDateToString(repairInfo.time1.toLong(),"HH:mm")
                img_handle.setImageResource(R.mipmap.dispose)
                tv_handleing_info.text="已经为您分配维修员，维修员${repairInfo.wxboy}(${repairInfo.wxphone})" +
                        "为您维修"
            }
            2->{
                arl_complete.visibility=View.VISIBLE
                tv_complete_date.text=DateUtil.getDateToString(repairInfo.time2.toLong(),"MM-dd")
                tv_complete_time.text=DateUtil.getDateToString(repairInfo.time2.toLong(),"HH:mm")
                img_handleing.setImageResource(R.mipmap.single)
                arl_handleing.visibility=View.VISIBLE
                tv_handleing_date.text=DateUtil.getDateToString(repairInfo.time1.toLong(),"MM-dd")
                tv_handle_time.text=DateUtil.getDateToString(repairInfo.time1.toLong(),"HH:mm")
                img_handle.setImageResource(R.mipmap.dispose)
                tv_handleing_info.text="已经为您分配维修员，维修员${repairInfo.wxboy}(${repairInfo.wxphone})" +
                        "为您维修"
            }
        }

        tv_device.text=repairInfo.men
        if (repairInfo.imgUrls!=null){
            for (i in repairInfo.imgUrls.indices){
                Log.e("imgUrl",repairInfo.imgUrls[i])
                when(i){
                    0-> Picasso.with(this).load(repairInfo.imgUrls[i]).resize(200,200).
                            centerCrop().into(img1)

                    1-> Picasso.with(this).load(repairInfo.imgUrls[i]).resize(200,200).
                            centerCrop().into(img2)

                    2-> Picasso.with(this).load(repairInfo.imgUrls[i]).resize(200,200).
                            centerCrop().into(img3)
                }
            }
        }
        tv_phone.text=repairInfo.lxphone
        tv_address.text=repairInfo.dizhi
        tv_number.text=repairInfo.time
        tv_date.text=DateUtil.getDateToString(repairInfo.time.toLong(),"yyyy-MM-dd")
        tv_remark.text=repairInfo.concent
    }

    fun onCLicks(v: View) {
        when (v.id) {
            R.id.img_back -> finish()
            R.id.arl_call -> {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:023 6887 2002"))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
    }
}
