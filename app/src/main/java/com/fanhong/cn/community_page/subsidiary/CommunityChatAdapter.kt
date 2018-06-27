package com.fanhong.cn.community_page.subsidiary


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import com.fanhong.cn.App
import com.fanhong.cn.R
import com.zhy.autolayout.utils.AutoUtils

import org.xutils.image.ImageOptions
import org.xutils.view.annotation.ViewInject
import org.xutils.x

import java.text.SimpleDateFormat
import java.util.Date


/**
 * Created by Administrator on 2017/6/30.
 */

class CommunityChatAdapter(private val context: Context, private val list: List<CommunityMessageBean>) : BaseAdapter() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private val options: ImageOptions = ImageOptions.Builder().setLoadingDrawableId(R.mipmap.head_default).setIgnoreGif(false).setFailureDrawableId(R.mipmap.head_default).setCircular(true).setUseMemCache(true).build()
    private var holderLeft: ViewHolderLeft? = null
    private var holderRight: ViewHolderRight? = null
    private var bean: CommunityMessageBean? = null

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].type
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        val type = getItemViewType(position)
        if (convertView == null) {
            when (type) {
                CommunityMessageBean.TYPE_LEFT -> {
                    convertView = mInflater.inflate(R.layout.item_chat_left, null)
                    holderLeft = ViewHolderLeft()
                    x.view().inject(holderLeft, convertView)
                    AutoUtils.autoSize(convertView!!)
                    convertView.tag = holderLeft
                }
                CommunityMessageBean.TYPE_RIGHT -> {
                    convertView = mInflater.inflate(R.layout.item_chat_right, null)
                    holderRight = ViewHolderRight()
                    x.view().inject(holderRight, convertView)
                    AutoUtils.autoSize(convertView!!)
                    convertView.tag = holderRight
                }
                else -> {
                }
            }
        } else {
            when (type) {
                CommunityMessageBean.TYPE_LEFT -> holderLeft = convertView.tag as ViewHolderLeft
                CommunityMessageBean.TYPE_RIGHT -> holderRight = convertView.tag as ViewHolderRight
                else -> {
                }
            }
        }
        bean = list[position]
        when (type) {
            CommunityMessageBean.TYPE_LEFT -> {
                holderLeft!!.tv_time!!.visibility = View.GONE
                if (App.old_msg_times.contains(bean!!.msgTime)) {
                    val sendTime = SimpleDateFormat("MM月dd日 HH:mm").format(Date(bean!!.msgTime))
                    holderLeft!!.tv_time!!.visibility = View.VISIBLE
                    holderLeft!!.tv_time!!.text = sendTime
                }
                holderLeft!!.tv_user!!.text = bean!!.userName
                holderLeft!!.tv_msg!!.text = bean!!.message
                x.image().bind(holderLeft!!.img_head, bean!!.headUrl, options)
            }
            CommunityMessageBean.TYPE_RIGHT -> {
                holderRight!!.tv_time!!.visibility = View.GONE
                if (App.old_msg_times.contains(bean!!.msgTime)) {
                    val sendTime = SimpleDateFormat("MM月dd日 HH:mm").format(Date(bean!!.msgTime))
                    holderRight!!.tv_time!!.visibility = View.VISIBLE
                    holderRight!!.tv_time!!.text = sendTime
                }
                holderRight!!.tv_user!!.text = bean!!.userName
                holderRight!!.tv_msg!!.text = bean!!.message
                x.image().bind(holderRight!!.img_head, bean!!.headUrl, options)
            }
            else -> {
            }
        }
        return convertView
    }

    private fun iswent5min(msgtime: Long, lastmsgtime: Long): Boolean {
        val sys = Date(lastmsgtime)
        val msg = Date(msgtime)
        val went = sys.time - msg.time
        //        Toast.makeText(context,String.valueOf(went),Toast.LENGTH_SHORT).show();
        return if (went > 1000 * 60 * 1) true else false
    }

    internal inner class ViewHolderLeft {
        @ViewInject(R.id.tv_msg_time_left)
        var tv_time: TextView? = null
        @ViewInject(R.id.tv_chat_user_left)
        var tv_user: TextView? = null
        @ViewInject(R.id.tv_chat_msg_left)
        var tv_msg: TextView? = null
        @ViewInject(R.id.img_chat_head_left)
        var img_head: ImageView? = null
    }

    internal inner class ViewHolderRight {
        @ViewInject(R.id.tv_msg_time_right)
        var tv_time: TextView? = null
        @ViewInject(R.id.tv_chat_user_right)
        var tv_user: TextView? = null
        @ViewInject(R.id.tv_chat_msg_right)
        var tv_msg: TextView? = null
        @ViewInject(R.id.img_chat_head_right)
        var img_head: ImageView? = null
    }
}
