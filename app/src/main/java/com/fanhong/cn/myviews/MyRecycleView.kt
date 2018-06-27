package com.fanhong.cn.myviews

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet

/**
 * Created by Administrator on 2018/2/23.
 */
class MyRecycleView(context:Context,attrs:AttributeSet?):RecyclerView(context, attrs) {
    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        super.onMeasure(widthSpec, expandSpec)
    }
}