package com.zhiguapp.dji

import android.content.Context
import dji.sdk.keyvalue.DJIKeyManager
import dji.sdk.keyvalue.callback.IListenCallback
import dji.sdk.keyvalue.key.AirLinkKey
import dji.sdk.keyvalue.key.BatteryKey
import dji.sdk.keyvalue.key.KeyTools
import dji.sdk.keyvalue.key.ProductKey
import dji.sdk.keyvalue.value.product.ProductType
import dji.v5.common.error.IDJIError
import dji.v5.common.register.DJISDKInitEvent
import dji.v5.manager.datacenter.MediaDataCenter
import dji.v5.manager.SDKManager
import dji.v5.manager.interfaces.ICameraStreamManager
import dji.v5.manager.interfaces.SDKManagerCallback
import dji.v5.network.DJINetworkManager

object DjiSdkController {
    private var initialized = false
    private var initializing = false
    private val keyListenerHolder = Any()
    private var cameraAvailabilityListener: ICameraStreamManager.AvailableCameraUpdatedListener? = null

    fun initialize(appContext: Context) {
        if (initialized || initializing) return
        initializing = true

        runCatching {
            DjiRuntime.updateState {
                it.copy(
                    enabled = true,
                    initEvent = "开始初始化 DJI SDK",
                    lastError = null
                )
            }

            SDKManager.getInstance().init(appContext, object : SDKManagerCallback {
                override fun onRegisterSuccess() {
                    initialized = true
                    initializing = false
                    DjiRuntime.updateState {
                        it.copy(
                            registered = true,
                            lastError = null,
                            initEvent = "DJI 注册成功"
                        )
                    }
                    registerStatusListeners()
                }

                override fun onRegisterFailure(error: IDJIError) {
                    initializing = false
                    DjiRuntime.updateState {
                        it.copy(
                            registered = false,
                            lastError = error.description(),
                            initEvent = "DJI 注册失败"
                        )
                    }
                }

                override fun onProductDisconnect(productId: Int) {
                    DjiRuntime.updateState {
                        it.copy(
                            connected = false,
                            productId = productId,
                            initEvent = "设备已断开"
                        )
                    }
                }

                override fun onProductConnect(productId: Int) {
                    initialized = true
                    initializing = false
                    DjiRuntime.updateState {
                        it.copy(
                            connected = true,
                            productId = productId,
                            initEvent = "检测到 DJI 设备连接"
                        )
                    }
                    registerStatusListeners()
                }

                override fun onProductChanged(productId: Int) {
                    DjiRuntime.updateState {
                        it.copy(
                            productId = productId,
                            initEvent = "设备状态已更新"
                        )
                    }
                }

                override fun onInitProcess(event: DJISDKInitEvent, totalProcess: Int) {
                    DjiRuntime.updateState {
                        it.copy(
                            initEvent = event.name,
                            initProgress = totalProcess
                        )
                    }

                    if (event == DJISDKInitEvent.INITIALIZE_COMPLETE) {
                        SDKManager.getInstance().registerApp()
                    }
                }

                override fun onDatabaseDownloadProgress(current: Long, total: Long) {
                    DjiRuntime.updateState {
                        it.copy(
                            initEvent = "数据库下载中 ${current}/${total}"
                        )
                    }
                }
            })

            DJINetworkManager.getInstance().addNetworkStatusListener { isAvailable ->
                DjiRuntime.updateState {
                    it.copy(networkAvailable = isAvailable)
                }
                if (isAvailable && !SDKManager.getInstance().isRegistered) {
                    SDKManager.getInstance().registerApp()
                }
            }
        }.onFailure { error ->
            initializing = false
            DjiRuntime.updateState {
                it.copy(
                    enabled = true,
                    initEvent = "DJI 初始化失败",
                    lastError = error.message ?: error.javaClass.simpleName
                )
            }
        }
    }

    private fun registerStatusListeners() {
        val productTypeKey = KeyTools.createKey(ProductKey.KeyProductType)
        DJIKeyManager.listen(productTypeKey, keyListenerHolder, valueListener<ProductType> { value ->
            DjiRuntime.updateState {
                it.copy(productType = value?.toString())
            }
        }, true)

        val batteryPercentKey = KeyTools.createKey(BatteryKey.KeyChargeRemainingInPercent, 0)
        DJIKeyManager.listen(batteryPercentKey, keyListenerHolder, valueListener<Int> { value ->
            DjiRuntime.updateState {
                it.copy(batteryPercent = value)
            }
        }, true)

        val signalQualityKey = KeyTools.createKey(AirLinkKey.KeySignalQuality)
        DJIKeyManager.listen(signalQualityKey, keyListenerHolder, valueListener<Int> { value ->
            DjiRuntime.updateState {
                it.copy(signalPercent = value)
            }
        }, true)

        if (cameraAvailabilityListener == null) {
            val listener = object : ICameraStreamManager.AvailableCameraUpdatedListener {
                override fun onAvailableCameraUpdated(cameras: List<dji.sdk.keyvalue.value.common.ComponentIndexType>) {
                    DjiRuntime.updateState {
                        it.copy(availableCameras = cameras.map { camera -> camera.name })
                    }
                }
            }
            cameraAvailabilityListener = listener
            MediaDataCenter.getInstance().cameraStreamManager.addAvailableCameraUpdatedListener(listener)
        }
    }

    private fun <T> valueListener(onUpdate: (T?) -> Unit): IListenCallback<T> {
        return IListenCallback { _, oldValue, newValue ->
            val latest = newValue ?: oldValue
            onUpdate(latest)
        }
    }
}
