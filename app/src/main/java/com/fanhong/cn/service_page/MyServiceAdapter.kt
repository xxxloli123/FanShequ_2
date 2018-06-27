package com.fanhong.cn.service_page

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.fanhong.cn.R
import com.zhy.autolayout.AutoLinearLayout
import com.zhy.autolayout.utils.AutoUtils
import org.xutils.view.annotation.ViewInject
import org.xutils.x

/**
 * Created by Administrator on 2018/2/23.
 */
class MyServiceAdapter(val context:Context, private val images:IntArray, val texts:IntArray
                       ):RecyclerView.Adapter<MyServiceAdapter.MyViewHolder>() {

    private var itemClick:ItemClick?=null
    interface ItemClick{
        fun itemclick(position:Int)
    }
    fun setItemClick(itemClick: ItemClick){
        this.itemClick = itemClick
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.item_of_service,parent,false)
        view.setPadding(5,5,5,5)
        var holder = MyViewHolder(view)
        return holder
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: MyViewHolder?, position: Int) {
        holder!!.imageView!!.setImageResource(images[position])
        holder!!.textView!!.text = context.getString(texts[position])
        holder!!.imageView!!.setOnClickListener {
            itemClick!!.itemclick(position)
        }
    }


    class MyViewHolder(view:View):RecyclerView.ViewHolder(view){
        @ViewInject(R.id.ser_item_img)
        var imageView:ImageView?=null
        @ViewInject(R.id.ser_item_text)
        var textView:TextView?=null
        init {
            x.view().inject(this,view)
            AutoUtils.autoSize(view)
        }
    }
}