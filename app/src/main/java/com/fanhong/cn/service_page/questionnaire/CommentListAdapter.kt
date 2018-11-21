package com.fanhong.cn.service_page.questionnaire

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.squareup.picasso.Picasso
import com.vondear.rxtool.RxTool
import com.vondear.rxtool.view.RxToast
import com.willy.ratingbar.ScaleRatingBar
import com.wzb.imagecheckbox.ImageCheckBox
import java.io.Serializable

class CommentListAdapter(s: List<CommentListAdapter.Comment>)
    : BaseQuickAdapter<CommentListAdapter.Comment, BaseViewHolder>(R.layout.item_comment, s) {
    private lateinit var pref: SharedPreferences

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        pref=parent.context.getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun convert(helper: BaseViewHolder, m: CommentListAdapter.Comment) {
        val s = if (m.username==null||m.username.isEmpty()) "id:" + m.uid else m.username+""
//        Attempt to invoke interface method 'int java.lang.CharSequence.length()' on a null object reference
//        m.username.isEmpty ctrl 點擊 isEmpty
//public inline fun CharSequence.isEmpty(): Boolean = length == 0

//        val s = if (m.username.isEmpty()) m.uid else m.username
        if (m.pinglunname==null||m.pinglunname.isEmpty())
        (helper.getView<View>(R.id.all_open_comment_list) ).visibility=View.GONE
        else helper.setText(R.id.tv_comment_name, m.pinglunname)
                .setText(R.id.tv_comment_number, "查看余下${m.zongshu}条回复")
        if (m.c2==null) (helper.getView<View>(R.id.tv_comment2) ).visibility=View.GONE
        else helper.setText(R.id.tv_comment2, m.c2!!.name+": "+m.c2!!.liuyan)
        helper.setText(R.id.tv_name, s)
                .setText(R.id.tv_star_number, m.dianzanshu.toString())
//                .addOnClickListener(R.id.icb_star)
                .addOnClickListener(R.id.img_comment)
                .addOnClickListener(R.id.all_open_comment_list)
        helper.setText(R.id.tv_time, m.time)
        helper.setText(R.id.tv_comment, m.liuyan)
        (helper.getView<View>(R.id.simpleRatingBar) as ScaleRatingBar).rating = (m.fs)

        val icb = (helper.getView<View>(R.id.icb_star) as ImageCheckBox)
        icb.isChecked= (m.dianzan=="1")
        icb.setOnClickListener {
            icb.isChecked=!icb.isChecked
            if (icb.isChecked){
                m.dianzan="1"
                helper.setText(R.id.tv_star_number, m.dianzanshu++.toString())
                helper.setText(R.id.tv_star_number, m.dianzanshu.toString())
//                helper.setText(R.id.tv_star_number, "")
            }else{
                m.dianzan="0"
                helper.setText(R.id.tv_star_number, m.dianzanshu--.toString())
                helper.setText(R.id.tv_star_number, m.dianzanshu.toString())
//                helper.setText(R.id.tv_star_number, "")
            }
//            RxToast.success(icb.isChecked.toString()+"  "+m.dianzanshu)
            star(m)
        }
        if (m.touxiang != null && m.touxiang.isNotEmpty()) Picasso.with(mContext)
                .load(m.touxiang.replace("\\", ""))
                .error(R.mipmap.head_portrait)
                .into(helper.getView<View>(R.id.civ_head) as ImageView)
        if (m.name!=null){
            helper.setVisible(R.id.img_comment,false)
            helper.setVisible(R.id.icb_star,false)
            helper.setVisible(R.id.tv_star_number,false)
        }
    }

    private fun star(m: Comment) {
        OkGo.post<String>(App.CMD)
                .tag(this)//
                .isMultipart(true)
                //        1079：点赞（APP->平台）
                //        cmd:数据类型
                //        uid:用户id
                //        pjid:这条评论的id
                .params("cmd", "1079")
                .params("uid", pref!!.getString(App.PrefNames.USERID, "-1"))
                .params("pjid", m.id)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGo 1079", response.body().toString())
                    }

                    override fun onError(response: Response<String>) {
                        Log.e("OkGoError", response.exception.toString())
                    }
                })
    }
    //    id:评价表的ID
                    //    uid:用户id
                    //    qid:小区id
                    //    time:评论时间
                    //    touxiang;用户头像
                    //    name;用户名
                    //    fs:用户对物业的综合评分 (平均分)
                    //    liuyan:留言
                    //    dianzan: 当前用户是否点赞 0 没点赞 1 点赞
                    //    dianzanshu:点赞的总数
                    //    zongsu:评论这条评论的总人数
                    //    pinglunname:第一个评论这条评论的人

    data class Comment (var dianzanshu: Int = 0,
                       val touxiang: String = "",
                       val uid: String = "",
                       val zongshu: Int = 0,
                       var dianzan: String = "",
                       val liuyan: String = "",
                       var pinglunname: String = "",
                       val id: String = "",
                       val time: String = "",
                       val qid: String = "",
                       val fs: Float = 0.0F,
                       val username: String = "",
                       //详情评论
                       val name: String = "",
                       var c2:Comment2?) : Serializable {

                        //    [id:评论id
                        //    uid:用户id
                        //    pjid:被评论的id
                        //    name:评论人姓名
                        //    touxiang:评论人头像
                        //    liuyan:评论内容
                        //    time:评论时间
        data class Comment2(val uid: String = "",
                            val pjid: String = "",
                            val liuyan: String = "",
                            val name: String = "",
                            val id: String = "",
                            val time: String = ""):Serializable
    }
}
