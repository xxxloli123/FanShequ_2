package com.fanhong.cn.user_page

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import com.zhy.autolayout.utils.AutoUtils
import kotlinx.android.synthetic.main.activity_order_details.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.view.annotation.ViewInject
import org.xutils.x

class OrderDetailsActivity : AppCompatActivity() {
    companion object {
        private var goodsList: MutableList<Goods> = ArrayList()
    }

    private var adapter: GoodsAdapter? = null
    private var gid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)
        tv_title.text = getString(R.string.orderDetails)
        img_back.setOnClickListener { finish() }
        gid = intent.getStringExtra("id")

        adapter = GoodsAdapter(this)
        lv_order_goods.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        initData()
    }

    private fun initData() {
        goodsList.clear()
//        goodsList.add(Goods("传世·酱聖酒", "1", "1", false))
        val uid = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE).getString(App.PrefNames.USERID, "-1")
        val param = RequestParams(App.CMD)
        param.addBodyParameter("cmd", "1005")
        param.addBodyParameter("uid", uid)
        param.addBodyParameter("iid", gid)
        x.http().post(param, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                when (JsonSyncUtils.getState(result)) {
                    200 -> {
                        val data = JsonSyncUtils.getJsonValue(result, "data")
                        tv_consignee.text = JsonSyncUtils.getJsonValue(data, "user")//收货人
                        tv_consigneeNumber.text = JsonSyncUtils.getJsonValue(data, "dh")//收货人电话
                        tv_address.text = JsonSyncUtils.getJsonValue(data, "ldh")//详细地址
                        when (JsonSyncUtils.getJsonValue(data, "zffs")) {//支付方式
                            "1" -> checkbox_aliPay.isChecked = true
                            "2" -> checkbox_weiPay.isChecked = true
                        }
                        tv_planDate.text = JsonSyncUtils.getJsonValue(data, "yjtime")//预计到达时间
                        tv_realDate.text = JsonSyncUtils.getJsonValue(data, "ddtime")//到达时间
                        tv_totalPaid.text = "￥${JsonSyncUtils.getJsonValue(data, "zjje")}"//支付金额

                        val goods = JsonSyncUtils.getJsonValue(data, "goods")
//                        Log.e("testLog", data)
                        goodsList = JsonSyncUtils.getOrderGoodsList(goods,gid)
                        runOnUiThread { adapter?.notifyDataSetChanged() }
                    }
                    else -> ToastUtil.showToastL("参数错误！")
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

//        adapter?.notifyDataSetChanged()
    }

    data class Goods(val name: String, val id: String, val number: String, val isEvaluated: Boolean,val orderId:String)
    private class GoodsAdapter(val context: Context) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val holder: ViewHolder?
            val view: View =
                    if (null == convertView) {
                        val view = LayoutInflater.from(context).inflate(R.layout.item_order_goods, parent, false)
                        holder = ViewHolder(view)
                        view.tag = holder
                        view
                    } else {
                        holder = convertView.tag as ViewHolder
                        convertView
                    }
            val data = goodsList[position]
            holder.name?.text = data.name
            holder.number?.text = "×${data.number}"
            if (!data.isEvaluated) {
                holder.evaluate?.visibility = View.VISIBLE
                holder.evaluate?.setOnClickListener {
                    val intent = Intent(context, EvaluateActivity::class.java)
                    intent.putExtra("goodsId", data.id)
                    intent.putExtra("orderId", data.orderId)
                    intent.putExtra("goodsName", data.name)
                    context.startActivity(intent)
                }
            } else holder.evaluate?.visibility = View.INVISIBLE
            return view
        }

        override fun getItem(position: Int): Any {
            return goodsList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return goodsList.size
        }

        private class ViewHolder(view: View) {
            @ViewInject(R.id.tv_order_goodsName)
            var name: TextView? = null
            @ViewInject(R.id.tv_order_goodsNumber)
            var number: TextView? = null
            @ViewInject(R.id.btn_order_goods_evaluate)
            var evaluate: Button? = null

            init {
                x.view().inject(this, view)
                AutoUtils.autoSize(view)
            }
        }
    }
}
