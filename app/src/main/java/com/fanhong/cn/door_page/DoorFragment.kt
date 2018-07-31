package com.fanhong.cn.door_page


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.fanhong.cn.App

import com.fanhong.cn.R
import com.fanhong.cn.door_page.models.Keymodel
import com.fanhong.cn.http.callback.StringDialogCallback
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.LogUtil
import com.fanhong.cn.tools.ToastUtil
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import kotlinx.android.synthetic.main.activity_top.*
import kotlinx.android.synthetic.main.fragment_door.*
import org.json.JSONException
import org.json.JSONObject
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x


/**
 * A simple [Fragment] subclass.
 */
class DoorFragment : Fragment() {
    val TAG = "DoorFragment"
    private var mSharedPref: SharedPreferences? = null
    private var groupList: MutableList<String>? = null
    private var datamap: MutableMap<String, MutableList<Keymodel>>? = null
    private var adapter: MyExpandableAdapter? = null

    private var imageView: ImageView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_door, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mSharedPref = activity!!.getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        tv_title.text = "门禁钥匙"
        img_back.setImageResource(R.mipmap.refresh)
        var size = (activity!!.windowManager.defaultDisplay.width.toFloat()) / 720
        var params = img_back.layoutParams
        params.height = (53 * size).toInt()
        params.width = (53 * size).toInt()
        img_back.layoutParams = params
//        img_back.setPadding(10,10,10,10)
        img_back.setOnClickListener { refreshKeys() }
        top_extra.text = "添加"
        top_extra.visibility = View.VISIBLE
        top_extra.setOnClickListener { addKeys() }
        all_fillStatusBar.setPadding(0,getStatusBar(),0,0)
        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * 获取状态栏高度
     * @return
     */
    fun  getStatusBar(): Int {
        /**
         * 获取状态栏高度
         */
        var statusBarHeight1 = -1
        //获取status_bar_height资源的ID
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight1
    }

    private fun openDoor(key: String) {
        var params = RequestParams(App.OPEN_URL)
        params.addBodyParameter("key", key)
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onFinished() {
            }

            override fun onSuccess(result: String?) {
                Log.i(TAG+"openDoor", result)
                if (JsonSyncUtils.getJsonValue(result!!, "error") == "succ") {
                    val s = JsonSyncUtils.getJsonValue(result!!, "data")
                    val uuid = JsonSyncUtils.getJsonValue(s, "cmd_uuid")
                    Log.i(TAG, s + "..." + uuid)
                    Handler().postDelayed({ checkOpen(uuid) }, 5000)
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                handler.sendEmptyMessage(12)
            }

        })
    }

    private fun checkOpen(uuid: String) {
        val params = RequestParams(App.CHECK_URL)
        params.addBodyParameter("cmd_uuid", uuid)
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                Log.i(TAG+"checkOpen", result)
                Log.i(TAG, "reslut==>" + result)
                if (JsonSyncUtils.getJsonValue(result, "result") == "0") {
                    handler.sendEmptyMessage(11)
                } else {
//                    网络延时超过3秒的则不提示了
                    handler.sendEmptyMessage(12)
                }
            }

            override fun onError(ex: Throwable, isOnCallback: Boolean) {
            }

            override fun onCancelled(cex: Callback.CancelledException) {
            }

            override fun onFinished() {
                handler.sendEmptyMessage(13)
            }
        })
    }

    private lateinit var m: Keymodel

    /**
     * 展示钥匙
     */
    private fun getKeys() {
        if (!isLogined()) {
            door_expandable_list.visibility=View.GONE
            return
        }
        door_expandable_list.visibility=View.VISIBLE
        groupList = ArrayList()
        datamap = HashMap()
        var params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "125")
        params.addBodyParameter("uid", mSharedPref!!.getString(App.PrefNames.USERID, "-1"))
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onFinished() {
            }

            override fun onSuccess(result: String?) {
                try {
                    var array = JSONObject(result).getJSONArray("data")
                    if (array.length() > 0) {
                        handler.sendEmptyMessage(112)
                        (0 until array.length())
                                .map { array.getJSONObject(it) }
                                .forEach {
                                    var childList = ArrayList<Keymodel>()

                                    var details = it.optString("content")
                                    var array1 = JSONObject(details).getJSONArray("list")
                                    (0 until array1.length())
                                            .map { array1.getJSONObject(it) }
                                            .forEach {
                                                var model = Keymodel(it.optString("bname"),
                                                        it.optString("key"),
                                                        false,
                                                        it.optInt("sh"))
                                                childList.add(model)
                                            }
                                    datamap!![it.optString("xq")] = childList
//                                    datamap!!.put(it.optString("xq"), childList)
                                    groupList!!.add(it.optString("xq"))
                                }
                        adapter = MyExpandableAdapter(activity!!, groupList!!, datamap!!)
                        adapter!!.setOpenClick(object : MyExpandableAdapter.OpenClick {
                            override fun opendoor(key: String, view: ImageView, model: Keymodel) {
                                imageView = view
                                handler.sendEmptyMessage(10)
                                openDoor(key)
                                m=model
//                                openDoor2(key)
                            }

                            override fun nokey() {
                                handler.sendEmptyMessage(14)
                            }

                        })
                        door_expandable_list.setAdapter(adapter)
                        handler.sendEmptyMessage(111)
                    } else {
                        handler.sendEmptyMessage(113)
                    }
                    Log.i(TAG, "datamap==>" + datamap!!.toString())
                    Log.i(TAG, "grouplist==>" + groupList!!.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

        })
    }

    private fun openDoor2(key: String) {
        OkGo.post<String>(App.OPEN_URL)
                .tag(this)//
                .isMultipart(true)
                .params("key", key)
                .execute(object : StringDialogCallback(activity!!) {
                    override fun onSuccess(response: Response<String>) {
                        Log.e("OkGobody", response.body().toString())
                        try {
                            val json = JSONObject(response.body()!!.toString())
                            Handler().postDelayed({ checkOpen(json.getJSONObject("data")
                                    .getString("cmd_uuid")) }, 5000)
                        }catch (e: JSONException) {
                            LogUtil.e("JSONException",e.toString())
                            ToastUtil.showToastL("数据解析异常")
                            e.printStackTrace()
                        }
                    }

                    override fun onError(response: Response<String>) {
                        Log.e("OkGoError", response.exception.toString())
                        AlertDialog.Builder(activity)
                                .setMessage("评价失败！ 是否重试?")
                                .setPositiveButton("确定") { _, _ ->

                                }.setNegativeButton("取消",null) .show()
                    }
                })
    }

    /**
     * 添加钥匙
     */
    private fun addKeys() {
//        ToastUtil.showToastL("refresh clicked")
        if (isLogined())startActivity(Intent(activity, AddKeyActivity::class.java))
        else         ToastUtil.showToastS("请先登录")
//        startActivity(Intent(activity, AddKeyActivity::class.java))
    }

    /**
     * 刷新钥匙
     */
    private fun refreshKeys() {
//        ToastUtil.showToastL("add clicked")
        this.onResume()
    }

    override fun onResume() {
        super.onResume()
        getKeys()
//        adapter = MyExpandableAdapter(activity, groupList!!, datamap!!)
//        adapter!!.setOpenClick(object : MyExpandableAdapter.OpenClick {
//            override fun opendoor(key: String, view: ImageView) {
//                imageView = view
//                handler.sendEmptyMessage(10)
//                openDoor(key)
//            }
//
//            override fun nokey() {
//                handler.sendEmptyMessage(14)
//            }
//
//        })
//        door_expandable_list.setAdapter(adapter)
    }

    private fun isLogined(): Boolean {
        return mSharedPref!!.getString(App.PrefNames.USERID, "-1") != "-1"
    }

    @SuppressLint("HandlerLeak")
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                111 -> adapter!!.notifyDataSetChanged()
                112 -> {
                    door_expandable_list.visibility = View.VISIBLE
                    no_key_txt.visibility = View.GONE
                }
                113 -> {
                    door_expandable_list.visibility = View.GONE
                    no_key_txt.visibility = View.VISIBLE
                }
                10 -> {
                    ToastUtil.showToastS("正在开门...")
                    imageView!!.isEnabled = false
                }
                11 -> ToastUtil.showToastS("开门成功！")
                12 -> ToastUtil.showToastS("开门失败，请重试！")
                13 -> {
                    if (m!=null)m.opening=false
                    imageView!!.isEnabled = true
                }
                14 -> ToastUtil.showToastS("钥匙审核中，请等待审核!")
            }
            super.handleMessage(msg)
        }
    }
}
