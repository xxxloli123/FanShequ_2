package com.fanhong.cn.service_page.shop

import org.xutils.db.annotation.Column
import org.xutils.db.annotation.Table
import java.io.Serializable

/**
 * Created by Administrator on 2018/3/1.
 */
@Table(name = "goods_car", onCreated = "")
class GoodsCarTable() : Serializable {
    @Column(name = "id", isId = true, autoGen = true)
    var id: Int = 0
    @Column(name = "c_uid", isId = false, autoGen = false)
    var uid: String = ""
    @Column(name = "c_id", isId = false, autoGen = false)
    var gid: String = ""
    @Column(name = "c_name")
    var name: String = ""
    @Column(name = "c_logo")
    var logo: String = ""
    @Column(name = "c_content")
    var content: String = ""
    @Column(name = "c_price")
    var price: String = ""
    @Column(name = "c_unit")
    var unit: String = ""
    @Column(name = "c_count")
    var count: Int = 0
    @Column(name = "c_select")
    var select: Boolean = false

    constructor(gid: String, name: String, logo: String, content: String, price: String, unit: String, count: Int) : this() {
        this.gid = gid
        this.name = name
        this.logo = logo
        this.content = content
        this.price = price
        this.unit = unit
        this.count = count
    }

    override fun toString(): String {
        return "{gid = $gid, name = $name, logo = $logo, content = $content, price = $price, unit = $unit, count = $count}"
    }
}