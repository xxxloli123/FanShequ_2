package com.fanhong.cn.home_page

import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.fanhong.cn.R
import com.zhy.autolayout.AutoLinearLayout
import kotlinx.android.synthetic.main.activity_banner_in.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.image.ImageOptions
import org.xutils.x

class BannerInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_banner_in)
        tv_title.text = "人脸识别"
        img_back.setOnClickListener { finish() }
        x.image().loadDrawable("assets://images/face_recognition_introduce.png",
                ImageOptions.Builder().setUseMemCache(true).build(), object : Callback.CommonCallback<Drawable> {
            override fun onSuccess(drawable: Drawable) {
                val display = resources.displayMetrics
                val width = display.widthPixels
                val height: Int
                Log.i("asd", "width:" + width + "drawable:" + drawable.minimumWidth + "::" + drawable.minimumHeight)
                height = drawable.minimumHeight * width / drawable.minimumWidth
                val params = AutoLinearLayout.LayoutParams(width, height)
                params.bottomMargin = 10 * width / 720
                Log.i("asd", "width:" + width + "imageview:" + width + "::" + height)
                img_introduce.layoutParams = params
                img_introduce.setImageDrawable(drawable)
            }

            override fun onError(throwable: Throwable, b: Boolean) {

            }

            override fun onCancelled(e: Callback.CancelledException) {

            }

            override fun onFinished() {

            }
        })
    }
}
