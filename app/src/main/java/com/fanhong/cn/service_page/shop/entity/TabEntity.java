package com.fanhong.cn.service_page.shop.entity;

import com.fanhong.cn.R;
import com.flyco.tablayout.listener.CustomTabEntity;

public class TabEntity implements CustomTabEntity {
    public String title;
    public int selectedIcon;
    public int unSelectedIcon;

    public TabEntity(String title) {
        this.title = title;
        this.selectedIcon = R.mipmap.smiling;
        this.unSelectedIcon = R.mipmap.smiling;
    }

    @Override
    public String getTabTitle() {
        return title;
    }

    @Override
    public int getTabSelectedIcon() {
        return selectedIcon;
    }

    @Override
    public int getTabUnselectedIcon() {
        return unSelectedIcon;
    }
}
