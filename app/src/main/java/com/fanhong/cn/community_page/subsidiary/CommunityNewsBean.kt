package com.fanhong.cn.community_page.subsidiary

/**
 * Created by Administrator on 2017/6/30.
 */

data class CommunityNewsBean(val id: String, val photoUrl: String, val title: String, val author: String, val time: String) {
    var news_flag = TYPE_NEWS

    fun setFlag(type: Int): CommunityNewsBean {
        when (type) {
            TYPE_INFORM, TYPE_NOTICE, TYPE_ACTIVE -> this.news_flag = type
            else -> this.news_flag = TYPE_NEWS
        }
        return this
    }

    companion object {
        var TYPE_NEWS = 1//新闻
        var TYPE_INFORM = 2//通知
        var TYPE_NOTICE = 3//公告
        var TYPE_ACTIVE = 4//活动
    }
}
