package com.fanhong.cn.home_page.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.fanhong.cn.R
import com.fanhong.cn.home_page.models.Banner
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.itme_home_activities.view.*

open class ActivitiesAdapter(banners: ArrayList<Banner>, private val mCallback: Callback)
    : RecyclerView.Adapter<ActivitiesAdapter.MyHolder>() ,View.OnClickListener{

    private var bas=banners
    lateinit var mContext : Context

    //刷新Adapter
    fun refresh(thisRIs:ArrayList<Banner>) {
        this.bas = thisRIs//传入list，然后调用notifyDataSetChanged方法
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

    override fun getItemCount(): Int =bas.size

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder!!.bind(bas[position],this,mContext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.itme_home_activities, parent, false)
        mContext=parent.context
        return MyHolder(view)
    }

    class MyHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val v=view
        fun bind(ba:Banner,clickListener:View.OnClickListener,context:Context){
            view.setOnClickListener(clickListener)
            view.setTag(R.id.repair_info,ba)
//            Picasso.with(context).load(ba.tupian).into(view.img_show)
            Glide.with(context)
                    .load(ba.tupian)
                    .into(view.img_show)
            view.tv_time.text=ba.sj
            view.tv_content.text=ba.title
        }
    }

}