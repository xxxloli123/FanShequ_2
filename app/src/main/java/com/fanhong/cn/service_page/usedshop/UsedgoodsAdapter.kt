package com.fanhong.cn.service_page.usedshop

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.fanhong.cn.R
import com.zhy.autolayout.utils.AutoUtils
import org.xutils.image.ImageOptions
import org.xutils.view.annotation.ViewInject
import org.xutils.x

/**
 * Created by Administrator on 2018/2/24.
 */
class UsedgoodsAdapter(val context:Context,val list:MutableList<UsedgoodsModel>):BaseAdapter() {

    private var callSeller:CallSeller?=null
    interface CallSeller{
        fun onCall(phone:String)
    }
    fun setCallSeller(callSeller: CallSeller){
        this.callSeller = callSeller
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var holder:UsedgoodsHoler
        var view:View
        if(convertView==null){
            view = LayoutInflater.from(context).inflate(R.layout.item_used_goods,null)
            holder = UsedgoodsHoler(view)
            view.tag = holder
        }else{
            view = convertView
            holder = view.tag as UsedgoodsHoler
        }
        var model = list[position]
        val options = ImageOptions.Builder().setLoadingDrawableId(R.mipmap.pictureloading)
                .setFailureDrawableId(R.mipmap.picturefailedloading).setUseMemCache(true).build()
        x.image().bind(holder.goodsPicture,model.goodsPicture,options)
        holder.goodsName?.text = model.goodsName
        holder.goodsMessage?.text = model.goodsMessages
        holder.goodsPrice?.text = model.price
        holder.ownerName?.text = model.ownerName
        holder.ownerPhone?.text = model.ownerPhone
        val phone = model.ownerPhone.replace("卖家电话：","")
        holder.callSeller!!.setOnClickListener {
            var builder = AlertDialog.Builder(context)
            builder.setTitle("将要拨打"+phone)
                    .setMessage("是否立即拨打？")
                    .setPositiveButton("确认"){
                        //动态申请权限，在activity中回调
                        dialog,which->callSeller!!.onCall(phone)
                    }
                    .setNegativeButton("取消",null)
                    .show()
        }
        return view
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }
    private class UsedgoodsHoler(view:View) {
        @ViewInject(R.id.goods_picture)
        var goodsPicture: ImageView?=null
        @ViewInject(R.id.goods_name)
        var goodsName: TextView?=null
        @ViewInject(R.id.goods_message)
        var goodsMessage: TextView?=null
        @ViewInject(R.id.goods_price)
        var goodsPrice: TextView?=null
        @ViewInject(R.id.owner_phone)
        var ownerPhone: TextView?=null
        @ViewInject(R.id.owner_name)
        var ownerName: TextView?=null
        @ViewInject(R.id.call_seller)
        var callSeller: TextView?=null
        init {
            x.view().inject(this,view)
            AutoUtils.autoSize(view)
        }
    }
}