package com.fanhong.cn.service_page.government

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import cn.finalteam.galleryfinal.FunctionConfig
import cn.finalteam.galleryfinal.GalleryFinal
import cn.finalteam.galleryfinal.model.PhotoInfo
import com.fanhong.cn.App
import com.fanhong.cn.BuildConfig
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import com.fanhong.cn.user_page.AccountSetsActivity
import kotlinx.android.synthetic.main.activity_add_fx.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x
import java.io.File

/**
 * Created by Administrator on 2018/3/15.
 */
class AddFxActivity : AppCompatActivity() {

    private var url1: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_fx)
        val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        tv_title.text = "分享"
        img_back.setOnClickListener { finish() }
        top_extra.text = "发布"
        top_extra.visibility = View.VISIBLE
        top_extra.setOnClickListener {
            val params = RequestParams(App.CMD)
            if (!TextUtils.isEmpty(url1)) {
                params.addBodyParameter("image", File(url1))
            }
            params.addBodyParameter("cmd", "95")
            params.addBodyParameter("uid", pref.getString(App.PrefNames.USERID, "-1"))
            params.addBodyParameter("times", System.currentTimeMillis().toString())
            params.addBodyParameter("content", add_fx_edit.text.toString())
            params.isMultipart = true
            x.http().post(params, object : Callback.CommonCallback<String> {
                override fun onSuccess(result: String) {
                    if (JsonSyncUtils.getJsonValue(result, "cw").equals("0")) {
                        ToastUtil.showToastS("分享成功！")
                        //  回调中清空
                        pref.edit().putString(App.PrefNames.SHAREDRAFT, "").apply()
                        this@AddFxActivity.finish()
                    } else {
                        ToastUtil.showToastS("分享失败！")
                    }
                }

                override fun onError(ex: Throwable, isOnCallback: Boolean) {

                }

                override fun onCancelled(cex: Callback.CancelledException) {

                }

                override fun onFinished() {

                }
            })
        }
        add_fx_edit.setText(pref.getString(App.PrefNames.SHAREDRAFT, ""))
        add_fx_edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pref.edit().putString(App.PrefNames.SHAREDRAFT, s.toString()).apply()
            }
        })
        add_pic1.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                val checkCamera = ContextCompat.checkSelfPermission(this@AddFxActivity, Manifest.permission.CAMERA)
                val checkSD = ContextCompat.checkSelfPermission(this@AddFxActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (checkCamera != PackageManager.PERMISSION_GRANTED || checkSD != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 112)
                    return@setOnClickListener
                }
            }
            choosePhoto()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 112){
                choosePhoto()
        }
    }

    private fun choosePhoto(){
        val cfg = FunctionConfig.Builder().setEnableCamera(true).build()
        GalleryFinal.openGallerySingle(112, cfg, object : GalleryFinal.OnHanlderResultCallback {
            override fun onHanlderSuccess(reqeustCode: Int, resultList: MutableList<PhotoInfo>?) {
                url1 = resultList?.get(0)?.photoPath
                add_pic1.setImageURI(Uri.fromFile(File(url1)))
            }

            override fun onHanlderFailure(requestCode: Int, errorMsg: String?) {
            }
        })
    }
}