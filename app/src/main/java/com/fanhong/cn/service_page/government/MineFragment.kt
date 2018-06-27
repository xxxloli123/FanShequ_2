package com.fanhong.cn.service_page.government

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import kotlinx.android.synthetic.main.fragment_party_mine.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.image.ImageOptions
import org.xutils.x

/**
 * Created by Administrator on 2018/2/28.
 */
class MineFragment : Fragment() {
    var pref: SharedPreferences? = null
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_party_mine, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pref = activity.getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val head = pref?.getString(App.PrefNames.HEADIMG, "")
        val option = ImageOptions.Builder().setIgnoreGif(false).setCircular(true).setFailureDrawableId(R.mipmap.default_photo).build()
        x.image().bind(user_picture, head, option)
        user_name.text = pref?.getString(App.PrefNames.NICKNAME, "") ?: ""
        user_phone.text = pref?.getString(App.PrefNames.USERNAME, "") ?: ""
        getScore(pref?.getString(App.PrefNames.USERID, "-1") ?: "-1")

        user_message.setOnClickListener { startActivity(Intent(activity, PersonalMessageActivity::class.java)) }
//        party_person_info.setOnClickListener { startActivity(Intent(activity, GovMemberInfoActivity::class.java)) }//已隐藏
        dues_message.setOnClickListener { startActivity(Intent(activity, GovDuesActivity::class.java)) }
    }

    private fun getScore(uid: String) {
        val param = RequestParams(App.CMD)
        param.addBodyParameter("cmd", "105")
        param.addBodyParameter("uid", uid)
        x.http().post(param, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                if (JsonSyncUtils.getJsonValue(result, "cmd") == "106") {
                    val score = JsonSyncUtils.getJsonValue(result, "fen")
                    tv_score.text = score
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
}