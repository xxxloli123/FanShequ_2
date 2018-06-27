package com.fanhong.cn.service_page.express

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fanhong.cn.R
import com.zhy.autolayout.utils.AutoUtils
import kotlinx.android.synthetic.main.activity_choose_express.*
import kotlinx.android.synthetic.main.activity_top.*
import java.util.ArrayList

class ChooseExpressActivity : AppCompatActivity() {

    private val expressList = ArrayList<String>()
    private var typeAdapter: ExpressTypeAdapter? = null
    private var status = 0

    private val expressage = arrayOf("顺丰快递", "韵达快递", "申通快递", "中通快递", "EMS", "圆通快递", "百世汇通")
    private val times = arrayOf("一小时以内", "两小时以内", "三小时以内", "四小时以内")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_express)
        status = intent.getIntExtra("status",0)
        when(status){
            1->{
                tv_title.text = "选择时间"
                times.indices.mapTo(expressList) { times[it] }
                typeAdapter = ExpressTypeAdapter(this,expressList,1)
            }
            2->{
                tv_title.text = "快递类型"
                expressage.indices.mapTo(expressList){expressage[it]}
                typeAdapter = ExpressTypeAdapter(this,expressList,2)
            }
        }
        express_type_recyc.adapter = typeAdapter
        express_type_recyc.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)

    }

    internal class ExpressTypeAdapter(private val context: Context, private val list: MutableList<String>, private val status: Int) : RecyclerView.Adapter<ExpressTypeAdapter.MyViewHolder>() {
        private val inflater: LayoutInflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = inflater.inflate(R.layout.express_type_item, null)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.express.text = list[position]
            holder.itemView.setOnClickListener {
                val pos = holder.layoutPosition
                val intent = Intent()
                intent.putExtra("string", list[pos])
                if (status == 2) {
                    (context as Activity).setResult(46, intent)
                    context.finish()
                } else if (status == 1) {
                    (context as Activity).setResult(45, intent)
                    context.finish()
                }
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            internal var express: TextView = itemView.findViewById(R.id.tv_express_type)

            init {
                AutoUtils.autoSize(itemView)
            }
        }
    }
}
