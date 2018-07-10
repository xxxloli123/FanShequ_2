package com.fanhong.cn.service_page.shop

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.LogUtil
import com.zhy.autolayout.utils.AutoUtils
import kotlinx.android.synthetic.main.fragment_shop_goods_list.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.image.ImageOptions
import org.xutils.view.annotation.ViewInject
import org.xutils.x


class GoodsListFragment() : Fragment() {
    internal var type: Int = 0

    fun setType(type: Int): GoodsListFragment {//设置商品类型
        this.type = type
        return this
    }

    internal val goods: MutableList<GoodsInfo> = ArrayList()

    private var adapter: GoodsAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_shop_goods_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = GoodsAdapter(activity, goods)
        lv_goods_list.adapter = adapter
        goods.clear()
        getData()
        swiper.setOnRefreshListener {
            goods.clear()
            getData()
        }
    }

    private fun getData() {
        val param = RequestParams(App.CMD)
        if (type == 2333)
//            1059 社区卖场(APP->平台)
//              cmd:数据类型
            param.addBodyParameter("cmd", "1059")
        else {
            param.addBodyParameter("cmd", "1009")
            param.addBodyParameter("page", "1")
            param.addBodyParameter("lx", "$type")
        }
        x.http().post(param, object : Callback.CommonCallback<String> {
            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onFinished() {
                swiper.isRefreshing = false
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

            override fun onSuccess(result: String) {
                LogUtil.e("TestLog", result)
//                if ("0" == JsonSyncUtils.getJsonValue(result, "cw")) {
//                    val data = JsonSyncUtils.getJsonValue(result, "data")
//                    goods += JsonSyncUtils.getGoodsList(data)
//                    activity.runOnUiThread { adapter?.notifyDataSetChanged() }
//                    progressBar.visibility = View.GONE
//                }
                when (JsonSyncUtils.getState(result)) {
                    200 -> {
                        val data = JsonSyncUtils.getJsonValue(result, "data")
                        goods += JsonSyncUtils.getGoodsList(data)
                        activity.runOnUiThread { adapter?.notifyDataSetChanged() }
                        progressBar.visibility = View.GONE
                    }
                    400 -> {
                    }
                }
            }
        })
    }

    data class GoodsInfo(val id: String, val name: String, val pic: String, val content: String, val price: String)

    class GoodsAdapter(val context: Context, val goods: MutableList<GoodsInfo>) : BaseAdapter() {
        private val option = ImageOptions.Builder().setUseMemCache(true).setFailureDrawableId(R.mipmap.img_default).setFailureDrawableId(R.mipmap.img_default).build()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val holder: ViewHolder?
            val view: View =
                    if (null == convertView) {
                        val view = LayoutInflater.from(context).inflate(R.layout.item_shop_goods_list, parent, false)
                        holder = ViewHolder(view)
                        view.tag = holder
                        view
                    } else {
                        holder = convertView.tag as ViewHolder
                        convertView
                    }
            val data = goods[position]
            x.image().bind(holder.image, data.pic, option)
            holder.title?.text = data.name
            holder.content?.text = data.content
            holder.price?.text = Html.fromHtml("<font color='#ff4d4d'>￥${data.price}</font>")

            holder.comment?.text=((Math.random() * 222)).toInt().toString()
            view.setOnClickListener {
                val intent = Intent(context, GoodsDetailsActivity::class.java)
                intent.putExtra("id", data.id)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                context.startActivity(intent)
            }
            return view
        }

        override fun getItem(position: Int): Any {
            return goods[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return goods.size
        }

        class ViewHolder(view: View) {
            @ViewInject(R.id.img_goods)
            var image: ImageView? = null
            @ViewInject(R.id.tv_name)
            var title: TextView? = null
            @ViewInject(R.id.tv_content)
            var content: TextView? = null
            @ViewInject(R.id.tv_price)
            var price: TextView? = null
            @ViewInject(R.id.tv_comment)
            var comment: TextView? = null

            init {
                x.view().inject(this, view)
                AutoUtils.autoSize(view)
            }
        }
    }
}