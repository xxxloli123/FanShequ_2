package com.fanhong.cn.service_page.repair

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Toast
import cn.finalteam.galleryfinal.FunctionConfig
import cn.finalteam.galleryfinal.GalleryFinal
import cn.finalteam.galleryfinal.model.PhotoInfo
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.home_page.ChooseCellActivity
import com.fanhong.cn.myviews.SpinerPopWindow
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.StringUtils
import kotlinx.android.synthetic.main.activity_repair.*
import kotlinx.android.synthetic.main.activity_top.*
import org.xutils.common.Callback
import org.xutils.common.util.KeyValue
import org.xutils.http.RequestParams
import org.xutils.http.body.MultipartBody
import org.xutils.view.annotation.Event
import org.xutils.x
import java.io.File
import java.util.*

/**
 * Created by Administrator on 2017/8/21.
 */
class RepairActivity : AppCompatActivity() {

//    private var pop: SpinerPopWindow<String>? = null
    private var isinput = true
    private var bundle: Bundle? = null
    private var doorType = "人行门"
//    private var cellsList: List<String> = ArrayList()
    private val picPaths = ArrayList<String>()
    private var cancel: Callback.Cancelable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair)
        x.view().inject(this)
        tv_title.text = "上门维修"
        edt_input_phone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (isinput) {
                    isinput = false
                    var text = s.toString().trim { it <= ' ' }.replace("-", "")
                    if (text.isNotEmpty()) {
                        if (text[0] == '1') {//以‘1’开头的说明是电话号码，否则认为是座机号码
                            text = StringUtils.addChar(3, text, '-')
                            edt_input_phone.setText(text)
                        } else {
                            text = StringUtils.addChar(text, '-')
                            edt_input_phone.setText(text)
                        }
                    }
                    edt_input_phone.setSelection(text.length)
                } else
                    isinput = true
            }
        })

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

//        val param = RequestParams(App.CMD)
//        param.addParameter("cmd", 1015)
//        x.http().post(param, object : Callback.CommonCallback<String> {
//            override fun onSuccess(result: String) {
//                if (JsonSyncUtils.getJsonValue(result, "state") == "200") {
//                    val data = JsonSyncUtils.getJsonValue(result, "data")
//                    cellsList = JsonSyncUtils.getStringList(data, "name")
//                }
//            }
//
//            override fun onError(ex: Throwable, isOnCallback: Boolean) {
//
//            }
//
//            override fun onCancelled(cex: Callback.CancelledException) {
//
//            }
//
//            override fun onFinished() {
//
//            }
//        })
    }

    @Event(R.id.img_back, R.id.btn_confirm, R.id.img_Add, R.id.rb_human_door,
            R.id.rb_unit_door, R.id.rb_park_door, R.id.rb_else_door, R.id.tv_cellName)
    private fun onCLicks(v: View) {
        when (v.id) {
            R.id.img_back -> finish()
            R.id.tv_cellName -> {
//                pop = SpinerPopWindow(this, cellsList , ""){ _, _, position, _ ->
//                    tv_cellName.text = cellsList[position]
//                    pop?.dismiss()
//                }
//                pop?.width = tv_cellName.width
//                pop?.showAsDropDown(tv_cellName, 0, 0)
                startActivityForResult(Intent(this,ChooseCellActivity::class.java),101)
            }
            R.id.rb_human_door -> {
                rb_unit_door!!.isChecked = false
                rb_park_door!!.isChecked = false
                rb_else_door!!.isChecked = false
                doorType = "人行门"
            }
            R.id.rb_unit_door -> {
                rb_human_door!!.isChecked = false
                rb_park_door!!.isChecked = false
                rb_else_door!!.isChecked = false
                doorType = "单元门"
            }
            R.id.rb_park_door -> {
                rb_human_door!!.isChecked = false
                rb_unit_door!!.isChecked = false
                rb_else_door!!.isChecked = false
                doorType = "车库门"
            }
            R.id.rb_else_door -> {
                rb_human_door!!.isChecked = false
                rb_unit_door!!.isChecked = false
                rb_park_door!!.isChecked = false
                doorType = "其他"
            }
            R.id.btn_confirm -> if (getForms()) {
                val params = RequestParams(App.CMD)
                val body = ArrayList<KeyValue>()
                body.add(KeyValue("cmd", "1014"))
                body.add(KeyValue("name", bundle!!.getString("name")))
                body.add(KeyValue("phone", bundle!!.getString("phone")))
                body.add(KeyValue("xqname", tv_cellName.text.toString()))
                body.add(KeyValue("dizhi", bundle!!.getString("addr")))
                body.add(KeyValue("men", doorType))
                body.add(KeyValue("concent", bundle!!.getString("details")))
                for (i in picPaths.indices) {
                    val file = File(picPaths[i])
                    body.add(KeyValue("file" + (i + 1), file))
                    body.add(KeyValue("tupian" + (i + 1), file.name))
                }
                params.requestBody = MultipartBody(body, "UTF-8")
                params.isMultipart = true
                val dialog = AlertDialog.Builder(this).setMessage("正在提交").create()
                dialog.setCancelable(false)
                cancel = x.http().post(params, object : Callback.CommonCallback<String> {
                    override fun onSuccess(s: String) {
//                        Log.e("TestLog", s)
                        if (JsonSyncUtils.getJsonValue(s, "state") == "200") {
                            val intent = Intent(this@RepairActivity, RepairSuccessActivity::class.java)
                            intent.putExtras(bundle!!)
                            startActivity(intent)
                        } else {
                            AlertDialog.Builder(this@RepairActivity).setMessage("提交失败！").setPositiveButton("确定", null).show()
                        }
                    }

                    override fun onError(throwable: Throwable, b: Boolean) {
//                        Log.e("TestLog", "err")
                        AlertDialog.Builder(this@RepairActivity).setMessage("提交失败！").setPositiveButton("确定", null).show()
                    }

                    override fun onCancelled(e: Callback.CancelledException) {
//                        Log.e("TestLog", "cancel")
                    }

                    override fun onFinished() {
                        dialog.dismiss()
                    }
                })
                dialog.show()
            } else
                Toast.makeText(this, "您的输入有误，请检查！", Toast.LENGTH_SHORT).show()
            R.id.img_Add -> {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    val checkCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    val checkSD = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (checkCamera + checkSD != 0) {
                        requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 102)
                        return
                    } else
                        openPicture(checkCamera)
                } else
                    openPicture(0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==51){
            tv_cellName.text =data?.getStringExtra("gardenName")
        }
    }

    private fun getForms(): Boolean {
        val name = edt_input_name.text.toString()
        if (StringUtils.isEmpty(name)) {
            Toast.makeText(this, "请输入联系人姓名！", Toast.LENGTH_SHORT).show()
            return false
        }
        val phone = edt_input_phone.text.toString().replace("-", "")
        if (!StringUtils.validPhoneNum("2", phone)) {
            Toast.makeText(this, "请输入正确的电话号码！", Toast.LENGTH_SHORT).show()
            return false
        }
        val addr = edt_input_address.text.toString()
        if (StringUtils.isEmpty(addr)) {
            Toast.makeText(this, "请输入详细地址！", Toast.LENGTH_SHORT).show()
            return false
        }
        val details = edt_input_details.text.toString()
        if (StringUtils.isEmpty(details)) {
            Toast.makeText(this, "请简要描述您所报修的损坏情况！", Toast.LENGTH_SHORT).show()
            return false
        }
        bundle = Bundle()
        bundle!!.putString("name", name)
        bundle!!.putString("phone", phone)
        bundle!!.putString("addr", addr)
        bundle!!.putString("details", details)
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 102) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            openPicture(grantResults[0])
        }
    }

    private fun openPicture(camera: Int) {
        val cfg = FunctionConfig.Builder()
                .setMutiSelectMaxSize(3/* - chosenSize*/)
                .setSelected(picPaths)
                .setEnableCamera(camera == 0)//如果有相机权限则允许使用相机
                .build()
        GalleryFinal.openGalleryMuti(103, cfg, object : GalleryFinal.OnHanlderResultCallback {
//            HanlderSuccess 处理成功
            override fun onHanlderSuccess(reqeustCode: Int, resultList: List<PhotoInfo>) {
                if (reqeustCode == 103) {
                    picPaths.clear()
                    if (resultList.isNotEmpty()) {
                        layout_chosen.visibility = View.VISIBLE
                        for (i in resultList) {
                            picPaths.add(i.photoPath)
                            when (resultList.indexOf(i)) {
                                0 -> {
                                    img_1.setImageURI(Uri.fromFile(File(i.photoPath)))
                                    img_2.visibility = View.GONE
                                    img_3.visibility = View.GONE
                                }
                                1 -> {
                                    img_2.setImageURI(Uri.fromFile(File(i.photoPath)))
                                    img_2.visibility = View.VISIBLE
                                }
                                2 -> {
                                    img_3.setImageURI(Uri.fromFile(File(i.photoPath)))
                                    img_3.visibility = View.VISIBLE
                                }
                            }
                        }
                    } else
                        layout_chosen!!.visibility = View.GONE
                }
            }

            override fun onHanlderFailure(requestCode: Int, errorMsg: String) {

            }
        })
    }
}
