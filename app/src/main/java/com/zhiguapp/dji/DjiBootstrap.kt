package com.zhiguapp.dji

import android.content.Context
import com.zhiguapp.BuildConfig

object DjiBootstrap {
    fun initialize(context: Context) {
        if (!BuildConfig.DJI_ENABLED) {
            DjiRuntime.updateState {
                it.copy(
                    enabled = false,
                    initEvent = "当前为 demo 变体，无需初始化 DJI",
                    lastError = null
                )
            }
            return
        }

        runCatching {
            val controllerClass = Class.forName("com.zhiguapp.dji.DjiSdkController")
            val instance = controllerClass.getField("INSTANCE").get(null)
            val method = controllerClass.getMethod("initialize", Context::class.java)
            method.invoke(instance, context.applicationContext)
        }.onFailure { error ->
            DjiRuntime.updateState {
                it.copy(
                    enabled = true,
                    initEvent = "DJI 初始化调用失败",
                    lastError = error.cause?.message ?: error.message ?: error.javaClass.simpleName
                )
            }
        }
    }
}
