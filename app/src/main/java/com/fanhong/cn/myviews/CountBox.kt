package com.fanhong.cn.myviews

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.fanhong.cn.R

/**
 * Created by Administrator on 2018/2/24.
 */
class CountBox : LinearLayout {

    var minSize: Int = 1
    var maxSize: Int = 50
    var count: Int
        set(value) {
            if (field != value) {
                field = value
                edtCount?.setText(value.toString())
            }
        }
    private var edtCount: EditText? = null

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        count = minSize
        val btnSub = Button(context)
        val btnAdd = Button(context)
        edtCount = EditText(context)
        val scal = context.resources.displayMetrics.widthPixels / 720f
        val w = 60 * scal + 0.5f
        val h = 60 * scal + 0.5f
        val lp1 = LinearLayout.LayoutParams(w.toInt(), h.toInt())
        lp1.setMargins((10 * scal).toInt(), 0, 0, 0)
        lp1.gravity = Gravity.CENTER
        btnSub.layoutParams = lp1
        edtCount!!.layoutParams = lp1
        btnAdd.layoutParams = lp1
        btnSub.setBackgroundResource(R.drawable.btn_sub_selector)
        edtCount!!.setBackgroundResource(R.drawable.biankuang_shape)
        btnAdd.setBackgroundResource(R.drawable.btn_add_selector)

        edtCount!!.keyListener = DigitsKeyListener.getInstance("0123456789")//限定输入字符
        edtCount!!.maxLines = 1
        edtCount!!.setPadding(0, 0, 0, 0)
        edtCount!!.gravity = Gravity.CENTER
        edtCount!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, 26 * scal)
        edtCount!!.setText("$count")
        btnAdd.setOnClickListener {
            if (count < maxSize) {
                val old = count
                count++
                edtCount!!.setText("$count")
                onCountChangeListener?.onCount(count, old)
            }
        }
        btnSub.setOnClickListener {
            if (count > minSize) {
                val old = count
                count--
                edtCount!!.setText("$count")
                onCountChangeListener?.onCount(count, old)
            }
        }
        edtCount!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val inputStr = edtCount!!.text.toString()
                if (inputStr.isEmpty()) {
                    edtCount!!.setText("$minSize")
                    edtCount!!.setSelection(edtCount!!.text.toString().length)
                    return
                }
                val input = inputStr.toInt()
                if (input == count) return
                when {
                    input in minSize..maxSize -> {
                        val old = count
                        count = input
                        onCountChangeListener?.onCount(count, old)
                    }
                    input < minSize -> {
                        edtCount!!.setText("$minSize")
                    }
                    input > maxSize -> {
                        edtCount!!.setText("$maxSize")
                    }
                }
                edtCount!!.setSelection(edtCount!!.text.toString().length)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        this.addView(btnSub)
        this.addView(edtCount)
        this.addView(btnAdd)
//        this.measure((w * 3).toInt(), h.toInt())
    }

    private var onCountChangeListener: OnCountChangeListener? = null

    fun onCountChange(count: (count: Int, oldCount: Int) -> Unit) {
        onCountChangeListener = object : OnCountChangeListener {
            override fun onCount(count: Int, oldCount: Int) {
                count(count, oldCount)
            }
        }
    }

    fun onCountChange(listener: OnCountChangeListener) {
        onCountChangeListener = listener
    }


    interface OnCountChangeListener {
        fun onCount(count: Int, oldCount: Int)
    }
}