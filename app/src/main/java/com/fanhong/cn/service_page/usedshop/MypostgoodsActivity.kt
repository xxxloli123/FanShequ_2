package com.fanhong.cn.service_page.usedshop

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.ToastUtil
import kotlinx.android.synthetic.main.activity_mypostgoods.*
import kotlinx.android.synthetic.main.activity_top.*
import org.json.JSONObject
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x

class MypostgoodsActivity : AppCompatActivity() {
    var list: MutableList<UsedgoodsModel>? = null
    var adapter: MygoodsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypostgoods)
        initViews()
        list = ArrayList()
        adapter = MygoodsAdapter(this@MypostgoodsActivity, list!!)
        adapter!!.setDelete(object : MygoodsAdapter.Delete {
            override fun remove(id: String, position: Int) {

                var builder = AlertDialog.Builder(this@MypostgoodsActivity)
                builder.setTitle("删除卖品")
                        .setMessage("是否确认删除？")
                        .setPositiveButton("确认"){
                            _,_-> deleteData(id, position)
                        }
                        .setNegativeButton("取消",null)
                        .show()
            }

        })
        my_goods_list.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        list?.clear()
        getDatas()
    }

    private fun getDatas() {
        var uid = getSharedPreferences(App.PREFERENCES_NAME,Context.MODE_PRIVATE).getString(App.PrefNames.USERID,"-1")
        var params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "35")
        params.addBodyParameter("uid", uid)
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onFinished() {
            }

            override fun onSuccess(result: String?) {
                var array = JSONObject(result).getJSONArray("data")
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
                adapter?.notifyDataSetChanged()
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

        })
    }

    private fun deleteData(id: String, position: Int) {
        var params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "37")
        params.addBodyParameter("id", id)
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onFinished() {
            }

            override fun onSuccess(result: String) {
                if (JsonSyncUtils.getJsonValue(result, "cw") == "0") {
                    list!!.removeAt(position)
                    adapter!!.notifyDataSetChanged()
                    ToastUtil.showToastS("删除成功！")
                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

        })
    }

    private fun initViews() {
        img_back.setOnClickListener {
            finish()
        }
        tv_title.text = "我的卖品"
    }
}
