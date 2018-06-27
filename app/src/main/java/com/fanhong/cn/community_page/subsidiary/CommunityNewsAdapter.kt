package com.fanhong.cn.community_page.subsidiary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.fanhong.cn.R
import com.zhy.autolayout.utils.AutoUtils
import org.xutils.image.ImageOptions
import org.xutils.view.annotation.ViewInject
import org.xutils.x

class CommunityNewsAdapter(private val context: Context, private val list: List<CommunityNewsBean>) : BaseAdapter() {
    private val options: ImageOptions = ImageOptions.Builder().setLoadingDrawableId(R.mipmap.img_default).setFailureDrawableId(R.mipmap.img_default).setUseMemCache(true).build()

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder?
        val view: View =
                if (null == convertView) {
                    val view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false)
                    holder = ViewHolder(view)
                    view.tag = holder
                    view
                } else {
                    holder = convertView.tag as ViewHolder
                    convertView
                }
        val data = list[position]
        x.image().bind(holder.news_photo, data.photoUrl, options)
        when (data.news_flag) {
            CommunityNewsBean.TYPE_INFORM -> {
                holder.news_flag?.setImageResource(R.mipmap.ilon_inform)
                holder.news_flag?.visibility = View.VISIBLE
            }
            CommunityNewsBean.TYPE_NOTICE -> {
                holder.news_flag?.setImageResource(R.mipmap.ilon_notice)
                holder.news_flag?.visibility = View.VISIBLE
            }
            else -> holder.news_flag?.visibility = View.INVISIBLE
        }
        holder.tv_news_title?.text = data.title
        holder.tv_news_from?.text = data.author
        holder.tv_news_time?.text = data.time
        return view
    }

    inner class ViewHolder(view: View) {
        @ViewInject(R.id.img_news_photo)
        var news_photo: ImageView? = null
        @ViewInject(R.id.news_flag)
        var news_flag: ImageView? = null
        @ViewInject(R.id.tv_news_title)
        var tv_news_title: TextView? = null
        @ViewInject(R.id.tv_news_from)
        var tv_news_from: TextView? = null
        @ViewInject(R.id.tv_news_time)
        var tv_news_time: TextView? = null

        init {
            x.view().inject(this, view)
            AutoUtils.autoSize(view)
        }
    }
}
