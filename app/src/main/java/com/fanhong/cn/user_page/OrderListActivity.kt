package com.fanhong.cn.user_page

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import com.zhy.autolayout.utils.AutoUtils
import kotlinx.android.synthetic.main.activity_order_list.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.view.annotation.ViewInject
import org.xutils.x

class OrderListActivity : AppCompatActivity() {
    companion object {
        private var orderList: MutableList<MyOrder> = ArrayList()
    }

    private var adapter: OrderAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_list)
        tv_title.text = getString(R.string.mytradeno)
        img_back.setOnClickListener { finish() }

        adapter = OrderAdapter(this)
        lv_orders.adapter = adapter
        lv_orders.setOnItemClickListener { _, _, position, _ ->
            val orderId = orderList[position].id
            val intent = Intent(this@OrderListActivity, OrderDetailsActivity::class.java)
            intent.putExtra("id", orderId)
            startActivity(intent)
        }
        initData()
    }

    private fun initData() {
        orderList.clear()

        val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val uid = pref.getString(App.PrefNames.USERID, "-1")
        val param = RequestParams(App.CMD)
        param.addBodyParameter("cmd", "1003")
        param.addBodyParameter("uid", uid)
        x.http().post(param, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                when (JsonSyncUtils.getState(result)) {
                    200 -> {
                        val data = JsonSyncUtils.getJsonValue(result, "data")
//                        Log.e("testLog",data)
                        orderList = JsonSyncUtils.getOrderList(data)
                        runOnUiThread { adapter?.notifyDataSetChanged() }
                    }
//                    else -> ToastUtil.showToastL("参数错误！")
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                ToastUtil.showToastL("连接服务器失败，请检查网络连接")
            }

            override fun onFinished() {
            }
        })
    }

    data class MyOrder(val id: String, val number: String, val name: String, val price: String, val time: String)
    private class OrderAdapter(val context: Context) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val holder: ViewHolder?
            val view: View =
                    if (null == convertView) {
                        val view = LayoutInflater.from(context).inflate(R.layout.item_order_list, parent, false)
                        holder = ViewHolder(view)
                        view.tag = holder
                        view
                    } else {
                        holder = convertView.tag as ViewHolder
                        convertView
                    }
            val data = orderList[position]
            holder.number?.text = data.number
            holder.time?.text = data.time
            holder.title?.text = data.name
            holder.price?.text = Html.fromHtml("实付款：<font color='#ff4d4d'>￥${data.price}</font>")
            return view
        }

        override fun getItem(position: Int): Any {
            return orderList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return orderList.size
        }

        inner class ViewHolder(view: View) {
            @ViewInject(R.id.tv_item_order_number)
            var number: TextView? = null
            @ViewInject(R.id.tv_item_order_time)
            var time: TextView? = null
            @ViewInject(R.id.tv_item_order_title)
            var title: TextView? = null
            @ViewInject(R.id.tv_item_order_price)
            var price: TextView? = null

            init {
                x.view().inject(this, view)
                AutoUtils.autoSize(view)
            }
        }
    }
}
