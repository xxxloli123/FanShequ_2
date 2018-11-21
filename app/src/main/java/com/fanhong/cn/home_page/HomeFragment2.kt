package com.fanhong.cn.home_page

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fanhong.cn.App
import com.fanhong.cn.HomeActivity
import com.fanhong.cn.R
import com.fanhong.cn.home_page.adapters.ActivitiesAdapter
import com.fanhong.cn.home_page.models.Banner
import com.fanhong.cn.home_page.adapters.GlideImageLoader
import com.fanhong.cn.home_page.fenxiao.HaveJoinedActivity
import com.fanhong.cn.home_page.fenxiao.ZSIntroductionActivity
import com.fanhong.cn.http.callback.StringDialogCallback
import com.fanhong.cn.login_pages.LoginActivity
import com.fanhong.cn.service_page.express.ExpressHomeActivity
import com.fanhong.cn.service_page.questionnaire.QuestionnaireActivity
import com.fanhong.cn.service_page.questionnaire.QuestionnaireFActivity
import com.fanhong.cn.service_page.repair.FillOrderActivity
import com.fanhong.cn.service_page.shop.CommunityMallActivity
import com.fanhong.cn.service_page.usedshop.UsedShopActivity
import com.fanhong.cn.service_page.verification.VerificationActivity
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.LogUtil
import com.fanhong.cn.tools.ToastUtil
import com.fanhong.cn.view.headerScrollView.HeaderScrollHelper
import com.google.gson.Gson
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.youth.banner.BannerConfig
import kotlinx.android.synthetic.main.fragment_home2.*
import me.leefeng.promptlibrary.PromptButton
import me.leefeng.promptlibrary.PromptDialog
import org.json.JSONException
import org.json.JSONObject
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x
import kotlin.collections.ArrayList

class HomeFragment2 : Fragment(),View.OnClickListener,ActivitiesAdapter.Callback, HeaderScrollHelper.ScrollableContainer {
    override fun getScrollableView(): View {
        return rv_activities_list
    }

    var count=0

    lateinit var pref: SharedPreferences
    private var bannerIMGs= ArrayList<Banner>()
    private var banners= ArrayList<Banner>()
    private  var adapter: ActivitiesAdapter? = null

    private fun check() {
        OkGo.post<String>(App.CMD)
                .tag(this)//
                .isMultipart(true)
                //        1067 查询是否评价物业（app->平台）
                //        cmd:数据类型
                //        uid：当前用户ID
//                        qid:小区id
                .params("cmd", "1067")
                .params("uid", pref!!.getString(App.PrefNames.USERID, "-1"))
                .params("qid", pref!!.getString(App.PrefNames.GARDENID, ""))
                .execute(object : StringDialogCallback(this.activity!!) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGo 1067", response.body().toString())
                        if (response.body().toString().contains("\"state\":\"200\"")) {
                            startActivity(Intent(activity,QuestionnaireActivity::class.java))
                        } else startActivity(Intent(activity,QuestionnaireFActivity::class.java))
                    }

                    override fun onError(response: Response<String>) {
                        Log.e("OkGoError", response.exception.toString())
                    }
                })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_position -> {
                startActivityForResult(Intent(activity, ChooseCellActivity::class.java), 110)
            }
            R.id.all_repair -> {
                if (isLogged()) startActivity(Intent(activity, FillOrderActivity::class.java))
                else notLogin()
            }
            R.id.all_questionnaire -> {
                if (isLogged()){
                    if (tv_position.text.toString() != "选择小区" &&
                            !tv_position.text.toString().isEmpty()){
                        check()
                    }else ToastUtil.showToastL("你还没有选择小区")
                }
                else notLogin()
            }
            R.id.all_repair_owner -> {
                if (isLogged()) {
                    if (tv_position.text.toString() != "选择小区" &&
                            !tv_position.text.toString().isEmpty()){
                        val intent = Intent(activity, FillOrderActivity::class.java)
                        intent.putExtra("owner",666)
                        startActivity(intent)
                    }else ToastUtil.showToastL("你还没有选择小区")
                } else notLogin()
            }
            R.id.all_community_mall -> {
                startActivity(Intent(activity, CommunityMallActivity::class.java))
            }
            R.id.all_old_goods -> {
                startActivity(Intent(activity, CommunityMallActivity::class.java))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            51 -> { //选择小区的回调
                val gardenName = data!!.getStringExtra("gardenName")
                tv_position.text = gardenName
                pref = activity!!.getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
            }
        }
    }

    private fun notLogin() {
        when(count){
            0->{
                PromptDialog(activity).showWarnAlert("你还没有登录？\n是否立即登录",
                        PromptButton("取消",null),
                        PromptButton("确认"){
                            startActivityForResult(Intent(activity, LoginActivity::class.java),HomeActivity.ACTION_LOGIN)})
            }
            2->{
                count=-1
                ToastUtil.showToastL("你还没有登录请登录")
            }
            else->ToastUtil.showToastL("你还没有登录请登录")
        }
        count++
    }

    private fun isLogged(): Boolean {
        return pref.getString(App.PrefNames.USERID, "-1") != "-1"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pref = activity!!.getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        tv_position.text=pref.getString(App.PrefNames.GARDENNAME, "选择小区")
        all_repair.setOnClickListener(this)
        all_repair_owner.setOnClickListener(this)
        tv_position.setOnClickListener(this)
        all_community_mall.setOnClickListener(this)
        all_questionnaire.setOnClickListener(this)
        all_old_goods.setOnClickListener{
            startActivity(Intent(activity,UsedShopActivity::class.java))
        }
        all_investment.setOnClickListener { if (isLogged()) {
            val uid = pref.getString(App.PrefNames.USERID, "-1")
            checkJoined(uid)
        } else {
            startActivity(Intent(activity, ZSIntroductionActivity::class.java))
        } }
        loadData()
        all_fillStatusBar.setPadding(0,getStatusBar(),0,0)

        all_express_delivery.setOnClickListener {
            startActivity(Intent(activity, ExpressHomeActivity::class.java))
        }
        all_year_examine.setOnClickListener {
            startActivity(Intent(activity, VerificationActivity::class.java))
        }
    }

    private fun test() {
        val banner=Banner()
        banner.sj="200184654"
        banner.title="dsafa"
        banners.add(banner)
        banners.add(banner)
        banners.add(banner)
        showActivities()
    }

    /**
     * 获取状态栏高度
     * @return
     */
    fun  getStatusBar(): Int {
        /**
         * 获取状态栏高度
         */
        var statusBarHeight1 = -1
        //获取status_bar_height资源的ID
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight1
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

    private fun loadData() {
//        1057：广告(app->平台）
//        cmd：数据类型
        if (bannerIMGs.isEmpty()||banners.isEmpty())OkGo.post<String>(App.CMD)
                .tag(this)//
                .params("cmd", "1057")
                .execute(object : StringDialogCallback(activity!!) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGo body",response.body().toString())
                        try {
                            val json = JSONObject(response.body()!!.toString())
                            if (json.getString("cw") == "0"){
                                val arr = json.getJSONArray("data")
                                if (arr.length() == 0) return
                                for (i in 0 until arr.length()) {
                                    val ba=Gson().fromJson(arr.getString(i), Banner::class.java)
                                    if (ba.lx=="0"&&bannerIMGs.isEmpty())bannerIMGs.add(ba)
                                    if (ba.lx=="1"&&banners.isEmpty()) banners.add(ba)
                                }
                                if (!banners.isEmpty())showActivities()
                                initView()
                            }
                        } catch (e: JSONException) {
                            LogUtil.e("JSONException",e.toString())
                            ToastUtil.showToastL("数据解析异常")
                            e.printStackTrace()
                        }
                    }
                    override fun onError(response: Response<String>) {
                        Log.e("OkGoError",response.exception.toString())
                        initView()
                    }
                })
    }

    private fun initView() {
        val titles = ArrayList<String>()
        val img = ArrayList<String>()
        for (b in bannerIMGs){
            titles.add(b.title)
            img.add(b.tupian)
        }

        if (titles.isEmpty()&&img.isEmpty()){
            img.add("R.mipmap.banner")
            titles.add("人脸识别，互联网时代的AI革命")
        }
        bn_banner.setImages(img)
                .setBannerTitles(titles)
                .setImageLoader(GlideImageLoader())
                .start()
        bn_banner.updateBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE)
        bn_banner.setOnClickListener {
            startActivity(Intent(activity, BannerInActivity::class.java))
        }
        mv_announcement.setContent("因自来水公司需要清洁自来水管，所以本周一将会停水一天")
    }

    private fun showActivities() {
        if (adapter==null){
            adapter= ActivitiesAdapter(banners,this)
            rv_activities_list.layoutManager=LinearLayoutManager(activity,
                    LinearLayoutManager.VERTICAL,false)
            rv_activities_list.adapter=adapter
        }
        else {
            adapter!!.refresh(banners)
            adapter!!.notifyDataSetChanged()
        }
    }

    override fun click(v: View) {

    }
}
