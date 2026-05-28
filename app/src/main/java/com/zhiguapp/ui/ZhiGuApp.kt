package com.zhiguapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
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
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.painterResource
import com.zhiguapp.R
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

private data class Point3D(
    val x: Float,
    val y: Float,
    val z: Float
)

private data class ProjectedPoint(
    val offset: Offset,
    val depth: Float
)

private data class DemoTruckProfile(
    val id: String,
    val label: String,
    val specLabel: String,
    val vehicleType: String,
    val totalLengthMeters: Float,
    val cargoLengthMeters: Float,
    val cargoWidthMeters: Float,
    val cargoHeightMeters: Float,
    val cabLengthMeters: Float,
    val articulated: Boolean,
    val trailerGapMeters: Float,
    val axleLayout: List<Float>
)

private data class AssistScenePreset(
    @DrawableRes val imageRes: Int,
    val title: String,
    val subtitle: String,
    val tags: List<String>
)

private val demoTruckProfiles = listOf(
    DemoTruckProfile(
        id = "light_42",
        label = "4.2m 轻卡",
        specLabel = "总长 5.99m / 栏板货厢 4.2m",
        vehicleType = "轻型货车",
        totalLengthMeters = 5.99f,
        cargoLengthMeters = 4.2f,
        cargoWidthMeters = 2.1f,
        cargoHeightMeters = 1.8f,
        cabLengthMeters = 1.65f,
        articulated = false,
        trailerGapMeters = 0f,
        axleLayout = listOf(-1.0f, 0.95f)
    ),
    DemoTruckProfile(
        id = "medium_76",
        label = "7.6m 中卡",
        specLabel = "总长 9.2m / 厢体 7.6m",
        vehicleType = "专用货车",
        totalLengthMeters = 9.2f,
        cargoLengthMeters = 7.6f,
        cargoWidthMeters = 2.45f,
        cargoHeightMeters = 2.6f,
        cabLengthMeters = 1.9f,
        articulated = false,
        trailerGapMeters = 0f,
        axleLayout = listOf(-1.35f, 0.3f, 1.6f)
    ),
    DemoTruckProfile(
        id = "box_96",
        label = "9.6m 厢货",
        specLabel = "总长 11.2m / 厢体 9.6m",
        vehicleType = "厢式货车",
        totalLengthMeters = 11.2f,
        cargoLengthMeters = 9.6f,
        cargoWidthMeters = 2.45f,
        cargoHeightMeters = 2.8f,
        cabLengthMeters = 2.0f,
        articulated = false,
        trailerGapMeters = 0f,
        axleLayout = listOf(-1.5f, 0.5f, 1.95f)
    ),
    DemoTruckProfile(
        id = "semi_1375",
        label = "13.75m 半挂",
        specLabel = "总长 17.1m / 挂车板长 13.75m",
        vehicleType = "牵引车",
        totalLengthMeters = 17.1f,
        cargoLengthMeters = 13.75f,
        cargoWidthMeters = 2.55f,
        cargoHeightMeters = 3.0f,
        cabLengthMeters = 2.35f,
        articulated = true,
        trailerGapMeters = 1.15f,
        axleLayout = listOf(-2.0f, -1.15f, 1.45f, 1.8f, 2.15f)
    )
)

private fun truckProfileForVehicleType(vehicleType: String): DemoTruckProfile {
    return demoTruckProfiles.firstOrNull { profile ->
        profile.vehicleType == vehicleType || profile.label == vehicleType
    } ?: demoTruckProfiles.last()
}

private enum class AssistViewMode(val label: String) {
    Overview("总览"),
    TopDown("顶视"),
    Side("侧视")
}

private enum class AssistRiskLevel(val label: String, val color: Color) {
    Safe("低风险", Mint),
    Notice("中风险", Amber),
    Alert("高风险", Coral)
}

private enum class AssistDemoMode(val label: String) {
    Normal("正常"),
    Approaching("风险逼近"),
    SafePass("安全通过")
}

private enum class AssistThemeMode(val label: String) {
    Day("白天"),
    Night("夜间")
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
    var viewMode by remember { mutableStateOf(AssistViewMode.Overview) }
    var demoMode by remember { mutableStateOf(AssistDemoMode.Normal) }
    var themeMode by remember { mutableStateOf(AssistThemeMode.Night) }
    var autoPlay by remember { mutableStateOf(false) }
    var truckProfileId by remember(state.vehicleType) { mutableStateOf(truckProfileForVehicleType(state.vehicleType).id) }
    val selectedTruckProfile = demoTruckProfiles.firstOrNull { it.id == truckProfileId } ?: demoTruckProfiles.last()

    LaunchedEffect(autoPlay, demoMode) {
        if (!autoPlay) return@LaunchedEffect
        kotlinx.coroutines.delay(2600)
        demoMode = when (demoMode) {
            AssistDemoMode.Normal -> AssistDemoMode.Approaching
            AssistDemoMode.Approaching -> AssistDemoMode.SafePass
            AssistDemoMode.SafePass -> AssistDemoMode.Normal
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { AssistHero(state, viewMode, demoMode, themeMode) }
        item {
            AssistControlCard(
                viewMode = viewMode,
                onViewModeChange = { viewMode = it },
                demoMode = demoMode,
                onDemoModeChange = {
                    autoPlay = false
                    demoMode = it
                },
                themeMode = themeMode,
                onThemeModeChange = { themeMode = it },
                autoPlay = autoPlay,
                onAutoPlayToggle = { autoPlay = !autoPlay },
                selectedTruckProfile = selectedTruckProfile,
                onTruckProfileChange = { truckProfileId = it.id }
            )
        }
        item { AssistScene3DCard(state, viewMode, demoMode, themeMode, selectedTruckProfile) }
        item { AssistSummaryRow(state, demoMode) }
        item { AssistTargetListCard(state, viewMode, demoMode) }
        item { AssistGuidanceCard(state, viewMode, demoMode, themeMode) }
    }
}

@Composable
private fun AssistHero(
    state: ZhiGuUiState,
    viewMode: AssistViewMode,
    demoMode: AssistDemoMode,
    themeMode: AssistThemeMode
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        val gradientColors = if (themeMode == AssistThemeMode.Night) {
            listOf(Night, InkBlue, OceanBlue.copy(alpha = 0.92f))
        } else {
            listOf(Color(0xFFEAF4FF), Color(0xFFB8D8FF), Color(0xFF7FB3FF))
        }
        val primaryTextColor = if (themeMode == AssistThemeMode.Night) Color.White else InkBlue
        val secondaryTextColor = if (themeMode == AssistThemeMode.Night) Color.White.copy(alpha = 0.84f) else InkBlue.copy(alpha = 0.74f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(gradientColors)
                )
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatusPill("REAL SCENE", primaryTextColor.copy(alpha = 0.14f))
                    StatusPill("系固点 3", primaryTextColor.copy(alpha = 0.14f))
                    StatusPill(viewMode.label, primaryTextColor.copy(alpha = 0.14f))
                    StatusPill(demoMode.label, primaryTextColor.copy(alpha = 0.14f))
                    StatusPill(themeMode.label, primaryTextColor.copy(alpha = 0.14f))
                }
                Text(
                    text = "辅助系固视图",
                    color = primaryTextColor,
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "用实景化视角展示半挂货物、绑带走向和车侧系固点位置。",
                    color = secondaryTextColor,
                    style = MaterialTheme.typography.bodyMedium
                )
                LinearProgressIndicator(
                    progress = { state.missionProgress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = CyanGlow,
                    trackColor = primaryTextColor.copy(alpha = 0.14f)
                )
            }
        }
    }
}

@Composable
private fun AssistControlCard(
    viewMode: AssistViewMode,
    onViewModeChange: (AssistViewMode) -> Unit,
    demoMode: AssistDemoMode,
    onDemoModeChange: (AssistDemoMode) -> Unit,
    themeMode: AssistThemeMode,
    onThemeModeChange: (AssistThemeMode) -> Unit,
    autoPlay: Boolean,
    onAutoPlayToggle: () -> Unit,
    selectedTruckProfile: DemoTruckProfile,
    onTruckProfileChange: (DemoTruckProfile) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("演示控制", style = MaterialTheme.typography.titleMedium)
                Text("切换视角、演示模式和主题，方便比赛时快速演示不同场景", style = MaterialTheme.typography.bodyMedium, color = Slate)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("自动轮播", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (autoPlay) "当前自动切换：正常 -> 风险逼近 -> 安全通过"
                        else "点击开启自动轮播，适合答辩时连续展示",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate
                    )
                }
                Button(onClick = onAutoPlayToggle) {
                    Icon(
                        imageVector = if (autoPlay) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = if (autoPlay) "暂停自动演示" else "开启自动演示"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (autoPlay) "暂停" else "自动演示")
                }
            }
            AssistControlSection(title = "显示视角") {
                AssistViewMode.entries.forEach { mode ->
                    FilterChip(
                        label = mode.label,
                        selected = mode == viewMode,
                        onClick = { onViewModeChange(mode) }
                    )
                }
            }
            AssistControlSection(title = "演示模式") {
                AssistDemoMode.entries.forEach { mode ->
                    FilterChip(
                        label = mode.label,
                        selected = mode == demoMode,
                        onClick = { onDemoModeChange(mode) }
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("演示车型", style = MaterialTheme.typography.titleMedium)
                Text(selectedTruckProfile.specLabel, style = MaterialTheme.typography.bodyMedium, color = Slate)
            }
            AssistControlSection(title = "货车长度") {
                demoTruckProfiles.forEach { profile ->
                    FilterChip(
                        label = profile.label,
                        selected = profile.id == selectedTruckProfile.id,
                        onClick = { onTruckProfileChange(profile) }
                    )
                }
            }
            AssistControlSection(title = "感知主题") {
                AssistThemeMode.entries.forEach { mode ->
                    FilterChip(
                        label = mode.label,
                        selected = mode == themeMode,
                        onClick = { onThemeModeChange(mode) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AssistControlSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun AssistSceneCard(
    state: ZhiGuUiState,
    viewMode: AssistViewMode,
    demoMode: AssistDemoMode,
    themeMode: AssistThemeMode,
    truckProfile: DemoTruckProfile
) {
    val sweepTransition = rememberInfiniteTransition(label = "perception_scene")
    val scanProgress by sweepTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.88f,
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

    val routeNodes = state.routeNodes
    val activeNodeIndex = routeNodes.indexOfFirst { it.status == RouteNodeStatus.Active }.let { if (it == -1) 0 else it }
    val completedCount = routeNodes.count { it.status == RouteNodeStatus.Completed }
    val fixingProgress = (completedCount + scanProgress.coerceIn(0.15f, 0.95f)) / routeNodes.size.toFloat()
    val strapTension = when (demoMode) {
        AssistDemoMode.Normal -> "8.4kN"
        AssistDemoMode.Approaching -> "7.2kN"
        AssistDemoMode.SafePass -> "9.1kN"
    }
    val fixingAngle = when (demoMode) {
        AssistDemoMode.Normal -> "46°"
        AssistDemoMode.Approaching -> "31°"
        AssistDemoMode.SafePass -> "52°"
    }
    val anchorSpacing = when (demoMode) {
        AssistDemoMode.Normal -> "1.0m"
        AssistDemoMode.Approaching -> "1.3m"
        AssistDemoMode.SafePass -> "0.9m"
    }
    val cargoStability = when (demoMode) {
        AssistDemoMode.Normal -> "重心覆盖中"
        AssistDemoMode.Approaching -> "尾部需补强"
        AssistDemoMode.SafePass -> "固定稳定"
    }
    val truckStatusColor = when (demoMode) {
        AssistDemoMode.SafePass -> Mint
        AssistDemoMode.Approaching -> Coral
        AssistDemoMode.Normal -> Amber
    }
    val currentRiskLevel = when {
        demoMode == AssistDemoMode.Approaching -> AssistRiskLevel.Alert
        demoMode == AssistDemoMode.SafePass -> AssistRiskLevel.Safe
        state.findings.count { !it.resolved } >= 3 -> AssistRiskLevel.Alert
        state.findings.count { !it.resolved } >= 1 -> AssistRiskLevel.Notice
        else -> AssistRiskLevel.Safe
    }
    val overlayText = Color(0xFF1E2A3A)
    val panelBackground = if (themeMode == AssistThemeMode.Night) NightCard.copy(alpha = 0.84f) else Color.White.copy(alpha = 0.82f)
    val scenePreset = when (viewMode) {
        AssistViewMode.Overview -> AssistScenePreset(
            imageRes = R.drawable.assist_scene_overview,
            title = "总览镜头",
            subtitle = "看整车、货物排布与主绑带覆盖路径",
            tags = listOf("高位总览", "平板半挂", "托盘货物")
        )
        AssistViewMode.TopDown -> AssistScenePreset(
            imageRes = R.drawable.assist_scene_top,
            title = "顶视走带",
            subtitle = "看绑带如何压过货面与篷布",
            tags = listOf("顶部走带", "压带方向", "覆盖范围")
        )
        AssistViewMode.Side -> AssistScenePreset(
            imageRes = R.drawable.assist_scene_side_points,
            title = "侧视点位",
            subtitle = "看车侧系固点、落点与层高关系",
            tags = listOf("侧边系固点", "落点位置", "货物层高")
        )
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("系固态势", "半实景展示固定方式、点位与走带逻辑")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp)
                    .clip(RoundedCornerShape(22.dp))
            ) {
                Image(
                    painter = painterResource(id = scenePreset.imageRes),
                    contentDescription = scenePreset.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusPill(scenePreset.title, Color.Black.copy(alpha = 0.38f))
                    StatusPill(truckProfile.label, Color.Black.copy(alpha = 0.38f))
                }

                SceneMarker(
                    modifier = Modifier
                        .align(
                            when (viewMode) {
                                AssistViewMode.Overview -> Alignment.CenterStart
                                AssistViewMode.TopDown -> Alignment.TopCenter
                                AssistViewMode.Side -> Alignment.CenterEnd
                            }
                        )
                        .offset(
                            x = when (viewMode) {
                                AssistViewMode.Overview -> 28.dp
                                AssistViewMode.TopDown -> 0.dp
                                AssistViewMode.Side -> (-38).dp
                            },
                            y = when (viewMode) {
                                AssistViewMode.Overview -> 44.dp
                                AssistViewMode.TopDown -> 36.dp
                                AssistViewMode.Side -> (-18).dp
                            }
                        ),
                    label = "起点",
                    color = Coral
                )
                SceneMarker(
                    modifier = Modifier
                        .align(
                            when (viewMode) {
                                AssistViewMode.Overview -> Alignment.Center
                                AssistViewMode.TopDown -> Alignment.Center
                                AssistViewMode.Side -> Alignment.Center
                            }
                        )
                        .offset(
                            x = when (viewMode) {
                                AssistViewMode.Overview -> 26.dp
                                AssistViewMode.TopDown -> 12.dp
                                AssistViewMode.Side -> 18.dp
                            },
                            y = when (viewMode) {
                                AssistViewMode.Overview -> 4.dp
                                AssistViewMode.TopDown -> 24.dp
                                AssistViewMode.Side -> (-8).dp
                            }
                        ),
                    label = "重心区",
                    color = Amber
                )
                SceneMarker(
                    modifier = Modifier
                        .align(
                            when (viewMode) {
                                AssistViewMode.Overview -> Alignment.CenterEnd
                                AssistViewMode.TopDown -> Alignment.BottomEnd
                                AssistViewMode.Side -> Alignment.BottomEnd
                            }
                        )
                        .offset(
                            x = when (viewMode) {
                                AssistViewMode.Overview -> (-68).dp
                                AssistViewMode.TopDown -> (-78).dp
                                AssistViewMode.Side -> (-60).dp
                            },
                            y = when (viewMode) {
                                AssistViewMode.Overview -> 36.dp
                                AssistViewMode.TopDown -> (-82).dp
                                AssistViewMode.Side -> (-88).dp
                            }
                        ),
                    label = "终点",
                    color = Mint
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                scenePreset.tags.forEach { tag ->
                    StatusPill(tag, OceanBlue.copy(alpha = 0.14f))
                }
                StatusPill(if (truckProfile.articulated) "半挂结构" else "整车结构", OceanBlue.copy(alpha = 0.14f))
                StatusPill("实景演示", OceanBlue.copy(alpha = 0.14f))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AssistTargetBadge(
                    title = "视角说明",
                    detail = scenePreset.subtitle,
                    color = OceanBlue
                )
                AssistTargetBadge(
                    title = "固定状态",
                    detail = cargoStability,
                    color = truckStatusColor
                )
                AssistTargetBadge(
                    title = "当前节点",
                    detail = routeNodes.getOrNull(activeNodeIndex)?.title ?: "前端左系固点",
                    color = Amber
                )
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = panelBackground)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "固定态势",
                        color = overlayText,
                        style = MaterialTheme.typography.titleMedium
                    )
                    RiskBand(level = currentRiskLevel)
                    PerceptionLegendRow("固定结果", cargoStability, truckStatusColor, overlayText)
                    PerceptionLegendRow("车型规格", truckProfile.specLabel, OceanBlue, overlayText)
                    PerceptionLegendRow("绑带张力", strapTension, Coral, overlayText)
                    PerceptionLegendRow("捆绑角度", fixingAngle, Amber, overlayText)
                    PerceptionLegendRow("点位间距", anchorSpacing, Mint, overlayText)
                    PerceptionLegendRow("路径进度", "${(fixingProgress * 100).toInt()}%", OceanBlue, overlayText)
                }
            }

            MultiAngleCard(
                themeMode = themeMode,
                pulseAlpha = pulseAlpha,
                activeNodeIndex = activeNodeIndex,
                demoMode = demoMode
            )
        }
    }
}

@Composable
private fun AssistSummaryRow(state: ZhiGuUiState, demoMode: AssistDemoMode) {
    val currentRiskLevel = when {
        demoMode == AssistDemoMode.Approaching -> AssistRiskLevel.Alert
        demoMode == AssistDemoMode.SafePass -> AssistRiskLevel.Safe
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
        AssistMetricCard("系固点位", "3", CyanGlow)
        AssistMetricCard("受力角度", when (demoMode) {
            AssistDemoMode.Normal -> "46°"
            AssistDemoMode.Approaching -> "31°"
            AssistDemoMode.SafePass -> "52°"
        }, Mint)
        AssistMetricCard("无人机速度", if (demoMode == AssistDemoMode.Approaching) "1.8m/s" else "1.4m/s", OceanBlue)
        AssistMetricCard("固定状态", if (currentRiskLevel == AssistRiskLevel.Safe) "稳定" else "预警", Amber)
        AssistMetricCard("风险等级", currentRiskLevel.label, currentRiskLevel.color)
    }
}

@Composable
private fun AssistScene3DCard(
    state: ZhiGuUiState,
    viewMode: AssistViewMode,
    demoMode: AssistDemoMode,
    themeMode: AssistThemeMode,
    truckProfile: DemoTruckProfile
) {
    val sweepTransition = rememberInfiniteTransition(label = "perception_scene_v2")
    val scanProgress by sweepTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_progress_v2"
    )
    val droneOffset by sweepTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drone_offset_v2"
    )
    val pulseAlpha by sweepTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha_v2"
    )

    val routeNodes = state.routeNodes
    val activeNodeIndex = routeNodes.indexOfFirst { it.status == RouteNodeStatus.Active }.let { if (it == -1) 0 else it }
    val completedCount = routeNodes.count { it.status == RouteNodeStatus.Completed }
    val fixingProgress = (completedCount + scanProgress.coerceIn(0.15f, 0.95f)) / routeNodes.size.toFloat()
    val strapTension = when (demoMode) {
        AssistDemoMode.Normal -> "8.4kN"
        AssistDemoMode.Approaching -> "7.2kN"
        AssistDemoMode.SafePass -> "9.1kN"
    }
    val fixingAngle = when (demoMode) {
        AssistDemoMode.Normal -> "46°"
        AssistDemoMode.Approaching -> "31°"
        AssistDemoMode.SafePass -> "52°"
    }
    val anchorSpacing = when (demoMode) {
        AssistDemoMode.Normal -> "1.0m"
        AssistDemoMode.Approaching -> "1.3m"
        AssistDemoMode.SafePass -> "0.9m"
    }
    val cargoStability = when (demoMode) {
        AssistDemoMode.Normal -> "重心覆盖中"
        AssistDemoMode.Approaching -> "尾部需补强"
        AssistDemoMode.SafePass -> "固定稳定"
    }
    val truckStatusColor = when (demoMode) {
        AssistDemoMode.SafePass -> Mint
        AssistDemoMode.Approaching -> Coral
        AssistDemoMode.Normal -> Amber
    }
    val currentRiskLevel = when {
        demoMode == AssistDemoMode.Approaching -> AssistRiskLevel.Alert
        demoMode == AssistDemoMode.SafePass -> AssistRiskLevel.Safe
        state.findings.count { !it.resolved } >= 3 -> AssistRiskLevel.Alert
        state.findings.count { !it.resolved } >= 1 -> AssistRiskLevel.Notice
        else -> AssistRiskLevel.Safe
    }
    var rotationX by remember(viewMode, truckProfile.id) {
        mutableStateOf(
            when (viewMode) {
                AssistViewMode.Overview -> 22f
                AssistViewMode.TopDown -> 68f
                AssistViewMode.Side -> 4f
            }
        )
    }
    var rotationY by remember(viewMode, truckProfile.id) {
        mutableStateOf(
            when (viewMode) {
                AssistViewMode.Overview -> -28f
                AssistViewMode.TopDown -> -16f
                AssistViewMode.Side -> -88f
            }
        )
    }
    var sceneScale by remember(truckProfile.id) { mutableStateOf(1.24f) }
    val overlayText = Color(0xFF1E2A3A)
    val panelBackground = if (themeMode == AssistThemeMode.Night) NightCard.copy(alpha = 0.84f) else Color.White.copy(alpha = 0.82f)
    val scenePreset = when (viewMode) {
        AssistViewMode.Overview -> AssistScenePreset(
            imageRes = R.drawable.assist_scene_overview,
            title = "总览镜头",
            subtitle = "高位斜俯视查看整车比例、货物排布和主压带覆盖",
            tags = listOf("高位总览", "平板半挂", "托盘货物")
        )
        AssistViewMode.TopDown -> AssistScenePreset(
            imageRes = R.drawable.assist_scene_top,
            title = "顶部压带",
            subtitle = "查看绑带跨过货面的覆盖范围与受力路径",
            tags = listOf("顶部走带", "覆盖范围", "受力闭环")
        )
        AssistViewMode.Side -> AssistScenePreset(
            imageRes = R.drawable.assist_scene_side_points,
            title = "侧视点位",
            subtitle = "查看车侧锚点、压带落点与货物层高关系",
            tags = listOf("侧边锚点", "压带落点", "货物层高")
        )
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("系固态势", "3D 演示半挂车、托盘货物、绑带路径和无人机牵引关系")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            if (themeMode == AssistThemeMode.Night) {
                                listOf(Color(0xFF18212A), Color(0xFF28323E), Color(0xFF10161D))
                            } else {
                                listOf(Color(0xFFF2F5F8), Color(0xFFE2E8EE), Color(0xFFD4DCE5))
                            }
                        )
                    )
            ) {
                ReferenceTruckFixingScene(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(viewMode) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                sceneScale = (sceneScale * zoom).coerceIn(0.88f, 2.15f)
                                rotationY = (rotationY + pan.x * 0.18f).coerceIn(-180f, 180f)
                                if (viewMode == AssistViewMode.Overview) {
                                    rotationX = (rotationX - pan.y * 0.10f).coerceIn(12f, 74f)
                                }
                            }
                        }
                        .pointerInput(viewMode) {
                            detectDragGestures { _, dragAmount ->
                                rotationY = (rotationY + dragAmount.x * 0.18f).coerceIn(-180f, 180f)
                                if (viewMode == AssistViewMode.Overview) {
                                    rotationX = (rotationX - dragAmount.y * 0.08f).coerceIn(12f, 74f)
                                }
                            }
                        },
                    demoMode = demoMode,
                    pulseAlpha = pulseAlpha,
                    droneOffset = droneOffset,
                    scanProgress = scanProgress,
                    activeNodeIndex = activeNodeIndex,
                    viewMode = viewMode,
                    rotationX = rotationX,
                    rotationY = rotationY,
                    truckProfile = truckProfile,
                    sceneScale = sceneScale
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusPill(scenePreset.title, Color.Black.copy(alpha = 0.38f))
                    StatusPill(truckProfile.label, Color.Black.copy(alpha = 0.38f))
                    StatusPill("3D", Color.Black.copy(alpha = 0.38f))
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AssistTargetBadge("视角", scenePreset.subtitle, OceanBlue)
                    AssistTargetBadge("固定状态", cargoStability, truckStatusColor)
                    AssistTargetBadge("当前节点", routeNodes.getOrNull(activeNodeIndex)?.title ?: "前端左系固点", Amber)
                    AssistTargetBadge("缩放", "${(sceneScale * 100).toInt()}%", Mint)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                scenePreset.tags.forEach { tag ->
                    StatusPill(tag, OceanBlue.copy(alpha = 0.14f))
                }
                StatusPill(if (truckProfile.articulated) "半挂结构" else "整车结构", OceanBlue.copy(alpha = 0.14f))
                StatusPill("双指缩放 / 拖动旋转", OceanBlue.copy(alpha = 0.14f))
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = panelBackground)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("固定态势", color = overlayText, style = MaterialTheme.typography.titleMedium)
                    RiskBand(level = currentRiskLevel)
                    PerceptionLegendRow("固定结果", cargoStability, truckStatusColor, overlayText)
                    PerceptionLegendRow("车型规格", truckProfile.specLabel, OceanBlue, overlayText)
                    PerceptionLegendRow("绑带张力", strapTension, Coral, overlayText)
                    PerceptionLegendRow("捆绑角度", fixingAngle, Amber, overlayText)
                    PerceptionLegendRow("点位间距", anchorSpacing, Mint, overlayText)
                    PerceptionLegendRow("路径进度", "${(fixingProgress * 100).toInt()}%", OceanBlue, overlayText)
                }
            }

            MultiAngleCard(
                themeMode = themeMode,
                pulseAlpha = pulseAlpha,
                activeNodeIndex = activeNodeIndex,
                demoMode = demoMode
            )
        }
    }
}

@Composable
private fun ReferenceTruckFixingScene(
    modifier: Modifier,
    demoMode: AssistDemoMode,
    pulseAlpha: Float,
    droneOffset: Float,
    scanProgress: Float,
    activeNodeIndex: Int,
    viewMode: AssistViewMode,
    rotationX: Float,
    rotationY: Float,
    truckProfile: DemoTruckProfile,
    sceneScale: Float
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width * 0.53f, size.height * 0.54f)
        val scale = size.minDimension * 0.46f * sceneScale
        val floorColor = Color(0xFF2C343A)
        val floorHighlight = Color(0xFF66727B).copy(alpha = 0.16f)
        val trailerTopColor = Color(0xFFD3B68E)
        val trailerEdgeColor = Color(0xFF1A1D21)
        val trailerSideColor = Color(0xFF343A40)
        val cabBodyColor = Color(0xFFF4F5F5)
        val cabShadowColor = Color(0xFFD9DCDD)
        val wheelColor = Color(0xFF16191D)
        val wheelHubColor = Color(0xFF7A8188)
        val palletColor = Color(0xFF8D6B42)
        val cargoTopColor = Color(0xFFF1F2F3)
        val cargoSideColor = Color(0xFFD9DDE0)
        val strapOrange = Color(0xFFE27A3F)
        val strapKhaki = Color(0xFFB8A06A)
        val outlineColor = Color(0xFF0F1215)

        fun project(point: Point3D): ProjectedPoint {
            val yaw = Math.toRadians(rotationY.toDouble())
            val pitch = Math.toRadians(
                when (viewMode) {
                    AssistViewMode.TopDown -> 72.0
                    AssistViewMode.Side -> 2.0
                    AssistViewMode.Overview -> rotationX.toDouble()
                }
            )
            val rotatedX = (point.x * kotlin.math.cos(yaw) + point.z * kotlin.math.sin(yaw)).toFloat()
            val rotatedZ = (-point.x * kotlin.math.sin(yaw) + point.z * kotlin.math.cos(yaw)).toFloat()
            val rotatedY = (point.y * kotlin.math.cos(pitch) - rotatedZ * kotlin.math.sin(pitch)).toFloat()
            val depth = (point.y * kotlin.math.sin(pitch) + rotatedZ * kotlin.math.cos(pitch)).toFloat()
            val orthoScale = scale * when (viewMode) {
                AssistViewMode.TopDown -> 0.9f
                AssistViewMode.Side -> 0.78f
                AssistViewMode.Overview -> 0.84f
            }
            return ProjectedPoint(
                offset = Offset(
                    x = center.x + rotatedX * orthoScale,
                    y = center.y - rotatedY * orthoScale + depth * orthoScale * 0.04f
                ),
                depth = depth
            )
        }

        fun drawFace(points: List<Point3D>, color: Color) {
            val projected = points.map(::project)
            val path = Path().apply {
                moveTo(projected.first().offset.x, projected.first().offset.y)
                projected.drop(1).forEach { lineTo(it.offset.x, it.offset.y) }
                close()
            }
            drawPath(path, color = color)
        }

        fun drawOutline(points: List<Point3D>, color: Color = outlineColor, width: Float = 2.6f) {
            val projected = points.map(::project)
            val path = Path().apply {
                moveTo(projected.first().offset.x, projected.first().offset.y)
                projected.drop(1).forEach { lineTo(it.offset.x, it.offset.y) }
                close()
            }
            drawPath(path = path, color = color, style = Stroke(width = width))
        }

        fun drawPolyline(points: List<Point3D>, color: Color, width: Float, dashed: Boolean = false) {
            val path = Path()
            points.map(::project).forEachIndexed { index, projected ->
                if (index == 0) path.moveTo(projected.offset.x, projected.offset.y) else path.lineTo(projected.offset.x, projected.offset.y)
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = width,
                    cap = StrokeCap.Round,
                    pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(18f, 12f)) else null
                )
            )
        }

        fun drawSegment3D(
            start: Point3D,
            end: Point3D,
            color: Color,
            width: Float,
            dashed: Boolean = false
        ) {
            val a = project(start).offset
            val b = project(end).offset
            drawLine(
                color = color,
                start = a,
                end = b,
                strokeWidth = width,
                cap = StrokeCap.Round,
                pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(14f, 10f)) else null
            )
        }

        val cabLength = 0.84f
        val gapLength = if (truckProfile.articulated) 0.24f else 0.04f
        val trailerLength = when {
            truckProfile.cargoLengthMeters >= 13f -> 2.95f
            truckProfile.cargoLengthMeters >= 9f -> 2.25f
            truckProfile.cargoLengthMeters >= 7f -> 1.92f
            else -> 1.36f
        }
        val vehicleLength = cabLength + gapLength + trailerLength
        val trailerWidth = 0.70f
        val trailerDeckHeight = 0.05f
        val trailerThickness = 0.12f
        val cargoHeight = 0.42f
        val vehicleRear = vehicleLength / 2f
        val vehicleFront = -vehicleLength / 2f
        val trailerRear = vehicleRear - 0.02f
        val trailerFront = trailerRear - trailerLength
        val cabFront = vehicleFront
        val cabRear = if (truckProfile.articulated) trailerFront - gapLength - 0.04f else trailerFront - 0.04f
        val cabWidth = trailerWidth * 0.42f
        val cabHeight = 0.60f
        val trailerScaleLabel = "${truckProfile.cargoLengthMeters}m"

        val floor = listOf(
            Point3D(vehicleFront - 1.0f, -0.28f, -1.7f),
            Point3D(vehicleRear + 1.2f, -0.28f, -1.7f),
            Point3D(vehicleRear + 1.2f, -0.28f, 1.7f),
            Point3D(vehicleFront - 1.0f, -0.28f, 1.7f)
        )
        drawFace(floor, floorColor)
        drawOutline(floor, floorHighlight, 1.8f)

        repeat(4) { index ->
            val start = project(Point3D(vehicleFront - 0.9f + index * 1.15f, -0.275f, -1.55f)).offset
            val end = project(Point3D(vehicleFront - 0.65f + index * 1.15f, -0.275f, 1.55f)).offset
            drawLine(color = floorHighlight, start = start, end = end, strokeWidth = 2f)
        }

        val trailerTop = listOf(
            Point3D(trailerFront, trailerDeckHeight, -trailerWidth),
            Point3D(trailerRear, trailerDeckHeight, -trailerWidth),
            Point3D(trailerRear, trailerDeckHeight, trailerWidth),
            Point3D(trailerFront, trailerDeckHeight, trailerWidth)
        )
        val trailerRight = listOf(
            Point3D(trailerRear, trailerDeckHeight, -trailerWidth),
            Point3D(trailerRear, -trailerThickness, -trailerWidth),
            Point3D(trailerRear, -trailerThickness, trailerWidth),
            Point3D(trailerRear, trailerDeckHeight, trailerWidth)
        )
        val trailerSide = listOf(
            Point3D(trailerFront, trailerDeckHeight, trailerWidth),
            Point3D(trailerRear, trailerDeckHeight, trailerWidth),
            Point3D(trailerRear, -trailerThickness, trailerWidth),
            Point3D(trailerFront, -trailerThickness, trailerWidth)
        )

        val cabTop = listOf(
            Point3D(cabFront, cabHeight, -cabWidth),
            Point3D(cabRear, cabHeight, -cabWidth),
            Point3D(cabRear, cabHeight, cabWidth),
            Point3D(cabFront, cabHeight, cabWidth)
        )
        val cabRight = listOf(
            Point3D(cabRear, cabHeight, -cabWidth),
            Point3D(cabRear, 0f, -cabWidth),
            Point3D(cabRear, 0f, cabWidth),
            Point3D(cabRear, cabHeight, cabWidth)
        )
        val cabSide = listOf(
            Point3D(cabFront, cabHeight, cabWidth),
            Point3D(cabRear, cabHeight, cabWidth),
            Point3D(cabRear, 0f, cabWidth),
            Point3D(cabFront, 0f, cabWidth)
        )
        val cabWindshield = listOf(
            Point3D(cabFront + cabLength * 0.12f, cabHeight * 0.84f, cabWidth * 0.82f),
            Point3D(cabFront + cabLength * 0.52f, cabHeight * 0.88f, cabWidth * 0.82f),
            Point3D(cabFront + cabLength * 0.52f, cabHeight * 0.42f, cabWidth * 0.82f),
            Point3D(cabFront + cabLength * 0.12f, cabHeight * 0.38f, cabWidth * 0.82f)
        )
        val cabSideWindow = listOf(
            Point3D(cabFront + cabLength * 0.36f, cabHeight * 0.82f, cabWidth * 0.84f),
            Point3D(cabRear - cabLength * 0.12f, cabHeight * 0.78f, cabWidth * 0.84f),
            Point3D(cabRear - cabLength * 0.12f, cabHeight * 0.46f, cabWidth * 0.84f),
            Point3D(cabFront + cabLength * 0.4f, cabHeight * 0.48f, cabWidth * 0.84f)
        )
        val grille = listOf(
            Point3D(cabFront + cabLength * 0.06f, cabHeight * 0.34f, cabWidth * 0.78f),
            Point3D(cabFront + cabLength * 0.3f, cabHeight * 0.34f, cabWidth * 0.78f),
            Point3D(cabFront + cabLength * 0.3f, cabHeight * 0.12f, cabWidth * 0.78f),
            Point3D(cabFront + cabLength * 0.06f, cabHeight * 0.12f, cabWidth * 0.78f)
        )

        val trailerShadow = listOf(
            Point3D(trailerFront + 0.1f, -0.255f, -trailerWidth * 1.08f),
            Point3D(trailerRear + 0.25f, -0.255f, -trailerWidth * 1.08f),
            Point3D(trailerRear + 0.25f, -0.255f, trailerWidth * 1.08f),
            Point3D(trailerFront + 0.1f, -0.255f, trailerWidth * 1.08f)
        )
        val cabShadow = listOf(
            Point3D(cabFront - 0.08f, -0.255f, -cabWidth * 1.15f),
            Point3D(cabRear + 0.1f, -0.255f, -cabWidth * 1.15f),
            Point3D(cabRear + 0.1f, -0.255f, cabWidth * 1.15f),
            Point3D(cabFront - 0.08f, -0.255f, cabWidth * 1.15f)
        )

        drawFace(trailerShadow, Color.Black.copy(alpha = 0.18f))
        drawFace(cabShadow, Color.Black.copy(alpha = 0.14f))

        drawFace(trailerTop, trailerTopColor)
        drawFace(trailerRight, trailerEdgeColor)
        drawFace(trailerSide, trailerSideColor)
        drawOutline(trailerTop)
        drawOutline(trailerRight)
        drawOutline(trailerSide)

        drawFace(cabTop, cabBodyColor)
        drawFace(cabRight, cabShadowColor)
        drawFace(cabSide, cabBodyColor)
        drawOutline(cabTop, Color(0xFFBFC5CA))
        drawOutline(cabRight, Color(0xFFB4BBC0))
        drawOutline(cabSide, Color(0xFFBFC5CA))
        drawFace(cabWindshield, Color(0xFF88A7B9).copy(alpha = 0.88f))
        drawFace(cabSideWindow, Color(0xFF7D99A9).copy(alpha = 0.84f))
        drawFace(grille, Color(0xFF353A3F))

        drawSegment3D(
            Point3D(cabFront + cabLength * 0.18f, cabHeight * 0.22f, cabWidth * 0.82f),
            Point3D(cabFront + cabLength * 0.28f, cabHeight * 0.22f, cabWidth * 0.82f),
            Color(0xFFF7F0C8),
            4f
        )
        drawSegment3D(
            Point3D(cabFront + cabLength * 0.18f, cabHeight * 0.22f, -cabWidth * 0.82f),
            Point3D(cabFront + cabLength * 0.28f, cabHeight * 0.22f, -cabWidth * 0.82f),
            Color(0xFFF7F0C8),
            4f
        )

        if (truckProfile.articulated) {
            drawPolyline(
                listOf(
                    Point3D(cabRear, 0.04f, 0f),
                    Point3D(cabRear + gapLength * 0.35f, 0.04f, 0f),
                    Point3D(trailerFront - 0.05f, 0.04f, 0f)
                ),
                Color(0xFF272B31),
                5f
            )
        }

        val sideRailY = trailerDeckHeight + 0.012f
        drawSegment3D(
            Point3D(trailerFront + 0.04f, sideRailY, trailerWidth * 0.98f),
            Point3D(trailerRear - 0.04f, sideRailY, trailerWidth * 0.98f),
            Color(0xFF0F1114),
            4f
        )
        drawSegment3D(
            Point3D(trailerFront + 0.04f, sideRailY, -trailerWidth * 0.98f),
            Point3D(trailerRear - 0.04f, sideRailY, -trailerWidth * 0.98f),
            Color(0xFF0F1114),
            4f
        )

        repeat(9) { index ->
            val beamX = trailerFront + 0.1f + index * ((trailerRear - trailerFront - 0.2f) / 8f)
            drawSegment3D(
                Point3D(beamX, -trailerThickness + 0.01f, trailerWidth * 0.96f),
                Point3D(beamX, -trailerThickness + 0.01f, -trailerWidth * 0.96f),
                Color(0xFF454B52),
                2.4f
            )
            drawSegment3D(
                Point3D(beamX, trailerDeckHeight + 0.002f, trailerWidth * 0.96f),
                Point3D(beamX, trailerDeckHeight + 0.002f, -trailerWidth * 0.96f),
                Color(0xFFE2C79C).copy(alpha = 0.44f),
                1.6f
            )
        }

        val landingLegX = trailerFront + 0.12f
        drawSegment3D(
            Point3D(landingLegX, 0f, trailerWidth * 0.45f),
            Point3D(landingLegX, -0.22f, trailerWidth * 0.45f),
            Color(0xFF616971),
            5f
        )
        drawSegment3D(
            Point3D(landingLegX, 0f, -trailerWidth * 0.28f),
            Point3D(landingLegX, -0.22f, -trailerWidth * 0.28f),
            Color(0xFF616971),
            5f
        )

        val palletRows = 3
        val palletColumns = 6
        val columnSpacing = 0.08f
        val palletWidth = 0.18f
        val palletDepth = 0.34f
        val palletHeight = 0.03f
        val stackHeight = cargoHeight
        val cargoStartX = trailerFront + 0.34f
        val cargoEndX = trailerRear - 0.42f
        val usableLength = cargoEndX - cargoStartX
        val cellLength = usableLength / palletColumns
        val rowCenters = listOf(trailerWidth * 0.5f, trailerWidth * 0.12f, -trailerWidth * 0.26f)

        for (row in 0 until palletRows) {
            for (column in 0 until palletColumns) {
                val xFront = cargoStartX + column * cellLength + columnSpacing * 0.25f
                val xRear = xFront + palletDepth
                val zCenter = rowCenters[row]
                val zNear = zCenter - palletWidth
                val zFar = zCenter + palletWidth
                val topHeight = stackHeight * (0.84f + 0.06f * ((column + row) % 4))

                val palletTop = listOf(
                    Point3D(xFront, palletHeight, zNear),
                    Point3D(xRear, palletHeight, zNear),
                    Point3D(xRear, palletHeight, zFar),
                    Point3D(xFront, palletHeight, zFar)
                )
                val palletSide = listOf(
                    Point3D(xFront, palletHeight, zFar),
                    Point3D(xRear, palletHeight, zFar),
                    Point3D(xRear, 0f, zFar),
                    Point3D(xFront, 0f, zFar)
                )
                val cargoTop = listOf(
                    Point3D(xFront, topHeight, zNear),
                    Point3D(xRear, topHeight, zNear),
                    Point3D(xRear, topHeight, zFar),
                    Point3D(xFront, topHeight, zFar)
                )
                val cargoRight = listOf(
                    Point3D(xRear, topHeight, zNear),
                    Point3D(xRear, palletHeight, zNear),
                    Point3D(xRear, palletHeight, zFar),
                    Point3D(xRear, topHeight, zFar)
                )
                val cargoSide = listOf(
                    Point3D(xFront, topHeight, zFar),
                    Point3D(xRear, topHeight, zFar),
                    Point3D(xRear, palletHeight, zFar),
                    Point3D(xFront, palletHeight, zFar)
                )

                drawFace(palletTop, palletColor)
                drawFace(palletSide, palletColor.copy(alpha = 0.92f))
                drawFace(cargoTop, cargoTopColor)
                drawFace(cargoRight, cargoSideColor)
                drawFace(cargoSide, cargoSideColor.copy(alpha = 0.96f))
                drawOutline(cargoTop, Color(0xFFBCC1C6), 1.6f)
                drawOutline(cargoRight, Color(0xFFBCC1C6), 1.6f)
                drawOutline(cargoSide, Color(0xFFBCC1C6), 1.6f)

                repeat(3) { seam ->
                    val seamX = xFront + (seam + 1) * (palletDepth / 4f)
                    drawSegment3D(
                        Point3D(seamX, topHeight + 0.001f, zNear),
                        Point3D(seamX, topHeight + 0.001f, zFar),
                        Color(0xFFCDD1D5),
                        1.4f
                    )
                }
                repeat(2) { seam ->
                    val seamZ = zNear + (seam + 1) * ((zFar - zNear) / 3f)
                    drawSegment3D(
                        Point3D(xFront, topHeight + 0.001f, seamZ),
                        Point3D(xRear, topHeight + 0.001f, seamZ),
                        Color(0xFFCDD1D5),
                        1.2f
                    )
                }

                if ((column + row) % 2 == 0) {
                    drawSegment3D(
                        Point3D((xFront + xRear) / 2f, topHeight + 0.002f, zNear),
                        Point3D((xFront + xRear) / 2f, palletHeight, zNear),
                        Color(0xFF6E7378).copy(alpha = 0.86f),
                        2f
                    )
                }
            }
        }

        for (markerIndex in 0..8) {
            val markerX = trailerFront + 0.18f + markerIndex * ((trailerRear - trailerFront - 0.36f) / 8f)
            drawSegment3D(
                Point3D(markerX, -0.02f, trailerWidth * 1.01f),
                Point3D(markerX + 0.05f, -0.02f, trailerWidth * 1.01f),
                if (markerIndex % 2 == 0) Color(0xFFE6F0F6) else Color(0xFFD44848),
                3.2f
            )
        }

        val strapCount = when {
            trailerLength >= 2.7f -> 5
            trailerLength >= 2.2f -> 4
            trailerLength >= 1.7f -> 3
            else -> 2
        }
        val strapAnchors = buildList {
            repeat(strapCount) { index ->
                val t = if (strapCount == 1) 0.5f else index / (strapCount - 1f)
                val strapX = trailerFront + 0.42f + (trailerLength - 0.84f) * t
                val topHeight = cargoHeight + 0.1f + 0.03f * ((index + 1) % 2)
                val topNear = Point3D(strapX, topHeight, trailerWidth * 0.24f)
                val topFar = Point3D(strapX, topHeight, -trailerWidth * 0.22f)
                val start = Point3D(strapX, trailerDeckHeight + 0.01f, trailerWidth * 0.92f)
                val end = Point3D(strapX, trailerDeckHeight + 0.01f, -trailerWidth * 0.82f)
                add(listOf(start, topNear, topFar, end))
            }
        }

        strapAnchors.forEach { strapPath ->
            drawPolyline(strapPath, strapOrange, 8f)
        }
        drawSegment3D(
            Point3D(droneOffset * 0f + trailerFront + 0.92f, cargoHeight + 0.04f, trailerWidth * 0.04f),
            Point3D(trailerFront + 0.88f, trailerDeckHeight + 0.02f, trailerWidth * 0.64f),
            strapKhaki,
            5f
        )

        val centerAnchor = Point3D((trailerFront + trailerRear) / 2f, cargoHeight + 0.18f, 0f)
        val tailAnchor = Point3D(trailerRear - 0.18f, trailerDeckHeight + 0.02f, trailerWidth * 0.78f)
        drawPolyline(
            listOf(centerAnchor, Point3D(trailerRear - 0.08f, cargoHeight * 0.55f, trailerWidth * 0.32f), tailAnchor),
            if (demoMode == AssistDemoMode.Approaching) Coral else Mint,
            5f,
            dashed = true
        )
        drawPolyline(
            listOf(
                Point3D(trailerFront + 0.22f, trailerDeckHeight + 0.02f, -trailerWidth * 0.78f),
                Point3D(trailerFront + 0.96f, cargoHeight * 0.88f, trailerWidth * 0.26f),
                Point3D(trailerRear - 0.48f, trailerDeckHeight + 0.02f, -trailerWidth * 0.66f)
            ),
            CyanGlow.copy(alpha = 0.96f),
            6f
        )

        drawLine(
            color = CyanGlow.copy(alpha = 0.46f),
            start = Offset(size.width * 0.10f, size.height * (0.20f + scanProgress * 0.42f)),
            end = Offset(size.width * 0.92f, size.height * (0.18f + scanProgress * 0.36f)),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )

        val axlePoints = if (truckProfile.articulated) {
            listOf(
                cabRear - 0.14f,
                cabRear - 0.02f,
                trailerRear - 0.44f,
                trailerRear - 0.18f,
                trailerRear + 0.08f
            )
        } else {
            listOf(cabRear - 0.04f, trailerRear - 0.26f)
        }
        axlePoints.forEach { x ->
            listOf(-trailerWidth * 0.82f, trailerWidth * 0.82f).forEach { z ->
                val wheelPoint = project(Point3D(x, -0.18f, z))
                drawCircle(wheelColor, radius = 16f, center = wheelPoint.offset)
                drawCircle(wheelHubColor, radius = 6f, center = wheelPoint.offset)
                drawCircle(Color.Black.copy(alpha = 0.16f), radius = 22f, center = wheelPoint.offset + Offset(0f, 8f))
            }
        }

        val startAnchor = strapAnchors.first().first()
        val routeCenterAnchor = centerAnchor
        val endAnchor = Point3D(trailerRear - 0.26f, trailerDeckHeight + 0.02f, trailerWidth * 0.82f)
        listOf(
            Triple(startAnchor, Coral, 0),
            Triple(routeCenterAnchor, Amber, 1),
            Triple(endAnchor, Mint, 2)
        ).forEach { (point, color, index) ->
            val projected = project(point)
            val isActive = index == activeNodeIndex
            val halo = if (isActive) 28f + 10f * pulseAlpha else 18f
            drawCircle(color.copy(alpha = if (isActive) 0.22f + 0.18f * pulseAlpha else 0.14f), radius = halo, center = projected.offset)
            drawCircle(color, radius = 11f, center = projected.offset)
            drawCircle(Color.White.copy(alpha = 0.9f), radius = 4f, center = projected.offset)
        }

        if (viewMode == AssistViewMode.Side) {
            val dimensionY = -0.34f
            val dimensionStart = Point3D(trailerFront, dimensionY, trailerWidth * 0.92f)
            val dimensionEnd = Point3D(trailerRear, dimensionY, trailerWidth * 0.92f)
            val leftTickTop = Point3D(trailerFront, dimensionY + 0.06f, trailerWidth * 0.92f)
            val rightTickTop = Point3D(trailerRear, dimensionY + 0.06f, trailerWidth * 0.92f)
            drawSegment3D(dimensionStart, dimensionEnd, Color(0xFFE8EDF2), 3.2f)
            drawSegment3D(dimensionStart, leftTickTop, Color(0xFFE8EDF2), 3.2f)
            drawSegment3D(dimensionEnd, rightTickTop, Color(0xFFE8EDF2), 3.2f)

            val startOffset = project(dimensionStart).offset
            val endOffset = project(dimensionEnd).offset
            val centerOffset = Offset(
                x = (startOffset.x + endOffset.x) / 2f,
                y = (startOffset.y + endOffset.y) / 2f - 18f
            )
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.42f),
                topLeft = Offset(centerOffset.x - 66f, centerOffset.y - 18f),
                size = Size(132f, 36f),
                cornerRadius = CornerRadius(16f, 16f)
            )
            drawLine(
                color = Amber,
                start = Offset(centerOffset.x - 28f, centerOffset.y),
                end = Offset(centerOffset.x + 28f, centerOffset.y),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        }

        val drone = Offset(
            size.width * (0.34f + 0.12f * droneOffset),
            size.height * (0.19f + 0.02f * kotlin.math.sin(droneOffset * Math.PI).toFloat())
        )
        val centerProjected = project(centerAnchor).offset
        drawCircle(Color.Black.copy(alpha = 0.18f), radius = 52f + 14f * pulseAlpha, center = drone)
        drawCircle(Color(0xFF1E2328), radius = 18f, center = drone)
        drawCircle(Color(0xFF3C444C), radius = 8f, center = drone)
        drawLine(Color(0xFFE6E8EA), Offset(drone.x - 40f, drone.y - 4f), Offset(drone.x + 40f, drone.y + 4f), 5f, StrokeCap.Round)
        drawLine(Color(0xFFE6E8EA), Offset(drone.x - 24f, drone.y - 26f), Offset(drone.x + 24f, drone.y + 26f), 5f, StrokeCap.Round)
        drawLine(Color(0xFF14181D), Offset(drone.x - 12f, drone.y + 10f), Offset(drone.x + 12f, drone.y + 10f), 4f, StrokeCap.Round)
        drawLine(Color(0xFF0E1114), Offset(drone.x, drone.y + 16f), centerProjected, 4f, StrokeCap.Round)
        drawLine(
            color = CyanGlow.copy(alpha = 0.46f),
            start = Offset(drone.x, drone.y + 16f),
            end = centerProjected,
            strokeWidth = 3f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
        )
    }
}

@Composable
private fun MultiAngleCard(
    themeMode: AssistThemeMode,
    pulseAlpha: Float,
    activeNodeIndex: Int,
    demoMode: AssistDemoMode
) {
    val textColor = if (themeMode == AssistThemeMode.Night) Color.White else InkBlue
    val subTextColor = if (themeMode == AssistThemeMode.Night) Color.White.copy(alpha = 0.76f) else InkBlue.copy(alpha = 0.72f)
    val cardBackground = if (themeMode == AssistThemeMode.Night) NightCard.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.92f)
    val angleCards = listOf(
        Triple("总览镜头", "看整车、货物排布和主绑带覆盖", AssistViewMode.Overview),
        Triple("顶视走带", "看绑带压带方向和覆盖范围", AssistViewMode.TopDown),
        Triple("侧视点位", "看车侧系固点、层高和落点", AssistViewMode.Side)
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("视角切换", color = textColor, style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                angleCards.forEachIndexed { index, (title, caption, mode) ->
                    Card(
                        modifier = Modifier.width(188.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = textColor.copy(alpha = if (themeMode == AssistThemeMode.Night) 0.08f else 0.05f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            if (themeMode == AssistThemeMode.Night) {
                                                listOf(InkBlue, OceanBlue.copy(alpha = 0.9f))
                                            } else {
                                                listOf(Color(0xFFDDEBFF), Color(0xFFA8CCFF))
                                            }
                                        )
                                    )
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height
                                    val start = Offset(w * 0.22f, h * 0.7f)
                                    val center = Offset(w * 0.5f, if (mode == AssistViewMode.Side) h * 0.38f else h * 0.46f)
                                    val end = Offset(w * 0.78f, h * 0.68f)

                                    when (mode) {
                                        AssistViewMode.TopDown -> {
                                            drawRoundRect(
                                                color = textColor.copy(alpha = 0.12f),
                                                topLeft = Offset(w * 0.14f, h * 0.2f),
                                                size = Size(w * 0.72f, h * 0.56f),
                                                cornerRadius = CornerRadius(20f, 20f)
                                            )
                                            drawRoundRect(
                                                color = Amber.copy(alpha = 0.74f),
                                                topLeft = Offset(w * 0.34f, h * 0.34f),
                                                size = Size(w * 0.32f, h * 0.24f),
                                                cornerRadius = CornerRadius(18f, 18f)
                                            )
                                        }
                                        AssistViewMode.Side -> {
                                            drawLine(
                                                color = textColor.copy(alpha = 0.16f),
                                                start = Offset(w * 0.14f, h * 0.78f),
                                                end = Offset(w * 0.86f, h * 0.78f),
                                                strokeWidth = 4f
                                            )
                                            drawRoundRect(
                                                color = Amber.copy(alpha = 0.74f),
                                                topLeft = Offset(w * 0.26f, h * 0.36f),
                                                size = Size(w * 0.42f, h * 0.24f),
                                                cornerRadius = CornerRadius(18f, 18f)
                                            )
                                        }
                                        AssistViewMode.Overview -> {
                                            val truck = Path().apply {
                                                moveTo(w * 0.28f, h * 0.28f)
                                                lineTo(w * 0.78f, h * 0.28f)
                                                lineTo(w * 0.66f, h * 0.72f)
                                                lineTo(w * 0.16f, h * 0.72f)
                                                close()
                                            }
                                            drawPath(truck, color = textColor.copy(alpha = 0.12f))
                                            val cargo = Path().apply {
                                                moveTo(w * 0.36f, h * 0.36f)
                                                lineTo(w * 0.62f, h * 0.36f)
                                                lineTo(w * 0.56f, h * 0.58f)
                                                lineTo(w * 0.3f, h * 0.58f)
                                                close()
                                            }
                                            drawPath(cargo, color = Amber.copy(alpha = 0.72f))
                                        }
                                    }

                                    drawPath(
                                        Path().apply {
                                            moveTo(start.x, start.y)
                                            quadraticTo(w * 0.38f, h * 0.32f, center.x, center.y)
                                            quadraticTo(w * 0.62f, h * 0.34f, end.x, end.y)
                                        },
                                        color = Coral,
                                        style = Stroke(width = 5f, cap = StrokeCap.Round)
                                    )
                                    drawPath(
                                        Path().apply {
                                            moveTo(start.x + 10f, start.y - 10f)
                                            quadraticTo(w * 0.44f, h * 0.82f, end.x - 12f, end.y - 8f)
                                        },
                                        color = CyanGlow,
                                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                                    )

                                    listOf(Coral, Amber, Mint).forEachIndexed { nodeIndex, color ->
                                        val point = when (nodeIndex) {
                                            0 -> start
                                            1 -> center
                                            else -> end
                                        }
                                        val active = nodeIndex == activeNodeIndex
                                        drawCircle(
                                            color = color.copy(alpha = if (active) 0.24f + 0.18f * pulseAlpha else 0.14f),
                                            radius = if (active) 12f + 6f * pulseAlpha else 10f,
                                            center = point
                                        )
                                        drawCircle(color, radius = 6f, center = point)
                                    }
                                }
                                StatusPill(
                                    if (index == activeNodeIndex) "当前讲解" else if (demoMode == AssistDemoMode.Approaching && index == 2) "补强关注" else "镜头 ${index + 1}",
                                    Color.Black.copy(alpha = 0.22f),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(10.dp)
                                )
                            }
                            Text(title, color = textColor, style = MaterialTheme.typography.titleMedium)
                            Text(caption, color = subTextColor, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SceneMarker(
    modifier: Modifier = Modifier,
    label: String,
    color: Color
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.Black.copy(alpha = 0.38f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun InteractiveFixingScene(
    modifier: Modifier,
    overlayText: Color,
    demoMode: AssistDemoMode,
    pulseAlpha: Float,
    droneOffset: Float,
    scanProgress: Float,
    activeNodeIndex: Int,
    viewMode: AssistViewMode,
    rotationX: Float,
    rotationY: Float,
    truckProfile: DemoTruckProfile,
    sceneScale: Float
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width * 0.5f, size.height * 0.52f)
        val scale = size.minDimension * 0.41f * sceneScale
        val perspective = 3.3f
        val floorColor = Color(0xFFE7ECF2)
        val bedTopColor = Color(0xFFD2DAE4)
        val bedSideColor = Color(0xFFB8C4D1)
        val cabTopColor = Color(0xFF4B5E73)
        val cabSideColor = Color(0xFF36485C)
        val cargoTopColor = Color(0xFFD9A03B)
        val cargoSideColor = Color(0xFFB77E26)
        val outlineColor = Color(0xFF243447)

        fun project(point: Point3D): ProjectedPoint {
            val yaw = Math.toRadians(rotationY.toDouble())
            val pitch = Math.toRadians(
                when (viewMode) {
                    AssistViewMode.TopDown -> 68.0
                    AssistViewMode.Side -> 8.0
                    AssistViewMode.Overview -> rotationX.toDouble()
                }
            )
            val rotatedX = (point.x * kotlin.math.cos(yaw) + point.z * kotlin.math.sin(yaw)).toFloat()
            val rotatedZ = (-point.x * kotlin.math.sin(yaw) + point.z * kotlin.math.cos(yaw)).toFloat()
            val rotatedY = (point.y * kotlin.math.cos(pitch) - rotatedZ * kotlin.math.sin(pitch)).toFloat()
            val depth = (point.y * kotlin.math.sin(pitch) + rotatedZ * kotlin.math.cos(pitch)).toFloat()
            val distance = perspective - depth
            val factor = scale / distance
            return ProjectedPoint(
                offset = Offset(center.x + rotatedX * factor, center.y - rotatedY * factor),
                depth = depth
            )
        }

        fun drawFace(points: List<Point3D>, color: Color) {
            val projected = points.map(::project)
            val path = Path().apply {
                moveTo(projected.first().offset.x, projected.first().offset.y)
                projected.drop(1).forEach { lineTo(it.offset.x, it.offset.y) }
                close()
            }
            drawPath(path, color = color)
        }

        fun drawOutline(points: List<Point3D>, color: Color = outlineColor, width: Float = 3f) {
            val projected = points.map(::project)
            val path = Path().apply {
                moveTo(projected.first().offset.x, projected.first().offset.y)
                projected.drop(1).forEach { lineTo(it.offset.x, it.offset.y) }
                close()
            }
            drawPath(path = path, color = color, style = Stroke(width = width))
        }

        fun drawPolyline(points: List<Point3D>, color: Color, width: Float, dashed: Boolean = false) {
            val path = Path()
            points.map(::project).forEachIndexed { index, projected ->
                if (index == 0) path.moveTo(projected.offset.x, projected.offset.y)
                else path.lineTo(projected.offset.x, projected.offset.y)
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = width,
                    cap = StrokeCap.Round,
                    pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(18f, 12f)) else null
                )
            )
        }

        val normalizedTotalLength = 2.9f
        val cargoLengthRatio = truckProfile.cargoLengthMeters / truckProfile.totalLengthMeters
        val cabLengthRatio = truckProfile.cabLengthMeters / truckProfile.totalLengthMeters
        val gapRatio = truckProfile.trailerGapMeters / truckProfile.totalLengthMeters
        val vehicleLength = normalizedTotalLength
        val cargoLength = vehicleLength * cargoLengthRatio.coerceIn(0.45f, 0.84f)
        val cabLength = vehicleLength * cabLengthRatio.coerceIn(0.12f, 0.22f)
        val gapLength = vehicleLength * gapRatio.coerceIn(0f, 0.08f)
        val widthScale = (truckProfile.cargoWidthMeters / 2.55f).coerceIn(0.82f, 1f)
        val cargoWidth = 0.55f * widthScale
        val cargoHeightScale = (truckProfile.cargoHeightMeters / 3.0f).coerceIn(0.6f, 1f)
        val cargoHeight = 0.5f * cargoHeightScale
        val bedHeight = 0.2f
        val vehicleRear = vehicleLength / 2f
        val vehicleFront = -vehicleLength / 2f
        val cargoRear = vehicleRear - 0.08f
        val cargoFront = cargoRear - cargoLength
        val cabFront = vehicleFront
        val cabRear = if (truckProfile.articulated) cargoFront - gapLength - 0.02f else cargoFront - 0.02f
        val cabWidth = cargoWidth * 0.86f
        val cabHeight = 0.46f

        val bedTop = listOf(
            Point3D(cargoFront, 0f, -cargoWidth),
            Point3D(cargoRear, 0f, -cargoWidth),
            Point3D(cargoRear, 0f, cargoWidth),
            Point3D(cargoFront, 0f, cargoWidth)
        )
        val bedRight = listOf(
            Point3D(cargoRear, 0f, -cargoWidth),
            Point3D(cargoRear, -bedHeight, -cargoWidth),
            Point3D(cargoRear, -bedHeight, cargoWidth),
            Point3D(cargoRear, 0f, cargoWidth)
        )
        val bedFront = listOf(
            Point3D(cargoFront, 0f, cargoWidth),
            Point3D(cargoRear, 0f, cargoWidth),
            Point3D(cargoRear, -bedHeight, cargoWidth),
            Point3D(cargoFront, -bedHeight, cargoWidth)
        )
        val cabTop = listOf(
            Point3D(cabFront, cabHeight, -cabWidth),
            Point3D(cabRear, cabHeight, -cabWidth),
            Point3D(cabRear, cabHeight, cabWidth),
            Point3D(cabFront, cabHeight, cabWidth)
        )
        val cabSide = listOf(
            Point3D(cabRear, cabHeight, -cabWidth),
            Point3D(cabRear, 0f, -cabWidth),
            Point3D(cabRear, 0f, cabWidth),
            Point3D(cabRear, cabHeight, cabWidth)
        )
        val cabFrontFace = listOf(
            Point3D(cabFront, cabHeight, cabWidth),
            Point3D(cabRear, cabHeight, cabWidth),
            Point3D(cabRear, 0f, cabWidth),
            Point3D(cabFront, 0f, cabWidth)
        )

        val cargoInsetFront = cargoFront + cargoLength * 0.18f
        val cargoInsetRear = cargoRear - cargoLength * 0.14f
        val cargoInsetWidth = cargoWidth * 0.58f
        val cargoTop = listOf(
            Point3D(cargoInsetFront, cargoHeight, -cargoInsetWidth),
            Point3D(cargoInsetRear, cargoHeight, -cargoInsetWidth),
            Point3D(cargoInsetRear, cargoHeight, cargoInsetWidth),
            Point3D(cargoInsetFront, cargoHeight, cargoInsetWidth)
        )
        val cargoRight = listOf(
            Point3D(cargoInsetRear, cargoHeight, -cargoInsetWidth),
            Point3D(cargoInsetRear, 0f, -cargoInsetWidth),
            Point3D(cargoInsetRear, 0f, cargoInsetWidth),
            Point3D(cargoInsetRear, cargoHeight, cargoInsetWidth)
        )
        val cargoFrontFace = listOf(
            Point3D(cargoInsetFront, cargoHeight, cargoInsetWidth),
            Point3D(cargoInsetRear, cargoHeight, cargoInsetWidth),
            Point3D(cargoInsetRear, 0f, cargoInsetWidth),
            Point3D(cargoInsetFront, 0f, cargoInsetWidth)
        )
        val groundPlane = listOf(
            Point3D(vehicleFront - 0.5f, -0.22f, -1.1f),
            Point3D(vehicleRear + 0.4f, -0.22f, -1.1f),
            Point3D(vehicleRear + 0.4f, -0.22f, 1.1f),
            Point3D(vehicleFront - 0.5f, -0.22f, 1.1f)
        )

        drawRoundRect(
            color = Color.White.copy(alpha = 0.62f),
            topLeft = Offset(size.width * 0.06f, size.height * 0.08f),
            size = Size(size.width * 0.88f, size.height * 0.82f),
            cornerRadius = CornerRadius(36f, 36f)
        )
        drawFace(groundPlane, floorColor)
        drawOutline(groundPlane, Color(0xFFBAC5D0), 2f)
        drawFace(bedTop, bedTopColor)
        drawFace(bedRight, bedSideColor)
        drawFace(bedFront, bedSideColor.copy(alpha = 0.95f))
        drawFace(cabTop, cabTopColor)
        drawFace(cabSide, cabSideColor)
        drawFace(cabFrontFace, cabTopColor.copy(alpha = 0.94f))
        drawFace(cargoTop, cargoTopColor)
        drawFace(cargoRight, cargoSideColor)
        drawFace(cargoFrontFace, cargoSideColor.copy(alpha = 0.94f))
        drawOutline(bedTop)
        drawOutline(bedRight, width = 2.5f)
        drawOutline(bedFront, width = 2.5f)
        drawOutline(cabTop)
        drawOutline(cabSide, width = 2.5f)
        drawOutline(cabFrontFace, width = 2.5f)
        drawOutline(cargoTop)
        drawOutline(cargoRight, width = 2.5f)
        drawOutline(cargoFrontFace, width = 2.5f)

        if (truckProfile.articulated) {
            drawPolyline(
                listOf(
                    Point3D(cabRear + 0.02f, 0.06f, 0f),
                    Point3D(cargoFront - gapLength * 0.4f, 0.06f, 0f),
                    Point3D(cargoFront - 0.02f, 0.06f, 0f)
                ),
                outlineColor.copy(alpha = 0.5f),
                width = 4f
            )
        }

        val startAnchor = Point3D(cargoFront + cargoLength * 0.08f, 0.05f, cargoWidth * 0.72f)
        val centerAnchor = Point3D((cargoFront + cargoRear) / 2f, cargoHeight + 0.08f, 0f)
        val endAnchor = Point3D(cargoRear - cargoLength * 0.1f, 0.05f, cargoWidth * 0.72f)
        val tailAnchor = Point3D(cargoRear - cargoLength * 0.02f, 0.1f, cargoWidth * 0.92f)

        drawPolyline(
            listOf(
                startAnchor,
                Point3D(cargoFront + cargoLength * 0.28f, cargoHeight + 0.1f, cargoWidth * 0.08f),
                centerAnchor,
                Point3D(cargoRear - cargoLength * 0.28f, cargoHeight + 0.1f, cargoWidth * 0.08f),
                endAnchor
            ),
            Coral.copy(alpha = 0.96f),
            width = 8f
        )
        drawPolyline(
            listOf(
                Point3D(cargoFront + cargoLength * 0.1f, 0.1f, -cargoWidth * 0.68f),
                Point3D((cargoFront + cargoRear) / 2f - cargoLength * 0.08f, cargoHeight * 0.8f, cargoWidth * 0.42f),
                Point3D(cargoRear - cargoLength * 0.14f, 0.08f, -cargoWidth * 0.56f)
            ),
            CyanGlow.copy(alpha = 0.94f),
            width = 6f
        )
        drawPolyline(
            listOf(centerAnchor, Point3D(cargoRear - cargoLength * 0.14f, cargoHeight * 0.36f, cargoWidth * 0.42f), tailAnchor),
            if (demoMode == AssistDemoMode.Approaching) Coral else Mint,
            width = 5f,
            dashed = true
        )

        drawLine(
            color = CyanGlow.copy(alpha = 0.52f),
            start = Offset(size.width * 0.14f, size.height * (0.22f + scanProgress * 0.46f)),
            end = Offset(size.width * 0.9f, size.height * (0.18f + scanProgress * 0.4f)),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )

        truckProfile.axleLayout.forEachIndexed { index, axle ->
            val x = if (truckProfile.articulated) {
                when {
                    index < 2 -> cabRear - 0.18f + axle * 0.18f
                    else -> cargoRear - 0.46f + (index - 2) * 0.22f
                }
            } else {
                vehicleFront + (axle + 1.4f) / 3.5f * vehicleLength
            }
            listOf(-cargoWidth * 0.82f, cargoWidth * 0.82f).forEach { z ->
                val wheelPoint = project(Point3D(x, -0.18f, z))
                drawCircle(Color(0xFF1B2330), radius = 10f, center = wheelPoint.offset)
                drawCircle(Color(0xFF7A8797), radius = 4f, center = wheelPoint.offset)
            }
        }

        listOf(
            Triple(startAnchor, Coral, 0),
            Triple(centerAnchor, Amber, 1),
            Triple(endAnchor, Mint, 2)
        ).forEach { (point, color, index) ->
            val projected = project(point)
            val isActive = index == activeNodeIndex
            val halo = if (isActive) 28f + 10f * pulseAlpha else 18f
            drawCircle(color.copy(alpha = if (isActive) 0.22f + 0.18f * pulseAlpha else 0.14f), radius = halo, center = projected.offset)
            drawCircle(color, radius = 11f, center = projected.offset)
            drawCircle(Color.White.copy(alpha = 0.9f), radius = 4f, center = projected.offset)
        }

        val drone = Offset(
            size.width * (0.2f + 0.2f * droneOffset),
            size.height * (0.18f + 0.02f * kotlin.math.sin(droneOffset * Math.PI).toFloat())
        )
        val centerProjected = project(centerAnchor).offset
        drawCircle(
            color = Color(0xFF5B6D83).copy(alpha = 0.12f * pulseAlpha),
            radius = 44f + 18f * pulseAlpha,
            center = drone
        )
        drawCircle(CyanGlow.copy(alpha = 0.9f), radius = 14f, center = drone)
        drawLine(
            color = outlineColor.copy(alpha = 0.92f),
            start = Offset(drone.x - 22f, drone.y),
            end = Offset(drone.x + 22f, drone.y),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = outlineColor.copy(alpha = 0.92f),
            start = Offset(drone.x, drone.y - 16f),
            end = Offset(drone.x, drone.y + 16f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = CyanGlow.copy(alpha = 0.55f),
            start = Offset(drone.x, drone.y + 14f),
            end = centerProjected,
            strokeWidth = 3f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
        )
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
private fun AssistTargetListCard(
    state: ZhiGuUiState,
    viewMode: AssistViewMode,
    demoMode: AssistDemoMode
) {
    val targetItems = listOf(
        AssistTargetItem(
            "前端左系固点",
            when (demoMode) {
                AssistDemoMode.Normal -> "起始固定位已锁定，建议缠绕 2 圈后起飞"
                AssistDemoMode.Approaching -> "前端受力不足，建议补充预紧"
                AssistDemoMode.SafePass -> "起始锚点稳定，受力正常"
            },
            Coral,
            Icons.Outlined.CheckCircle,
            if (demoMode == AssistDemoMode.SafePass) AssistRiskLevel.Safe else AssistRiskLevel.Notice
        ),
        AssistTargetItem(
            "重心区加固点",
            when (demoMode) {
                AssistDemoMode.Normal -> "交叉绑带覆盖重心区，当前角度 46°"
                AssistDemoMode.Approaching -> "重心区覆盖偏浅，建议提高交叉角度"
                AssistDemoMode.SafePass -> "重心区受力均衡，覆盖完成"
            },
            Amber,
            Icons.Outlined.ViewInAr,
            if (demoMode == AssistDemoMode.Approaching) AssistRiskLevel.Alert else AssistRiskLevel.Notice
        ),
        AssistTargetItem(
            "后端右系固点",
            when (demoMode) {
                AssistDemoMode.Normal -> "终点固定中，尾部防移位约束待确认"
                AssistDemoMode.Approaching -> "尾部补强不足，存在移位风险"
                AssistDemoMode.SafePass -> "终点锁定完成，尾部补强到位"
            },
            Mint,
            Icons.Outlined.SwapHoriz,
            when (demoMode) {
                AssistDemoMode.Approaching -> AssistRiskLevel.Notice
                AssistDemoMode.SafePass -> AssistRiskLevel.Safe
                AssistDemoMode.Normal -> if (state.missionProgress >= 100) AssistRiskLevel.Safe else AssistRiskLevel.Notice
            }
        ),
        AssistTargetItem(
            "无人机牵引轨迹",
            if (viewMode == AssistViewMode.Side) "侧视跟踪绑带抬升高度，当前航速 ${if (demoMode == AssistDemoMode.Approaching) "1.8" else "1.4"}m/s"
            else "沿规划路径稳定巡检，实时校验点位与绑带走向",
            CyanGlow,
            Icons.Outlined.Air,
            AssistRiskLevel.Safe
        ),
        AssistTargetItem(
            "货厢固定面",
            "货厢边界与货物轮廓已锁定，可持续讲解固定路径和覆盖范围",
            OceanBlue,
            Icons.Outlined.LocalShipping,
            AssistRiskLevel.Safe
        )
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("固定点位", "核心锚点与路径说明")
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
private fun AssistGuidanceCard(
    state: ZhiGuUiState,
    viewMode: AssistViewMode,
    demoMode: AssistDemoMode,
    themeMode: AssistThemeMode
) {
    val currentRiskLevel = when {
        demoMode == AssistDemoMode.Approaching -> AssistRiskLevel.Alert
        demoMode == AssistDemoMode.SafePass -> AssistRiskLevel.Safe
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
            SectionHeader("演示策略", "比赛讲解建议")
            DashboardStatusLine("当前任务", state.missionStatus)
            DashboardStatusLine("风险数量", "${state.findings.count { !it.resolved }} 项")
            DashboardStatusLine("显示视角", viewMode.label)
            DashboardStatusLine("演示模式", demoMode.label)
            DashboardStatusLine("感知主题", themeMode.label)
            DashboardStatusLine("当前等级", currentRiskLevel.label)
            DashboardStatusLine(
                "建议播报",
                when (demoMode) {
                    AssistDemoMode.Normal -> if (state.missionProgress < 50) "先讲起点和重心区，再切俯视说明交叉路径如何覆盖货物" else "重点讲三处系固点如何形成完整受力闭环"
                    AssistDemoMode.Approaching -> "强调尾部补强不足和角度偏差，展示系统如何一眼定位问题"
                    AssistDemoMode.SafePass -> "强调起点、重心区、终点已全部锁定，形成稳定固定效果"
                }
            )
            Text(
                text = "这一页现在更适合作为“系固讲解大屏”使用，重点不是算法真实性，而是让评委快速看明白货物如何被固定、哪里是关键点、为什么这样绑更稳定。",
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
private fun PerceptionLegendRow(label: String, value: String, color: Color, textColor: Color = Color.White) {
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
            Text(label, color = textColor.copy(alpha = 0.84f), style = MaterialTheme.typography.bodyMedium)
        }
        Text(value, color = textColor, style = MaterialTheme.typography.bodyMedium)
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
private fun StatusPill(text: String, containerColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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
