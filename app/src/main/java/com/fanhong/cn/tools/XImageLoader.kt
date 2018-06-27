package com.fanhong.cn.tools

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import cn.finalteam.galleryfinal.ImageLoader
import cn.finalteam.galleryfinal.widget.GFImageView
import org.xutils.image.ImageOptions
import org.xutils.x


/**
 * Created by Administrator on 2018/2/1.
 */
class XImageLoader : ImageLoader {
    private var mImageConfig: Bitmap.Config? = null

    constructor() : this(Bitmap.Config.RGB_565)

    constructor(config: Bitmap.Config) {
        this.mImageConfig = config
    }

    override fun displayImage(activity: Activity?, path: String?, imageView: GFImageView?, defaultDrawable: Drawable?, width: Int, height: Int) {
        val options = ImageOptions.Builder()
                .setLoadingDrawable(defaultDrawable)
                .setFailureDrawable(defaultDrawable)
                .setConfig(mImageConfig)
                .setSize(width, height)
                .setCrop(true)
                .setUseMemCache(false)
                .build()
        x.image().bind(imageView, "file://" + path, options)
    }

    override fun clearMemoryCache() {

    }

}