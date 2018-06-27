package com.fanhong.cn.moudle;

import java.io.Serializable;
import java.util.ArrayList;

public class RepairInfoM implements Serializable {
    /**
     * id : 37
     * uid : 130
     * lxphone : 13888888888
     * dizhi : 天安数码城
     * concent : 一直以为小心翼翼
     * time : 1528707917667
     * time1 : null
     * time2 : null
     * tupian : ["http://m.wuyebest.com/Public/app/images/gxwx/1528707896835","",""]
     * men : 车库门:1个,人行门:1个,单元门:1个
     * zt : 0
     * wxboy :
     * fs : null
     * pl : null
     * wxphone :
     */

    private String id;
    private String uid;
    private String lxphone;
    private String dizhi;
    private String concent;
    private String time;
    private String time1;
    private String time2;
    private String men;
    private int zt;
    private String wxboy;
    private String fs;
    private String pl;
    private String wxphone;
    /**
     * time1 : null
     * time2 : null
     * tupian : ["http://m.wuyebest.com/Public/app/images/gxwx/1528707896835","",""]
     * zt : 0
     * fs : null
     * pl : null
     */

    private ArrayList<String> imgUrls;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLxphone() {
        return lxphone;
    }

    public void setLxphone(String lxphone) {
        this.lxphone = lxphone;
    }

    public String getDizhi() {
        return dizhi;
    }

    public void setDizhi(String dizhi) {
        this.dizhi = dizhi;
    }

    public String getConcent() {
        return concent;
    }

    public void setConcent(String concent) {
        this.concent = concent;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMen() {
        return men;
    }

    public void setMen(String men) {
        this.men = men;
    }


    public String getWxboy() {
        return wxboy;
    }

    public void setWxboy(String wxboy) {
        this.wxboy = wxboy;
    }


    public String getWxphone() {
        return wxphone;
    }

    public void setWxphone(String wxphone) {
        this.wxphone = wxphone;
    }


    public int getZt() {
        return zt;
    }

    public void setZt(int zt) {
        this.zt = zt;
    }

    public ArrayList<String> getImgUrls() {
        return imgUrls;
    }

    public void setImgUrls(ArrayList<String> imgUrls) {
        this.imgUrls = imgUrls;
    }

    public String getTime1() {
        return time1;
    }

    public void setTime1(String time1) {
        this.time1 = time1;
    }

    public String getTime2() {
        return time2;
    }

    public void setTime2(String time2) {
        this.time2 = time2;
    }

    public void setFs(String fs) {
        this.fs = fs;
    }

    public String getPl() {
        return pl;
    }

    public void setPl(String pl) {
        this.pl = pl;
    }

    public String getFs() {
        return fs;
    }
}
