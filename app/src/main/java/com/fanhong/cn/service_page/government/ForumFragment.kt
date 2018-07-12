package com.fanhong.cn.service_page.government

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import com.zhy.autolayout.AutoLinearLayout
import com.zhy.autolayout.utils.AutoUtils
import kotlinx.android.synthetic.main.fragment_party_forum.*
import org.json.JSONException
import org.json.JSONObject
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.image.ImageOptions
import org.xutils.view.annotation.ViewInject
import org.xutils.x
import java.util.*

/**
 * Created by Administrator on 2018/2/28.
 */
class ForumFragment : Fragment() {
    var list: MutableList<LtItemModel> = ArrayList()
    private var adapter: LtAdapter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_party_forum, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lt_get_more.visibility = View.VISIBLE
        adapter = LtAdapter(activity!!, list)
        lt_recyclerview.adapter = adapter
        lt_recyclerview.layoutManager = LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)
val pref = activity!!.getSharedPreferences(App.PREFERENCES_NAME,Context.MODE_PRIVATE)
        lt_get_more.setOnClickListener {
            val param = RequestParams(App.CMD)
            param.addBodyParameter("cmd", "117")
            x.http().post(param, object : Callback.CommonCallback<String> {
                override fun onSuccess(result: String) {
                    if (JsonSyncUtils.getJsonValue(result, "cmd") == "118") {
                        try {
                            val jsonArray = JSONObject(result).getJSONArray("data")
                            if (jsonArray.length() <= 3) {
                                ToastUtil.showToastS("没有更多了")
                                return
                            }
                            list.clear()
                            (0 until jsonArray.length())
                                    .map { jsonArray.optJSONObject(it) }
                                    .mapTo(list) {
                                        val photo = it.optString("logo", "")
                                        val name = it.optString("name", "")
                                        val content = it.optString("content", "")
                                        LtItemModel(photo, name, content)
                                    }
                            handler.sendEmptyMessage(3)
                            handler.sendEmptyMessage(4)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
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
        lt_submit.setOnClickListener {
            if (TextUtils.isEmpty(lt_edit.text.toString().trim({ it <= ' ' }))) {
                Toast.makeText(activity!!, "请输入发表内容！", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val param = RequestParams(App.CMD)
            param.addBodyParameter("cmd","113")
            param.addBodyParameter("uid",pref.getString(App.PrefNames.USERID,"-1"))
            param.addBodyParameter("content",lt_edit.text.toString())
            x.http().post(param,object :Callback.CommonCallback<String>{
                override fun onSuccess(result: String) {
                    if (JsonSyncUtils.getJsonValue(result, "cw") == "0") {
                        ToastUtil.showToastS("发表成功！")
                        lt_edit.setText("")
                        if (isSoftShowing()){
                            hideSoftinputyer(lt_submit)
                        }
                        getDatas()
                    }else ToastUtil.showToastS("发表失败！")
                }

                override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                }

                override fun onCancelled(cex: Callback.CancelledException?) {
                }

                override fun onFinished() {
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        getDatas()
    }

    private fun getDatas() {
        list.clear()
        val param = RequestParams(App.CMD)
        param.addBodyParameter("cmd", "115")
        x.http().post(param, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                if (JsonSyncUtils.getJsonValue(result, "cmd") == "116") {
                    try {
                        val jsonArray = JSONObject(result).getJSONArray("data")
                        if (jsonArray.length() > 0) {
                            handler.sendEmptyMessage(2)
                            (0 until jsonArray.length())
                                    .map { jsonArray.optJSONObject(it) }
                                    .mapTo(list) {
                                        val photo = it.optString("logo", "")
                                        val name = it.optString("name", "")
                                        val content = it.optString("content", "")
                                        LtItemModel(photo, name, content)
                                    }
                            handler.sendEmptyMessage(3)
                        } else
                            handler.sendEmptyMessage(1)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
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

    private val handler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            1 -> {
                //没有数据时
                lt_layout.visibility = View.GONE
                lt_empty.visibility = View.VISIBLE
            }
            2 -> {
                lt_layout.visibility = View.VISIBLE
                lt_empty.visibility = View.GONE
            }
            3 -> adapter?.notifyDataSetChanged()
            4 -> if (list.size > 3) {
                lt_get_more.visibility = View.GONE
            } else {
                lt_get_more.visibility = View.VISIBLE
            }
        }
        true
    })

    //隐藏软键盘的方法
    private fun hideSoftinputyer(view: View) {
        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun isSoftShowing(): Boolean {
        //获取当前屏幕内容的高度
        val screenHeight = activity!!.window.decorView.height
        //获取View可见区域的bottom
        val rect = Rect()
        activity!!.window.decorView.getWindowVisibleDisplayFrame(rect)

        return screenHeight - rect.bottom != 0
    }

    data class LtItemModel(val photo: String, val name: String, val content: String)
    private class LtAdapter(private val context: Context, val list: List<LtItemModel>) : RecyclerView.Adapter<LtAdapter.MyViewHolder>() {
        private val inflater: LayoutInflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = inflater.inflate(R.layout.item_lt, parent, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val options1 = ImageOptions.Builder().setLoadingDrawableId(R.mipmap.pictureloading)
                    .setFailureDrawableId(R.mipmap.picturefailedloading).setCircular(true).setUseMemCache(true).build()
            val model = list[position]
            if (TextUtils.isEmpty(model.photo)) {
                holder.photo!!.setImageResource(R.mipmap.default_photo)
            } else
                x.image().bind(holder.photo, model.photo, options1)
            holder.name!!.text = model.name
            holder.content!!.text = model.content
        }

        override fun getItemCount(): Int = if (list.isNotEmpty()) list.size else 0

        internal class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            @ViewInject(R.id.item_layout)
            var layout: AutoLinearLayout? = null
            @ViewInject(R.id.lt_photo)
            var photo: ImageView? = null
            @ViewInject(R.id.lt_name)
            var name: TextView? = null
            @ViewInject(R.id.lt_content)
            var content: TextView? = null


            init {
                x.view().inject(this, itemView)
                AutoUtils.autoSize(itemView)
            }
        }
    }
}

