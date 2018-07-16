package com.fanhong.cn.user_page

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.View
import com.bumptech.glide.Glide
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.http.callback.StringDialogCallback
import com.fanhong.cn.myviews.PhotoSelectWindow
import com.fanhong.cn.tools.*
import com.fanhong.cn.user_page.shippingaddress.MyAddressActivity
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import kotlinx.android.synthetic.main.activity_account_sets.*
import me.leefeng.promptlibrary.PromptDialog
import org.devio.takephoto.app.TakePhotoActivity
import org.devio.takephoto.compress.CompressConfig
import org.devio.takephoto.model.CropOptions
import org.devio.takephoto.model.TResult
import org.devio.takephoto.model.TakePhotoOptions
import org.json.JSONException
import org.json.JSONObject
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.image.ImageOptions
import org.xutils.x
import java.io.File
import java.io.IOException

class AccountSetsActivity : TakePhotoActivity() {
    companion object {
        private val REQUEST_TAKE_PHOTO = 11
        private val REQUEST_CHOOSE_PIC = 12
        private val TAKE_PHOTO = 21
        private val CHOOSE_PIC = 22
        private val CUT_PICTURE = 23
        private val SET_NICK = 24
    }

    private var file: File? = null
    private var cropFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_sets)
        img_back.setOnClickListener { finish() }

        initViews()
        file = File(Environment.getExternalStorageDirectory().toString() + "/FanShequ/headImage.jpg")
        if (!file!!.parentFile.exists()) {
            file!!.parentFile.mkdirs()
            try {
                file!!.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun initViews() {
        val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val number = pref.getString(App.PrefNames.USERNAME, "")
        val nick = pref.getString(App.PrefNames.NICKNAME, "")

        tv_phone.text = number
        tv_nick.text = nick
    }

    fun onHeadImg(v: View) {
        val popupWindow = PhotoSelectWindow(this)
        popupWindow.setOnTakePhoto(View.OnClickListener {
            popupWindow.dismiss()
            selectPhoto(true)
        }).setOnChoosePic(View.OnClickListener {
            popupWindow.dismiss()
            selectPhoto(false)
        })
        popupWindow.showAtLocation(layout_main, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
    }

    fun onNickName(v: View) {
        startActivityForResult(Intent(this, NickSetActivity::class.java), SET_NICK)
    }

    fun onAddress(v: View) {
        startActivity(Intent(this@AccountSetsActivity, MyAddressActivity::class.java))
    }

    fun onResetPwd(v: View) {
        startActivity(Intent(this, ResetPwdActivity::class.java))
    }

    private fun selectPhoto(b: Boolean) {
        val builder = CropOptions.Builder()
        //是否takePhoto剪裁
        builder.setWithOwnCrop(true)
        //压缩图片
        val config = CompressConfig.Builder().setMaxSize(233 * 1020)
                .setMaxPixel(0)
                .enableReserveRaw(false)
                .create()
        takePhoto.onEnableCompress(config, true)//true 是否显示进度条
        //b true拍照
        if (b) {
            val file = File(Environment.getExternalStorageDirectory(), "/temp/" + System.currentTimeMillis() + ".jpg")
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            val imageUri = Uri.fromFile(file)
            takePhoto.onPickFromCaptureWithCrop(imageUri, builder.create())
        } else {
            val builder1 = TakePhotoOptions.Builder()
            builder1.setWithOwnGallery(true)
            takePhoto.setTakePhotoOptions(builder1.create())
            takePhoto.onPickMultipleWithCrop(1, builder.create())
        }
    }
    override fun takeFail(result: TResult?, msg: String?) {
        ToastUtil.showToastL("选择图片失败")
        super.takeFail(result, msg)
    }

    override fun takeSuccess(result: TResult?) {
        submitImg(File(result!!.image.compressPath))
//        setPicToView(File(result!!.image.compressPath))
        super.takeSuccess(result)
    }

    private fun submitImg(result: File) {
        val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        OkGo.post<String>(App.HEAD_UPLOAD)
                .tag(this)//
                .isMultipart(true)
                .params("touxiang", result)
                .params("tel", pref.getString(App.PrefNames.USERNAME, ""))
                .params("uid", pref.getString(App.PrefNames.USERID, "-1"))
                .execute(object : StringDialogCallback(this) {
                    @SuppressLint("ApplySharedPref")
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGobodyHEAD_UPLOAD", response.body().toString())

                        try {
                            val json = JSONObject(response.body()!!.toString())
                            if (json.getString("cw") == "0"){
                                PromptDialog(this@AccountSetsActivity).showSuccess("更换成功")

                                val editor = pref.edit()
                                editor.putString(App.PrefNames.TOKEN, json.getString("token"))
                                editor.putString(App.PrefNames.HEADIMG, json.getString("logo"))
                                editor.commit()
                            }else {
                                PromptDialog(this@AccountSetsActivity).showError("更换失败")
                            }
                        }catch (e: JSONException) {
                            LogUtil.e("JSONException",e.toString())
                            ToastUtil.showToastL("数据解析异常")
                            e.printStackTrace()
                        }
                    }

                    override fun onError(response: Response<String>) {
                        PromptDialog(this@AccountSetsActivity).showError("更换失败")
                    }
                })
    }

    /**
     * 保存裁剪之后的图片数据
     * @param picdata
     */
    private fun setPicToView(result: File) {
            val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
            val username = pref.getString(App.PrefNames.USERNAME, "")
            val userId = pref.getString(App.PrefNames.USERID, "-1")
            val param = RequestParams(App.HEAD_UPLOAD)
            param.addBodyParameter("touxiang", result)
            param.addBodyParameter("tel", username)
            param.addBodyParameter("uid", userId)
            param.isMultipart = true//以表单形式提交，否则后台接收不到
            val cancelAble = x.http().post(param, object : Callback.CommonCallback<String> {
                override fun onFinished() {
                }

                @SuppressLint("ApplySharedPref")
                override fun onSuccess(result: String) {
                    val cw = JsonSyncUtils.getJsonValue(result, "cw")
                                Log.e("testLog", result)
                    if (cw == "0") {
                        ToastUtil.showToastL("修改成功！")
                        val token = JsonSyncUtils.getJsonValue(result, "token")
                        val headUrl = JsonSyncUtils.getJsonValue(result, "logo")
                        val editor = pref.edit()
                        editor.putString(App.PrefNames.TOKEN, token)
                        editor.putString(App.PrefNames.HEADIMG, headUrl)
                        editor.commit()

//                        x.image().clearMemCache()
//                        x.image().clearCacheFiles()
                    } else ToastUtil.showToastL("修改失败！")
                }

                override fun onCancelled(cex: Callback.CancelledException?) {
                }

                override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                    ToastUtil.showToastL("访问服务器失败，请检查网络连接")
                }
            })
//            cancelAble.cancel()

    }

}
