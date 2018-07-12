package com.fanhong.cn.service_page.government

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.service_page.government.subsidiary.SharedAdapter
import com.fanhong.cn.tools.JsonSyncUtils
import kotlinx.android.synthetic.main.fragment_party_shared.*
import org.json.JSONException
import org.json.JSONObject
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Administrator on 2018/2/28.
 */
class SharedFragment : Fragment() {
    private val list = ArrayList<FxItemModel>()
    private var adapter: SharedAdapter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_party_shared, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SharedAdapter(activity!!, list)
        adapter?.setItemClick { id, content, imgUrl ->
            val intent = Intent(activity!!, DetailsActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("content", content)
            intent.putExtra("url", imgUrl)
            startActivity(intent)
        }
        fx_recyclerview.adapter = adapter
        fx_recyclerview.layoutManager = LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)
        refresh_fx.setOnRefreshListener {
            getDate()

        }

        add_fx.setOnClickListener { startActivity(Intent(activity!!, AddFxActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        getDate()
    }

    private fun getDate() {
        list.clear()
        val params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "97")
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                try {
                    val jsonArray = JSONObject(result).getJSONArray("data")
                    (0 until jsonArray.length())
                            .map { jsonArray.optJSONObject(it) }
                            .mapTo(list) {
                                val content = it.optString("content", "")
                                val author = it.optString("name", "")
                                val time = SimpleDateFormat("yyyy-MM-dd").format(Date(it.optLong("times", 0L)))
                                val photoUrl = it.optString("logo", "")
                                val picUrl = it.optString("image", "")
                                val id = it.optInt("id", -1)
                                FxItemModel(content, author, time, photoUrl, picUrl, id)
                            }
                    activity!!.runOnUiThread { adapter?.notifyDataSetChanged() }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            override fun onError(ex: Throwable, isOnCallback: Boolean) {

            }

            override fun onCancelled(cex: Callback.CancelledException) {

            }

            override fun onFinished() {
                refresh_fx.isRefreshing = false
            }
        })
    }

    data class FxItemModel(var content: String, var author: String, var time: String, var photoUrl: String, var picUrl: String, var id: Int)
}