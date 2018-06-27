package com.fanhong.cn.service_page.government

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import kotlinx.android.synthetic.main.activity_personal_message.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x

class PersonalMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_message)
        tv_title.text = "个人信息"
        img_back.setOnClickListener { finish() }
        val param = RequestParams(App.CMD)
        param.addBodyParameter("cmd","109")
        param.addBodyParameter("uid",getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE).getString(App.PrefNames.USERID,"-1"))
        x.http().post(param,object :Callback.CommonCallback<String>{
            override fun onSuccess(result: String) {
                if (JsonSyncUtils.getJsonValue(result,"cmd")=="110"){
                    val data = JsonSyncUtils.getJsonValue(result, "data")
                    val nameStr = JsonSyncUtils.getJsonValue(data, "name")
                    val sexStr = JsonSyncUtils.getJsonValue(data, "sex")
                    val nationStr = JsonSyncUtils.getJsonValue(data, "nation")
                    val ageStr = JsonSyncUtils.getJsonValue(data, "age")
                    val birthdayStr = JsonSyncUtils.getJsonValue(data, "birthday")
                    val idcardStr = JsonSyncUtils.getJsonValue(data, "idcard")
                    val educationStr = JsonSyncUtils.getJsonValue(data, "education")
                    val jointimeStr = JsonSyncUtils.getJsonValue(data, "jointime")
                    val positivetimeStr = JsonSyncUtils.getJsonValue(data, "turnovertime")
                    val addressStr = JsonSyncUtils.getJsonValue(data, "address")
                    val phoneStr = JsonSyncUtils.getJsonValue(data, "tel")
                    val positionStr = JsonSyncUtils.getJsonValue(data, "post")
                    val whichbranchStr = JsonSyncUtils.getJsonValue(data, "zhibu")
                    runOnUiThread {
                        party_user_name.text = nameStr
                        party_user_sex.text = sexStr
                        party_user_nationality.text = nationStr
                        party_user_age.text = ageStr
                        party_user_birthday.text = birthdayStr
                        party_user_idcardnumber.text = idcardStr
                        party_user_education.text = educationStr
                        party_user_jointime.text = jointimeStr
                        party_user_positivetime.text = positivetimeStr
                        party_user_address.text = addressStr
                        party_user_phonenumber.text = phoneStr
                        party_user_position.text = positionStr
                        party_user_whichbranch.text = whichbranchStr
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
}
