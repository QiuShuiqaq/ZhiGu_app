package com.zhiguapp.dji

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DjiRuntimeState(
    val enabled: Boolean = false,
    val initEvent: String = "当前未启用 DJI 变体",
    val initProgress: Int = 0,
    val registered: Boolean = false,
    val connected: Boolean = false,
    val productId: Int? = null,
    val productType: String? = null,
    val batteryPercent: Int? = null,
    val signalPercent: Int? = null,
    val availableCameras: List<String> = emptyList(),
    val networkAvailable: Boolean = false,
    val lastError: String? = null
)

object DjiRuntime {
    private val mutableState = MutableStateFlow(DjiRuntimeState())
    val state: StateFlow<DjiRuntimeState> = mutableState.asStateFlow()

    fun setEnabled(enabled: Boolean) {
        mutableState.update {
            it.copy(
                enabled = enabled,
                initEvent = if (enabled) "等待 SDK 初始化" else "当前为 demo 变体，可直接进行本地演示"
            )
        }
    }

    fun updateState(transform: (DjiRuntimeState) -> DjiRuntimeState) {
        mutableState.update(transform)
    }
}
