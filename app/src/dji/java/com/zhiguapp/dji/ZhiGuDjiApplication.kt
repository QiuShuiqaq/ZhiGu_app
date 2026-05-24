package com.zhiguapp.dji

import android.content.Context
import com.cySdkyc.clx.Helper
import com.zhiguapp.ZhiGuApplication

class ZhiGuDjiApplication : ZhiGuApplication() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        runCatching {
            Helper.install(this)
        }.onFailure { error ->
            DjiRuntime.updateState {
                it.copy(
                    enabled = true,
                    initEvent = "DJI 环境预加载失败",
                    lastError = error.message ?: error.javaClass.simpleName
                )
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
    }
}
