package com.fanhong.cn.home_page.adapters;

import android.content.Context;
import android.widget.ImageView;

import com.fanhong.cn.R;
import com.squareup.picasso.Picasso;
import com.youth.banner.loader.ImageLoader;


public class GlideImageLoader extends ImageLoader {

    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        //具体方法内容自己去选择，次方法是为了减少banner过多的依赖第三方包，所以将这个权限开放给使用者去选择
        if (path.toString().contains("R.mipmap"))
            Picasso.with(context.getApplicationContext())
                .load(R.mipmap.banner)
                .into(imageView);
        else Picasso.with(context.getApplicationContext())
                .load(path.toString())
                .into(imageView);
        
    }

//    @Override
//    public ImageView createImageView(Context context) {
//        //圆角
//        return new RoundAngleImageView(context);
//    }
}
