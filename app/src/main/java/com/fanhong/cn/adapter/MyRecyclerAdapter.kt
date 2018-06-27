package com.fanhong.cn.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import com.fanhong.cn.R
import com.squareup.picasso.Picasso
import java.io.File

open class MyRecyclerAdapter(private val files:ArrayList<File>, private val mCallback: Callback) : RecyclerView.Adapter<MyRecyclerAdapter.MyHolder>() ,View.OnClickListener{

    var mContext :Context? = null

    /**
     * 自定义接口，用于回调按钮点击事件到Activity
     * @author Ivan Xu
     * 2014-11-26
     */

    interface Callback {
        fun click(v: View)
    }

    override fun onClick(v: View?) {
        mCallback.click(v!!)
    }

    override fun getItemCount(): Int =files.size

    override fun onBindViewHolder(holder: MyHolder?, position: Int) {
//       val bitmap = BitmapFactory.decodeFile(files[position].absolutePath)
//        val  file=FileUtil.compressImage(files[position], createTempFile().path)
//        holder!!.img.setImageURI(Uri.fromFile(FileHelp.saveBitmapFile(bitmap)))
        Picasso.with(mContext).load(files[position]).resize(200,200).
                centerCrop().into(holder!!.img)
//        Picasso.with(mContext).

//        val fos= FileOutputStream(files[position])
//        holder!!.img.setImageBitmap(FileUtil.myCompressImage(bitmap))
        holder.ibtDelete.tag=position
        holder.ibtDelete.setOnClickListener(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.itme_img, parent, false)
        mContext=parent.context
        return MyHolder(view)
    }

    class MyHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var img:ImageView = view.findViewById(R.id.img) as ImageView
        var ibtDelete:ImageButton = view.findViewById(R.id.ibt_delete) as ImageButton
    }

}