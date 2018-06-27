package com.fanhong.cn.service_page.government.subsidiary

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.fanhong.cn.R
import com.fanhong.cn.service_page.government.SharedFragment
import com.zhy.autolayout.AutoLinearLayout
import com.zhy.autolayout.AutoRelativeLayout
import com.zhy.autolayout.utils.AutoUtils
import org.xutils.image.ImageOptions
import org.xutils.view.annotation.ViewInject
import org.xutils.x

/**
 * Created by Administrator on 2017/11/21.
 */

class SharedAdapter(private val context: Context, private val list: List<SharedFragment.FxItemModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private var click: ItemClick? = null

    fun setItemClick(click: (id: Int, content: String, imgUrl: String) -> Unit) {
        this.click = object : ItemClick {
            override fun itemClick(id: Int, content: String, imgUrl: String) {
                click(id, content, imgUrl)
            }
        }
    }

    interface ItemClick {
        fun itemClick(id: Int, content: String, imgUrl: String)
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].picUrl.isNotEmpty()) {
            A_PICTURE
        } else
            NO_PICTURE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view: View
        var holder: RecyclerView.ViewHolder? = null
        when (viewType) {
            NO_PICTURE -> {
                view = inflater.inflate(R.layout.item_fx, parent, false)
                holder = MyViewHolder1(view)
            }
            A_PICTURE -> {
                view = inflater.inflate(R.layout.item_fx_1, parent, false)
                holder = MyViewHolder2(view)
            }
            else -> {
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val options = ImageOptions.Builder().setLoadingDrawableId(R.mipmap.pictureloading)
                .setFailureDrawableId(R.mipmap.picturefailedloading).setUseMemCache(true).build()
        val options1 = ImageOptions.Builder().setLoadingDrawableId(R.mipmap.pictureloading)
                .setFailureDrawableId(R.mipmap.picturefailedloading).setCircular(true).setUseMemCache(true).build()
        val model = list[position]
        when (getItemViewType(position)) {
            NO_PICTURE -> {
                val myViewHolder1 = holder as MyViewHolder1
                val str = model.content
                if (str.length > 8) {
                    myViewHolder1.title!!.text = str.substring(0, 8) + "..."
                } else {
                    myViewHolder1.title!!.text = str
                }
                myViewHolder1.content!!.text = str
                if (!TextUtils.isEmpty(model.photoUrl)) {
                    val ul = model.photoUrl
                    x.image().bind(myViewHolder1.photo, ul, options1)
                } else {
                    myViewHolder1.photo!!.setImageResource(R.mipmap.default_photo)
                }
                myViewHolder1.author!!.text = model.author
                myViewHolder1.time!!.text = model.time
                val id = model.id
                myViewHolder1.layout!!.setOnClickListener { click?.itemClick(id, str, "") }
            }
            A_PICTURE -> {
                val myViewHolder2 = holder as MyViewHolder2
                val str1 = model.content
                if (str1.length > 8) {
                    myViewHolder2.title1!!.text = str1.substring(0, 8) + "..."
                } else {
                    myViewHolder2.title1!!.text = str1
                }
                myViewHolder2.content1!!.text = str1
                if (!TextUtils.isEmpty(model.photoUrl)) {
                    val ul = model.photoUrl
                    x.image().bind(myViewHolder2.photo1, ul, options1)
                } else {
                    myViewHolder2.photo1!!.setImageResource(R.mipmap.default_photo)
                }
                myViewHolder2.author1!!.text = model.author
                myViewHolder2.time1!!.text = model.time
                x.image().bind(myViewHolder2.fxImage, model.picUrl, options)
                val imgurl = model.picUrl
                val id1 = model.id
                myViewHolder2.layout1!!.setOnClickListener { click?.itemClick(id1, str1, imgurl) }
            }
            else -> {
            }
        }
    }

    override fun getItemCount(): Int {
        return if (list.size > 0) list.size else 0
    }


    internal inner class MyViewHolder1(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @ViewInject(R.id.fx_layout)
        var layout: AutoRelativeLayout? = null
        @ViewInject(R.id.title)
        var title: TextView? = null
        @ViewInject(R.id.content)
        var content: TextView? = null
        @ViewInject(R.id.author_photo)
        var photo: ImageView? = null
        @ViewInject(R.id.author)
        var author: TextView? = null
        @ViewInject(R.id.time)
        var time: TextView? = null

        init {
            x.view().inject(this, itemView)
            AutoUtils.autoSize(itemView)
        }
    }

    internal inner class MyViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @ViewInject(R.id.fx_layout_1)
        var layout1: AutoLinearLayout? = null
        @ViewInject(R.id.title1)
        var title1: TextView? = null
        @ViewInject(R.id.content1)
        var content1: TextView? = null
        @ViewInject(R.id.author_photo1)
        var photo1: ImageView? = null
        @ViewInject(R.id.author1)
        var author1: TextView? = null
        @ViewInject(R.id.time1)
        var time1: TextView? = null
        @ViewInject(R.id.fx_image)
        var fxImage: ImageView? = null

        init {
            x.view().inject(this, itemView)
            AutoUtils.autoSize(itemView)
        }
    }

    companion object {

        private val NO_PICTURE = 1
        private val A_PICTURE = 2
    }
}
