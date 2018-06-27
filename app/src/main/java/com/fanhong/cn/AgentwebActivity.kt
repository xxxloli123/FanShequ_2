package com.fanhong.cn

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.URLUtil
import com.just.library.AgentWeb
import com.zhy.autolayout.AutoRelativeLayout
import kotlinx.android.synthetic.main.activity_agentweb.*
import kotlinx.android.synthetic.main.activity_top.*

class AgentwebActivity : AppCompatActivity() {

    private var mAgentweb:AgentWeb?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agentweb)
        initViews()
    }
    private fun initViews(){
        img_back.setImageResource(R.mipmap.guanbiwangye)
        var params = img_back.layoutParams
        params.height = 50
        params.width = 50
        img_back.layoutParams = params
        img_back.setOnClickListener {
            finish()
        }
        tv_title.text = intent.getStringExtra("title")
        var url = intent.getStringExtra("url")
        //检验网站合法性
        if(URLUtil.isNetworkUrl(url)){
            goWeb(url)
        }
    }

    private fun goWeb(url: String?) {
        mAgentweb = AgentWeb.with(this)
                            .setAgentWebParent(agent_body,
                                    AutoRelativeLayout.LayoutParams(-1,-1))
                            .useDefaultIndicator()      //使用默认进度条
                            .defaultProgressBarColor()  //使用默认进度条的颜色
                            .createAgentWeb()
                            .ready()
                            .go(url)
        mAgentweb!!.jsEntraceAccess.quickCallJs("callByAndroid")
    }

    //封装的返回键监听
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return mAgentweb!!.handleKeyEvent(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}
