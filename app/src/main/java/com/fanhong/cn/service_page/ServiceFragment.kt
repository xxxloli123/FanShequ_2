package com.fanhong.cn.home_page


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.OrientationHelper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fanhong.cn.AgentwebActivity
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.home_page.fenxiao.HaveJoinedActivity
import com.fanhong.cn.home_page.fenxiao.ZSIntroductionActivity
import com.fanhong.cn.login_pages.LoginActivity
import com.fanhong.cn.service_page.MyServiceAdapter
import com.fanhong.cn.service_page.government.GovernMainActivity
import com.fanhong.cn.service_page.repair.RepairActivity
import com.fanhong.cn.service_page.shop.ShopIndexActivity
import com.fanhong.cn.service_page.usedshop.UsedShopActivity
import com.fanhong.cn.service_page.verification.VerificationActivity
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import kotlinx.android.synthetic.main.activity_top.*
import kotlinx.android.synthetic.main.fragment_service.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x


/**
 * A simple [Fragment] subclass.
 */
class ServiceFragment : Fragment() {
    companion object {
        //定义数组来存放按钮图片
        private val mImageViewArray1 = intArrayOf(R.drawable.service_store, R.drawable.service_es,
                R.drawable.service_dai, R.drawable.service_distribution,
                R.drawable.service_fix,
                R.drawable.service_dang_1)

        //定义数组文字
        private val mTextviewArray1 = intArrayOf(R.string.service_store, R.string.service_es,
                R.string.service_dai, R.string.service_zsdl,
                R.string.service_fix,
                R.string.service_dang)

        private val mImageViewArray2 = intArrayOf(R.drawable.service_mt, R.drawable.service_dz,
                R.drawable.service_tb, R.drawable.service_jds, R.drawable.service_wph,
                R.drawable.service_yms, R.drawable.service_xc, R.drawable.service_qne,
                R.drawable.service_tn, R.drawable.service_tc)

        private val mTextviewArray2 = intArrayOf(R.string.service_mt, R.string.service_dz,
                R.string.service_tb, R.string.service_jds, R.string.service_wph, R.string.service_yms,
                R.string.service_xc, R.string.service_qne, R.string.service_tn, R.string.service_tc)

        private val mUrlArray2 = intArrayOf(R.string.url_meituan, R.string.url_dianping, R.string.url_taobao,
                R.string.url_jingdong, R.string.url_weiping, R.string.url_yamaxun, R.string.url_xiecheng,
                R.string.url_quna, R.string.url_tuniu, R.string.url_tongcheng)

        private val mImageViewArray3 = intArrayOf(R.drawable.service_hj, R.drawable.service_funhos,
                R.drawable.service_gwy, R.drawable.service_zc, R.drawable.service_my)

        private val mTextviewArray3 = intArrayOf(R.string.service_hj, R.string.service_funhos,
                R.string.service_gwy, R.string.service_zc, R.string.service_my)

        private val mUrlArray3 = intArrayOf(R.string.url_huzhang, R.string.url_quyy,
                R.string.url_gongwuyuan, R.string.url_zhichen, R.string.url_muying)
    }

    private var adapter: MyServiceAdapter? = null
    private var mSharedPref: SharedPreferences? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_service, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mSharedPref = activity!!.getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        img_back.visibility = View.GONE
        tv_title.text = "社区服务"
        initViews()
        super.onViewCreated(view, savedInstanceState)
    }

    fun initViews() {
        adapter = MyServiceAdapter(activity!!, mImageViewArray1, mTextviewArray1)
        adapter!!.setItemClick(object : MyServiceAdapter.ItemClick {
            override fun itemclick(position: Int) {
                //便民服务的点击事件
                convenientService(position)
            }

        })
        recycle1.adapter = adapter
        recycle1.layoutManager = GridLayoutManager(activity!!, 4, OrientationHelper.VERTICAL, false)

        adapter = MyServiceAdapter(activity!!, mImageViewArray2, mTextviewArray2)
        adapter!!.setItemClick(object : MyServiceAdapter.ItemClick {
            override fun itemclick(position: Int) {
                //衣食住行的点击事件
                var url = activity!!.getString(mUrlArray2[position])
                var title = activity!!.getString(mTextviewArray2[position])
                var intent = Intent(activity!!, AgentwebActivity::class.java)
                intent.putExtra("url", url)
                intent.putExtra("title", title)
                startActivity(intent)
            }

        })
        recycle2.adapter = adapter
        recycle2.layoutManager = GridLayoutManager(activity!!, 4)

        adapter = MyServiceAdapter(activity!!, mImageViewArray3, mTextviewArray3)
        adapter!!.setItemClick(object : MyServiceAdapter.ItemClick {
            override fun itemclick(position: Int) {
                //教育医疗的点击事件
                var url = activity!!.getString(mUrlArray3[position])
                var title = activity!!.getString(mTextviewArray3[position])
                var intent = Intent(activity!!, AgentwebActivity::class.java)
                intent.putExtra("url", url)
                intent.putExtra("title", title)
                startActivity(intent)
            }

        })
        recycle3.adapter = adapter
        recycle3.layoutManager = GridLayoutManager(activity!!, 4)
    }

    private fun convenientService(position: Int) {
        when (position) {
            0 -> startActivity(Intent(activity!!, ShopIndexActivity::class.java))
            1 -> startActivity(Intent(activity!!, UsedShopActivity::class.java))
            2 -> {startActivity(Intent(activity!!,VerificationActivity::class.java))
            }
            3 -> {
                if (isLogined()) {
                    var uid = mSharedPref!!.getString(App.PrefNames.USERID, "-1")
                    checkJoined(uid)
                } else {
                    startActivity(Intent(activity!!, ZSIntroductionActivity::class.java))
                }
            }
            4 -> {
                if (isLogined()) {
                    startActivity(Intent(activity!!, RepairActivity::class.java))
                } else AlertDialog.Builder(activity!!).setTitle("你还没有登录哦").setMessage("是否立即登录？").setPositiveButton("确认") { _, _ ->
                    startActivity(Intent(activity!!, LoginActivity::class.java))
                }.setNegativeButton("取消", null).show()
            }
            5 -> {
                if (isLogined()) {
                    val param = RequestParams(App.CMD)
                    param.addBodyParameter("cmd", "93")
                    param.addBodyParameter("tel", mSharedPref!!.getString(App.PrefNames.USERNAME,""))
                    x.http().post(param, object : Callback.CommonCallback<String> {
                        override fun onSuccess(result: String) {
                            if (JsonSyncUtils.getJsonValue(result, "cw") == "0") {
                                startActivity(Intent(activity!!, GovernMainActivity::class.java))
                            } else ToastUtil.showToastL("暂无进入权限...")
                        }

                        override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                        }

                        override fun onCancelled(cex: Callback.CancelledException?) {
                        }

                        override fun onFinished() {
                        }
                    })
                } else AlertDialog.Builder(activity!!).setTitle("你还没有登录哦").setMessage("是否立即登录？").setPositiveButton("确认") { _, _ ->
                    startActivity(Intent(activity!!, LoginActivity::class.java))
                }.setNegativeButton("取消", null).show()
            }
        }
    }

    private fun isLogined(): Boolean {
        return mSharedPref!!.getString(App.PrefNames.USERID, "-1") != "-1"
    }

    private fun choosedCell(): Boolean {
        return !TextUtils.isEmpty(mSharedPref!!.getString(App.PrefNames.GARDENNAME, ""))
    }

    private fun checkJoined(uid: String) {
        var params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "69")
        params.addBodyParameter("uid", uid)
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onFinished() {
            }

            override fun onSuccess(result: String?) {
                var data = JsonSyncUtils.getJsonValue(result!!, "data").toInt()
                when (data) {
                    0 -> startActivity(Intent(activity!!, ZSIntroductionActivity::class.java))
                    1 -> startActivity(Intent(activity!!, HaveJoinedActivity::class.java))
                    else -> ToastUtil.showToastS("登录状态异常")
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                ToastUtil.showToastS("登录状态异常")
            }

        })
    }
}
