package com.fanhong.cn.service_page.usedshop

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.fanhong.cn.R
import com.zhy.autolayout.utils.AutoUtils
import org.xutils.image.ImageOptions
import org.xutils.view.annotation.ViewInject
import org.xutils.x

/**
 * Created by Administrator on 2018/2/26.
 */
class MygoodsAdapter(val context: Context, val list: MutableList<UsedgoodsModel>) : BaseAdapter() {

    private var delete:Delete?=null
    interface Delete{
        fun remove(id:String,position: Int)
    }
    fun setDelete(delete: Delete){
        this.delete = delete
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var holer:MygoodsHoler
        var view:View
        if(convertView==null){
            view = LayoutInflater.from(context).inflate(R.layout.item_myused_goods,null)
            holer = MygoodsHoler(view)
            view.tag = holer
        }else{
            view = convertView
            holer = view.tag as MygoodsHoler
        }
        var model = list[position]
        var option = ImageOptions.Builder().setLoadingDrawableId(R.mipmap.pictureloading)
                .setFailureDrawableId(R.mipmap.picturefailedloading).setUseMemCache(true).build()
        x.image().bind(holer.goodsPicture,model.goodsPicture,option)
        holer.goodsName!!.text = model.goodsName
        holer.goodsPrice!!.text = model.price
        holer.goodsMessage!!.text = model.goodsMessages
        holer.ownerName!!.text = model.ownerName
        holer.ownerPhone!!.text = model.ownerPhone

        var id = model.id
        holer.deleteBtn!!.setOnClickListener{
            delete!!.remove(id,position)
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

    private class MygoodsHoler(view: View) {
        @ViewInject(R.id.my_picture)
        var goodsPicture: ImageView? = null
        @ViewInject(R.id.my_goods_name)
        var goodsName: TextView? = null
        @ViewInject(R.id.my_message)
        var goodsMessage: TextView? = null
        @ViewInject(R.id.my_price)
        var goodsPrice: TextView? = null
        @ViewInject(R.id.my_phone)
        var ownerPhone: TextView? = null
        @ViewInject(R.id.my_name)
        var ownerName: TextView? = null
        @ViewInject(R.id.delete_mygoods)
        var deleteBtn: Button? = null

        init {
            x.view().inject(this, view)
            AutoUtils.autoSize(view)
        }
    }
}