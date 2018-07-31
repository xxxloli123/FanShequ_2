package com.fanhong.cn.door_page.models

/**
 * Created by Administrator on 2018/2/22.
 */
data class Keymodel( var buildingName:String,
                     var key:String,
                     var opening:Boolean=false,
                     var status:Int){
    override fun toString(): String {
        return "Keymodel(buildingName='$buildingName', key='$key', status=$status)"
    }
}
