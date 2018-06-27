package com.fanhong.cn.community_page.subsidiary

/**
 * Created by Administrator on 2017/6/30.
 */

data class CommunityMessageBean(var headUrl: String, var userName: String, var message: String, var msgTime: Long, var type: Int) {

    constructor() : this("", "", "", 0, TYPE_LEFT)

    companion object {
        val TYPE_LEFT = 0
        val TYPE_RIGHT = 1
    }
}
