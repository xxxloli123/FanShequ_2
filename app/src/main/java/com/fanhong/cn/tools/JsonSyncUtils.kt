package com.fanhong.cn.tools

import com.fanhong.cn.community_page.subsidiary.CommunityNewsBean
import com.fanhong.cn.service_page.shop.GoodsDetailsActivity
import com.fanhong.cn.service_page.shop.GoodsListFragment
import com.fanhong.cn.user_page.OrderDetailsActivity
import com.fanhong.cn.user_page.OrderListActivity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


/**
 * Created by Administrator on 2018/1/18.
 */
object JsonSyncUtils {
    /*
     *通用方法
     */

    /**
     *
     * @param jsonObject
     * @param key
     * @return
     */
    @Synchronized
    fun getJsonValue(jsonObject: String, key: String): String {
        var value = ""
        try {
            val obj = JSONObject(jsonObject)
            value = obj.getString(key)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return value
    }

    /**
     *
     * @param jsonObject
     * @return
     */
    @Synchronized
    fun getState(jsonObject: String): Int {
        var value = ""
        try {
            val obj = JSONObject(jsonObject)
            value = obj.getString("state")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return value.toInt()
    }

    /**
     *
     * @param data
     * @param key
     * @return
     */
    @Synchronized
    fun getStringList(data: String, key: String): MutableList<String> {
        val list: MutableList<String> = ArrayList()
        try {
            val jsonArray = JSONArray(data)
            (0 until jsonArray.length())
                    .map { jsonArray.optJSONObject(it) }
                    .mapTo(list) { it.optString(key) }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return list
    }


    /*
     * 定制方法
     */

    @Synchronized
    fun getOrderList(data: String): MutableList<OrderListActivity.MyOrder> {
        val list: MutableList<OrderListActivity.MyOrder> = ArrayList()
        try {
            val jsonArray = JSONArray(data)
            (0 until jsonArray.length())
                    .map { jsonArray.optJSONObject(it) }
                    .mapTo(list) {
                        val id = it.optString("id")
                        val number = it.optString("ddh")
                        val time = it.optString("time")
                        val name = it.optString("goods")
                        val price = it.optString("zjje")
                        OrderListActivity.MyOrder(id, number, name, price, time)
                    }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return list
    }

    @Synchronized
    fun getGoodsList(data: String): MutableList<GoodsListFragment.GoodsInfo> {
        val list: MutableList<GoodsListFragment.GoodsInfo> = ArrayList()
        try {
            val jsonArray = JSONArray(data)
            (0 until jsonArray.length())
                    .map { jsonArray.optJSONObject(it) }
                    .mapTo(list) {
                        val id = it.optString("id")
                        val name = it.optString("name")
                        val pic = it.optString("logo")
                        val content = it.optString("describe")
                        val price = it.optString("jg")
                        GoodsListFragment.GoodsInfo(id, name, pic, content, price)
                    }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return list
    }

    @Synchronized
    fun getOrderGoodsList(data: String, orderId: String): MutableList<OrderDetailsActivity.Goods> {
        val list: MutableList<OrderDetailsActivity.Goods> = ArrayList()
        try {
            val jsonArray = JSONArray(data)
            (0 until jsonArray.length())
                    .map { jsonArray.optJSONObject(it) }
                    .mapTo(list) {
                        val id = it.optString("id")
                        val number = it.optString("gsl")
                        val name = it.optString("name")
                        val isEvaluate = it.optString("pl") == "2"//1未评价，2已评价
                        OrderDetailsActivity.Goods(name, id, number, isEvaluate, orderId)
                    }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return list
    }

    @Synchronized
    fun addNews(list: MutableList<CommunityNewsBean>, jsonObject: String, type: Int): MutableList<CommunityNewsBean> {
        try {
            val obj = JSONObject(jsonObject)
            var datagroup = ""
            when (type) {
                CommunityNewsBean.TYPE_NEWS -> datagroup = "data1"
                CommunityNewsBean.TYPE_INFORM -> datagroup = "data2"
                CommunityNewsBean.TYPE_NOTICE -> datagroup = "data3"
                CommunityNewsBean.TYPE_ACTIVE -> datagroup = "data4"
            }
            val dataJ = obj.getString(datagroup)
            val array = JSONArray(dataJ)
            for (i in 0 until array.length()) {
                val objbean = array.opt(i) as JSONObject
                val id = objbean.getString("id")
                val photoUrl = objbean.getString("logo")
                val title = objbean.getString("bt")
                val author = objbean.getString("zz")
                val time = objbean.getString("time")
                list.add(CommunityNewsBean(id, photoUrl, title, author, time).setFlag(type))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return list
    }

    @Synchronized
    fun getDiscussList(data: String): MutableList<GoodsDetailsActivity.DiscussInfo> {
        val list: MutableList<GoodsDetailsActivity.DiscussInfo> = ArrayList()
        try {
            val jsonArray = JSONArray(data)
            (0 until jsonArray.length())
                    .map { jsonArray.optJSONObject(it) }
                    .mapTo(list) {
                        val name = it.optString("user")
                        val head = it.optString("logo")
                        val date = it.optString("time")
                        val content = it.optString("nr")
                        val pics = arrayOf("","","")
                        val pic = JSONArray(it.optString("tupian"))
//                        (0 until pic.length())
//                                .map { pics[it] = pic.optString(it) }
                        for (i in 0 until pic.length()){
                            pics[i] = pic.optString(i)
                        }
                        GoodsDetailsActivity.DiscussInfo(date,name,head,content,pics[0],pics[1],pics[2])
                    }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return list
    }
}