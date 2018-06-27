package com.fanhong.cn.user_page.shippingaddress

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.fanhong.cn.R
import com.zhy.autolayout.utils.AutoUtils
import org.xutils.view.annotation.ViewInject
import org.xutils.x

/**
 * Created by Administrator on 2018/1/26.
 */
class AddressAdapter(val context: Context,
                     private val list: MutableList<MyAddressActivity.AddressModel>) : BaseAdapter() {

    var controlable: Boolean = false
    var mConclick: ControlClick? = null

    interface ControlClick {
        fun delAddress(adrid: String,position: Int)
        fun edtAddress(model:MyAddressActivity.AddressModel)
    }
    fun setConclick(conclick:ControlClick){
        mConclick = conclick
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var holder: ViewHolder
        var view: View
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }
        val model = list[position]
        holder.tvName!!.text = model.name
        holder.tvPhone!!.text = model.phone
        holder.tvAddress!!.text = model.address
        when (model.isDefault) {
            0 -> holder.ivDefault!!.visibility = View.GONE
            else -> holder.ivDefault!!.visibility = View.VISIBLE
        }
        when (controlable) {
            true -> {
                holder.tvDelete!!.visibility = View.VISIBLE
                holder.tvEdit!!.visibility = View.VISIBLE
            }
            false -> {
                holder.tvDelete!!.visibility = View.GONE
                holder.tvEdit!!.visibility = View.GONE
            }
        }
        holder.tvDelete!!.setOnClickListener{
            mConclick!!.delAddress(model.adrid!!,position)
        }
        holder.tvEdit!!.setOnClickListener{
            mConclick!!.edtAddress(model)
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

    private class ViewHolder(view: View) {
        @ViewInject(R.id.whose_address)
        var tvName: TextView? = null
        @ViewInject(R.id.phone_of_address)
        var tvPhone: TextView? = null
        @ViewInject(R.id.detail_of_address)
        var tvAddress: TextView? = null
        @ViewInject(R.id.edit_address)
        var tvEdit: TextView? = null
        @ViewInject(R.id.delete_address)
        var tvDelete: TextView? = null
        @ViewInject(R.id.iv_default)
        var ivDefault: ImageView? = null

        init {
            x.view().inject(this, view)
            AutoUtils.autoSize(view)
        }
    }
}