package com.zhiguapp

import android.app.Application
import com.zhiguapp.dji.DjiRuntime

open class ZhiGuApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DjiRuntime.setEnabled(BuildConfig.DJI_ENABLED)
    }
}
