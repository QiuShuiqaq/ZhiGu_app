package com.zhiguapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
    } else {
        DjiVideoPlaceholder(state = state, modifier = modifier)
    }
}
