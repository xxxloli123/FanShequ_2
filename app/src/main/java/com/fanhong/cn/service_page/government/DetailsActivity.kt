package com.fanhong.cn.service_page.government

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import com.zhy.autolayout.utils.AutoUtils
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.activity_top.*
import org.json.JSONArray
import org.json.JSONObject
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.image.ImageOptions
import org.xutils.view.annotation.ViewInject
import org.xutils.x

/**
 * Created by Administrator on 2018/3/15.
 */
class DetailsActivity : AppCompatActivity() {

    private var shareid: Int = 0
    private val list: MutableList<CommentModel> = ArrayList()
    private var adapter: CommentAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        initViews()
    }

    private fun initViews() {
        tv_title.text = "内容详情"
        img_back.setOnClickListener { finish() }
        val intent = intent
        detail_content.text = intent.getStringExtra("content")
        shareid = intent.getIntExtra("id", 0)

        val url = intent.getStringExtra("url")
        if (TextUtils.isEmpty(url)) {
            img_fx.visibility = View.GONE
        } else {
            val options = ImageOptions.Builder().setLoadingDrawableId(R.mipmap.pictureloading)
                    .setFailureDrawableId(R.mipmap.picturefailedloading).setUseMemCache(true).build()
            x.image().loadDrawable(url, options, object : Callback.CommonCallback<Drawable> {
                override fun onSuccess(drawable: Drawable) {
                    //获取屏幕宽度
                    val wm = this@DetailsActivity.windowManager
                    val screenWidth = wm.defaultDisplay.width

                    val MaxHeight = screenWidth * 10
                    val picheight = (screenWidth.toFloat() / drawable.minimumWidth * drawable.minimumHeight).toInt()
                    img_fx.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, picheight)
                    img_fx.maxHeight = MaxHeight
                    img_fx.setImageDrawable(drawable)

                }

                override fun onError(ex: Throwable, isOnCallback: Boolean) {

                }

                override fun onCancelled(cex: Callback.CancelledException) {

                }

                override fun onFinished() {

                }
            })
        }

        adapter = CommentAdapter(this,list)
        detail_list.adapter = adapter

        detail_publish.setOnClickListener {
            val str = detail_edit.text.toString()
            if (TextUtils.isEmpty(str)) {
                ToastUtil.showToastL("评论内容不能为空")
            } else {
                addData(str)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    private fun getData() {
        list.clear()
        val param = RequestParams(App.CMD)
        param.addBodyParameter("cmd", "121")
        param.addBodyParameter("shareid", "$shareid")
        x.http().post(param, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                if (JsonSyncUtils.getJsonValue(result, "cmd") == "122") {
                    val arry = JSONObject(result).getJSONArray("data")
                    (0 until arry.length())
                            .map { arry.optJSONObject(it) }
                            .mapTo(list) {
                                val name = it.optString("name", "")
                                val photo = it.optString("logo", "")
                                val time = it.optString("times", "")
                                val content = it.optString("comment", "")
                                CommentModel(photo, name, time, content)
                            }

                    runOnUiThread { adapter?.notifyDataSetChanged() }
                }
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onFinished() {
            }
        })
    }

    private fun addData(str: String) {
        val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "119")
        params.addBodyParameter("shareid", shareid.toString() + "")
        params.addBodyParameter("uid", pref.getString(App.PrefNames.USERID, "-1"))
        params.addBodyParameter("comment", str)
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                if (JsonSyncUtils.getJsonValue(result, "cw") == "0") {
                    ToastUtil.showToastS("评论发表成功")
                    runOnUiThread {
                        detail_edit.setText("")
                        getData()
                    }
                }
                else ToastUtil.showToastS("评论发表失败")
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onFinished() {
            }
        })
    }

    internal data class CommentModel(val photo: String, val name: String, val time: String, val content: String)
    internal class CommentAdapter(private val context: Context, private val list: List<CommentModel>) : BaseAdapter() {
        private val inflater: LayoutInflater = LayoutInflater.from(context)

        override fun getCount(): Int {
            return if (list.isNotEmpty()) list.size else 0
        }

        override fun getItem(position: Int): Any {
            return list[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val viewHolder: ViewHolder
            val view = if (convertView == null) {
                val view = inflater.inflate(R.layout.item_party_detail, parent, false)
                viewHolder = ViewHolder(view)
                view.tag = viewHolder
                view
            } else {
                viewHolder = convertView.tag as ViewHolder
                convertView
            }
            val options1 = ImageOptions.Builder().setLoadingDrawableId(R.mipmap.pictureloading)
                    .setFailureDrawableId(R.mipmap.picturefailedloading).setCircular(true).setUseMemCache(true).build()
            val model = list[position]
            if (TextUtils.isEmpty(model.photo)) {
                viewHolder.photo!!.setImageResource(R.mipmap.default_photo)
            } else
                x.image().bind(viewHolder.photo, model.photo, options1)
            viewHolder.name!!.text = model.name
            viewHolder.time!!.text = model.time
            viewHolder.content!!.text = model.content
            return view
        }

        internal inner class ViewHolder(view: View) {
            @ViewInject(R.id.photo)
            var photo: ImageView? = null
            @ViewInject(R.id.name)
            var name: TextView? = null
            @ViewInject(R.id.time)
            var time: TextView? = null
            @ViewInject(R.id.content)
            var content: TextView? = null

            init {
                x.view().inject(this, view)
                AutoUtils.autoSize(view)
            }
        }
    }
}