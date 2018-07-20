package com.fanhong.cn.user_page

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.FileUtil
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import kotlinx.android.synthetic.main.activity_evaluate.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.common.util.KeyValue
import org.xutils.http.RequestParams
import org.xutils.http.body.MultipartBody
import org.xutils.x
import java.io.File


class EvaluateActivity : AppCompatActivity() {

    private var orderId = ""
    private var goodsId = ""
    private var goodsName = ""
    private var screenWidth = 0

    private var screeHeight = 0
    private val REQUEST_PERMISSION = 21
    private val START_GALLERYFINAL = 23
    private var selectedPaths: MutableList<String> = ArrayList()
    private var selectedFiles: MutableList<File> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluate)
        tv_title.text = getString(R.string.evaluate)
        img_back.setOnClickListener { finish() }
        goodsId = intent.getStringExtra("goodsId")
        orderId = intent.getStringExtra("orderId")
        goodsName = intent.getStringExtra("goodsName")
        val outMetrics = resources.displayMetrics
        screenWidth = outMetrics.widthPixels
        screeHeight = outMetrics.heightPixels

        img_evaAdd.setOnClickListener {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                val checkCamera = ContextCompat.checkSelfPermission(this@EvaluateActivity, Manifest.permission.CAMERA)
                val checkSD = ContextCompat.checkSelfPermission(this@EvaluateActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (checkCamera != PackageManager.PERMISSION_GRANTED || checkSD != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION)
                    return@setOnClickListener
                }
            }
            openPicture()
        }

//        img_eva1.setOnClickListener {
//            GalleryFinal.openEdit(25, selectedPaths[0], object : GalleryFinal.OnHanlderResultCallback {
//                override fun onHanlderSuccess(reqeustCode: Int, resultList: MutableList<PhotoInfo>?) {
//                    if (null != resultList) {
//                        val path = resultList[0].photoPath
//                        selectedPaths[0] = path
//                        val file = File(path)
//                        selectedFiles[0] = file
//                        img_eva1.setImageURI(Uri.fromFile(file))
//                    }
//                }
//
//                override fun onHanlderFailure(requestCode: Int, errorMsg: String?) {
//                }
//            })
//        }
//        img_eva2.setOnClickListener {
//            GalleryFinal.openEdit(25, selectedPaths[1], object : GalleryFinal.OnHanlderResultCallback {
//                override fun onHanlderSuccess(reqeustCode: Int, resultList: MutableList<PhotoInfo>?) {
//                    if (null != resultList) {
//                        val path = resultList[0].photoPath
//                        selectedPaths[1] = path
//                        val file = File(path)
//                        selectedFiles[1] = file
//                        img_eva2.setImageURI(Uri.fromFile(file))
//                    }
//                }
//
//                override fun onHanlderFailure(requestCode: Int, errorMsg: String?) {
//                }
//            })
//        }
//        img_eva3.setOnClickListener {
//            GalleryFinal.openEdit(25, selectedPaths[2], object : GalleryFinal.OnHanlderResultCallback {
//                override fun onHanlderSuccess(reqeustCode: Int, resultList: MutableList<PhotoInfo>?) {
//                    if (null != resultList) {
//                        val path = resultList[0].photoPath
//                        selectedPaths[2] = path
//                        val file = File(path)
//                        selectedFiles[2] = file
//                        img_eva3.setImageURI(Uri.fromFile(file))
//                    }
//                }
//
//                override fun onHanlderFailure(requestCode: Int, errorMsg: String?) {
//                }
//            })
//        }

        layout_progress.setOnClickListener { }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            openPicture()
        }
    }

    private fun openPicture() {
//        val selected: ArrayList<String> = selectedPaths as ArrayList<String>
//        selectedPaths to selected
//        val funCfg = FunctionConfig.Builder()
//                .setMutiSelectMaxSize(3/* - chosenSize*/)
//                .setSelected(selectedPaths as ArrayList<String>)
//                .setEnableCamera(true)
//                .build()
//        GalleryFinal.openGalleryMuti(START_GALLERYFINAL, funCfg, object : GalleryFinal.OnHanlderResultCallback {
//            override fun onHanlderSuccess(reqeustCode: Int, resultList: MutableList<PhotoInfo>?) {
//                if (reqeustCode == START_GALLERYFINAL) {
//                    if (null != resultList) {
//                        selectedPaths.clear()
//                        selectedFiles.clear()
//                        for (i in 0 until resultList.size) {
//                            val picPath = resultList[i].photoPath
//                            val picId = resultList[i].photoId
////                            Log.e("testLog", "photoId = $picId \nphotoPath = $picPath")
//                            selectedPaths.add(picPath)
//                            val file = File(picPath)
//                            selectedFiles.add(file)
//                            when (i) {
//                                0 -> {
//                                    img_eva1.setImageURI(Uri.fromFile(file))
//                                    img_eva2.visibility = View.GONE
//                                    img_eva3.visibility = View.GONE
//                                }
//                                1 -> {
//                                    img_eva2.setImageURI(Uri.fromFile(file))
//                                    img_eva2.visibility = View.VISIBLE
//                                }
//                                2 -> {
//                                    img_eva3.setImageURI(Uri.fromFile(file))
//                                    img_eva3.visibility = View.VISIBLE
//                                }
//                            }
//
//                        }
//                        if (selectedPaths.size > 0)
//                            layout_chosen.visibility = View.VISIBLE
//                        else
//                            layout_chosen.visibility = View.GONE
//                    }
//                }
//            }
//
//            override fun onHanlderFailure(requestCode: Int, errorMsg: String?) {
//            }
//        })
    }

    private var cancel: Callback.Cancelable? = null

    fun onCommit(v: View) {
        val evaluate = edt_evaluate.text.toString()
        if (evaluate.isEmpty()) {
            ToastUtil.showToastL("评论为空，不说点什么吗？")
            return
        }
        layout_progress.visibility = View.VISIBLE

        Handler().post {
            val uid = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE).getString(App.PrefNames.USERID, "-1")
            val param = RequestParams(App.CMD)
            val files: MutableList<KeyValue> = ArrayList()
            files.add(KeyValue("cmd", " 1006"))
            files.add(KeyValue("uid", uid))
            files.add(KeyValue("gid", goodsId))
            files.add(KeyValue("ddh", orderId))
            files.add(KeyValue("nr", evaluate))
            var filenames = ""
            for (i in 0 until selectedFiles.size) {
                val dir = File("${Environment.getExternalStorageDirectory()}/Fanshequ")
                if (!dir.exists())
                    dir.mkdir()
                val fileName = "${System.currentTimeMillis()}${(Math.random() * 99999).toInt()}_$i.jpg"
                val file: File = FileUtil.compressImage(selectedFiles[i], "${Environment.getExternalStorageDirectory()}/Fanshequ/$fileName")
                files.add(KeyValue("file$i", file))
                if (i != 0) filenames += ","
                filenames += fileName
            }
            files.add(KeyValue("tupian", filenames))
            val body = MultipartBody(files, "UTF-8")
            param.requestBody = body
            param.isMultipart = true
            layout_progress.visibility = View.VISIBLE
            cancel = x.http().post(param, object : Callback.CommonCallback<String> {
                override fun onCancelled(cex: Callback.CancelledException?) {

                }

                override fun onSuccess(result: String) {
                    Log.e("testLog", "result:" + result)
                    when (JsonSyncUtils.getState(result)) {
                        200 -> {
                            ToastUtil.showToastL("提交成功！")
                            finish()
                        }
                        400->ToastUtil.showToastL("数据错误，请重试")
                    }
                }

                override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                    ToastUtil.showToastL("连接服务器失败，请检查网络连接")
                    Log.e("testLog", "error:" + ex.toString())
                }

                override fun onFinished() {
                    layout_progress.visibility = View.GONE
                    for (i in 6 until files.size - 1) {
                        val file = files[i].value as File
                        if (file.exists()) Log.e("testLog","file$i delete:${file.delete()}")
                    }
                }
            })
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (layout_progress.visibility == View.VISIBLE) {
                val alert = AlertDialog.Builder(this).setMessage("正在上传，确定要退出吗？")
                        .setPositiveButton("终止退出", { _, _ ->
                            cancel?.cancel() ?: return@setPositiveButton
                            finish()
                        })
                        .setNegativeButton("先等等", null)
                alert.show()
                return true
            }
        }

        return super.onKeyDown(keyCode, event)
    }
}
