package com.fanhong.cn.service_page.shop

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.myviews.CountBox
import com.zhy.autolayout.utils.AutoUtils
import kotlinx.android.synthetic.main.activity_shop_car.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.util.KeyValue
import org.xutils.db.sqlite.WhereBuilder
import org.xutils.image.ImageOptions
import org.xutils.view.annotation.ViewInject
import org.xutils.x

class ShopCarActivity : AppCompatActivity() {

    companion object {
        var total = 0.0f
        var uid = "-1"
    }

    private val goods: MutableList<GoodsCarTable> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_car)
        tv_title.text = "购物车"
        img_back.setOnClickListener { finish() }


        btn_buyNow.setOnClickListener {
            if (total > 0) {
                val i = Intent(this, OrderConfirmActivity::class.java)
                i.putExtra("from", "car")
                i.putExtra("total", total)
                startActivity(i)
            }
        }
        uid = getUid()
    }

    private fun getUid(): String = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE).getString(App.PrefNames.USERID, "-1")

    override fun onResume() {
        super.onResume()
        goods.clear()
        goods += App.db.selector(GoodsCarTable::class.java).where("c_uid", "=", getUid()).findAll()
        lv_car.adapter = GoodsAdapter(this, goods).setTotalChangeListener { total ->
            tv_goods_total.text = "￥$total"
        }
        total = 0f
        total = App.db.selector(GoodsCarTable::class.java).where("c_select", "=", true).and("c_uid", "=", getUid()).findAll()
                .map { it.count * it.price.toFloat() }
                .sum()
        total = (Math.round(total * 100)).toFloat() / 100
        tv_goods_total.text = "￥$total"
    }

    private class GoodsAdapter(val context: Context, val list: MutableList<GoodsCarTable>) : BaseAdapter() {
        private val option = ImageOptions.Builder().setUseMemCache(true).setFailureDrawableId(R.mipmap.img_default).setFailureDrawableId(R.mipmap.img_default).build()
        private var onTotalChangeListener: OnTotalChange? = null
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val holder: ViewHolder
            val view: View =
                    if (null == convertView) {
                        val view = LayoutInflater.from(context).inflate(R.layout.item_goods_car, parent, false)
                        holder = ViewHolder(view)
                        view.tag = holder
                        view
                    } else {
                        holder = convertView.tag as ViewHolder
                        convertView
                    }
            val data = list[position]
            x.image().bind(holder.ivLogo, data.logo, option)
            holder.check?.isChecked = data.select
            holder.tvTitle?.text = data.name
            holder.tvDescription?.text = data.content
            holder.tvPrice?.text = "￥${data.price}"
            holder.tvUnit?.text = data.unit
            holder.countBox?.count = data.count
            holder.check?.setOnCheckedChangeListener { _, isChecked ->
                val sum = holder.countBox!!.count * data.price.toFloat()
                if (isChecked) {
                    total += sum
                } else {
                    total -= sum
                }
                total = (Math.round(total * 100)).toFloat() / 100
                data.select = isChecked
                App.db.update(GoodsCarTable::class.java, WhereBuilder.b("c_id", "=", data.gid).and("c_uid", "=", uid), KeyValue("c_select", isChecked))
                onTotalChangeListener?.onTotalChange(total)
            }
            holder.btnClose?.setOnClickListener {
                AlertDialog.Builder(context).setMessage("是否删除此项？").setPositiveButton("确定") { _, _ ->
                    val select = App.db.selector(GoodsCarTable::class.java).where("c_uid", "=", uid).and("c_id", "=", data.gid).findFirst().select
                    val i = App.db.delete(GoodsCarTable::class.java, WhereBuilder.b("c_id", "=", data.gid).and("c_uid", "=", uid))
                    if (i > 0) {
                        if (select) {
                            val sum = holder.countBox!!.count * data.price.toFloat()
                            total -= sum
                            total = (Math.round(total * 100)).toFloat() / 100
                            onTotalChangeListener?.onTotalChange(total)
                        }
                        list.removeAt(position)
                        (context as Activity).runOnUiThread { notifyDataSetChanged() }
                    }
                }.setNegativeButton("取消", null).show()
            }
            holder.countBox?.onCountChange { count, oldCount ->
                val info = App.db.selector(GoodsCarTable::class.java).where("c_id", "=", data.gid).and("c_uid", "=", uid).findFirst()
                info.count = count
                App.db.saveOrUpdate(info)

                if (info.select) {
                    val sum = (count - oldCount) * data.price.toFloat()
                    total += sum
                    total = (Math.round(total * 100)).toFloat() / 100
                    onTotalChangeListener?.onTotalChange(total)
                }
                list[position].count = count
//                (context as Activity).runOnUiThread { notifyDataSetChanged() }
            }
            view.setOnClickListener {
                val intent = Intent(context, GoodsDetailsActivity::class.java)
                intent.putExtra("id", data.gid)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                context.startActivity(intent)
            }
            return view
        }

        override fun getItem(position: Int): Any = list[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getCount(): Int = list.size

        private class ViewHolder(view: View) {
            @ViewInject(R.id.checkbox_check)
            var check: CheckBox? = null
            @ViewInject(R.id.iv_goods_logo)
            var ivLogo: ImageView? = null
            @ViewInject(R.id.tv_goods_title)
            var tvTitle: TextView? = null
            @ViewInject(R.id.btn_close)
            var btnClose: Button? = null
            @ViewInject(R.id.tv_goodsDescription)
            var tvDescription: TextView? = null
            @ViewInject(R.id.tv_goods_price)
            var tvPrice: TextView? = null
            @ViewInject(R.id.tv_goods_unit)
            var tvUnit: TextView? = null
            @ViewInject(R.id.car_countBox)
            var countBox: CountBox? = null

            init {
                x.view().inject(this, view)
                AutoUtils.autoSize(view)
                countBox?.maxSize = 100
                countBox?.minSize = 1
                countBox?.count = 1

            }
        }

        private interface OnTotalChange {
            fun onTotalChange(total: Float)
        }

        fun setTotalChangeListener(onTotalChange: (total: Float) -> Unit): GoodsAdapter {
            onTotalChangeListener = object : OnTotalChange {
                override fun onTotalChange(total: Float) {
                    onTotalChange(total)
                }
            }
            return this
        }
    }
}
