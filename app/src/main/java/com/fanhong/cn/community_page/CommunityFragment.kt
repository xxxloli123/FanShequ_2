package com.fanhong.cn.home_page


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.community_page.ChatRoomActivity
import com.fanhong.cn.community_page.NewsDetailsActivity
import com.fanhong.cn.community_page.subsidiary.CommunityNewsAdapter
import com.fanhong.cn.community_page.subsidiary.CommunityNewsBean
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import kotlinx.android.synthetic.main.fragment_community.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x


/**
 * A simple [Fragment] subclass.
 */
class CommunityFragment : Fragment() {
    private var pref: SharedPreferences? = null
    private var anim: AnimationDrawable? = null

    private var listComm: MutableList<CommunityNewsBean> = ArrayList()
    private var listNews: MutableList<CommunityNewsBean> = ArrayList()
    private var adapterComm: CommunityNewsAdapter? = null
    private var adapterNews: CommunityNewsAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        pref = activity.getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        return inflater?.inflate(R.layout.fragment_community, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        anim = img_news_bar.drawable as AnimationDrawable

        btn_joinChat.setOnClickListener {
            //打开社区群聊
            startActivity(Intent(activity, ChatRoomActivity::class.java))
        }

        adapterComm = CommunityNewsAdapter(activity, listComm)
        adapterNews = CommunityNewsAdapter(activity, listNews)
        lv_community_news.adapter = adapterComm
        lv_nearby_news.adapter = adapterNews
        lv_community_news.setOnItemClickListener { _, _, position, _ ->
            val i = Intent(activity, NewsDetailsActivity::class.java)
            i.putExtra("id", listComm[position].id)
            startActivity(i)
        }
        lv_nearby_news.setOnItemClickListener { _, _, position, _ ->
            val i = Intent(activity, NewsDetailsActivity::class.java)
            i.putExtra("id", listNews[position].id)
            startActivity(i)
        }
    }

    override fun onResume() {
        super.onResume()
        top_extra.text = pref?.getString(App.PrefNames.GARDENNAME, "")
        top_extra.requestFocus()
        anim?.start()
        initNewsData()
    }

    private fun initNewsData() {
        progressBar_community.visibility = View.VISIBLE
        listComm.clear()
        listNews.clear()

        val param = RequestParams(App.CMD)
        val xid = pref?.getString(App.PrefNames.GARDENID, "-1")
        if (xid == "-1") {
            showFail()
            return
        }
        param.addBodyParameter("cmd", "47")
        param.addBodyParameter("xid", xid)
        param.connectTimeout = 3000
        x.http().post(param, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                if (JsonSyncUtils.getJsonValue(result, "cw") == "0") {
                    listComm = JsonSyncUtils.addNews(listComm, result, CommunityNewsBean.TYPE_INFORM)
                    listComm = JsonSyncUtils.addNews(listComm, result, CommunityNewsBean.TYPE_NOTICE)

                    listNews = JsonSyncUtils.addNews(listNews, result, CommunityNewsBean.TYPE_NEWS)
                    when {
                        listComm.size + listNews.size == 0 -> //两种新闻条数都为0
                            showFail()
                        listComm.size == 0 -> {//仅社区新闻条数为0
                            layout_comm_news.visibility = View.GONE
                            layout_nearby_news.visibility = View.VISIBLE
                            news_fail.visibility = View.GONE
                            progressBar_community.visibility = View.GONE
                            anim?.stop()
                        }
                        listNews.size == 0 -> {//仅附近新闻条数为0
                            layout_comm_news.visibility = View.VISIBLE
                            layout_nearby_news.visibility = View.GONE
                            news_fail.visibility = View.GONE
                            progressBar_community.visibility = View.GONE
                            anim?.stop()
                        }
                        else -> {//两种新闻条数都不为0
                            layout_comm_news.visibility = View.VISIBLE
                            layout_nearby_news.visibility = View.VISIBLE
                            news_fail.visibility = View.GONE
                            progressBar_community.visibility = View.GONE
                            anim?.stop()
                        }
                    }
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
                showFail()
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                showFail()
            }

            override fun onFinished() {
                activity.runOnUiThread {
                    adapterComm?.notifyDataSetChanged()
                    adapterNews?.notifyDataSetChanged()
                }
            }
        })
    }

    private fun showFail() {
        news_fail.visibility = View.VISIBLE
        progressBar_community.visibility = View.GONE
        anim?.stop()
    }
}
