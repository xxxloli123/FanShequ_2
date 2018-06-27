package com.fanhong.cn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler

class LauncherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        val pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val isFirstStart = pref.getBoolean(App.PrefNames.FIRST_START, true)
        if (isFirstStart) {
            //TODO: show welcome pages here when first start
            Handler().postDelayed({ startActivity(Intent(this@LauncherActivity, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)) }, 3000)
            pref.edit().putBoolean(App.PrefNames.FIRST_START, false).commit()
        } else
            Handler().postDelayed({ startActivity(Intent(this@LauncherActivity, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)) }, 3000)
    }
}
