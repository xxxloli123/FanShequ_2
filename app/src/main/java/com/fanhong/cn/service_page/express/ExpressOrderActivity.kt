package com.fanhong.cn.service_page.express

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import com.zhy.autolayout.AutoRelativeLayout
import com.zhy.autolayout.utils.AutoUtils
import kotlinx.android.synthetic.main.activity_express_order.*
import kotlinx.android.synthetic.main.activity_top.*
import org.json.JSONException
import org.json.JSONObject
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x
import java.util.*

class ExpressOrderActivity : AppCompatActivity() {

    private val mysendModelList = ArrayList<MysendModel>()
    private var mySendexpressAdapter: MySendexpressAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_express_order)
        tv_title.text = "我的订单"
        img_back.setOnClickListener { finish() }
        mySendexpressAdapter = MySendexpressAdapter(this,mysendModelList)
        mySendexpressAdapter?.setItemClick { id, position ->
            val builder = AlertDialog.Builder(this@ExpressOrderActivity)
            builder.setTitle("删除此条数据！")
            builder.setMessage("确定删除此条运单吗？")
            builder.setPositiveButton("确定") { _, _ ->
                val params = RequestParams(App.CMD)
                params.addBodyParameter("cmd", "91")
                params.addBodyParameter("id", id.toString())
                x.http().post(params, object : Callback.CommonCallback<String> {
                    override fun onSuccess(result: String) {
                        if (JsonSyncUtils.getJsonValue(result, "cw") == "0") {
                            ToastUtil.showToastS("删除成功！")
                            mysendModelList.removeAt(position)
                            handler.sendEmptyMessage(1)
                        } else {
                            ToastUtil.showToastS("删除失败！")
                        }
                    }

                    override fun onError(ex: Throwable, isOnCallback: Boolean) {

                    }

                    override fun onCancelled(cex: Callback.CancelledException) {

                    }

                    override fun onFinished() {

                    }
                })
            }
            builder.setNegativeButton("取消") { dialog, which -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
        }
        my_send_ex_recyc.adapter = mySendexpressAdapter
    }

    override fun onResume() {
        super.onResume()
        initDatas()
    }

    private val handler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            1 -> if (mySendexpressAdapter != null) {
                mySendexpressAdapter?.notifyDataSetChanged()
            }
            2 -> {
                my_send_ex_recyc.visibility = View.VISIBLE
                empty_order.visibility = View.GONE
            }
            3 -> {
                my_send_ex_recyc.visibility = View.GONE
                empty_order.visibility = View.VISIBLE
            }
        }
        true
    })
    private fun initDatas() {
        mysendModelList.clear()
        val params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "87")
        params.addBodyParameter("uid",getSharedPreferences(App.PREFERENCES_NAME,Context.MODE_PRIVATE).getString(App.PrefNames.USERID,"-1"))
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                if (JsonSyncUtils.getJsonValue(result, "cw") == "0") {
                    try {
                        val jsonArray = JSONObject(result).optJSONArray("data")
                        if (jsonArray.length() > 0) {
                            handler.sendEmptyMessage(2)
                            (0 until jsonArray.length())
                                    .map { jsonArray.optJSONObject(it) }
                                    .mapTo(mysendModelList){
                                        val sendCity = it.optString("jsf", "null")
                                        val receiveCity = it.optString("ssf", "null")
                                        val sendName = it.optString("jmz")
                                        val receiveName = it.optString("smz")
                                        val id = it.optInt("id", 0)
                                        MysendModel(sendCity, receiveCity, sendName, receiveName, id)
                                    }
                            handler.sendEmptyMessage(1)
                        } else {
                            handler.sendEmptyMessage(3)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }

            override fun onError(ex: Throwable, isOnCallback: Boolean) {

            }

            override fun onCancelled(cex: Callback.CancelledException) {

            }

            override fun onFinished() {

            }
        })
    }
    data class MysendModel(var sendCity: String, var receiveCity: String, var sendName: String, var receiveName: String, var id: Int)
    internal class MySendexpressAdapter(private val context: Context, private val list: List<MysendModel>) : BaseAdapter() {
        private val inflater: LayoutInflater = LayoutInflater.from(context)

        private var itemClick: ItemClick? = null

        fun setItemClick(l: (id: Int, position: Int) -> Unit) {
            this.itemClick = object : ItemClick {
                override fun onClick(id: Int, position: Int) {
                    l(id, position)
                }
            }
        }

        interface ItemClick {
            fun onClick(id: Int, position: Int)
        }

        override fun getCount(): Int = list.size

        override fun getItem(position: Int): Any = list[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val myViewHolder: MyViewHolder
            val view =
                    if (convertView == null) {
                        val view = inflater.inflate(R.layout.sendexpress_item, null)
                        myViewHolder = MyViewHolder(view!!)
                        view!!.tag = myViewHolder
                        view
                    } else {
                        myViewHolder = convertView.tag as MyViewHolder
                        convertView
                    }
            val model = list[position]
            myViewHolder.scity.text = model.sendCity
            myViewHolder.sname.text = model.sendName
            myViewHolder.rcity.text = model.receiveCity
            myViewHolder.rname.text = model.receiveName
            val id = model.id
            myViewHolder.itemLayout.setOnLongClickListener {
                itemClick?.onClick(id, position)
                true
            }
            return view
        }

        private inner class MyViewHolder(itemView: View) {
            internal var itemLayout: AutoRelativeLayout = itemView.findViewById(R.id.item_layout)
            internal var scity: TextView = itemView.findViewById(R.id.send_express_city)
            internal var sname: TextView = itemView.findViewById(R.id.send_express_name)
            internal var rcity: TextView = itemView.findViewById(R.id.get_express_city)
            internal var rname: TextView = itemView.findViewById(R.id.get_express_name)

            init {
                AutoUtils.autoSize(itemView)
            }
        }
    }

}
