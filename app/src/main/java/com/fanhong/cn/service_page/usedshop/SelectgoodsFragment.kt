package com.fanhong.cn.service_page.usedshop


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.ToastUtil
import kotlinx.android.synthetic.main.fragment_select_usedgoods.*
import org.json.JSONObject
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x

/**
 * Created by Administrator on 2018/2/24.
 */
class SelectgoodsFragment : Fragment() {

    //    var mSharedPref: SharedPreferences? = null
    var adapter: UsedgoodsAdapter? = null
    var list: MutableList<UsedgoodsModel>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater!!.inflate(R.layout.fragment_select_usedgoods, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list = ArrayList()
        adapter = UsedgoodsAdapter(activity!!, list!!)
        adapter!!.setCallSeller(object : UsedgoodsAdapter.CallSeller {
            override fun onCall(phone: String) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.CALL_PHONE), 100)
                        var msg = handler.obtainMessage(15, phone)
                        handler.sendMessage(msg)
                        return
                    }
                }
                callNumber(phone)
            }

        })
        used_goods_list.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        list!!.clear()
        getDatas()
    }

    private fun getDatas() {

        var params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "33")
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onFinished() {
            }

            override fun onSuccess(result: String?) {
                var array = JSONObject(result).getJSONArray("data")
                if (array.length() > 0) {
                    handler.sendEmptyMessage(13)
                    (0 until array.length())
                            .map { array.getJSONObject(it) }
                            .forEach {
                                var model = UsedgoodsModel(it.optString("name"),
                                        it.optString("tupian"),
                                        "商品描述：" + it.optString("ms"),
                                        "卖家电话：" + it.optString("dh"),
                                        "卖家姓名：" + it.optString("user"),
                                        it.optString("id"), it.optString("jg"))
                                list!!.add(model)
                            }
                    handler.sendEmptyMessage(11)
                } else {
                    handler.sendEmptyMessage(12)
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callNumber(number!!)
            } else
                ToastUtil.showToastL("需要通话权限！")
        }
    }

    private fun callNumber(num: String) {
        val intent = Intent(Intent.ACTION_CALL)
        val data = Uri.parse("tel:" + num)
        intent.data = data
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private var number: String? = null
    var handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                11 -> adapter!!.notifyDataSetChanged()
                12 -> {
                    used_goods_list.visibility = View.GONE
                    no_goods_txt.visibility = View.VISIBLE
                }
                13 -> {
                    used_goods_list.visibility = View.VISIBLE
                    no_goods_txt.visibility = View.GONE
                }
                15 -> {
                    number = msg.obj as String
                }
            }
            super.handleMessage(msg)
        }
    }
}