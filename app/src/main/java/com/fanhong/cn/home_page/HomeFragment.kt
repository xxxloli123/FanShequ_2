package com.fanhong.cn.home_page


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.fanhong.cn.AgentwebActivity
import com.fanhong.cn.App
import com.fanhong.cn.HomeActivity
import com.fanhong.cn.R
import com.fanhong.cn.home_page.fenxiao.HaveJoinedActivity
import com.fanhong.cn.home_page.fenxiao.ZSIntroductionActivity
import com.fanhong.cn.login_pages.LoginActivity
import com.fanhong.cn.service_page.express.ExpressHomeActivity
import com.fanhong.cn.service_page.repair.FillOrderActivity
import com.fanhong.cn.service_page.repair.RepairActivity
import com.fanhong.cn.service_page.shop.ShopIndexActivity
import com.fanhong.cn.service_page.verification.VerificationActivity
import com.fanhong.cn.tools.DialogUtil
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import kotlinx.android.synthetic.main.fragment_home.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x


/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    private var mSharedPref: SharedPreferences? = null
    private var array: JSONArray? = null
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        initViews()
    }

    private fun initViews() {
        mSharedPref = activity.getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        get_location.setOnClickListener {
            startActivityForResult(Intent(activity, ChooseCellActivity::class.java), 110)
        }
        getNotification()
        home_banner.setOnClickListener {
            startActivity(Intent(activity, BannerInActivity::class.java))
        }
        tv_shequ_gonggao.setOnClickListener {
            if (isLogined()) {
                if (choosedCell()) {
                    (activity as HomeActivity).setRadioButtonChecked(3)
                } else {
                    DialogUtil.showDialog(activity, "chooseCell", 110)
                }
            } else {
                DialogUtil.showDialog(activity, "login", 100)
            }
        }
        tv_wuye_star.setOnClickListener {
            if (isLogined()) {
                if (choosedCell()) {
                    val i = Intent(activity, StarManagerActivity::class.java)
                    i.putExtra("id", mSharedPref!!.getString(App.PrefNames.GARDENID, ""))
                    i.putExtra("name", mSharedPref!!.getString(App.PrefNames.GARDENNAME, ""))
                    startActivity(i)
                } else {
                    DialogUtil.showDialog(activity, "chooseCell", HomeActivity.ACTION_CHOOSE_BY_COMMUNITY)
                }
            } else {
                DialogUtil.showDialog(activity, "login", HomeActivity.ACTION_LOGIN_BY_COMMUNITY)
            }
        }
        tv_zhaoshang_daili.setOnClickListener {
            if (isLogined()) {
                var uid = mSharedPref!!.getString(App.PrefNames.USERID, "-1")
                checkJoined(uid)
            } else {
                startActivity(Intent(activity, ZSIntroductionActivity::class.java))
            }
        }
        tv_store.setOnClickListener { startActivity(Intent(activity, ShopIndexActivity::class.java)) }
        tv_expressage.setOnClickListener { startActivity(Intent(activity, ExpressHomeActivity::class.java)) }
        tv_daiban.setOnClickListener { startActivity(Intent(activity, VerificationActivity::class.java)) }
        tv_repair.setOnClickListener {
            if (isLogined()) {
                startActivity(Intent(activity, FillOrderActivity::class.java))
            } else AlertDialog.Builder(activity).setTitle("你还没有登录哦").setMessage("是否立即登录？").setPositiveButton("确认", { _, _ ->
                startActivity(Intent(activity, LoginActivity::class.java))
            }).setNegativeButton("取消", null).show()
        }

        ylgh.setOnClickListener {
            val intent = Intent(activity, AgentwebActivity::class.java)
            intent.putExtra("ishome", true)
            intent.putExtra("url","https://m.quyiyuan.com")
            startActivity(intent)
        }
        xxyl.setOnClickListener {
            val intent = Intent(activity, AgentwebActivity::class.java)
            intent.putExtra("ishome", true)
            intent.putExtra("url", "http://i.meituan.com/chongqing?cid=2&stid_b=1&cateType=poi")
            startActivity(intent)
        }
        mydy.setOnClickListener {
            val intent = Intent(activity, AgentwebActivity::class.java)
            intent.putExtra("ishome", true)
            intent.putExtra("url", "https://m.maoyan.com")
            startActivity(intent)
        }
        hwwl.setOnClickListener {
            val intent = Intent(activity, AgentwebActivity::class.java)
            intent.putExtra("ishome", true)
            intent.putExtra("url","http://m.ctrip.com/webapp/attractions/index.html#!/index?from=http%3A%2F%2Fm.ctrip.com%2Fhtml5%2F")
            startActivity(intent)
        }
    }

    private fun getNotification() {
        var params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "43")
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onFinished() {
            }

            override fun onSuccess(result: String?) {
                try {
                    array = JSONObject(result).getJSONArray("data")
                    if (array!!.length() > 0) {
                        show_notify.text = array!![array!!.length() - 1].toString()
                        recycleShow()
                    }
                } catch (e: JSONException) {
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

        })
    }

    private fun recycleShow() {
        var alphaAnimation = AlphaAnimation(1.0f, 0.0f)
        alphaAnimation.startOffset = 2000  //
        alphaAnimation.duration = 1000
        var i = 0
        alphaAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
                show_notify.text = array!![i].toString()
                i++
                if (i > array!!.length() - 1) {
                    i = 0
                }
            }

            override fun onAnimationEnd(animation: Animation?) {
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })
        alphaAnimation.repeatCount = Animation.INFINITE
        show_notify.startAnimation(alphaAnimation)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            51 -> { //选择小区的回调
                val gardenName = data!!.getStringExtra("gardenName")
                show_cell_name.text = gardenName
            }
        }
    }

    override fun onResume() {
        super.onResume()
        show_cell_name.text = mSharedPref!!.getString(App.PrefNames.GARDENNAME, "")
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
                    0 -> startActivity(Intent(activity, ZSIntroductionActivity::class.java))
                    1 -> startActivity(Intent(activity, HaveJoinedActivity::class.java))
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
