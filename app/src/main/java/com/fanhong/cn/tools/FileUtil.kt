package com.fanhong.cn.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*
import android.provider.MediaStore
import android.content.ContentResolver
import android.content.Context
import android.net.Uri


object FileUtil {
    /**
     * 压缩图片（质量压缩）
     *
     * @param bitmap
     */
    fun compressImage(bitmap: Bitmap, filename: String): File {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        var options = 100
        while (baos.toByteArray().size / 1024 > 1024) {  //循环判断如果压缩后图片是否大于1M,大于继续压缩
            baos.reset()//重置baos即清空baos
            options -= 10//每次都减少10
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos)//这里压缩options%，把压缩后的数据存放到baos中
        }
        val file = File(filename)
        try {
            val fos = FileOutputStream(file)
            try {
                fos.write(baos.toByteArray())
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

//        bitmap.recycle()
        return file
    }
    /**
     * 压缩图片（质量压缩）
     *
     * @param bitmap
     */
    fun compressImage(file: File, filename: String): File {
        val bitmap:Bitmap = BitmapFactory.decodeFile(file.path)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        var options = 100
        while (baos.toByteArray().size / 1024 > 1024) {  //循环判断如果压缩后图片是否大于1M,大于继续压缩
            baos.reset()//重置baos即清空baos
            options -= 10//每次都减少10
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos)//这里压缩options%，把压缩后的数据存放到baos中
        }
        val file = File(filename)
        try {
            val fos = FileOutputStream(file)
            try {
                fos.write(baos.toByteArray())
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

//        bitmap.recycle()
        return file
    }

    fun getFileByUri(uri: Uri, context: Context): File? {
        var path: String? = null
        if ("file" == uri.getScheme()) {
            path = uri.getEncodedPath()
            if (path != null) {
                path = Uri.decode(path)
                val cr = context.getContentResolver()
                val buff = StringBuffer()
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=").append("'$path'").append(")")
                val cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA), buff.toString(), null, null)
                var index = 0
                var dataIdx = 0
                cur!!.moveToFirst()
                while (!cur!!.isAfterLast()) {
                    index = cur!!.getColumnIndex(MediaStore.Images.ImageColumns._ID)
                    index = cur!!.getInt(index)
                    dataIdx = cur!!.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    path = cur!!.getString(dataIdx)
                    cur!!.moveToNext()
                }
                cur!!.close()
                if (index == 0) {
                } else {
                    val u = Uri.parse("content://media/external/images/media/$index")
                    println("temp uri is :$u")
                }
            }
            if (path != null) {
                return File(path)
            }
        } else if ("content" == uri.getScheme()) {
            // 4.2.2以后
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.getContentResolver().query(uri, proj, null, null, null)
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = cursor.getString(columnIndex)
            }
            cursor.close()

            return File(path)
        } else {
            //Log.i(TAG, "Uri Scheme:" + uri.getScheme());
        }
        return null
    }
}
