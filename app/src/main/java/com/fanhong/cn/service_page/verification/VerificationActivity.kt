package com.fanhong.cn.service_page.verification

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.icu.lang.UScript.getCode
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.LocationSource
import com.amap.api.maps.model.*
import com.amap.api.maps.model.animation.TranslateAnimation
import com.amap.api.maps2d.CameraUpdateFactory
import com.amap.api.maps2d.model.MyLocationStyle
import com.amap.api.maps2d.model.Text
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.tools.JsonSyncUtils
import com.fanhong.cn.tools.StringUtils
import kotlinx.android.synthetic.main.activity_verification.*
import kotlinx.android.synthetic.main.agree_sheets.*
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x
import java.util.*

class VerificationActivity : AppCompatActivity(), AMapLocationListener, LocationSource,
        GeocodeSearch.OnGeocodeSearchListener{
    override fun onRegeocodeSearched(result: RegeocodeResult?, rCode: Int) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result?.regeocodeAddress != null
                    && result.regeocodeAddress.formatAddress != null) {
                val address =  (result.regeocodeAddress.city
                        + result.regeocodeAddress.district
                        + result.regeocodeAddress.streetNumber.street
                        + result.regeocodeAddress.streetNumber.number
                        +(result.regeocodeAddress.pois[0])
                        )
                default_location.setText(address)
            }
        } else {
            Toast.makeText(this@VerificationActivity, "error code is $rCode", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onGeocodeSearched(p0: GeocodeResult?, p1: Int) {
    }

    private var bundle: Bundle? = null
    private var mListener: LocationSource.OnLocationChangedListener? = null
    private var mlocationClient: AMapLocationClient? = null
    private var mLocationOption: AMapLocationClientOption? = null

    private var aMap: AMap? = null
    private var locationMarker: Marker? = null
    private var searchLatlonPoint: LatLonPoint? = null
    private var geocoderSearch: GeocodeSearch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)
        tv_title.text = "年审预约"
        img_back.setOnClickListener { finish() }
        sheet_protocol.text = "《代办年审须知》"
        bundle = savedInstanceState
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermission()
        } else {
            init(bundle)
        }

        get_code.setOnClickListener {
            if (StringUtils.validPhoneNum("2", car_hostphone.text.toString())) {
                getCode(car_hostphone.text.toString())
            } else {
                Toast.makeText(this, "请输入正确的电话号码！", Toast.LENGTH_SHORT).show()
            }
        }
        sheet_protocol.setOnClickListener { createDialog() }
        submit_caryuyue.setOnClickListener {
            if (TextUtils.isEmpty(default_location.text)) {
                Toast.makeText(this, "请输入地址！", Toast.LENGTH_SHORT).show()
            } else if (TextUtils.isEmpty(car_hostname.text)) {
                Toast.makeText(this, "请输入姓名！", Toast.LENGTH_SHORT).show()
            } else if (TextUtils.isEmpty(car_hostphone.text) || !StringUtils.validPhoneNum("2", car_hostphone.text.toString())) {
                Toast.makeText(this, "请输入正确的手机号！", Toast.LENGTH_SHORT).show()
                //                } else if (!getCode.getText().toString().equals("")) {
                //                    Toast.makeText(this, "验证码错误！", Toast.LENGTH_SHORT).show();
            } else if (!agree_sheet_protocol.isChecked) {
                Toast.makeText(this, "请阅读并同意代办年审须知！", Toast.LENGTH_SHORT).show()
            } else {
                submitData()
            }
        }
    }

    private fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(applicationContext, "没有权限,请手动开启定位权限", Toast.LENGTH_SHORT).show()
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            ActivityCompat.requestPermissions(this@VerificationActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE), 100)
        } else {
            init(bundle)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>
                                            , grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 获取到权限，作相应处理（调用定位SDK应当确保相关权限均被授权，否则可能引起定位失败）
                init(bundle)
            } else {
                // 没有获取到权限，做特殊处理
                Toast.makeText(applicationContext, "获取位置权限失败，请手动开启", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun init(savedInstanceState: Bundle?) {
        geocoderSearch = GeocodeSearch(this)
        geocoderSearch!!.setOnGeocodeSearchListener(this)
        gaode_map.onCreate(savedInstanceState)
        if (aMap == null) {
            aMap = gaode_map.map
            setUpMap()
        }
        aMap!!.setOnMapLoadedListener { addMarkerInScreenCenter(null) }
        aMap!!.setOnCameraChangeListener(object :AMap.OnCameraChangeListener{
            override fun onCameraChangeFinish(cameraPosition: CameraPosition) {
                geoAddress()
                startJumpAnimation()
                searchLatlonPoint = LatLonPoint(cameraPosition.target.latitude, cameraPosition.target.longitude)
            }

            override fun onCameraChange(p0: CameraPosition?) {
            }
        })
    }

    fun startJumpAnimation() {
        if (locationMarker != null) {
            //根据屏幕距离计算需要移动的目标点
            val latLng = locationMarker!!.position
            val point = aMap!!.projection.toScreenLocation(latLng)
            point.y -= dip2px(this, 125f)
            val target = aMap!!.projection
                    .fromScreenLocation(point)
            //使用TranslateAnimation,填写一个需要移动的目标点
            val animation = TranslateAnimation(target)
            animation.setInterpolator { input ->
                // 模拟重加速度的interpolator
                if (input <= 0.5) {
                    (0.5f - 2.0 * (0.5 - input) * (0.5 - input)).toFloat()
                } else {
                    (0.5f - Math.sqrt(((input - 0.5f) * (1.5f - input)).toDouble())).toFloat()
                }
            }
            //整个移动所需要的时间
            animation.setDuration(600)
            //设置动画
            locationMarker!!.setAnimation(animation)
            //开始动画
            locationMarker!!.startAnimation()

        } else {
            Log.e("ama", "screenMarker is null")
        }
    }
    //dip和px转换
    private fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
    fun geoAddress() {
        if (searchLatlonPoint != null) {
            val query = RegeocodeQuery(searchLatlonPoint, 200f, GeocodeSearch.AMAP)// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
            geocoderSearch!!.getFromLocationAsyn(query)
        }
    }
    private fun addMarkerInScreenCenter(locationLatLng: LatLng?) {
        val latLng = aMap!!.cameraPosition.target
        val screenPosition = aMap!!.projection.toScreenLocation(latLng)
        locationMarker = aMap!!.addMarker(MarkerOptions()
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.dingwei)))
        //设置Marker在屏幕上,不跟随地图移动
        locationMarker!!.setPositionByPixels(screenPosition.x, screenPosition.y)
        locationMarker!!.zIndex = 1f

    }

    /**
     * 设置一些amap的属性
     */
    private fun setUpMap() {
//        aMap!!.uiSettings.isZoomControlsEnabled = false
        aMap!!.setLocationSource(this)// 设置定位监听
        aMap!!.uiSettings.isMyLocationButtonEnabled = true// 设置默认定位按钮是否显示
        aMap!!.isMyLocationEnabled = true// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
//        setupLocationStyle()
        aMap!!.setMyLocationType(AMap.LOCATION_TYPE_LOCATE)
    }

    /**
     *
    private fun setupLocationStyle() {
    // 自定义系统定位蓝点
    val myLocationStyle = MyLocationStyle()
    // 自定义定位蓝点图标
    myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.gps_point))
    // 自定义精度范围的圆形边框颜色
    myLocationStyle.strokeColor(Color.argb(180, 3, 145, 255))
    //自定义精度范围的圆形边框宽度
    myLocationStyle.strokeWidth(4f)
    // 设置圆形的填充颜色
    myLocationStyle.radiusFillColor(Color.argb(10, 0, 0, 180))
    // 将自定义的 myLocationStyle 对象添加到地图上
    aMap!!.setMyLocationStyle(myLocationStyle)
    }
     */

    private var agreeSheet: Button? = null
    private fun createDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.layout_dialog_sheet, null)
        builder.setView(view)
        val alertDialog = builder.create()
        alertDialog.show()
        agreeSheet = view.findViewById(R.id.read_and_agree)
        val textView = view.findViewById<TextView>(R.id.tv_content)
        textView.setText(R.string.carmustknows)
        agreeSheet!!.setOnClickListener {
            alertDialog.dismiss()
            agree_sheet_protocol.isChecked = true
        }
    }

    //获取手机验证码
    private fun getCode(str: String) {
        val params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "1")
        params.addBodyParameter("name", str)
        startCount()
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onSuccess(s: String) {
                if (JsonSyncUtils.getJsonValue(s, "cw") == "0") {
                    Toast.makeText(this@VerificationActivity, "获取验证码成功！请耐心等待服务器的短信", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@VerificationActivity, "重新获取验证码", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(ex: Throwable, isOnCallback: Boolean) {

            }

            override fun onCancelled(cex: Callback.CancelledException) {

            }

            override fun onFinished() {

            }
        })
    }

    private fun goNext() {
        val intent = Intent(this, CarFormConfirmActivity::class.java)
        intent.putExtra("address", default_location.text.toString())
        intent.putExtra("name", car_hostname.text.toString())
        intent.putExtra("phone", car_hostphone.text.toString())
        startActivity(intent)
    }
    private var timerTask: TimerTask? = null
    private var timer: Timer? = null
    private val handler = @SuppressLint("HandlerLeak") object : Handler() {
        override fun handleMessage(msg: Message) {
            val tt = msg.what
            when (tt) {
                65 -> mListener = msg.obj as LocationSource.OnLocationChangedListener
                80 -> {
                    get_code.text = msg.arg1.toString() + "秒后可重试"
                    get_code.isEnabled = false
                }
                81 -> {
                    get_code.text = "重新获取验证码"
                    get_code.isEnabled = true
                    timer!!.cancel()
                }
            }
        }
    }

    fun startCount() {
        timer = Timer()
        timerTask = object : TimerTask() {
            internal var count = 30

            override fun run() {
                if (count > 0) {
                    val message = Message()
                    message.what = 80
                    message.arg1 = count
                    handler.sendMessage(message)
                } else {
                    val message1 = Message()
                    message1.what = 81
                    handler.sendMessage(message1)
                }
                count--
            }
        }
        timer!!.schedule(timerTask, 0, 1000)
    }

    private fun submitData() {
        val params = RequestParams(App.CMD)
        params.addBodyParameter("cmd", "85")
        params.addBodyParameter("yzm", input_code.text.toString())
        //        params.addBodyParameter("mapdz",editText1.getText().toString());
        //        params.addBodyParameter("name",editText2.getText().toString());
        //        params.addBodyParameter("phone",editText3.getText().toString());
        x.http().post(params, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String) {
                val cw = JsonSyncUtils.getJsonValue(result, "cw")
                if (cw == "1") {
                    Toast.makeText(this@VerificationActivity, "短信验证码错误", Toast.LENGTH_SHORT).show()
                } else if (cw == "0") {
                    goNext()
                }
            }

            override fun onError(ex: Throwable, isOnCallback: Boolean) {

            }

            override fun onCancelled(cex: Callback.CancelledException) {

            }

            override fun onFinished() {

            }
        })
    }

    override fun deactivate() {
        mListener = null
        if (mlocationClient != null) {
            mlocationClient!!.stopLocation()
            mlocationClient!!.onDestroy()
        }
        mlocationClient = null
    }

    override fun activate(onLocationChangedListener: LocationSource.OnLocationChangedListener?) {
        val msg = handler.obtainMessage(65)
        msg.obj = onLocationChangedListener
        handler.sendMessage(msg)
        if (mlocationClient == null)
            mlocationClient = AMapLocationClient(applicationContext)
        mLocationOption = AMapLocationClientOption()
        //设置定位监听
        mlocationClient!!.setLocationListener(this)
        //设置为高精度定位模式
        mLocationOption!!.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        //只定位一次
        mLocationOption!!.isOnceLocation = true
        //如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
        mLocationOption!!.isNeedAddress = true
        //设置定位参数
        mlocationClient!!.setLocationOption(mLocationOption)
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求

        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        mlocationClient!!.startLocation()
    }

    override fun onLocationChanged(aMapLocation: AMapLocation?) {
//        handler.sendEmptyMessage(65)
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation.errorCode == 0) {
                //                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                //                amapLocation.getLatitude();//获取纬度
                //                amapLocation.getLongitude();//获取经度
                //                amapLocation.getAccuracy();//获取精度信息
                //                amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                //                amapLocation.getCountry();//国家信息
                //                amapLocation.getProvince();//省信息
                //                amapLocation.getCity();//城市信息
                //                amapLocation.getDistrict();//城区信息
                //                amapLocation.getStreet();//街道信息
                //                amapLocation.getStreetNum();//街道门牌号信息
                //                amapLocation.getCityCode();//城市编码
                //                amapLocation.getAdCode();//地区编码
                //                amapLocation.getAoiName();//获取当前定位点的AOI信息
                //                amapLocation.getBuildingId();//获取当前室内定位的建筑物Id
                //                amapLocation.getFloor();//获取当前室内定位的楼层
                //                amapLocation.getGpsStatus();//获取GPS的当前状态
                //TODO 未能获取到建筑物id和室内楼层
                //                Log.i("xq","BuildingId==>"+aMapLocation.getBuildingId());
                //                Log.i("xq","Floor==>"+aMapLocation.getFloor());

//                val strings = aMapLocation.address.split("靠近".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                val strings2 = aMapLocation.address.substring(0,aMapLocation.address.indexOf("靠"))
                default_location.setText(strings2)
                default_location.isFocusable = true
                mListener!!.onLocationChanged(aMapLocation)// 显示系统小蓝点
                val curLatlng = LatLng(aMapLocation.latitude, aMapLocation.longitude)
                aMap!!.moveCamera(com.amap.api.maps.CameraUpdateFactory.newLatLngZoom(curLatlng, 16f))
                //调用停止定位
                deactivate()
            } else {
                val errText = "定位失败," + aMapLocation.errorCode + ": " + aMapLocation.errorInfo
                Log.i("AmapErr", errText)
                //                mLocationErrText.setVisibility(View.VISIBLE);
                //                mLocationErrText.setText(errText);
            }
        }
    }

    /**
     * 方法必须重写
     */
    override fun onResume() {
        super.onResume()
        gaode_map.onResume()
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        gaode_map.onPause()
        deactivate()
    }

    /**
     * 方法必须重写
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        gaode_map.onSaveInstanceState(outState)
    }

    /**
     * 方法必须重写
     */
    override fun onDestroy() {
        super.onDestroy()
        gaode_map.onDestroy()
        if (null != mlocationClient) {
            mlocationClient!!.onDestroy()
        }
    }
}
