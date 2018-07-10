package com.fanhong.cn.tools;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

public class ScreenUtil {
    public static void setStatusBarView(View v, Context c) {
        /**
         * 设置view高度为statusbar的高度，并填充statusbar
         */
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.width = LinearLayout.LayoutParams.MATCH_PARENT;
        lp.height = getStatusBar(c);
        v.setLayoutParams(lp);
    }

    /**
     * 获取状态栏高度
     * @return
     */
    public static int getStatusBar(Context c){
        /**
         * 获取状态栏高度
         * */
        int statusBarHeight1 = -1;
        //获取status_bar_height资源的ID
        int resourceId = c.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = c.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight1;
    }
}
