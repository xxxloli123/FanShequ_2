package com.fanhong.cn.home_page.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fanhong.cn.R;
import com.fanhong.cn.home_page.models.Banner;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/9/15.
 */

public class ActivitiesAdapter2 extends BaseAdapter {

    private ArrayList<Banner> bas;
    private Context context;

    public ActivitiesAdapter2(Context context, ArrayList<Banner> bas) {
        this.bas = bas;
        this.context = context;
    }


    @Override
    public int getCount() {
        return bas == null ? 0 : bas.size();
    }

    @Override
    public Object getItem(int i) {
        return bas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.itme_home_activities, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Picasso.with(context).load(bas.get(i).getTupian()).centerCrop().into(holder.imgShow);
        holder.tvTime.setText(bas.get(i).getSj());
        holder.tvContent.setText(bas.get(i).getTitle());
        return view;
    }

    class ViewHolder {

        ImageView imgShow;
        TextView tvTime;
        TextView tvContent;

        ViewHolder(View view) {

        }
    }
}
