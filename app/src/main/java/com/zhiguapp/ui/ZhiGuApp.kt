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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.zhiguapp.ui.theme.OceanBlue
import com.zhiguapp.ui.theme.Slate

private enum class RootTab(val label: String) {
    Dashboard("首页"),
    Mission("作业"),
    Inspect("检测"),
    Settings("设置")
}

private data class StatusMetric(
    val title: String,
    val value: String,
    val accent: Color
)

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
