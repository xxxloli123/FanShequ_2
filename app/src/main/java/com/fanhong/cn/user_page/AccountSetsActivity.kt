package com.fanhong.cn.user_page

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.net.Uri.fromFile
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.myviews.PhotoSelectWindow
import com.fanhong.cn.tools.FileUtil
import com.fanhong.cn.tools.GetImagePath
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import com.fanhong.cn.user_page.shippingaddress.MyAddressActivity
import kotlinx.android.synthetic.main.activity_account_sets.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.image.ImageOptions
import org.xutils.x
import java.io.File
import java.io.IOException

class AccountSetsActivity : AppCompatActivity() {
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
        tv_title.text = getString(R.string.usersettings)
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
        val headUrl = pref.getString(App.PrefNames.HEADIMG, "")
        val number = pref.getString(App.PrefNames.USERNAME, "")
        val nick = pref.getString(App.PrefNames.NICKNAME, "")
        val option = ImageOptions.Builder().setCircular(true)
                .setLoadingDrawableId(R.mipmap.ico_tx)
                .setFailureDrawableId(R.mipmap.ico_tx)
                .setUseMemCache(true).build()
        x.image().bind(img_head, headUrl, option)
        tv_phone.text = number
        tv_nick.text = nick
    }

    fun onHeadImg(v: View) {
        val popupWindow = PhotoSelectWindow(this)
        popupWindow.setOnTakePhoto(View.OnClickListener {
            popupWindow.dismiss()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                val checkCamera = ContextCompat.checkSelfPermission(this@AccountSetsActivity, Manifest.permission.CAMERA)
                val checkSD = ContextCompat.checkSelfPermission(this@AccountSetsActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (checkCamera != PackageManager.PERMISSION_GRANTED || checkSD != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_TAKE_PHOTO)
                    return@OnClickListener
                }
            }
            useCamera()
        }).setOnChoosePic(View.OnClickListener {
            popupWindow.dismiss()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                if (ContextCompat.checkSelfPermission(this@AccountSetsActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_TAKE_PHOTO)
                    return@OnClickListener
                }
            }
            choosePhoto()
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

    private fun useCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            FileProvider.getUriForFile(this, "applicationId.fileprovider", file)
        else Uri.fromFile(file)

        //添加权限
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)//添加这一句表示对目标应用临时授权该Uri所代表的文件
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)//添加这一句表示对目标应用临时授权该Uri所代表的文件

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)//将拍取的照片保存到指定URI
        startActivityForResult(intent, TAKE_PHOTO)
    }

    private fun choosePhoto() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        if (Build.VERSION.SDK_INT >= 24) {
            val uri = FileProvider.getUriForFile(this, "applicationId.fileprovider", file)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivityForResult(intent, CHOOSE_PIC)
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    private fun startPhotoZoom(uri: Uri) {
        cropFile = File(Environment.getExternalStorageDirectory().toString() + "/Fanshequ/cropImage.jpg")
        if (!cropFile!!.parentFile.exists()) {
            cropFile!!.parentFile.mkdirs()
            try {
                cropFile!!.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        val outputUri = fromFile(cropFile)
        val intent = Intent("com.android.camera.action.CROP")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
            intent.setDataAndType(uri, "image/*")
            intent.putExtra("noFaceDetection", false)//去除默认的人脸识别，否则和剪裁匡重叠
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val url = GetImagePath.getPath(this, uri)
                intent.setDataAndType(Uri.fromFile(File(url)), "image/*")
            } else {
                intent.setDataAndType(uri, "image/*")
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
        }
        intent.putExtra("crop", "true")
        //宽高比例
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        //裁剪照片的宽高
        intent.putExtra("outputX", 300)
        intent.putExtra("outputY", 300)
        intent.putExtra("return-data", true)

        startActivityForResult(intent, CUT_PICTURE)

    }

    /**
     * 保存裁剪之后的图片数据
     * @param picdata
     */
    private fun setPicToView(picdata: Intent) {
        val extras = picdata.extras
        if (extras != null) {
            // 取得SDCard图片路径做显示  这个bitmap是一个图片预览会很小
            val photo = extras.getParcelable<Bitmap>("data")
//            val drawable = BitmapDrawable(null, photo)
            val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
            val username = pref.getString(App.PrefNames.USERNAME, "")
            val userId = pref.getString(App.PrefNames.USERID, "-1")
            val file: File = FileUtil.compressImage(photo, Environment.getExternalStorageDirectory().toString() + "/Fanshequ/$username.jpg")

//            Log.e("testLog", "file.length=${file.length()}")
            val param = RequestParams(App.HEAD_UPLOAD)
            param.addBodyParameter("touxiang", file)
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
                        var option = ImageOptions.Builder().setCircular(true).build()
                        x.image().bind(img_head,file.path.toString(),option)
//                        img_head.setImageBitmap(photo)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_TAKE_PHOTO -> useCamera()
            REQUEST_CHOOSE_PIC -> choosePhoto()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            TAKE_PHOTO -> {
                var uri = Uri.fromFile(file)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(this, "applicationId.fileprovider", file)
                }
                startPhotoZoom(uri)
            }
            CHOOSE_PIC -> {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val imgUri = File(GetImagePath.getPath(this, data!!.data))
                        val uri = FileProvider.getUriForFile(this, "applicationId.fileprovider", imgUri)
                        startPhotoZoom(uri)
                    } else
                        startPhotoZoom(data!!.data)
                } catch (e: NullPointerException) {
                    e.printStackTrace() // 用户点击取消操作
                }

            }
            CUT_PICTURE -> if (data != null) setPicToView(data)

            SET_NICK -> if (null != data) tv_nick.text = data.getStringExtra("nick")

        }
    }
}
