package com.fanhong.cn.home_page

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.widget.AdapterView
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.home_page.adapters.SortAdapter
import com.fanhong.cn.home_page.models.SortModel
import com.fanhong.cn.myviews.SideBar
import com.fanhong.cn.tools.CharacterParser
import kotlinx.android.synthetic.main.activity_choosecell.*
import kotlinx.android.synthetic.main.activity_top.*
import org.json.JSONException
import org.json.JSONObject
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x
import java.util.*
import kotlin.Comparator


/**
 * Created by Administrator on 2018/1/17.
 */
class ChooseCellActivity : AppCompatActivity() {
    var list: MutableList<NameIdModel> = ArrayList()
    var names: MutableList<String> = ArrayList()
    var adapter: SortAdapter? = null
    //汉字转拼音
    var sorts: MutableList<SortModel> = ArrayList()
    var characterParser: CharacterParser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choosecell)
        tv_title.text = getString(R.string.choosegarden)
        img_back.setOnClickListener { finish() }
        getGardenName()
    }

    private fun getGardenName() {
        var params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "29")
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onFinished() {

            }

            override fun onSuccess(result: String) {
                try {
                    var array = JSONObject(result).getJSONArray("data")
                    (0 until array.length())
                            .map { array.getJSONObject(it) }
                            .forEach {
                                list.add(NameIdModel(it.optString("id"), it.optString("name").trim()))
                                names.add(it.optString("name").trim())
                            }
                    initViews()
                } catch (e: JSONException) {

                }
            }

            override fun onCancelled(cex: Callback.CancelledException?) {
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
            }

        })
    }

    private fun initViews() {
        characterParser = CharacterParser.instance
        sidebar.setTextView(dialog)
        sidebar.setOnLetterChangedListener(object : SideBar.OnLetterChangedListener {
            override fun onLetterChanged(s: String) {
                //s字母首次出现的位置
                val position = adapter!!.getPositionForSection(s[0].toInt())
                if (position != -1) {
                    cell_list.setSelection(position)
                }
            }
        })
        filledData(list)
        Collections.sort(sorts,pinyinComparator)
        adapter = SortAdapter(this@ChooseCellActivity,sorts)
        cell_list.adapter = adapter
        cell_list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            var cellName = ((adapter!!.getItem(position)) as SortModel).lname
            var cellId = NameIdModel.findModel(list,cellName)!!.id
//            Log.i("xq","xid==>$cellId")
            var intent = Intent()
            intent.putExtra("gardenName",cellName)
                    .putExtra("gardenId",cellId)
            val editor = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)!!.edit()
            editor.putString(App.PrefNames.GARDENNAME, cellName)
            editor.putString(App.PrefNames.GARDENID, cellId)
            editor.apply()
            setResult(51,intent)
            this@ChooseCellActivity.finish()
        }
        filter_edit.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s.toString())
            }

        })
    }

    /**
     * 为ListView填充数据
     *
     * @param date
     * @return
     */
    private fun filledData(date: List<NameIdModel>) {
        for (i in date) {
            // 汉字转换成拼音
            val pinyin = characterParser!!.getSelling(i.name)
            val sortString = pinyin.substring(0, 1).toUpperCase()
            var sortModel = SortModel(sortString,i.name)
            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]".toRegex())) {
                sortModel.letter = sortString.toUpperCase()
            } else {
                sortModel.letter = "#"
            }
            sorts.add(sortModel)
        }
    }

    private fun filterData(filterStr: String) {
        var filterDateList: MutableList<SortModel> = ArrayList()

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = sorts
        } else {
            if (!names.contains(filterStr)) {
                filterDateList.clear()
                for (sortModel in sorts) {
                    val name = sortModel.lname
                    if (name.indexOf(filterStr) != -1 || characterParser!!.getSelling(name).startsWith(filterStr)) {
                        filterDateList.add(sortModel)
                    }
                }
            } else {
                filterDateList.clear()

            }
        }
        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator)
        adapter!!.updateListView(filterDateList)
    }

    //按拼音或者符号排序的方法
    private val pinyinComparator : Comparator<SortModel> = Comparator { o1, o2 ->
        if (o1.letter == "@" || o2.letter == "#") {
            -1
        } else if (o1.letter == "#" || o2.letter == "@") {
            1
        } else {
            o1.letter.compareTo(o2.letter)
        }
    }


    //小区名字+id的model
    data class NameIdModel(val id: String, val name: String) {
        companion object {
            fun  findModel(list:List<NameIdModel>, name:String):NameIdModel?
               = list.firstOrNull { it.name == name }
        }
    }
}