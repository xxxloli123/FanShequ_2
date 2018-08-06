package com.fanhong.cn.service_page.repair

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.home_page.ChooseCellActivity
import com.fanhong.cn.service_page.repair.adapter.MyRecyclerAdapter
import com.fanhong.cn.http.callback.StringDialogCallback
import com.fanhong.cn.myviews.PhotoSelectWindow
import com.fanhong.cn.tools.*
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.lzy.okgo.request.PostRequest
import kotlinx.android.synthetic.main.activity_fill_order.*
import me.leefeng.promptlibrary.PromptDialog
import org.devio.takephoto.app.TakePhotoActivity
import org.devio.takephoto.compress.CompressConfig
import org.devio.takephoto.model.CropOptions
import org.devio.takephoto.model.TImage
import org.devio.takephoto.model.TResult
import org.devio.takephoto.model.TakePhotoOptions
import org.xutils.common.Callback
import org.xutils.common.util.KeyValue
import org.xutils.http.RequestParams
import org.xutils.http.body.MultipartBody
import org.xutils.x
import java.io.File
import kotlin.collections.ArrayList

class FillOrderActivity : TakePhotoActivity(), MyRecyclerAdapter.Callback {

    private var adapter: MyRecyclerAdapter? = null
    var isOwner = false

    private var imgs = ArrayList<TImage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_order)
        tv_title.text = "填写订单"
        if (intent.getIntExtra("owner", 0) == 666) {
            isOwner = true
            edt_community_name.hint = "门牌号"
            edt_phone.visibility = View.GONE
            edt_repair_article.visibility = View.GONE
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
            R.id.edt_community_name -> {
                startActivityForResult(Intent(this@FillOrderActivity, ChooseCellActivity::class.java), 110)
            }
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

            R.id.img_add -> if (imgs.size < 3) addImg() else ToastUtil.showToastL("图片只能上传三张")
            R.id.btn_submit -> submit2()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            51 -> { //选择小区的回调
                val gardenName = data!!.getStringExtra("gardenName")
                edt_community_name.text = gardenName
            }
        }
    }

    private fun submit2() {
        val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val request = OkGo.post<String>(App.CMD)
                .tag(this)//
                .isMultipart(true)
        for (i in imgs.indices) {
            val f = File(imgs[i].compressPath)
            request.params("tupian" + (i + 1), f.name)
            if (!isOwner)request.params("f" + (i + 1), f)
        }
        if (isOwner) {
            if (StringUtils.isEmpty(edt_remark.text.toString())) {
                Toast.makeText(this, "请填写维修内容！", Toast.LENGTH_SHORT).show()
                return
            }
            val r = OkGo.post<String>(App.IMG)
                    .tag(this)//
                    .isMultipart(true)
            for (i in imgs.indices) {
                r.params("f" + (i + 1), File(imgs[i].compressPath))
            }
            r.execute(object : StringDialogCallback(this) {
                override fun onSuccess(response: Response<String>?) {
                    Log.e("OkGoIMG", response!!.body().toString())
                    request
                            //        1053：业主报修（app->平台）
                            //        cmd:数据类型
                            //        uid:用户ID
                            //        qid:小区 id   33
                            //        menpai:门牌号（文本框 用户填写）
                            //        concent:维修内容
                            //        tupian1,tupian2,tupian3:用户上传图片
                            .params("cmd", "1053")
                            .params("uid", pref.getString(App.PrefNames.USERID, "-1"))
                            .params("qid", pref.getString(App.PrefNames.GARDENID, ""))
                            .params("menpai", edt_community_name.text.toString())
                            .params("concent", edt_remark.text.toString())
                            .params("time", System.currentTimeMillis().toString())
                    myExecute(request)
                }
                override fun onError(response: Response<String>) {
                    Log.e("OkGoIMGError", response.exception.toString())
                    AlertDialog.Builder(this@FillOrderActivity).setMessage("提交失败！ 是否重试?")
                            .setPositiveButton("确定") { _, _ ->
                                submit2()
                            }.setNegativeButton("取消", null).show()
                }
            })
        } else {
            val phone = edt_phone.text.toString().replace("-", "")
            if (!StringUtils.validPhoneNum("2", phone)) {
                Toast.makeText(this, "请输入正确的电话号码！", Toast.LENGTH_SHORT).show()
                return
            }
            if (StringUtils.isEmpty(edt_repair_article.text.toString())) {
                Toast.makeText(this, "请选择维修物品！", Toast.LENGTH_SHORT).show()
                return
            }
            request
                    //        1047：报修信息(APP->平台)
                    //        cmd:数据类型
                    //        uid:当前用户ID
                    //        men:需要维修的门
                    //        time;订单号 （时间戳就行）
                    //        lxphone:联系电话
                    //        dizhi:报修地址
                    //        concent:详细信息
                    //        tupian:图片 .tag(this)//
                    .params("cmd", "1047")
                    .params("uid", pref.getString(App.PrefNames.USERID, "-1"))
                    .params("men", edt_repair_article.text.toString())
                    .params("lxphone", edt_phone.text.toString().replace("-", ""))
                    .params("dizhi", edt_community_name.text.toString())
                    .params("concent", edt_remark.text.toString())
                    .params("time", System.currentTimeMillis().toString())
            myExecute(request)
        }
    }

    private fun myExecute(request: PostRequest<String>?) {
        request!!.execute(object : StringDialogCallback(this) {
            override fun onSuccess(response: Response<String>) {
                Log.e("OkGo", response.body().toString())
                if (response.body().toString().contains("\"cw\":\"0\"")
                        || response.body().toString().contains("\"state\":\"200\"")) {
                    PromptDialog(this@FillOrderActivity).showSuccess("提交成功", true)
                    Handler().postDelayed({ finish() }, 1500)
                } else PromptDialog(this@FillOrderActivity).showError("提交失败")
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

    private fun addImg() {
        val popupWindow = PhotoSelectWindow(this@FillOrderActivity)
        popupWindow.setOnTakePhoto(View.OnClickListener {
            popupWindow.dismiss()
            selectPhoto(true)
        }).setOnChoosePic(View.OnClickListener {
            popupWindow.dismiss()
            selectPhoto(false)
        })
        popupWindow.showAtLocation(layout_fill, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
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
            takePhoto.onPickMultipleWithCrop(3 - imgs.size, builder.create())
        }
    }

    override fun takeFail(result: TResult?, msg: String?) {
//        if (result!!.image!=null)imgs = result.images
        ToastUtil.showToastL("选择图片失败")
        super.takeFail(result, msg)
    }

    override fun takeSuccess(result: TResult?) {
        imgs.addAll(result!!.images)
        super.takeSuccess(result)
        showImg()
    }

    private fun showImg() {
        if (adapter == null) {
            adapter = MyRecyclerAdapter(imgs, this)
            rv_img.layoutManager = LinearLayoutManager(this,
                    LinearLayoutManager.HORIZONTAL, false)
            rv_img.adapter = adapter
        } else adapter!!.notifyDataSetChanged()
    }

    override fun click(v: View) {
        imgs.removeAt(v.tag.toString().toInt())
        adapter!!.notifyDataSetChanged()
    }

}
