package com.fanhong.cn.view

import android.content.Context
import android.support.v7.widget.AppCompatRadioButton
import android.util.AttributeSet

class MySelectButton : AppCompatRadioButton {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

    }

    override fun setChecked(checked: Boolean) {
        super.setChecked(checked)
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}
}
