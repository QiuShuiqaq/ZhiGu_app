package com.zhiguapp.ui

import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.v5.manager.datacenter.camera.CameraStreamManager
import dji.v5.manager.interfaces.ICameraStreamManager

@Composable
fun DjiVideoPanel(
    state: ZhiGuUiState,
    modifier: Modifier = Modifier
) {
    if (state.shouldUseDemoVideo && state.demoVideoUri != null) {
        DemoVideoPlayer(
            videoUri = state.demoVideoUri,
            videoName = state.demoVideoName,
            modifier = modifier
        )
        return
    }

    if (!state.shouldUseDjiLive) {
        DjiVideoPlaceholder(state = state, modifier = modifier)
        return
    }

    val context = LocalContext.current
    val streamManager = remember { CameraStreamManager.getInstance() }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                factory = {
                    SurfaceView(context).apply {
                        setBackgroundColor(android.graphics.Color.BLACK)
                        streamManager.init()
                        holder.addCallback(
                            object : SurfaceHolder.Callback {
                                override fun surfaceCreated(holder: SurfaceHolder) {
                                    attachSurface(streamManager, holder.surface, width, height)
                                }

                                override fun surfaceChanged(
                                    holder: SurfaceHolder,
                                    format: Int,
                                    width: Int,
                                    height: Int
                                ) {
                                    attachSurface(streamManager, holder.surface, width, height)
                                }

                                override fun surfaceDestroyed(holder: SurfaceHolder) {
                                    streamManager.removeCameraStreamSurface(holder.surface)
                                }
                            }
                        )
                    }
                }
            )
            Text(
                text = "DJI 图传容器已挂载。可用相机：${if (state.djiAvailableCameras.isEmpty()) "待发现" else state.djiAvailableCameras.joinToString()}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

private fun attachSurface(
    streamManager: CameraStreamManager,
    surface: Surface,
    width: Int,
    height: Int
) {
    val safeWidth = if (width > 0) width else 1280
    val safeHeight = if (height > 0) height else 720
    streamManager.putCameraStreamSurface(
        ComponentIndexType.LEFT_OR_MAIN,
        surface,
        safeWidth,
        safeHeight,
        ICameraStreamManager.ScaleType.CENTER_INSIDE
    )
    streamManager.enableStream(ComponentIndexType.LEFT_OR_MAIN, true)
}
