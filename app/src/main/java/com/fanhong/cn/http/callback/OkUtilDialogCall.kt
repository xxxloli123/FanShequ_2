package com.fanhong.cn.http.callback

import android.app.Activity
import com.vondear.rxui.view.dialog.RxDialogShapeLoading
import com.zhy.http.okhttp.callback.StringCallback
import okhttp3.Request

abstract class OkUtilDialogCall(activity: Activity) : StringCallback(){
    private val dialog: RxDialogShapeLoading?

    init {
        dialog = RxDialogShapeLoading(activity)
        dialog.setLoadingText("请求网络中")
    }

    override fun onBefore(request: Request?, id: Int) {
        if (dialog != null && !dialog.isShowing) {
            dialog.show()
        }
        super.onBefore(request, id)
    }

    override fun onAfter(id: Int) {
        if (dialog != null && dialog.isShowing) {
            dialog.dismiss()
        }
        super.onAfter(id)
    }
}
