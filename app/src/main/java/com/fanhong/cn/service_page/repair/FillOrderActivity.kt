package com.fanhong.cn.service_page.repair

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.Uri.fromFile
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.service_page.repair.adapter.MyRecyclerAdapter
import com.fanhong.cn.http.callback.StringDialogCallback
import com.fanhong.cn.myviews.PhotoSelectWindow
import com.fanhong.cn.tools.*
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.lzy.okgo.request.PostRequest
import kotlinx.android.synthetic.main.activity_fill_order.*
import me.leefeng.promptlibrary.PromptDialog
import org.json.JSONException
import org.json.JSONObject
import org.xutils.common.Callback
import org.xutils.common.util.KeyValue
import org.xutils.http.RequestParams
import org.xutils.http.body.MultipartBody
import org.xutils.x
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class FillOrderActivity : AppCompatActivity(), MyRecyclerAdapter.Callback {

    private var isinput = true

    companion object {
        private val REQUEST_TAKE_PHOTO = 11
        private val REQUEST_CHOOSE_PIC = 12
        private val TAKE_PHOTO = 21
        private val CHOOSE_PIC = 22
        private val CUT_PICTURE = 23
        private val SET_NICK = 24
    }

    private var file: File? = null
    private var files = ArrayList<File>()
    private var cancel: Callback.Cancelable? = null
    private var adapter: MyRecyclerAdapter? = null
    private var cropFile: File? = null
    var isOwner = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_order)
        tv_title.text = "填写订单"
        if (intent.getIntExtra("owner", 0) == 666) {
            isOwner = true
            edt_community_name.hint = "门牌号"
            edt_phone.visibility=View.GONE
            edt_repair_article.visibility=View.GONE
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    @SuppressLint("SetTextI18n")
//    @Event(R.id.img_back, R.id.ibt_garage_minus, R.id.ibt_garage_add, R.id.ibt_sidewalk_minus,
//            R.id.ibt_sidewalk_add, R.id.ibt_unit_minus, R.id.ibt_unit_add, R.id.btn_save,
//            R.id.edt_repair_article, R.id.btn_submit,R.id.img_add)
    fun onCLicks(v: View) {
        when (v.id) {
            R.id.img_back -> finish()
            R.id.edt_repair_article -> {
                edt_repair_article.visibility = View.GONE
                all_select.visibility = View.VISIBLE
            }
            R.id.ibt_garage_minus -> if (tv_garage_quantity.text != "0")
                tv_garage_quantity.text = (tv_garage_quantity.text.toString().toInt() - 1).toString()

            R.id.ibt_garage_add -> tv_garage_quantity.text = (tv_garage_quantity.text.toString().toInt() + 1).toString()

            R.id.ibt_sidewalk_minus -> if (tv_sidewalk_quantity.text != "0")
                tv_sidewalk_quantity.text = (tv_sidewalk_quantity.text.toString().toInt() - 1).toString()
            R.id.ibt_sidewalk_add -> tv_sidewalk_quantity.text = (tv_sidewalk_quantity.text.toString().toInt() + 1).toString()

            R.id.ibt_unit_minus -> if (tv_unit_quantity.text != "0")
                tv_unit_quantity.text = ((tv_unit_quantity.text.toString().toInt() - 1)).toString()
            R.id.ibt_unit_add -> tv_unit_quantity.text = (tv_unit_quantity.text.toString().toInt() + 1).toString()

            R.id.btn_save -> {
                val garage = if (tv_garage_quantity.text != "0") "车库门x" + tv_garage_quantity.text else ""
                val sidewalk = if (tv_sidewalk_quantity.text != "0") "人行门x" + tv_sidewalk_quantity.text else ""
                val unit = if (tv_unit_quantity.text != "0") "单元门x" + tv_unit_quantity.text else ""
                val strings = ArrayList<String>()
                strings.add(garage)
                strings.add(sidewalk)
                strings.add(unit)
                var string = ""
                for (i in strings.indices) {
                    if (!strings[i].isEmpty()) {
                        string += if (string.isEmpty()) strings[i] else "," + strings[i]
                    }
                }
                edt_repair_article.text = string
                edt_repair_article.visibility = View.VISIBLE
                all_select.visibility = View.GONE
            }

            R.id.img_add -> if (files.size < 4) addImg() else ToastUtil.showToastL("图片只能上传三张")
            R.id.btn_submit -> submit2()
        }
    }

    private fun submit2() {
        val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (isOwner) {
            if (StringUtils.isEmpty(edt_remark.text.toString())) {
                Toast.makeText(this, "请填写维修内容！", Toast.LENGTH_SHORT).show()
                return
            }
            OkGo.post<String>(App.IMG)
                    .tag(this)//
                    .addFileParams("f",files)
                    .isMultipart(true)
                    .execute(object : StringDialogCallback(this){
                        override fun onSuccess(response: Response<String>?) {
                            Log.e("OkGoIMG", response!!.body().toString())
                            val request = OkGo.post<String>(App.CMD)
                            //        1053：业主报修（app->平台）
                            //        cmd:数据类型
                            //        uid:用户ID
                            //        qid:小区id   33
                            //        menpai:门牌号（文本框 用户填写）
                            //        concent:维修内容
                            //        tupian1,tupian2,tupian3:用户上传图片
                                    .tag(this)//
                                    .isMultipart(true)
                                    .params("cmd", "1053")
                                    .params("uid", pref.getString(App.PrefNames.USERID, "-1"))
                                    .params("qid", pref.getString(App.PrefNames.GARDENID, ""))
                                    .params("menpai", edt_community_name.text.toString())
                                    .params("concent", edt_remark.text.toString())
                                    .params("time", System.currentTimeMillis().toString())
                            for (i in files.indices) {
                                request.params("tupian" + (i + 1), files[i].nameWithoutExtension)
                            }
                            myExecute(request)
                        }
                        override fun onError(response: Response<String>) {
                            Log.e("OkGoIMGError", response.exception.toString())
                            AlertDialog.Builder(this@FillOrderActivity).setMessage("onError提交失败！ 是否重试?")
                                    .setPositiveButton("确定") { _, _ ->
                                        submit2()
                                    }.setNegativeButton("取消", null).show()
                        }
                    })
        } else{
            val phone = edt_phone.text.toString().replace("-", "")
            if (!StringUtils.validPhoneNum("2", phone)) {
                Toast.makeText(this, "请输入正确的电话号码！", Toast.LENGTH_SHORT).show()
                return
            }
            if (StringUtils.isEmpty(edt_repair_article.text.toString())) {
                Toast.makeText(this, "请选择维修物品！", Toast.LENGTH_SHORT).show()
                return
            }
            val request = OkGo.post<String>(App.CMD)
            //        1047：报修信息(APP->平台)
            //        cmd:数据类型
            //        uid:当前用户ID
            //        men:需要维修的门
            //        time;订单号 （时间戳就行）
            //        lxphone:联系电话
            //        dizhi:报修地址
            //        concent:详细信息
            //        tupian:图片 .tag(this)//
                    .isMultipart(true)
                    .params("cmd", "1047")
                    .params("uid", pref.getString(App.PrefNames.USERID, "-1"))
                    .params("men", edt_repair_article.text.toString())
                    .params("lxphone", edt_phone.text.toString().replace("-", ""))
                    .params("dizhi", edt_community_name.text.toString())
                    .params("concent", edt_remark.text.toString())
                    .params("time", System.currentTimeMillis().toString())
            for (i in files.indices) {
                request.params("tupian" + (i + 1), files[i].nameWithoutExtension)
                request.params("f" + (i + 1), files[i])
            }
            myExecute(request)
        }
    }

    private fun myExecute(request: PostRequest<String>?) {
        request!!.execute(object : StringDialogCallback(this) {
            override fun onSuccess(response: Response<String>) {
                Log.e("OkGo", response.body().toString())
                if (response.body().toString().contains("\"cw\":\"1\"")
                        ||response.body().toString().contains("\"state\":\"200\"")){
                    PromptDialog(this@FillOrderActivity).showSuccess("提交成功",true)
                    Handler().postDelayed({ finish() }, 1500)
                }else PromptDialog(this@FillOrderActivity).showError("提交失败")
            }
            override fun onError(response: Response<String>) {
                Log.e("OkGoError", response.exception.toString())
                AlertDialog.Builder(this@FillOrderActivity).setMessage("onError提交失败！ 是否重试?")
                        .setPositiveButton("确定") { _, _ ->
                            submit2()
                        }.setNegativeButton("取消", null).show()
            }
        })
    }

    private fun submit() {
        val phone = edt_phone.text.toString().replace("-", "")
        if (!StringUtils.validPhoneNum("2", phone)) {
            Toast.makeText(this, "请输入正确的电话号码！", Toast.LENGTH_SHORT).show()
            return
        }
        val addr = edt_community_name.text.toString()
        if (StringUtils.isEmpty(addr)) {
            Toast.makeText(this, "请输入详细地址！", Toast.LENGTH_SHORT).show()
            return
        }
//        1047：报修信息(APP->平台)
//        cmd:数据类型
//        uid:当前用户ID
//        men:需要维修的门
//        lxphone:联系电话
//        dizhi:报修地址
//        concent:详细信息
//        tupian:图片
//        submitIMG()
        val params = RequestParams(App.CMD)
        val body = java.util.ArrayList<KeyValue>()
        val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        body.add(KeyValue("cmd", "1047"))
        body.add(KeyValue("uid", pref.getString(App.PrefNames.USERID, "-1")))
        body.add(KeyValue("men", edt_repair_article.text.toString()))
        body.add(KeyValue("lxphone", edt_phone.text.toString().replace("-", "")))
        body.add(KeyValue("dizhi", edt_community_name.text.toString()))
        body.add(KeyValue("concent", edt_remark.text.toString()))
        body.add(KeyValue("time", System.currentTimeMillis().toString()))
        for (i in files.indices) {
            body.add(KeyValue("file", files[i]))
            body.add(KeyValue("tupian" + (i + 1), files[i].nameWithoutExtension))
            Log.e("body for", body.toString())
        }

        Log.e("body", body.toString())
        params.requestBody = MultipartBody(body, "UTF-8")
        params.isMultipart = true
        val dialog = AlertDialog.Builder(this).setMessage("正在提交").create()
        dialog.setCancelable(false)
        cancel = x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onSuccess(s: String) {
                Log.e("TestLog", s)
                if (JsonSyncUtils.getJsonValue(s, "state") == "200") {
                    val intent = Intent(this@FillOrderActivity, RepairSuccessActivity::class.java)
                    startActivity(intent)
                } else {
                    AlertDialog.Builder(this@FillOrderActivity).setMessage("提交失败！").setPositiveButton("确定", null).show()
                }
            }

            override fun onError(throwable: Throwable, b: Boolean) {
                Log.e("TestLog", "err")
                AlertDialog.Builder(this@FillOrderActivity).setMessage("提交失败！").setPositiveButton("确定", null).show()
            }

            override fun onCancelled(e: Callback.CancelledException) {
//                        Log.e("TestLog", "cancel")
            }

            override fun onFinished() {
                dialog.dismiss()
            }
        })
        dialog.show()
    }

    private fun addImg() {
        val popupWindow = PhotoSelectWindow(this@FillOrderActivity)
        popupWindow.setOnTakePhoto(View.OnClickListener {
            popupWindow.dismiss()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                val checkCamera = ContextCompat.checkSelfPermission(this@FillOrderActivity, Manifest.permission.CAMERA)
                val checkSD = ContextCompat.checkSelfPermission(this@FillOrderActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
                if (ContextCompat.checkSelfPermission(this@FillOrderActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_TAKE_PHOTO)
                    return@OnClickListener
                }
            }
            choosePhoto()
        })
        popupWindow.showAtLocation(layout_fill, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)

    }

    private fun useCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        file = File(Environment.getExternalStorageDirectory().toString() +
                "/Fanshequ/Camera" + (Date(System.currentTimeMillis()).toString()) + ".jpg")
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
            file = File(Environment.getExternalStorageDirectory().toString() +
                    "/Fanshequ/Photo" + (Date(System.currentTimeMillis()).toString()) + ".jpg")
//            file= createTempDir()
            val uri = FileProvider.getUriForFile(this, "applicationId.fileprovider", file)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivityForResult(intent, CHOOSE_PIC)
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
        }
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
//        intent.putExtra("crop", "true")
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
            // 取得SDCard图片路径做显示
//            val photo:Bitmap = extras.getParcelable<Bitmap>("data")
//            val file: File = FileHelp.saveBitmapFile(photo)
//            showFiles.add(file)
            val file: File = FileUtil.compressImage(cropFile!!, Environment.getExternalStorageDirectory().toString() +
                    "/Fanshequ/hou" + (System.currentTimeMillis().toString()) + ".jpg")
            files.add(file)
            showImg()
        }
    }

    private fun showImg() {
        if (adapter == null) {
            adapter = MyRecyclerAdapter(files, this)
            rv_img.layoutManager = LinearLayoutManager(this,
                    LinearLayoutManager.HORIZONTAL, false)
            rv_img.adapter = adapter
        } else adapter!!.notifyDataSetChanged()
    }

    override fun click(v: View) {
        files.removeAt(v.tag.toString().toInt())
        adapter!!.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
        val checkSD = ContextCompat.checkSelfPermission(this@FillOrderActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (checkSD == PackageManager.PERMISSION_GRANTED) {
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (ContextCompat.checkSelfPermission(this@FillOrderActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                FileHelp.deleteFiles(Environment.getExternalStorageDirectory().toString() +
                        "/Fanshequ")
            }
        }
    }

}
