package com.fanhong.cn.door_page

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.net.Uri.fromFile
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.service_page.repair.adapter.MyRecyclerAdapter
import com.fanhong.cn.home_page.ChooseCellActivity
import com.fanhong.cn.http.callback.StringDialogCallback
import com.fanhong.cn.myviews.PhotoSelectWindow
import com.fanhong.cn.myviews.SpinerPopWindow
import com.fanhong.cn.tools.*
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import kotlinx.android.synthetic.main.activity_add_key.*
import kotlinx.android.synthetic.main.activity_top.*
import kotlinx.android.synthetic.main.agree_sheets.*
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

class AddKeyActivity : AppCompatActivity() ,MyRecyclerAdapter.Callback{

    var cellId: String? = null
    var lastCellId: String? = null
    var cellIdList: MutableList<String>? = ArrayList()
    var cellList: MutableList<String>? = ArrayList()
    var buildingId: String? = null
    var buildingIdList: MutableList<String>? = ArrayList()
    var buildingList: MutableList<String>? = ArrayList()

    private var selectedPaths: MutableList<String> = ArrayList()
    private var selectedFiles: MutableList<File> = ArrayList()

    var mSharedPref: SharedPreferences? = null
    var uid: String? = null
    var ssp: SpinerPopWindow<String>? = null

    private var file: File? = null
    private var files = ArrayList<File>()
    private var cropFile: File? = null
    private var cancel: Callback.Cancelable? = null
    private var adapter: MyRecyclerAdapter? =null

    private val REQUEST_PERMISSION = 21
    private val START_GALLERYFINAL = 23

    companion object {
        private val REQUEST_TAKE_PHOTO = 11
        private val REQUEST_CHOOSE_PIC = 12
        private val TAKE_PHOTO = 21
        private val CHOOSE_PIC = 22
        private val CUT_PICTURE = 23
        private val SET_NICK = 24
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_key)
        FileHelp.deleteFiles(Environment.getExternalStorageDirectory().toString() +
                "/Fanshequ")
        mSharedPref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        uid = mSharedPref?.getString(App.PrefNames.USERID, "-1")
        var gardenName = mSharedPref!!.getString(App.PrefNames.GARDENNAME, "")
        if (!gardenName.isEmpty()) {
            key_choosegarden.text = gardenName
            cellId = mSharedPref!!.getString(App.PrefNames.GARDENID, "")
            lastCellId = cellId
            key_choosebuilding.isEnabled = true
        }
        initViews()
    }

    private fun initViews() {
        img_back.setOnClickListener {
            goBack()
        }
        tv_title.text = getString(R.string.applykey)
        key_choosegarden.setOnClickListener {
            //            var params = RequestParams(App.CMD)
//            params.addBodyParameter("cmd", "29")
//            x.http().post(params, object : Callback.CommonCallback<String> {
//                override fun onFinished() {
//                }
//
//                override fun onSuccess(result: String) {
//                    if (JsonSyncUtils.getJsonValue(result, "cw") == "0") {
//                        val data = JsonSyncUtils.getJsonValue(result, "data")
//                        cellList = JsonSyncUtils.getStringList(data, "name")
//                        cellIdList = JsonSyncUtils.getStringList(data,"id")
//
//                        ssp = SpinerPopWindow(this@AddKeyActivity, cellList!!, ""){ parent, view, position, id ->
//                            key_choosegarden.text = cellList!![position]
//                            cellId = cellIdList!![position]
//                            if(lastCellId != cellId){
//                                key_choosebuilding.text = "选择楼栋"
//                                buildingId = ""
//                                lastCellId = cellId
//                            }
//                            ssp!!.dismiss()
//                        }
//                        ssp?.width = key_choosegarden.width
//                        ssp?.showAsDropDown(key_choosegarden)
//                    }
//                }
//
//                override fun onCancelled(cex: Callback.CancelledException?) {
//                }
//
//                override fun onError(ex: Throwable?, isOnCallback: Boolean) {
//                }
//            })
            startActivityForResult(Intent(this, ChooseCellActivity::class.java), 101)
        }
        key_choosebuilding.setOnClickListener {
            var params = RequestParams(App.CMD)
            params.addBodyParameter("cmd", "1001")
            params.addBodyParameter("xid", cellId)
            x.http().post(params, object : Callback.CommonCallback<String> {
                override fun onFinished() {
                }

                override fun onSuccess(result: String) {
                    if (JsonSyncUtils.getJsonValue(result!!, "state") == "200") {
                        var data = JsonSyncUtils.getJsonValue(result!!, "data")
                        buildingList = JsonSyncUtils.getStringList(data!!, "bname")
                        buildingIdList = JsonSyncUtils.getStringList(data!!, "id")

                        ssp = SpinerPopWindow(this@AddKeyActivity, buildingList!!, "") { parent, view, position, id ->
                            key_choosebuilding.text = buildingList!![position]
                            buildingId = buildingIdList!![position]
                            ssp!!.dismiss()
                        }
                        ssp?.width = key_choosebuilding.width
                        ssp?.showAsDropDown(key_choosebuilding)
                    }
                }


                override fun onCancelled(cex: Callback.CancelledException?) {
                }

                override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                }

            })
        }
        img_key_add.setOnClickListener {
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
//                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
//                val checkCamera = ContextCompat.checkSelfPermission(this@AddKeyActivity, Manifest.permission.CAMERA)
//                val checkSD = ContextCompat.checkSelfPermission(this@AddKeyActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                if (checkCamera != PackageManager.PERMISSION_GRANTED || checkSD != PackageManager.PERMISSION_GRANTED) {
//                    requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION)
//                    return@setOnClickListener
//                }
//            }
            addImg()
        }
        sheet_protocol.setOnClickListener {

        }
        /**
         * 上传数据
         */
        key_submit.setOnClickListener {
            if (TextUtils.isEmpty(cellId)) {
                ToastUtil.showToastS("请选择小区")
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(buildingId)) {
                ToastUtil.showToastS("请选择楼栋")
                return@setOnClickListener
            }
//            if (selectedFiles.size == 0) {
//                ToastUtil.showToastS("请至少上传一张人脸图片")
//                return@setOnClickListener
//            }
            if (files.size == 0) {
                ToastUtil.showToastS("请至少上传一张人脸图片")
                return@setOnClickListener
            }
            if (!agree_sheet_protocol.isChecked) {
                ToastUtil.showToastS("请阅读并同意用户协议")
                return@setOnClickListener
            }
            submit2()
        }
    }

    private fun submit() {
        val dialog = AlertDialog.Builder(this).setMessage("正在提交").create()
        dialog.setCancelable(false)
        dialog.show()

        val kvList: MutableList<KeyValue> = ArrayList()
        kvList.add(KeyValue("cmd", "1012"))
        kvList.add(KeyValue("uid", uid))
        kvList.add(KeyValue("xid", cellId))
        kvList.add(KeyValue("dizhi", buildingId))
        var filenames = ""
        for (i in 0 until files.size) {
            val dir = File("${Environment.getExternalStorageDirectory()}/Fanshequ")
            if (!dir.exists())
                dir.mkdir()
            val fileName = "${System.currentTimeMillis()}${(Math.random() * 99999).toInt()}_$i.jpg"
//                val file: File = FileUtil.compressImage(files[i], "${Environment.getExternalStorageDirectory()}/Fanshequ/$fileName")
            kvList.add(KeyValue("touxiang${i + 1}", files[i]))
            if (i != 0) filenames += ","
            filenames += fileName
        }
        kvList.add(KeyValue("xinxi", filenames))

        val params = RequestParams(App.CMD)
        params.requestBody = MultipartBody(kvList, "UTF-8")
        params.isMultipart = true
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                when (JsonSyncUtils.getState(result)) {
                    200 -> {
                        FileHelp.deleteFiles(Environment.getExternalStorageDirectory().toString() +
                                "/Fanshequ")
                        AlertDialog.Builder(this@AddKeyActivity).setMessage("上传成功！").setPositiveButton("确定", null).show()
                        finish()
                    }
                    400 -> {
                        AlertDialog.Builder(this@AddKeyActivity).setMessage("提交失败！").setPositiveButton("确定", null).show()
                        ToastUtil.showToastL("上传失败，请重试")
                    }
                }
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                AlertDialog.Builder(this@AddKeyActivity).setMessage("提交失败！").setPositiveButton("确定", null).show()
                ToastUtil.showToastL("连接服务器失败，请检查网络！")
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
                ToastUtil.showToastL("onCancelled连接服务器失败，请检查网络！")
            }

            override fun onFinished() {
                dialog.dismiss()
            }
        })
    }

    private fun submit2() {
        var filenames = ""
        val request = OkGo.post<String>(App.CMD)
                .tag(this)//
                .isMultipart(true)
                .params("cmd", "1012")
                .params("uid", uid)
                .params("xid", cellId)
                .params("dizhi", buildingId)
        for (i in files.indices){
            val dir = File("${Environment.getExternalStorageDirectory()}/Fanshequ")
            if (!dir.exists())
                dir.mkdir()
            request.params("touxiang" + (i + 1), files[i])
            val fileName = "${System.currentTimeMillis()}${(Math.random() * 99999).toInt()}_$i.jpg"
            if (i != 0) filenames += ","
            filenames += fileName
        }
        request.params("xinxi", filenames)
        request.execute(object : StringDialogCallback(this) {
            override fun onSuccess(response: Response<String>) {
                Log.e("OkGo", response.body().toString())
                try {
                    val json = JSONObject(response.body()!!.toString())
                    if (json.getString("state") == "200"){
                        FileHelp.deleteFiles(Environment.getExternalStorageDirectory().toString() +
                                "/Fanshequ")
                        AlertDialog.Builder(this@AddKeyActivity).setMessage("提交成功!")
                                .setPositiveButton("确定") { _, _ ->
                                    finish()
                                } .show()
                    }else{
                        PromptDialog(this@AddKeyActivity).showError("提交失败!")
                    }
                }catch (e: JSONException) {
                    LogUtil.e("JSONException",e.toString())
                    ToastUtil.showToastL("数据解析异常")
                    e.printStackTrace()
                }
            }

            override fun onError(response: Response<String>) {
                Log.e("OkGoError", response.exception.toString())
                AlertDialog.Builder(this@AddKeyActivity).setMessage("onError提交失败！ 是否重试?")
                        .setPositiveButton("确定") { _, _ ->
                            submit2()
                        }.setNegativeButton("取消",null) .show()
            }
        })
    }

    private fun addImg() {
        val popupWindow = PhotoSelectWindow(this@AddKeyActivity)
        popupWindow.setOnTakePhoto(View.OnClickListener {
            popupWindow.dismiss()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                val checkCamera = ContextCompat.checkSelfPermission(this@AddKeyActivity, Manifest.permission.CAMERA)
                val checkSD = ContextCompat.checkSelfPermission(this@AddKeyActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
                if (ContextCompat.checkSelfPermission(this@AddKeyActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_TAKE_PHOTO)
                    return@OnClickListener
                }
            }
            choosePhoto()
        })
        popupWindow.showAtLocation(layout_add_key, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)

    }

    private fun useCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        file=File(Environment.getExternalStorageDirectory().toString() +
                "/Fanshequ/Camera"+(Date(System.currentTimeMillis()).toString())+".jpg")
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
            file=File(Environment.getExternalStorageDirectory().toString() +
                    "/Fanshequ/Photo"+(Date(System.currentTimeMillis()).toString())+".jpg")
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
        if (resultCode == 51) {
            key_choosegarden.text = data?.getStringExtra("gardenName")
            cellId = data?.getStringExtra("gardenId")
            if (lastCellId != cellId) {
                key_choosebuilding.text = "选择楼栋"
                buildingId = ""
                lastCellId = cellId
            }
        }

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
     * @param uri
     */
    private fun startPhotoZoom(uri: Uri) {
        cropFile = File(Environment.getExternalStorageDirectory().
                toString() + "/Fanshequ/cropImage.jpg")
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
            val file:File = FileUtil.compressImage(cropFile!!, Environment.getExternalStorageDirectory().toString() +
                    "/Fanshequ/hou"+(System.currentTimeMillis()).toString()+".jpg")
            files.add(file)
            showImg()
        }
    }

    private fun showImg() {
        if (adapter==null){
            adapter= MyRecyclerAdapter(files,this)
            rv_img.layoutManager= LinearLayoutManager(this,
                    LinearLayoutManager.HORIZONTAL,false)
            rv_img.adapter=adapter
        }else adapter!!.notifyDataSetChanged()
    }

    override fun click(v: View) {
        files.removeAt(v.tag.toString().toInt())
        adapter!!.notifyDataSetChanged()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private var time1 = 0L

    private fun goBack() {
        var time2 = System.currentTimeMillis()
        if (time2 - time1 > 2000) {
            ToastUtil.showToastS("将会失去所有数据，再按一次返回")
            time1 = time2
        } else {
            finish()
        }
    }

}
