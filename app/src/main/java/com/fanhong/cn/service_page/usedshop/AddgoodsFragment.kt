package com.fanhong.cn.service_page.usedshop

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.myviews.PhotoSelectWindow
import com.fanhong.cn.tools.*
import kotlinx.android.synthetic.main.fragment_add_usedgoods.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Administrator on 2018/2/24.
 */
class AddgoodsFragment : Fragment() {
    companion object {
        private val REQUEST_TAKE_PHOTO = 11
        private val REQUEST_CHOOSE_PIC = 12
        private val TAKE_PHOTO = 21
        private val CHOOSE_PIC = 22
        private val CUT_PICTURE = 23
    }

    private var file: File? = null  //拍照剪切前的图片文件
    private var cropFile: File? = null  //剪切后的图片文件
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_add_usedgoods, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var mSharedPref = activity!!.getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        var uid = mSharedPref.getString(App.PrefNames.USERID, "-1")
        file = File(Environment.getExternalStorageDirectory().toString() + "/" + getPhotoFileName())
        if (!file!!.parentFile.exists()) {
            file!!.parentFile.mkdirs()
            try {
                file!!.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        img_goods_add.setOnClickListener {
            hideSoftinputyer(img_goods_add)
            var ppw = PhotoSelectWindow(activity!!)
            ppw.setOnTakePhoto(View.OnClickListener {
                ppw.dismiss()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                    val checkCamera = ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA)
                    val checkSD = ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (checkCamera != PackageManager.PERMISSION_GRANTED || checkSD != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_TAKE_PHOTO)
                        return@OnClickListener
                    }
                }
                useCamera()
            })
            ppw.setOnChoosePic(View.OnClickListener {
                ppw.dismiss()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                    if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_TAKE_PHOTO)
                        return@OnClickListener
                    }
                }
                choosePhoto()
            })
            ppw.showAtLocation(main_layout, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
        }
        btn_submit.setOnClickListener {
            if (TextUtils.isEmpty(edt_goods_name.text) || TextUtils.isEmpty(edt_goods_details.text) ||
                    cropFile == null || TextUtils.isEmpty(edt_goods_name.text) ||
                    TextUtils.isEmpty(edt_goods_price.text) || TextUtils.isEmpty(edt_owner_name.text) ||
                    TextUtils.isEmpty(edt_owner_phone.text)) {
                ToastUtil.showToastS("信息填写不完整！")
                return@setOnClickListener
            }
            if(!StringUtils.validPhoneNum("2",edt_owner_phone.text.toString())){
                ToastUtil.showToastS("请输入正确的联系号码！")
                return@setOnClickListener
            }
            //压缩图片
            var file = FileUtil.compressImage(cropFile!!, cropFile!!.absolutePath) //文件全路径
            var params = RequestParams(App.CMD)
            params.addBodyParameter("cmd", "1008")
            params.addBodyParameter("name", edt_goods_name.text.toString())
            params.addBodyParameter("ms", edt_goods_details.text.toString())
            params.addBodyParameter("touxiang", file)
//            params.addBodyParameter("touxiang", cropFile)
            params.addBodyParameter("jg", edt_goods_price.text.toString())
            params.addBodyParameter("dh", edt_owner_phone.text.toString())
            params.addBodyParameter("user", edt_owner_name.text.toString())
            params.addBodyParameter("uid", uid)
            params.isMultipart = true //以表单形式，否则后台接收不到
            x.http().post(params, object : Callback.CommonCallback<String> {
                override fun onFinished() {
                }

                override fun onSuccess(result: String) {
                    var state = JsonSyncUtils.getJsonValue(result,"state")
                    Log.i("xq","返回码==>"+state)
                    if (state == "200") {
                        ToastUtil.showToastS("上传成功！")
                        clearData()
                        (activity!! as UsedShopActivity).setCheck(0)
                    } else {
                        ToastUtil.showToastS("提交失败,请重试~")
                    }
                }

                override fun onCancelled(cex: Callback.CancelledException?) {
                }

                override fun onError(ex: Throwable?, isOnCallback: Boolean) {
//                    Log.i("xq","返回码==>"+ex.toString())
                    ToastUtil.showToastS("提交失败,请重试~")
                }

            })
        }
    }

    private fun useCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            FileProvider.getUriForFile(activity!!, "applicationId.fileprovider", file!!)
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
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
//        intent.type = "image/*"
        if (Build.VERSION.SDK_INT >= 24) {
            val uri = FileProvider.getUriForFile(activity!!, "applicationId.fileprovider", file!!)
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
        cropFile = File(Environment.getExternalStorageDirectory().toString() + "/crop/" + getPhotoFileName())
        if (!cropFile!!.parentFile.exists()) {
            cropFile!!.parentFile.mkdirs()
            try {
                cropFile!!.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        val outputUri = Uri.fromFile(cropFile)
        val intent = Intent("com.android.camera.action.CROP")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
            intent.setDataAndType(uri, "image/*")
            intent.putExtra("noFaceDetection", false)//去除默认的人脸识别，否则和剪裁匡重叠
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val url = GetImagePath.getPath(activity!!, uri)
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

    private fun setPicToView(intent: Intent) {
        var bundle = intent.extras
        if (bundle != null) {
            var bitmap = bundle.getParcelable<Bitmap>("data")
            img_goods_add.setImageBitmap(bitmap)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_TAKE_PHOTO -> useCamera()
            REQUEST_CHOOSE_PIC -> choosePhoto()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK)
            return
        when (requestCode) {
            TAKE_PHOTO -> {
                var uri = Uri.fromFile(file)
                if (Build.VERSION.SDK_INT >= 24) {
                    uri = FileProvider.getUriForFile(activity!!, "applicationId.fileprovider", file!!)
                }
                startPhotoZoom(uri)
            }
            CHOOSE_PIC -> {
//                startPhotoZoom(data?.data as Uri)
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val imgUri = File(GetImagePath.getPath(activity!!, data!!.data))
                        val uri = FileProvider.getUriForFile(activity!!, "applicationId.fileprovider", imgUri)
                        startPhotoZoom(uri)
                    } else
                        startPhotoZoom(data!!.data)
                } catch (e: NullPointerException) {
                    e.printStackTrace() // 用户点击取消操作
                }
            }
            CUT_PICTURE -> {
                setPicToView(data!!)
            }
        }
    }

    // 获取拍照照片的名字，时间命名
    private fun getPhotoFileName(): String {
        val date = Date(System.currentTimeMillis())
        //3位随机数
        val kk = Random().nextInt(900) + 100
        val dateFormat = SimpleDateFormat(
                "'IMG'_yyyyMMdd_HHmmss")
        return dateFormat.format(date) + "_" + kk + ".jpg"
    }

    //清空数据
    private fun clearData(){
        edt_goods_name.setText("")
        edt_goods_details.setText("")
        img_goods_add.setImageResource(R.mipmap.btn_add_img)
        edt_goods_price.setText("")
        edt_owner_name.setText("")
        edt_owner_phone.setText("")
    }

    //隐藏软键盘的方法
    private fun hideSoftinputyer(view: View) {
        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        Log.i("windowToken", view.windowToken.toString())
        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}