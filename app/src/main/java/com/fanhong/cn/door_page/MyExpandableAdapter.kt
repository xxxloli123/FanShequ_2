package com.fanhong.cn.door_page

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.fanhong.cn.R
import com.fanhong.cn.door_page.models.Keymodel
import com.zhy.autolayout.utils.AutoUtils
import org.xutils.view.annotation.ViewInject
import org.xutils.x


/**
 * Created by Administrator on 2018/2/22.
 */
class MyExpandableAdapter(val context: Context,
                          private val groupList: MutableList<String>,
                          private val datamap: MutableMap<String, MutableList<Keymodel>>):BaseExpandableListAdapter() {

    private val STRINGS = arrayOf("审核中", "可用", "审核未通过")
    private val COLOR = intArrayOf(R.color.red, R.color.skyblue, R.color.red)
    private val MIPMAP = intArrayOf(R.mipmap.yaoshizzsh, R.mipmap.yaoshi, R.mipmap.yaoshizzsh)

    interface OpenClick{
        fun opendoor(key: String, view: ImageView, model: Keymodel)
        fun nokey()
    }
    private var openClick:OpenClick?=null

    fun setOpenClick(openClick:OpenClick){
        this.openClick = openClick
    }

    override fun getGroup(groupPosition: Int): Any {
        return groupList!![groupPosition]
    }

    //子项是否可选中，如果需要设置子项的点击事件，需要返回true
    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    //是否具有稳定id
    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?,
                              parent: ViewGroup?): View {
        var groupHolder:GroupHolder
        var view:View
        if(convertView==null){
            view = LayoutInflater.from(context).inflate(R.layout.item_door_key1,null)
            groupHolder = GroupHolder(view)
            view.tag = groupHolder
        }else{
            view = convertView
            groupHolder = view.tag as GroupHolder
        }
        groupHolder.cellName!!.text = groupList[groupPosition]
        if(isExpanded){
            groupHolder.icoNext!!.setImageResource(R.mipmap.ico_qj1)
        }else{
            groupHolder.icoNext!!.setImageResource(R.mipmap.ico_qj)
        }
        return view
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return datamap[groupList[groupPosition]]!!.size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return datamap[groupList[groupPosition]]!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean,
                              convertView: View?, parent: ViewGroup?): View {
        var childHolder:ChildHolder
        var view1:View
        if(convertView==null){
            view1 = LayoutInflater.from(context).inflate(R.layout.item_door_key2,parent,false)
            childHolder = ChildHolder(view1)
            view1.tag = childHolder
        }else{
            view1 = convertView
            childHolder = view1.tag as ChildHolder
        }
        var model = getChild(groupPosition,childPosition) as Keymodel
        childHolder.buidingName!!.text = model.buildingName
        val status = model.status
        val key = model.key
        childHolder.icon!!.setImageResource(MIPMAP[status])
        childHolder.keyData!!.text = STRINGS[status]
        childHolder.keyData!!.setTextColor(ContextCompat.getColor(context,COLOR[status]))
        val holder = childHolder
        childHolder.power!!.setOnClickListener{
//            ToastUtil.showToastS("第"+groupPosition+"组的第"+childPosition+"个子项被点击了")
            when(status){
                0-> openClick!!.nokey()
                1-> {
                    openClick!!.opendoor(key,holder.power!!,model)
                    model.opening=true
                }
            }
        }
        holder.power!!.isEnabled=!model.opening
        return view1
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return groupList!!.size
    }

    private class GroupHolder(view: View){
//        @ViewInject(R.id.cell_item_layout)
//        var layout: AutoLinearLayout? = null
        @ViewInject(R.id.cell_name)
        var cellName: TextView? = null
        @ViewInject(R.id.door_next_img)
        var icoNext: ImageView? = null
        init {
            x.view().inject(this,view)
            AutoUtils.autoSize(view)
        }
    }

    private class ChildHolder(view:View){
        @ViewInject(R.id.icon_key)
        var icon: ImageView? = null
        @ViewInject(R.id.loudong_name)
        var buidingName: TextView? = null
        @ViewInject(R.id.key_status)
        var keyData: TextView? = null
        @ViewInject(R.id.power_img)
        var power: ImageView? = null
        init {
            x.view().inject(this,view)
            AutoUtils.autoSize(view)
        }
    }

}