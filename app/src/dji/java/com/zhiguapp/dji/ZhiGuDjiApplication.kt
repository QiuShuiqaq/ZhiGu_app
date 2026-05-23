package com.zhiguapp.dji

import android.content.Context
import com.cySdkyc.clx.Helper
import com.zhiguapp.ZhiGuApplication

class ZhiGuDjiApplication : ZhiGuApplication() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Helper.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        DjiSdkController.initialize(this)
    }
}
