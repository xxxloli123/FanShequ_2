package com.fanhong.cn.myviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.fanhong.cn.R

class SideBar(context: Context,attrs: AttributeSet?) : View(context, attrs) {
    companion object {
        val b = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I",
                "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                "W", "X", "Y", "Z", "#")
    }
    private var paint:Paint?=null
    private var choose = -1 //选中状态
    private var onLetterListener: OnLetterChangedListener? = null //按下事件处理器
    private var mTextView: TextView? = null


//    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

//    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setTextView(mTextDialog: TextView) {
        mTextView = mTextDialog
    }

    override fun onDraw(canvas: Canvas) {
        //获取焦点时改变背景颜色
        var singleHeight = height / b.size

        for (i in 0 until b.size) {
            paint = Paint()
            paint!!.color = Color.GRAY
            paint!!.typeface = Typeface.DEFAULT_BOLD
            paint!!.isAntiAlias = true
            paint!!.textSize = dip2px(context, 10f)
            if (i == choose) {
                paint!!.color = Color.parseColor("#3399ff")
                paint!!.isFakeBoldText = true
            }
            // x坐标 = 中间 - 字符串宽度的一半
            var xPos = width / 2 - paint!!.measureText(b[i]) / 2
            var yPos = singleHeight * i + singleHeight
            canvas.drawText(b[i], xPos, yPos.toFloat(), paint!!)
//            paint!!.reset()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val oldChoose = choose
        val c = (event!!.y / height * b.size).toInt()  // 点击y坐标所占总高度的比例*b数组的长度就等于点击b中的个数.
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                setBackgroundDrawable(ColorDrawable(0x00000000))
                choose = -1
                invalidate()
                mTextView!!.visibility = View.INVISIBLE
            }
            else -> {
                setBackgroundResource(R.drawable.sidebar_background)
                if (oldChoose != c) {
                    onLetterListener!!.onLetterChanged(b[c])
                    mTextView!!.text = b[c]
                    mTextView!!.visibility = View.VISIBLE
                }
                choose = c
                invalidate()
            }
        }
        return true
    }

    private fun dip2px(c: Context, dpValue: Float): Float {
        val scale = c.resources.displayMetrics.density
        return dpValue * scale + 0.5f
    }

    interface OnLetterChangedListener {
        fun onLetterChanged(s: String)
    }

    fun setOnLetterChangedListener(onLetterChangedListener: OnLetterChangedListener) {
        onLetterListener = onLetterChangedListener!!
    }
}

