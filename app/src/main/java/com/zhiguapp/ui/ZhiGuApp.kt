package com.zhiguapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zhiguapp.dji.DjiBootstrap
import com.zhiguapp.ui.theme.Amber
import com.zhiguapp.ui.theme.Coral
import com.zhiguapp.ui.theme.CyanGlow
import com.zhiguapp.ui.theme.InkBlue
import com.zhiguapp.ui.theme.Mint
import com.zhiguapp.ui.theme.Night
import com.zhiguapp.ui.theme.NightCard
import com.zhiguapp.ui.theme.OceanBlue
import com.zhiguapp.ui.theme.Slate

private enum class RootTab(val label: String) {
    Dashboard("首页"),
    Assist("辅助"),
    Mission("作业"),
    Inspect("检测"),
    Settings("设置")
}

private data class StatusMetric(
    val title: String,
    val value: String,
    val accent: Color
)

private enum class AssistViewMode(val label: String) {
    TopDown("俯视"),
    Side("侧视")
}

private enum class AssistRiskLevel(val label: String, val color: Color) {
    Safe("低风险", Mint),
    Notice("中风险", Amber),
    Alert("高风险", Coral)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZhiGuApp(viewModel: ZhiGuViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(RootTab.Dashboard) }
    val context = LocalContext.current
    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val name = resolveDisplayName(context, uri) ?: "演示视频"
            viewModel.setDemoVideo(uri.toString(), name)
        }
    }

    if (!state.isLoggedIn) {
        LoginScreen(
            account = state.accountInput,
            password = state.passwordInput,
            onAccountChange = viewModel::updateAccountInput,
            onPasswordChange = viewModel::updatePasswordInput,
            onDemoLogin = viewModel::loginWithDemoAccount,
            onLogin = viewModel::login
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("智固")
                        Text(
                            text = when (currentTab) {
                                RootTab.Dashboard -> "演示总览"
                                RootTab.Assist -> "辅助感知"
                                RootTab.Mission -> "任务推进"
                                RootTab.Inspect -> "风险闭环"
                                RootTab.Settings -> "配置与设备"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate
                        )
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::logout) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = "退出")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("退出")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                RootTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    RootTab.Dashboard -> Icons.Outlined.Flight
                                    RootTab.Assist -> Icons.Outlined.WarningAmber
                                    RootTab.Mission -> Icons.Outlined.Directions
                                    RootTab.Inspect -> Icons.Outlined.ImageSearch
                                    RootTab.Settings -> Icons.Outlined.Settings
                                },
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        when (currentTab) {
            RootTab.Dashboard -> DashboardScreen(state, padding)
            RootTab.Assist -> AssistScreen(state, padding)
            RootTab.Mission -> MissionScreen(state, padding, viewModel)
            RootTab.Inspect -> InspectScreen(state, padding, viewModel)
            RootTab.Settings -> SettingsScreen(
                state = state,
                padding = padding,
                onPickVideo = { videoPicker.launch("video/*") },
                onSelectStreamMode = viewModel::selectStreamMode,
                onClearVideo = viewModel::clearDemoVideo,
                onInitializeDji = { DjiBootstrap.initialize(context.applicationContext) }
            )
        }
    }
}

private fun resolveDisplayName(context: android.content.Context, uri: Uri): String? {
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) return cursor.getString(index)
                }
            }
    }
    return uri.lastPathSegment?.substringAfterLast('/')
}

@Composable
private fun LoginScreen(
    account: String,
    password: String,
    onAccountChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onDemoLogin: () -> Unit,
    onLogin: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(InkBlue, OceanBlue, MaterialTheme.colorScheme.background)
                    )
                )
                .padding(24.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("智固", style = MaterialTheme.typography.headlineLarge)
                    Text("本地演示版", style = MaterialTheme.typography.titleMedium, color = Slate)
                    OutlinedTextField(
                        value = account,
                        onValueChange = onAccountChange,
                        label = { Text("账号") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("密码") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = onLogin, modifier = Modifier.weight(1f)) {
                            Text("登录")
                        }
                        Button(onClick = onDemoLogin, modifier = Modifier.weight(1f)) {
                            Text("演示入口")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardScreen(state: ZhiGuUiState, padding: PaddingValues) {
    val metrics = listOf(
        StatusMetric("任务进度", "${state.missionProgress}%", Mint),
        StatusMetric("演示评分", "${state.missionScore}", CyanGlow),
        StatusMetric("待处理风险", "${state.findings.count { !it.resolved }}", Coral),
        StatusMetric("当前模板", state.selectedTemplate.label, Amber)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { DashboardHero(state) }
        item { MetricRow(metrics) }
        item { DjiVideoPanel(state = state) }
        item { SceneStatusCard(state) }
        item { LiveFeedsCard(state) }
        item { QuickStatusCard(state) }
        item { DemoTimelineCard(state) }
        item { DemoRecordSummaryCard(state) }
    }
}

@Composable
private fun DashboardHero(state: ZhiGuUiState) {
    val pulse = rememberInfiniteTransition(label = "hero_pulse")
    val glow by pulse.animateFloat(
        initialValue = 0.82f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_glow"
    )

    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            InkBlue,
                            OceanBlue,
                            CyanGlow.copy(alpha = glow)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatusPill(
                        when {
                            state.shouldUseDemoVideo -> "DEMO VIDEO"
                            state.shouldUseDjiLive -> "DJI LIVE"
                            else -> "DEMO READY"
                        },
                        Color.White.copy(alpha = 0.16f)
                    )
                    StatusPill("SCORE ${state.missionScore}", Color.White.copy(alpha = 0.16f))
                }
                Text("牵引与检测演示", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                AnimatedStatusText(state.missionStatus)
                LinearProgressIndicator(
                    progress = { state.missionProgress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.22f)
                )
            }
        }
    }
}

@Composable
private fun AnimatedStatusText(text: String) {
    AnimatedContent(
        targetState = text,
        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(180)) },
        label = "status_text"
    ) { value ->
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun MetricRow(metrics: List<StatusMetric>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        metrics.forEach { metric ->
            Card(
                modifier = Modifier.width(152.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(metric.accent)
                    )
                    Text(metric.title, style = MaterialTheme.typography.bodyMedium, color = Slate)
                    Text(metric.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LiveFeedsCard(state: ZhiGuUiState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("画面", "动态视角")
            state.cameraFeeds.forEachIndexed { index, feed ->
                val alpha = 1f - (index * 0.08f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(alpha),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(feed.name, style = MaterialTheme.typography.titleMedium)
                    StatusPill(feed.state, if (feed.state == "在线") Mint.copy(alpha = 0.14f) else Amber.copy(alpha = 0.16f))
                }
            }
        }
    }
}

@Composable
private fun AssistScreen(state: ZhiGuUiState, padding: PaddingValues) {
    var viewMode by remember { mutableStateOf(AssistViewMode.TopDown) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { AssistHero(state, viewMode) }
        item { AssistViewModeCard(viewMode = viewMode, onModeChange = { viewMode = it }) }
        item { AssistSceneCard(state, viewMode) }
        item { AssistSummaryRow(state) }
        item { AssistTargetListCard(state, viewMode) }
        item { AssistGuidanceCard(state, viewMode) }
    }
}

@Composable
private fun AssistHero(state: ZhiGuUiState, viewMode: AssistViewMode) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Night,
                            InkBlue,
                            OceanBlue.copy(alpha = 0.92f)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatusPill("AUX DRIVE VIEW", Color.White.copy(alpha = 0.14f))
                    StatusPill("目标 4", Color.White.copy(alpha = 0.14f))
                    StatusPill(viewMode.label, Color.White.copy(alpha = 0.14f))
                }
                Text(
                    text = "辅助感知屏",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "模拟展示货车周边人员、障碍物与无人机巡检态势，风格接近智能驾驶感知大屏。",
                    color = Color.White.copy(alpha = 0.84f),
                    style = MaterialTheme.typography.bodyMedium
                )
                LinearProgressIndicator(
                    progress = { state.missionProgress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = CyanGlow,
                    trackColor = Color.White.copy(alpha = 0.14f)
                )
            }
        }
    }
}

@Composable
private fun AssistViewModeCard(
    viewMode: AssistViewMode,
    onModeChange: (AssistViewMode) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("显示视角", style = MaterialTheme.typography.titleMedium)
                Text("切换俯视与侧视，模拟更接近车机感知屏的展示方式", style = MaterialTheme.typography.bodyMedium, color = Slate)
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AssistViewMode.entries.forEach { mode ->
                    FilterChip(
                        label = mode.label,
                        selected = mode == viewMode,
                        onClick = { onModeChange(mode) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AssistSceneCard(state: ZhiGuUiState, viewMode: AssistViewMode) {
    val sweepTransition = rememberInfiniteTransition(label = "perception_scene")
    val scanProgress by sweepTransition.animateFloat(
        initialValue = 0.16f,
        targetValue = 0.84f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_progress"
    )
    val droneOffset by sweepTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drone_offset"
    )
    val pulseAlpha by sweepTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val truckStatus = if (state.findings.count { !it.resolved } > 0) "待复核" else "稳定"
    val truckStatusColor = if (truckStatus == "稳定") Mint else Amber
    val leftTargetStatus = if (state.missionProgress >= 50) "已识别" else "扫描中"
    val rightTargetStatus = if (state.missionProgress >= 75) "接近" else "监测中"
    val rearTargetStatus = if (state.missionProgress >= 100) "安全" else "占用"
    val leftDistance = if (state.missionProgress >= 50) "1.2m" else "2.4m"
    val rightDistance = if (state.missionProgress >= 75) "0.8m" else "1.6m"
    val rearDistance = if (state.missionProgress >= 100) "1.5m" else "0.4m"
    val currentRiskLevel = when {
        state.findings.count { !it.resolved } >= 3 -> AssistRiskLevel.Alert
        state.findings.count { !it.resolved } >= 1 -> AssistRiskLevel.Notice
        else -> AssistRiskLevel.Safe
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("态势图", "智能辅助驾驶风格示意")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                InkBlue,
                                OceanBlue.copy(alpha = 0.92f),
                                Night
                            )
                        )
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val laneLeft = width * 0.22f
                    val laneRight = width * 0.78f
                    val centerX = width * 0.5f
                    val truckTop = height * 0.52f
                    val truckWidth = width * 0.2f
                    val truckHeight = height * 0.28f
                    val droneX = width * (0.28f + 0.44f * droneOffset)
                    val droneY = height * (0.14f + 0.03f * kotlin.math.sin(droneOffset * Math.PI).toFloat())
                    val leftTarget = Offset(width * 0.24f, height * 0.36f)
                    val rightTarget = Offset(width * 0.77f, height * 0.42f)
                    val rearTarget = Offset(width * 0.5f, height * 0.88f)
                    if (viewMode == AssistViewMode.TopDown) {
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.06f),
                            topLeft = Offset(width * 0.08f, height * 0.08f),
                            size = Size(width * 0.84f, height * 0.82f),
                            cornerRadius = CornerRadius(36f, 36f)
                        )

                        drawLine(
                            color = Color.White.copy(alpha = 0.18f),
                            start = Offset(laneLeft, height * 0.06f),
                            end = Offset(laneLeft, height * 0.94f),
                            strokeWidth = 4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f))
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.18f),
                            start = Offset(laneRight, height * 0.06f),
                            end = Offset(laneRight, height * 0.94f),
                            strokeWidth = 4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f))
                        )
                        drawLine(
                            color = CyanGlow.copy(alpha = 0.65f),
                            start = Offset(width * 0.12f, height * scanProgress),
                            end = Offset(width * 0.88f, height * scanProgress),
                            strokeWidth = 5f,
                            cap = StrokeCap.Round
                        )

                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.08f),
                            topLeft = Offset(centerX - truckWidth / 2f, truckTop),
                            size = Size(truckWidth, truckHeight),
                            cornerRadius = CornerRadius(28f, 28f)
                        )
                        drawRoundRect(
                            color = Amber.copy(alpha = 0.72f),
                            topLeft = Offset(centerX - truckWidth / 2.4f, truckTop + truckHeight * 0.16f),
                            size = Size(truckWidth / 1.2f, truckHeight * 0.48f),
                            cornerRadius = CornerRadius(22f, 22f)
                        )
                        drawRoundRect(
                            color = Mint.copy(alpha = 0.95f),
                            topLeft = Offset(centerX - truckWidth / 2f - 10f, truckTop + truckHeight * 0.82f),
                            size = Size(20f, 20f),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        drawRoundRect(
                            color = Mint.copy(alpha = 0.95f),
                            topLeft = Offset(centerX + truckWidth / 2f - 10f, truckTop + truckHeight * 0.82f),
                            size = Size(20f, 20f),
                            cornerRadius = CornerRadius(10f, 10f)
                        )

                        drawCircle(
                            color = Color.White.copy(alpha = 0.12f * pulseAlpha),
                            radius = 46f + 22f * pulseAlpha,
                            center = Offset(droneX, droneY)
                        )
                        drawCircle(
                            color = CyanGlow.copy(alpha = 0.9f),
                            radius = 16f,
                            center = Offset(droneX, droneY)
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.9f),
                            start = Offset(droneX - 24f, droneY),
                            end = Offset(droneX + 24f, droneY),
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.9f),
                            start = Offset(droneX, droneY - 18f),
                            end = Offset(droneX, droneY + 18f),
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = CyanGlow.copy(alpha = 0.55f),
                            start = Offset(droneX, droneY + 14f),
                            end = Offset(centerX, truckTop - 14f),
                            strokeWidth = 3f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                        )

                        listOf(leftTarget, rightTarget, rearTarget).forEachIndexed { index, target ->
                            val alertColor = when (index) {
                                0 -> Coral
                                1 -> Amber
                                else -> Mint
                            }
                            drawRoundRect(
                                color = alertColor.copy(alpha = 0.9f),
                                topLeft = Offset(target.x - 30f, target.y - 24f),
                                size = Size(60f, 48f),
                                cornerRadius = CornerRadius(12f, 12f),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                            )
                            drawCircle(
                                color = alertColor.copy(alpha = 0.18f + 0.18f * pulseAlpha),
                                radius = 28f + 10f * pulseAlpha,
                                center = target
                            )
                        }
                    } else {
                        val groundY = height * 0.78f
                        val truckSideLeft = width * 0.26f
                        val truckSideTop = height * 0.44f
                        val personX = width * 0.18f
                        val coneX = width * 0.78f
                        val fanCenter = Offset(width * 0.74f, truckSideTop + height * 0.09f)

                        drawLine(
                            color = Color.White.copy(alpha = 0.18f),
                            start = Offset(width * 0.1f, groundY),
                            end = Offset(width * 0.9f, groundY),
                            strokeWidth = 4f
                        )
                        drawLine(
                            color = CyanGlow.copy(alpha = 0.65f),
                            start = Offset(width * 0.14f, height * scanProgress),
                            end = Offset(width * 0.86f, height * scanProgress),
                            strokeWidth = 5f,
                            cap = StrokeCap.Round
                        )
                        drawRoundRect(
                            color = Amber.copy(alpha = 0.76f),
                            topLeft = Offset(truckSideLeft, truckSideTop),
                            size = Size(width * 0.42f, height * 0.18f),
                            cornerRadius = CornerRadius(26f, 26f)
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.12f),
                            topLeft = Offset(truckSideLeft + width * 0.26f, truckSideTop - height * 0.06f),
                            size = Size(width * 0.12f, height * 0.08f),
                            cornerRadius = CornerRadius(22f, 22f)
                        )
                        drawCircle(Mint, 16f, Offset(truckSideLeft + width * 0.08f, truckSideTop + height * 0.19f))
                        drawCircle(Mint, 16f, Offset(truckSideLeft + width * 0.34f, truckSideTop + height * 0.19f))
                        drawLine(
                            color = Color.White.copy(alpha = 0.45f),
                            start = Offset(truckSideLeft + width * 0.44f, truckSideTop + height * 0.09f),
                            end = Offset(truckSideLeft + width * 0.5f, truckSideTop + height * 0.09f),
                            strokeWidth = 5f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.45f),
                            start = Offset(truckSideLeft + width * 0.48f, truckSideTop + height * 0.06f),
                            end = Offset(truckSideLeft + width * 0.5f, truckSideTop + height * 0.09f),
                            strokeWidth = 5f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.45f),
                            start = Offset(truckSideLeft + width * 0.48f, truckSideTop + height * 0.12f),
                            end = Offset(truckSideLeft + width * 0.5f, truckSideTop + height * 0.09f),
                            strokeWidth = 5f,
                            cap = StrokeCap.Round
                        )

                        drawCircle(
                            color = Color.White.copy(alpha = 0.12f * pulseAlpha),
                            radius = 46f + 20f * pulseAlpha,
                            center = Offset(width * 0.38f, height * 0.18f)
                        )
                        drawCircle(CyanGlow.copy(alpha = 0.9f), 16f, Offset(width * 0.38f, height * 0.18f))
                        drawLine(
                            color = Color.White.copy(alpha = 0.9f),
                            start = Offset(width * 0.35f, height * 0.18f),
                            end = Offset(width * 0.41f, height * 0.18f),
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.9f),
                            start = Offset(width * 0.38f, height * 0.15f),
                            end = Offset(width * 0.38f, height * 0.21f),
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = CyanGlow.copy(alpha = 0.55f),
                            start = Offset(width * 0.38f, height * 0.21f),
                            end = Offset(width * 0.48f, truckSideTop - 16f),
                            strokeWidth = 3f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                        )

                        drawRoundRect(
                            color = Coral.copy(alpha = 0.92f),
                            topLeft = Offset(personX - 28f, height * 0.5f),
                            size = Size(56f, 92f),
                            cornerRadius = CornerRadius(16f, 16f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                        )
                        drawCircle(
                            color = Amber.copy(alpha = 0.92f),
                            radius = 28f,
                            center = Offset(coneX, height * 0.6f)
                        )
                        drawCircle(
                            color = Mint.copy(alpha = 0.2f + 0.16f * pulseAlpha),
                            radius = 30f + 8f * pulseAlpha,
                            center = Offset(width * 0.72f, groundY - 14f)
                        )
                        drawArc(
                            color = Coral.copy(alpha = 0.16f),
                            startAngle = -36f,
                            sweepAngle = 72f,
                            useCenter = true,
                            topLeft = Offset(fanCenter.x - 110f, fanCenter.y - 110f),
                            size = Size(220f, 220f),
                            style = Fill
                        )
                        drawArc(
                            color = Coral.copy(alpha = 0.42f),
                            startAngle = -28f,
                            sweepAngle = 56f,
                            useCenter = false,
                            topLeft = Offset(fanCenter.x - 110f, fanCenter.y - 110f),
                            size = Size(220f, 220f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                        )
                    }
                }

                AssistTargetBadge(
                    title = "人员",
                    detail = "$leftTargetStatus · $leftDistance",
                    color = Coral,
                    modifier = Modifier
                        .align(if (viewMode == AssistViewMode.TopDown) Alignment.TopStart else Alignment.CenterStart)
                        .offset(
                            x = if (viewMode == AssistViewMode.TopDown) 42.dp else 18.dp,
                            y = if (viewMode == AssistViewMode.TopDown) 132.dp else 18.dp
                        )
                )
                AssistTargetBadge(
                    title = "锥桶",
                    detail = "$rightTargetStatus · $rightDistance",
                    color = Amber,
                    modifier = Modifier
                        .align(if (viewMode == AssistViewMode.TopDown) Alignment.TopEnd else Alignment.CenterEnd)
                        .offset(
                            x = if (viewMode == AssistViewMode.TopDown) (-44).dp else (-18).dp,
                            y = if (viewMode == AssistViewMode.TopDown) 168.dp else 56.dp
                        )
                )
                AssistTargetBadge(
                    title = "尾部余量",
                    detail = "$rearTargetStatus · $rearDistance",
                    color = Mint,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-24).dp)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusPill("DRONE LINK", Color.White.copy(alpha = 0.14f))
                    Text(
                        text = "AI 感知模拟中",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (viewMode == AssistViewMode.TopDown) {
                            "检测目标：无人机 1 / 货车 1 / 人员与物体 3"
                        } else {
                            "检测目标：侧视跟踪 / 货车轮廓 / 周边风险 3"
                        },
                        color = Color.White.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "货车稳定性 $truckStatus",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (viewMode == AssistViewMode.TopDown) "无人机正在执行侧前方巡检轨迹" else "侧视感知正在追踪车身、人员与后方空间",
                        color = Color.White.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NightCard.copy(alpha = 0.84f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "目标态势",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        RiskBand(level = currentRiskLevel)
                        PerceptionLegendRow("货车", truckStatus, truckStatusColor)
                        PerceptionLegendRow("左侧目标", "$leftTargetStatus / $leftDistance", Coral)
                        PerceptionLegendRow("右侧目标", "$rightTargetStatus / $rightDistance", Amber)
                        PerceptionLegendRow("后方余量", "$rearTargetStatus / $rearDistance", Mint)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusPill("货车 1", Amber.copy(alpha = 0.16f))
                StatusPill("无人机 1", CyanGlow.copy(alpha = 0.18f))
                StatusPill("人员 1", Coral.copy(alpha = 0.14f))
                StatusPill("物体 2", Mint.copy(alpha = 0.14f))
            }
        }
    }
}

@Composable
private fun AssistSummaryRow(state: ZhiGuUiState) {
    val currentRiskLevel = when {
        state.findings.count { !it.resolved } >= 3 -> AssistRiskLevel.Alert
        state.findings.count { !it.resolved } >= 1 -> AssistRiskLevel.Notice
        else -> AssistRiskLevel.Safe
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AssistMetricCard("识别目标", "4", CyanGlow)
        AssistMetricCard("安全余量", if (state.missionProgress >= 75) "0.8m" else "0.4m", Mint)
        AssistMetricCard("无人机速度", "1.4m/s", OceanBlue)
        AssistMetricCard("货车状态", if (state.findings.count { !it.resolved } > 0) "预警" else "稳定", Amber)
        AssistMetricCard("风险等级", currentRiskLevel.label, currentRiskLevel.color)
    }
}

@Composable
private fun AssistMetricCard(title: String, value: String, accent: Color) {
    Card(
        modifier = Modifier.width(148.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Text(title, style = MaterialTheme.typography.bodyMedium, color = Slate)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AssistTargetListCard(state: ZhiGuUiState, viewMode: AssistViewMode) {
    val targetItems = listOf(
        AssistTargetItem("左侧人员", if (state.missionProgress >= 50) "已识别，距车 1.2m" else "目标靠近中，距车 2.4m", Coral, Icons.Outlined.Person, AssistRiskLevel.Alert),
        AssistTargetItem("右侧障碍物", if (state.missionProgress >= 75) "锥桶区域，建议保持 0.8m 以上距离" else "边界检测中，当前 1.6m", Amber, Icons.Outlined.Construction, AssistRiskLevel.Notice),
        AssistTargetItem("尾部余量", if (state.missionProgress >= 100) "尾部空间充足，当前 1.5m" else "尾部需复核，当前 0.4m", Mint, Icons.Outlined.SwapHoriz, if (state.missionProgress >= 100) AssistRiskLevel.Safe else AssistRiskLevel.Notice),
        AssistTargetItem("无人机轨迹", if (viewMode == AssistViewMode.TopDown) "沿货车左前方巡检轨迹稳定飞行，航速 1.4m/s" else "侧视下保持低速平稳跟踪，俯角稳定", CyanGlow, Icons.Outlined.Air, AssistRiskLevel.Safe),
        AssistTargetItem("货车轮廓", "车身边界已锁定，车头朝向右前，持续跟踪中", OceanBlue, Icons.Outlined.LocalShipping, AssistRiskLevel.Safe)
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("目标列表", "周边目标识别结果")
            targetItems.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(item.color.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = item.color,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text(item.detail, style = MaterialTheme.typography.bodyMedium, color = Slate)
                        }
                    }
                    StatusPill(item.riskLevel.label, item.riskLevel.color.copy(alpha = 0.14f))
                }
            }
        }
    }
}

@Composable
private fun AssistGuidanceCard(state: ZhiGuUiState, viewMode: AssistViewMode) {
    val currentRiskLevel = when {
        state.findings.count { !it.resolved } >= 3 -> AssistRiskLevel.Alert
        state.findings.count { !it.resolved } >= 1 -> AssistRiskLevel.Notice
        else -> AssistRiskLevel.Safe
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("辅助策略", "演示建议")
            DashboardStatusLine("当前任务", state.missionStatus)
            DashboardStatusLine("风险数量", "${state.findings.count { !it.resolved }} 项")
            DashboardStatusLine("显示视角", viewMode.label)
            DashboardStatusLine("当前等级", currentRiskLevel.label)
            DashboardStatusLine("建议播报", if (state.missionProgress < 50) "说明无人机正在识别周边人员与物体" else "说明系统已锁定货车周边关键目标")
            Text(
                text = "这个页面是纯展示型辅助感知屏，不依赖真实算法输入，适合在比赛现场稳定模拟类似智能驾驶的感知界面效果。",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate,
                textAlign = TextAlign.Start
            )
        }
    }
}

private data class AssistTargetItem(
    val title: String,
    val detail: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val riskLevel: AssistRiskLevel
)

@Composable
private fun RiskBand(level: AssistRiskLevel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "风险色带",
            color = Color.White.copy(alpha = 0.76f),
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AssistRiskLevel.entries.forEach { item ->
                val selected = item == level
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (selected) item.color.copy(alpha = 0.88f)
                            else item.color.copy(alpha = 0.18f)
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.label,
                        color = if (selected) Color.White else Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun AssistTargetBadge(
    title: String,
    detail: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NightCard.copy(alpha = 0.92f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(title, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
            Text(detail, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PerceptionLegendRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(label, color = Color.White.copy(alpha = 0.84f), style = MaterialTheme.typography.bodyMedium)
        }
        Text(value, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SceneStatusCard(state: ZhiGuUiState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("现场", "环境与状态")
            DashboardStatusLine("作业地点", state.operationLocation)
            DashboardStatusLine("天气", state.operationWeather)
            DashboardStatusLine("同步", state.syncStatus)
            DashboardStatusLine("报告", state.reportStatus)
            DashboardStatusLine("素材", "${state.mediaCount} 项")
        }
    }
}

@Composable
private fun QuickStatusCard(state: ZhiGuUiState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader("总览", "核心状态")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusPill("阶段 ${state.missionStage.label}", Mint.copy(alpha = 0.14f))
                StatusPill("耗时 ${state.estimatedDuration}", OceanBlue.copy(alpha = 0.12f))
                StatusPill("距离 ${state.estimatedDistance}", Amber.copy(alpha = 0.16f))
                StatusPill("图传 ${state.streamMode.label}", CyanGlow.copy(alpha = 0.16f))
            }
            DashboardStatusLine("设备", state.djiProductType ?: "模拟演示")
            DashboardStatusLine("路径", state.selectedTemplate.label)
            DashboardStatusLine("摘要", state.settingsSummary)
        }
    }
}

@Composable
private fun DemoTimelineCard(state: ZhiGuUiState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("时间线", "演示事件")
            state.demoEvents.forEach { event ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CyanGlow)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(event.title, style = MaterialTheme.typography.titleMedium)
                        Text(event.time, style = MaterialTheme.typography.bodyMedium, color = Slate)
                    }
                    StatusPill(event.tag, OceanBlue.copy(alpha = 0.12f))
                }
            }
        }
    }
}

@Composable
private fun DemoRecordSummaryCard(state: ZhiGuUiState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("记录", "本地摘要")
            state.demoRecords.forEach { record ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(record.title, style = MaterialTheme.typography.titleMedium)
                        Text(record.summary, style = MaterialTheme.typography.bodyMedium, color = Slate)
                    }
                    StatusPill(record.status, Mint.copy(alpha = 0.14f))
                }
            }
        }
    }
}

@Composable
private fun MissionScreen(state: ZhiGuUiState, padding: PaddingValues, viewModel: ZhiGuViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { MissionHero(state) }
        item { PreflightCard(state, viewModel) }
        item { MissionFormCard(state, viewModel) }
        item { TemplatePicker(state, viewModel) }
        item { RouteNodesCard(state) }
        item { MissionGuideCard(state, viewModel) }
        item { RoutePreviewCard(state) }
    }
}

@Composable
private fun MissionHero(state: ZhiGuUiState) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader("作业", state.missionStage.detail)
            LinearProgressIndicator(
                progress = { state.missionProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = OceanBlue
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusPill(state.vehiclePlate, OceanBlue.copy(alpha = 0.12f))
                StatusPill(state.cargoType, Mint.copy(alpha = 0.12f))
                StatusPill("${state.cargoWeight} 吨", Amber.copy(alpha = 0.16f))
                StatusPill("评分 ${state.missionScore}", CyanGlow.copy(alpha = 0.14f))
            }
        }
    }
}

@Composable
private fun MissionFormCard(state: ZhiGuUiState, viewModel: ZhiGuViewModel) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader("参数", "基础信息")
            OutlinedTextField(
                value = state.vehiclePlate,
                onValueChange = viewModel::updateVehiclePlate,
                label = { Text("车牌号") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.vehicleType,
                    onValueChange = viewModel::updateVehicleType,
                    label = { Text("车型") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.cargoType,
                    onValueChange = viewModel::updateCargoType,
                    label = { Text("货物") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.cargoWeight,
                    onValueChange = viewModel::updateCargoWeight,
                    label = { Text("吨位") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.centerPosition,
                    onValueChange = viewModel::updateCenterPosition,
                    label = { Text("重心") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            OutlinedTextField(
                value = state.bindingMaterial,
                onValueChange = viewModel::updateBindingMaterial,
                label = { Text("捆绑材料") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun PreflightCard(state: ZhiGuUiState, viewModel: ZhiGuViewModel) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("检查", "起飞前")
            state.preflightChecks.forEach { check ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.togglePreflightCheck(check.id) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(check.title, style = MaterialTheme.typography.titleMedium)
                    StatusPill(
                        if (check.done) "已完成" else "待确认",
                        if (check.done) Mint.copy(alpha = 0.14f) else Amber.copy(alpha = 0.16f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplatePicker(state: ZhiGuUiState, viewModel: ZhiGuViewModel) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("路径", "模板选择")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PathTemplate.entries.forEach { template ->
                    FilterChip(
                        label = template.label,
                        selected = template == state.selectedTemplate,
                        onClick = { viewModel.selectTemplate(template) }
                    )
                }
            }
            Button(onClick = viewModel::generateRoutePreview, modifier = Modifier.fillMaxWidth()) {
                Text("生成路径")
            }
        }
    }
}

@Composable
private fun RouteNodesCard(state: ZhiGuUiState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("节点", "执行路径")
            state.routeNodes.forEach { node ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (node.status) {
                                    RouteNodeStatus.Completed -> Mint
                                    RouteNodeStatus.Active -> OceanBlue
                                    RouteNodeStatus.Pending -> Amber
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(node.title, style = MaterialTheme.typography.titleMedium)
                        Text(node.note, style = MaterialTheme.typography.bodyMedium, color = Slate)
                    }
                    StatusPill(
                        node.status.label,
                        when (node.status) {
                            RouteNodeStatus.Completed -> Mint.copy(alpha = 0.14f)
                            RouteNodeStatus.Active -> OceanBlue.copy(alpha = 0.12f)
                            RouteNodeStatus.Pending -> Amber.copy(alpha = 0.16f)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionGuideCard(state: ZhiGuUiState, viewModel: ZhiGuViewModel) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("执行", state.missionStage.label)
            AnimatedContent(
                targetState = state.missionStatus,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                label = "mission_status"
            ) { value ->
                Text(value, style = MaterialTheme.typography.titleMedium)
            }
            state.missionLogs.take(3).forEach { log ->
                DashboardStatusLine("提示", log)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = viewModel::advanceMissionStage, modifier = Modifier.weight(1f)) {
                    Text(if (state.missionProgress >= 100) "完成" else "下一步")
                }
                Button(onClick = viewModel::resetMissionDemo, modifier = Modifier.weight(1f)) {
                    Text("重置")
                }
            }
        }
    }
}

@Composable
private fun RoutePreviewCard(state: ZhiGuUiState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("预览", "路线摘要")
            Text(state.routePreview, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusPill("耗时 ${state.estimatedDuration}", OceanBlue.copy(alpha = 0.12f))
                StatusPill("距离 ${state.estimatedDistance}", Amber.copy(alpha = 0.16f))
            }
            Text(state.routeAdvice, style = MaterialTheme.typography.bodyMedium, color = Slate)
        }
    }
}

@Composable
private fun InspectScreen(state: ZhiGuUiState, padding: PaddingValues, viewModel: ZhiGuViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { InspectHero(state) }
        item { InspectionModeCard(state, viewModel) }
        item { InspectionSummaryCard(state, viewModel) }
        item { EvidenceCard(state, viewModel) }
        items(state.findings, key = { it.id }) { finding ->
            RiskCard(finding, onToggle = { viewModel.toggleFindingResolved(finding.id) })
        }
    }
}

@Composable
private fun InspectHero(state: ZhiGuUiState) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader("检测", "风险与闭环")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusPill("模式 ${state.inspectionMode.label}", OceanBlue.copy(alpha = 0.12f))
                StatusPill("待整改 ${state.findings.count { !it.resolved }}", Coral.copy(alpha = 0.12f))
                StatusPill("记录 ${state.demoRecords.size}", Mint.copy(alpha = 0.12f))
            }
        }
    }
}

@Composable
private fun InspectionModeCard(state: ZhiGuUiState, viewModel: ZhiGuViewModel) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("模式", "检测方式")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InspectionMode.entries.forEach { mode ->
                    FilterChip(
                        label = mode.label,
                        selected = mode == state.inspectionMode,
                        onClick = { viewModel.selectInspectionMode(mode) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InspectionSummaryCard(state: ZhiGuUiState, viewModel: ZhiGuViewModel) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("闭环", "整改摘要")
            AnimatedContent(
                targetState = state.latestRecordSummary,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                label = "inspection_summary"
            ) { value ->
                Text(value, style = MaterialTheme.typography.titleMedium)
            }
            DashboardStatusLine("报告状态", state.reportStatus)
            DashboardStatusLine("同步状态", state.syncStatus)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = viewModel::generateInspectionReport, modifier = Modifier.weight(1f)) {
                    Text("生成报告")
                }
                Button(onClick = viewModel::createDemoRecord, modifier = Modifier.weight(1f)) {
                    Text("生成记录")
                }
            }
        }
    }
}

@Composable
private fun EvidenceCard(state: ZhiGuUiState, viewModel: ZhiGuViewModel) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("证据", "留痕材料")
            state.evidenceItems.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleEvidenceCaptured(item.id) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                        Text(item.status, style = MaterialTheme.typography.bodyMedium, color = Slate)
                    }
                    StatusPill(
                        if (item.captured) "已留痕" else "待补充",
                        if (item.captured) Mint.copy(alpha = 0.14f) else Amber.copy(alpha = 0.16f)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = viewModel::captureDemoMedia, modifier = Modifier.weight(1f)) {
                    Text("新增素材")
                }
                Button(onClick = viewModel::generateInspectionReport, modifier = Modifier.weight(1f)) {
                    Text("更新报告")
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    state: ZhiGuUiState,
    padding: PaddingValues,
    onPickVideo: () -> Unit,
    onSelectStreamMode: (StreamMode) -> Unit,
    onClearVideo: () -> Unit,
    onInitializeDji: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SettingsHero(state) }
        item {
            StreamSourceCard(
                state = state,
                onPickVideo = onPickVideo,
                onSelectStreamMode = onSelectStreamMode,
                onClearVideo = onClearVideo,
                onInitializeDji = onInitializeDji
            )
        }
        item { DeviceStatusCard(state) }
        item { PermissionReadinessCard() }
        item { ConfigOverviewCard(state) }
    }
}

@Composable
private fun SettingsHero(state: ZhiGuUiState) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(OceanBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Tune, contentDescription = null, tint = OceanBlue)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text("设置", style = MaterialTheme.typography.titleLarge)
                Text(state.settingsSummary, style = MaterialTheme.typography.bodyMedium, color = Slate)
            }
        }
    }
}

@Composable
private fun StreamSourceCard(
    state: ZhiGuUiState,
    onPickVideo: () -> Unit,
    onSelectStreamMode: (StreamMode) -> Unit,
    onClearVideo: () -> Unit,
    onInitializeDji: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("图传源", "实时 / 备用")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StreamMode.entries.forEach { mode ->
                    FilterChip(
                        label = mode.label,
                        selected = mode == state.streamMode,
                        onClick = { onSelectStreamMode(mode) }
                    )
                }
            }
            DashboardStatusLine("当前模式", state.streamMode.label)
            DashboardStatusLine("演示视频", state.demoVideoName)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onPickVideo, modifier = Modifier.weight(1f)) {
                    Text("选择视频")
                }
                Button(onClick = onClearVideo, modifier = Modifier.weight(1f)) {
                    Text("清除视频")
                }
            }
            if (state.djiEnabled) {
                Button(
                    onClick = onInitializeDji,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (state.djiRegistered || state.djiConnected) {
                            "DJI 已初始化"
                        } else {
                            "初始化 DJI"
                        }
                    )
                }
            }
            Text(
                when {
                    state.shouldUseDemoVideo -> "当前图传区将优先播放演示视频。"
                    state.shouldUseDjiLive -> "当前图传区将优先使用 DJI 实时图传。"
                    else -> "当前图传区使用默认演示占位画面。"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Slate
            )
        }
    }
}

@Composable
private fun DeviceStatusCard(state: ZhiGuUiState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("无人机", "设备与网络")
            DashboardStatusLine("运行模式", if (state.djiEnabled) "DJI" else "DEMO")
            DashboardStatusLine("连接状态", if (state.djiConnected) "已连接" else "待连接")
            DashboardStatusLine("注册状态", if (state.djiRegistered) "已注册" else "未注册")
            DashboardStatusLine("设备型号", state.djiProductType ?: "模拟演示")
            DashboardStatusLine("电量", "${state.djiBatteryPercent ?: "--"}%")
            DashboardStatusLine("信号", "${state.djiSignalPercent ?: "--"}%")
            DashboardStatusLine("网络", if (state.djiNetworkAvailable) "可用" else "不可用")
            DashboardStatusLine("相机", if (state.djiAvailableCameras.isEmpty()) "--" else state.djiAvailableCameras.joinToString())
            if (!state.djiLastError.isNullOrBlank()) {
                Text(state.djiLastError, color = Coral, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ConfigOverviewCard(state: ZhiGuUiState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("配置", "当前参数")
            DashboardStatusLine("车牌", state.vehiclePlate)
            DashboardStatusLine("车型", state.vehicleType)
            DashboardStatusLine("货物", state.cargoType)
            DashboardStatusLine("吨位", "${state.cargoWeight} 吨")
            DashboardStatusLine("重心", state.centerPosition)
            DashboardStatusLine("材料", state.bindingMaterial)
            DashboardStatusLine("模板", state.selectedTemplate.label)
            DashboardStatusLine("地点", state.operationLocation)
            DashboardStatusLine("天气", state.operationWeather)
        }
    }
}

@Composable
private fun PermissionReadinessCard() {
    val context = LocalContext.current
    val permissions = remember {
        buildList {
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
    val statuses = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) {
        permissions.forEach { permission ->
            statuses[permission] = ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        result.forEach { (permission, granted) ->
            statuses[permission] = granted
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader("权限", "相机 / 相册 / 定位")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                permissions.forEach { permission ->
                    val granted = statuses[permission] == true
                    StatusPill(
                        permission.substringAfterLast('.'),
                        if (granted) Mint.copy(alpha = 0.14f) else Amber.copy(alpha = 0.16f)
                    )
                }
            }
            Button(onClick = { launcher.launch(permissions.toTypedArray()) }, modifier = Modifier.fillMaxWidth()) {
                Text("授权")
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            )
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                shape = RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 11.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun RiskCard(finding: InspectionFinding, onToggle: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (finding.resolved) Mint else finding.color)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(finding.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (finding.resolved) "已整改" else finding.level,
                    color = if (finding.resolved) Mint else finding.color,
                    fontWeight = FontWeight.Bold
                )
                Text(finding.action, style = MaterialTheme.typography.bodyMedium, color = Slate)
            }
            TextButton(onClick = onToggle) {
                Text(if (finding.resolved) "恢复" else "整改")
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, caption: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(caption, style = MaterialTheme.typography.bodyMedium, color = Slate)
    }
}

@Composable
private fun StatusPill(text: String, containerColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(containerColor)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun DashboardStatusLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Slate)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
