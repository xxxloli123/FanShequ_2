package com.fanhong.cn.service_page.questionnaire

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fanhong.cn.R
import kotlinx.android.synthetic.main.item_questionnaire.view.*

open class QuestionnaireAdapter(questions: ArrayList<Question>, private val mCallqck: Callqck)
    : RecyclerView.Adapter<QuestionnaireAdapter.MyHolder>() ,View.OnClickListener{

    private var qs=questions
    lateinit var mContext : Context

    //刷新Adapter
    fun refresh(thisRIs:ArrayList<Question>) {
        this.qs = thisRIs//传入list，然后调用notifyDataSetChanged方法
        notifyDataSetChanged()
    }

    /**
     * 自定义接口，用于回调按钮点击事件到Activity
     * @author Ivan Xu
     * 2014-11-26
     */
    interface Callqck {
        fun click(v: View)
    }

    override fun onClick(v: View?) {
        mCallqck.click(v!!)
    }

    override fun getItemCount(): Int =qs.size

    override fun onBindViewHolder(holder: MyHolder?, position: Int) {
        holder!!.bind(position,qs[position], this)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_questionnaire, parent, false)
        mContext=parent.context
        return MyHolder(view)
    }

    class MyHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val v=view
        fun bind(p:Int,q: Question, clickListener: View.OnClickListener){
            view.setOnClickListener(clickListener)
            view.tag=p
            view.tv_question.text=q.timu
            view.tv_serial.text="${p+1}、"
            view.simpleRatingBar.setOnRatingChangeListener { p0, p1 ->
                q.fraction = p1
            }
        }
    }

}