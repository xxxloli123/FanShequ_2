package com.fanhong.cn.service_page.questionnaire

import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.fanhong.cn.R
import com.fanhong.cn.service_page.shop.GoodsListFragment
import com.squareup.picasso.Picasso
import com.vondear.rxtool.view.RxToast
import com.willy.ratingbar.ScaleRatingBar

import java.util.ArrayList

class CommentListAdapter(s: List<CommentListAdapter.Comment>)
    : BaseQuickAdapter<CommentListAdapter.Comment, BaseViewHolder>(R.layout.item_comment, s) {

    override fun convert(helper: BaseViewHolder, m: CommentListAdapter.Comment) {
        val s = if (m.username == null || m.username.isEmpty()) "id:"+m.uid else m.username
//        Attempt to invoke interface method 'int java.lang.CharSequence.length()' on a null object reference
//        m.username.isEmpty ctrl 點擊 isEmpty
//public inline fun CharSequence.isEmpty(): Boolean = length == 0

//        val s = if (m.username.isEmpty()) m.uid else m.username
        helper.setText(R.id.tv_name, s)
        helper.setText(R.id.tv_time, m.time)
        helper.setText(R.id.tv_comment, m.liuyan)
        (helper.getView<View>(R.id.simpleRatingBar) as ScaleRatingBar).rating = (m.fs)
        if (m.touxiang != null&&!m.touxiang.isEmpty()) Picasso.with(mContext)
                .load(m.touxiang.replace("\\", ""))
                .error(R.mipmap.head_portrait)
                .into(helper.getView<View>(R.id.civ_head) as ImageView)
    }

                    //    id:评价表的ID
                    //    uid:用户id
                    //    qid:小区id
                    //    time:评论时间
                    //    touxiang;用户头像
                    //    username;用户名
                    //    fs:用户对物业的综合评分 (平均分)
                    //    liuyan:留言
    data class Comment(val touxiang: String = "",
                       val uid: String = "",
                       val liuyan: String = "",
                       val username: String = "",
                       val id: String = "",
                       val time: String = "",
                       val qid: String = "",
                       val fs: Float = 0.0F)
}
