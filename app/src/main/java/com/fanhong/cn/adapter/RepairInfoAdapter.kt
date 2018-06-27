package com.fanhong.cn.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fanhong.cn.R
import com.fanhong.cn.moudle.RepairInfoM
import kotlinx.android.synthetic.main.itme_repair_info.view.*

open class RepairInfoAdapter( RIs: ArrayList<RepairInfoM>, private val mCallback: Callback)
    : RecyclerView.Adapter<RepairInfoAdapter.MyHolder>() ,View.OnClickListener{

    private var ris=RIs

    //刷新Adapter
    fun refresh(thisRIs:ArrayList<RepairInfoM>) {
        this.ris = thisRIs//传入list，然后调用notifyDataSetChanged方法
        notifyDataSetChanged()
    }

    /**
     * 自定义接口，用于回调按钮点击事件到Activity
     * @author Ivan Xu
     * 2014-11-26
     */
    interface Callback {
        fun click(v: View)
    }

    override fun onClick(v: View?) {
        mCallback.click(v!!)
    }

    override fun getItemCount(): Int =ris.size

    override fun onBindViewHolder(holder: MyHolder?, position: Int) {
        holder!!.bind(ris[position],this)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.itme_repair_info, parent, false)
        return MyHolder(view)
    }

    class MyHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(ri:RepairInfoM,clickListener:View.OnClickListener){
            view.setOnClickListener(clickListener)
            view.setTag(R.id.repair_info,ri)
            view.tv_number.text=ri.time
            when (ri.zt){
                0 ->{
                    view.iv_status.setImageResource(R.mipmap.line_up)
                }
                1 -> {
                    view.iv_status.setImageResource(R.mipmap.processing)
                }
                2 -> {
                    view.iv_status.setImageResource(R.mipmap.complete2)
                    view.btn_evaluate.text="去评价"
                    view.btn_evaluate.isClickable=true
                    view.btn_evaluate.setOnClickListener(clickListener)
                    view.btn_evaluate.setTag(R.id.repair_info,ri)
                }
            }
            view.tv_device.text=ri.men
            view.tv_phone.text=ri.lxphone
            view.tv_address.text=ri.dizhi
        }
    }

}