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
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
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
import com.vondear.rxtool.view.RxToast
import com.vondear.rxui.view.dialog.RxDialogShapeLoading
import kotlinx.android.synthetic.main.activity_add_key.*
import kotlinx.android.synthetic.main.activity_top.*
import kotlinx.android.synthetic.main.agree_sheets.*
import me.leefeng.promptlibrary.PromptDialog
import org.devio.takephoto.app.TakePhotoActivity
import org.devio.takephoto.compress.CompressConfig
import org.devio.takephoto.model.CropOptions
import org.devio.takephoto.model.TImage
import org.devio.takephoto.model.TResult
import org.devio.takephoto.model.TakePhotoOptions
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

class AddKeyActivity : TakePhotoActivity() ,MyRecyclerAdapter.Callback{

    var cellId: String? = null
    var lastCellId: String? = null
    var buildingId: String? = null
    var buildingIdList: MutableList<String>? = ArrayList()
    var buildingList: MutableList<String>? = ArrayList()

    var mSharedPref: SharedPreferences? = null
    var uid: String? = null
    var ssp: SpinerPopWindow<String>? = null
    private var imgs = ArrayList<TImage>()

    private var adapter: MyRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val s=""
        setContentView(R.layout.activity_add_key)
        mSharedPref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        uid = mSharedPref?.getString(App.PrefNames.USERID, "-1")
        var gardenName = mSharedPref!!.getString(App.PrefNames.GARDENNAME, "")
        if (!gardenName.isEmpty()) {
            key_choosegarden.text = gardenName
            cellId = mSharedPref!!.getString(App.PrefNames.GARDENID, "")
            lastCellId = cellId
        }
        initViews()
    }

    fun selectBuilding(v:View){
        //没选择小区
        if (cellId==null)return

        val rxDialogShapeLoading = RxDialogShapeLoading(this)
        rxDialogShapeLoading.show()
        var params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "1001")
        params.addBodyParameter("xid", cellId)
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onFinished() {
            }

            override fun onSuccess(result: String) {
                LogUtil.e("1001",result)
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
                    rxDialogShapeLoading.cancel()
                    ssp?.showAsDropDown(key_choosebuilding)
                }else rxDialogShapeLoading.cancel(RxDialogShapeLoading.RxCancelType.error,"请求数据失败")
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                rxDialogShapeLoading.cancel(RxDialogShapeLoading.RxCancelType.error,"请求数据失败")
            }
        })
    }

    private fun initViews() {
        img_back.setOnClickListener {
            goBack()
        }
        tv_title.text = getString(R.string.applykey)
        key_choosegarden.setOnClickListener {
            startActivityForResult(Intent(this, ChooseCellActivity::class.java), 101)
        }

        img_key_add.setOnClickListener {
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
            if (imgs.size == 0) {
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

    private fun submit2() {
        var filenames = ""
        val request = OkGo.post<String>(App.CMD)
                .tag(this)//
                .isMultipart(true)
                            //        cmd：数据类型
                            //        uid：用户id
                            //        xid：小区id
                            //        dizhi：楼栋id
                            //        xinxi：图片信息
                            //        rltp：人脸图片
                .params("cmd", "1012")
                .params("uid", uid)
                .params("xid", cellId)
                .params("dizhi", buildingId)
        for (i in imgs.indices){
            request.params("touxiang" + (i + 1), File(imgs[i].compressPath))
            filenames += if(i==0){File(imgs[i].compressPath).name}else{","+File(imgs[i].compressPath).name}
        }
        request.params("xinxi", filenames)
        request.execute(object : StringDialogCallback(this) {
            override fun onSuccess(response: Response<String>) {
                Log.e("OkGo 1012", response.body().toString())
                if (response.body().toString().contains("\"cw\":\"1\"")
                        || response.body().toString().contains("\"state\":\"200\"")) {
                    PromptDialog(this@AddKeyActivity).showSuccess("提交成功", true)
                    Handler().postDelayed({ finish() }, 1500)
                } else PromptDialog(this@AddKeyActivity).showError("系统错误")
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
            selectPhoto(true)
        }).setOnChoosePic(View.OnClickListener {
            popupWindow.dismiss()
            selectPhoto(false)
        })
        popupWindow.showAtLocation(layout_add_key, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
    }

    private fun selectPhoto(b: Boolean) {
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
            takePhoto.onPickFromCapture(imageUri)
        } else {
            val builder1 = TakePhotoOptions.Builder()
            builder1.setWithOwnGallery(true)
            takePhoto.setTakePhotoOptions(builder1.create())
            takePhoto.onPickFromGallery()
        }
    }

    override fun takeFail(result: TResult?, msg: String?) {
        RxToast.error("选择图片失败")
        super.takeFail(result, msg)
    }

    override fun takeSuccess(result: TResult?) {
        imgs=(result!!.images)
        super.takeSuccess(result)
        showImg()
    }

    private fun showImg() {
        if (adapter == null) {
            adapter = MyRecyclerAdapter(imgs, this,true)
            rv_img.layoutManager = LinearLayoutManager(this,
                    LinearLayoutManager.HORIZONTAL, false)
            rv_img.adapter = adapter
        } else adapter!!.refresh(imgs)
    }

    override fun click(v: View) {
        imgs.removeAt(v.tag.toString().toInt())
        adapter!!.notifyDataSetChanged()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
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
