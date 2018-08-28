package com.fanhong.cn.service_page.shop.adapters

import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.fanhong.cn.R
import com.fanhong.cn.service_page.shop.GoodsListFragment
import com.vondear.rxtool.view.RxToast

import java.util.ArrayList

class GoodsListAdapter(layoutResId: Int, goods: List<GoodsListFragment.GoodsInfo>) : BaseQuickAdapter<GoodsListFragment.GoodsInfo, BaseViewHolder>(layoutResId, goods) {

    override fun convert(helper: BaseViewHolder, m: GoodsListFragment.GoodsInfo) {
        helper.setText(R.id.tv_name, m.name)
        Glide.with(mContext).load(m.pic).into(helper.getView<View>(R.id.img_goods) as ImageView)
        (helper.getView<View>(R.id.tv_price) as TextView).text = Html.fromHtml("<font color='#ff4d4d'>￥" + m.price + "</font>")

    }

}
