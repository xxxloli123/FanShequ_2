package com.fanhong.cn.myviews

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.fanhong.cn.R

/**
 * Created by Administrator on 2018/1/24.
 */
class PhotoSelectWindow(context: Context) : PopupWindow(context) {
    private var takePhotoBtn: Button? = null
    private var pickPhotoBtn: Button? = null

    init {
        val menuView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_pic, null)
        takePhotoBtn = menuView.findViewById(R.id.btn_takePhoto)
        pickPhotoBtn = menuView.findViewById(R.id.btn_pickPhoto)
        menuView.findViewById<Button>(R.id.btn_cancel).setOnClickListener { dismiss() }

        contentView = menuView
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        isFocusable = true
        animationStyle = R.style.PopupAnimation
        setBackgroundDrawable(ColorDrawable(-0x80000000))
        menuView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val height = menuView.findViewById<LinearLayout>(R.id.pop_layout).top
                val y = event.y.toInt()
                if (y < height) {
                    dismiss()
                }
            }
            true
        }
    }

    fun setOnTakePhoto(listener: View.OnClickListener): PhotoSelectWindow {
        takePhotoBtn?.setOnClickListener(listener)
        return this
    }

    fun setOnChoosePic(listener: View.OnClickListener): PhotoSelectWindow {
        pickPhotoBtn?.setOnClickListener(listener)
        return this
    }
}