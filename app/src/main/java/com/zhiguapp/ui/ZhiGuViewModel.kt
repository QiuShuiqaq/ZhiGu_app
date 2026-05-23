package com.zhiguapp.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhiguapp.dji.DjiRuntime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class PathTemplate(val label: String) {
    Basic("基础交叉路径"),
    Fragile("多点均匀路径"),
    LongCargo("纵横组合路径")
}

enum class InspectionMode(val label: String) {
    Full("全流程检测"),
    Focus("重点区域"),
    Custom("自定义检测")
}

enum class MissionStage(val label: String, val detail: String) {
    Prepare("步骤 1", "固定牵引绳并确认起点"),
    Takeoff("步骤 2", "无人机起飞并校准姿态"),
    Execute("步骤 3", "沿推荐路径执行牵引"),
    Finish("步骤 4", "完成终点固定并留存记录")
}

enum class StreamMode(val label: String) {
    Auto("自动"),
    DemoVideo("演示视频"),
    DjiLive("DJI 实时")
}

enum class RouteNodeStatus(val label: String) {
    Pending("待执行"),
    Active("进行中"),
    Completed("已完成")
}

data class InspectionFinding(
    val id: String,
    val title: String,
    val level: String,
    val action: String,
    val color: Color,
    val resolved: Boolean = false
)

data class DemoRecord(
    val id: String,
    val title: String,
    val summary: String,
    val status: String
)

data class DemoEvent(
    val id: String,
    val time: String,
    val title: String,
    val tag: String
)

data class CameraFeed(
    val name: String,
    val state: String
)

data class PreflightCheck(
    val id: String,
    val title: String,
    val done: Boolean = false
)

data class RouteNode(
    val id: String,
    val title: String,
    val note: String,
    val status: RouteNodeStatus
)

data class EvidenceItem(
    val id: String,
    val title: String,
    val status: String,
    val captured: Boolean = false
)

data class ZhiGuUiState(
    val isLoggedIn: Boolean = false,
    val accountInput: String = "judge@zhigu.local",
    val passwordInput: String = "",
    val operatorName: String = "",
    val vehiclePlate: String = "粤A12345",
    val vehicleType: String = "牵引车",
    val cargoType: String = "精密设备",
    val cargoWeight: String = "8.0",
    val centerPosition: String = "中部",
    val bindingMaterial: String = "12mm钢丝绳",
    val selectedTemplate: PathTemplate = PathTemplate.Basic,
    val routePreview: String = "起点：前端左系固点 -> 途经：重心区加固点 -> 终点：后端右系固点",
    val routeAdvice: String = "建议优先覆盖重心区，并避开货物锐边和尾部悬空区。",
    val estimatedDuration: String = "06:30",
    val estimatedDistance: String = "24m",
    val operationLocation: String = "上海洋山港堆场 A 区",
    val operationWeather: String = "多云 24°C 东南风 3 级",
    val syncStatus: String = "本地已保存",
    val reportStatus: String = "待生成",
    val mediaCount: Int = 12,
    val inspectionMode: InspectionMode = InspectionMode.Full,
    val findings: List<InspectionFinding> = defaultFindings(),
    val missionStage: MissionStage = MissionStage.Prepare,
    val missionProgress: Int = 25,
    val missionStatus: String = "等待开始演示作业",
    val missionLogs: List<String> = defaultMissionLogs(),
    val missionScore: Int = 91,
    val preflightChecks: List<PreflightCheck> = defaultPreflightChecks(),
    val routeNodes: List<RouteNode> = defaultRouteNodes(MissionStage.Prepare),
    val evidenceItems: List<EvidenceItem> = defaultEvidenceItems(),
    val demoRecords: List<DemoRecord> = defaultDemoRecords(),
    val latestRecordSummary: String = "最近一次演示记录已生成，可用于答辩展示作业闭环。",
    val demoEvents: List<DemoEvent> = defaultDemoEvents(),
    val cameraFeeds: List<CameraFeed> = defaultCameraFeeds(),
    val settingsSummary: String = "本地演示 / 权限已准备 / DJI 可扩展",
    val streamMode: StreamMode = StreamMode.Auto,
    val demoVideoUri: String? = null,
    val demoVideoName: String = "未选择演示视频",
    val djiEnabled: Boolean = false,
    val djiInitEvent: String = "当前未启用 DJI 模式",
    val djiInitProgress: Int = 0,
    val djiRegistered: Boolean = false,
    val djiConnected: Boolean = false,
    val djiProductType: String? = null,
    val djiBatteryPercent: Int? = 78,
    val djiSignalPercent: Int? = 86,
    val djiAvailableCameras: List<String> = listOf("主视角", "吊舱视角"),
    val djiNetworkAvailable: Boolean = true,
    val djiLastError: String? = null
) {
    val shouldUseDemoVideo: Boolean
        get() = when (streamMode) {
            StreamMode.DemoVideo -> demoVideoUri != null
            StreamMode.DjiLive -> false
            StreamMode.Auto -> !djiConnected && demoVideoUri != null
        }

    val shouldUseDjiLive: Boolean
        get() = when (streamMode) {
            StreamMode.DjiLive -> djiEnabled
            StreamMode.DemoVideo -> false
            StreamMode.Auto -> djiEnabled && djiConnected
        }
}

class ZhiGuViewModel : ViewModel() {
    private val localState = MutableStateFlow(ZhiGuUiState())

    val uiState: StateFlow<ZhiGuUiState> = combine(localState, DjiRuntime.state) { local, dji ->
        val merged = local.copy(
            djiEnabled = dji.enabled,
            djiInitEvent = dji.initEvent,
            djiInitProgress = dji.initProgress,
            djiRegistered = dji.registered,
            djiConnected = dji.connected,
            djiProductType = dji.productType ?: local.djiProductType,
            djiBatteryPercent = dji.batteryPercent ?: local.djiBatteryPercent,
            djiSignalPercent = dji.signalPercent ?: local.djiSignalPercent,
            djiAvailableCameras = if (dji.availableCameras.isEmpty()) local.djiAvailableCameras else dji.availableCameras,
            djiNetworkAvailable = dji.networkAvailable || local.djiNetworkAvailable,
            djiLastError = dji.lastError
        )
        merged.copy(settingsSummary = buildSettingsSummary(merged))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ZhiGuUiState()
    )

    fun updateAccountInput(value: String) {
        localState.update { it.copy(accountInput = value) }
    }

    fun updatePasswordInput(value: String) {
        localState.update { it.copy(passwordInput = value) }
    }

    fun login() {
        localState.update {
            val account = it.accountInput.ifBlank { "judge@zhigu.local" }
            it.copy(
                isLoggedIn = true,
                operatorName = account.substringBefore('@').ifBlank { "judge" }
            )
        }
    }

    fun loginWithDemoAccount() {
        localState.update {
            it.copy(
                isLoggedIn = true,
                accountInput = "judge@zhigu.local",
                operatorName = "judge"
            )
        }
    }

    fun logout() {
        localState.update {
            it.copy(
                isLoggedIn = false,
                passwordInput = ""
            )
        }
    }

    fun updateVehiclePlate(value: String) {
        localState.update { it.copy(vehiclePlate = value) }
    }

    fun updateVehicleType(value: String) {
        localState.update { it.copy(vehicleType = value) }
    }

    fun updateCargoType(value: String) {
        localState.update { it.copy(cargoType = value) }
    }

    fun updateCargoWeight(value: String) {
        localState.update { it.copy(cargoWeight = value) }
    }

    fun updateCenterPosition(value: String) {
        localState.update { it.copy(centerPosition = value) }
    }

    fun updateBindingMaterial(value: String) {
        localState.update { it.copy(bindingMaterial = value) }
    }

    fun selectTemplate(template: PathTemplate) {
        localState.update { it.copy(selectedTemplate = template) }
    }

    fun selectStreamMode(mode: StreamMode) {
        localState.update { state ->
            val adjustedMode = if (mode == StreamMode.DemoVideo && state.demoVideoUri == null) {
                StreamMode.Auto
            } else {
                mode
            }
            val updated = state.copy(streamMode = adjustedMode)
            updated.copy(settingsSummary = buildSettingsSummary(updated))
        }
    }

    fun setDemoVideo(uri: String, name: String) {
        localState.update { state ->
            val updated = state.copy(
                demoVideoUri = uri,
                demoVideoName = name,
                streamMode = if (state.streamMode == StreamMode.DjiLive) StreamMode.DjiLive else StreamMode.DemoVideo
            )
            updated.copy(settingsSummary = buildSettingsSummary(updated))
        }
    }

    fun clearDemoVideo() {
        localState.update { state ->
            val updated = state.copy(
                demoVideoUri = null,
                demoVideoName = "未选择演示视频",
                streamMode = if (state.streamMode == StreamMode.DemoVideo) StreamMode.Auto else state.streamMode
            )
            updated.copy(settingsSummary = buildSettingsSummary(updated))
        }
    }

    fun togglePreflightCheck(id: String) {
        localState.update { state ->
            val updatedChecks = state.preflightChecks.map { check ->
                if (check.id == id) check.copy(done = !check.done) else check
            }
            val readyCount = updatedChecks.count { it.done }
            state.copy(
                preflightChecks = updatedChecks,
                missionStatus = if (readyCount == updatedChecks.size) {
                    "起飞前检查完成，可进入作业执行"
                } else {
                    "起飞前检查 $readyCount/${updatedChecks.size}"
                }
            )
        }
    }

    fun toggleEvidenceCaptured(id: String) {
        localState.update { state ->
            val updatedItems = state.evidenceItems.map { item ->
                if (item.id == id) {
                    val captured = !item.captured
                    item.copy(
                        captured = captured,
                        status = if (captured) "已留痕" else "待补充"
                    )
                } else {
                    item
                }
            }
            val capturedCount = updatedItems.count { it.captured }
            state.copy(
                evidenceItems = updatedItems,
                reportStatus = if (capturedCount >= 2) "可生成" else "待补证",
                latestRecordSummary = "整改证据 $capturedCount/${updatedItems.size} 已准备"
            )
        }
    }

    fun captureDemoMedia() {
        localState.update { state ->
            val updatedCount = state.mediaCount + 1
            state.copy(
                mediaCount = updatedCount,
                syncStatus = "待同步",
                demoEvents = listOf(
                    DemoEvent("evt_media_$updatedCount", "09:4${updatedCount % 10}", "新增现场素材", "媒体")
                ) + state.demoEvents.take(4)
            )
        }
    }

    fun generateInspectionReport() {
        localState.update { state ->
            state.copy(
                reportStatus = "已生成",
                syncStatus = "待导出",
                latestRecordSummary = "检测报告已生成，可用于现场答辩展示。",
                demoEvents = listOf(
                    DemoEvent("evt_report", "09:48", "生成检测报告", "报告")
                ) + state.demoEvents.take(4)
            )
        }
    }

    fun generateRoutePreview() {
        localState.update {
            val route = when (it.selectedTemplate) {
                PathTemplate.Basic -> "起点：前端左系固点 -> 途经：重心区加固点 -> 终点：后端右系固点"
                PathTemplate.Fragile -> "起点：左前缓冲点 -> 中段双侧均载点 -> 尾端柔性固定点"
                PathTemplate.LongCargo -> "起点：前端横向约束点 -> 中部纵向稳定点 -> 后端防移位点"
            }
            val advice = when (it.selectedTemplate) {
                PathTemplate.Basic -> "适合常规普货，讲解交叉路径如何兼顾效率和稳定。"
                PathTemplate.Fragile -> "适合易碎或高价值货物，重点强调受力均匀和缓冲。"
                PathTemplate.LongCargo -> "适合长条形货物，重点强调纵向稳定和尾部防移位。"
            }
            val duration = when (it.selectedTemplate) {
                PathTemplate.Basic -> "06:30"
                PathTemplate.Fragile -> "08:10"
                PathTemplate.LongCargo -> "07:20"
            }
            val distance = when (it.selectedTemplate) {
                PathTemplate.Basic -> "24m"
                PathTemplate.Fragile -> "31m"
                PathTemplate.LongCargo -> "28m"
            }
            it.copy(
                routePreview = route,
                routeAdvice = advice,
                estimatedDuration = duration,
                estimatedDistance = distance
            )
        }
    }

    fun advanceMissionStage() {
        localState.update { state ->
            val next = when (state.missionStage) {
                MissionStage.Prepare -> MissionStage.Takeoff
                MissionStage.Takeoff -> MissionStage.Execute
                MissionStage.Execute -> MissionStage.Finish
                MissionStage.Finish -> MissionStage.Finish
            }
            val progress = when (next) {
                MissionStage.Prepare -> 25
                MissionStage.Takeoff -> 50
                MissionStage.Execute -> 75
                MissionStage.Finish -> 100
            }
            val status = when (next) {
                MissionStage.Prepare -> "已完成起点固定，等待无人机起飞"
                MissionStage.Takeoff -> "无人机姿态校准完成，准备进入牵引路径"
                MissionStage.Execute -> "正在沿推荐路径执行牵引，请重点讲解重心区处理"
                MissionStage.Finish -> "终点固定完成，可进入检测与记录展示"
            }
            val score = when (next) {
                MissionStage.Prepare -> 91
                MissionStage.Takeoff -> 93
                MissionStage.Execute -> 96
                MissionStage.Finish -> 98
            }
            val latestEvent = when (next) {
                MissionStage.Prepare -> DemoEvent("evt_prepare", "09:18", "起点固定完成", "起点")
                MissionStage.Takeoff -> DemoEvent("evt_takeoff", "09:21", "无人机起飞校准完成", "起飞")
                MissionStage.Execute -> DemoEvent("evt_execute", "09:26", "进入重心区路径", "执行")
                MissionStage.Finish -> DemoEvent("evt_finish", "09:31", "终点固定完成", "完成")
            }
            state.copy(
                missionStage = next,
                missionProgress = progress,
                missionStatus = status,
                missionScore = score,
                missionLogs = buildList {
                    add("${next.label}：${next.detail}")
                    addAll(state.missionLogs.take(3))
                },
                routeNodes = defaultRouteNodes(next),
                demoEvents = listOf(latestEvent) + state.demoEvents.take(4)
            )
        }
    }

    fun resetMissionDemo() {
        localState.update {
            it.copy(
                missionStage = MissionStage.Prepare,
                missionProgress = 25,
                missionStatus = "等待开始演示作业",
                missionLogs = defaultMissionLogs(),
                missionScore = 91,
                routeNodes = defaultRouteNodes(MissionStage.Prepare),
                preflightChecks = defaultPreflightChecks(),
                evidenceItems = defaultEvidenceItems(),
                demoEvents = defaultDemoEvents(),
                reportStatus = "待生成",
                syncStatus = "本地已保存"
            )
        }
    }

    fun selectInspectionMode(mode: InspectionMode) {
        localState.update { it.copy(inspectionMode = mode) }
    }

    fun toggleFindingResolved(id: String) {
        localState.update { state ->
            val updatedFindings = state.findings.map { finding ->
                if (finding.id == id) finding.copy(resolved = !finding.resolved) else finding
            }
            val pendingCount = updatedFindings.count { !it.resolved }
            val latestSummary = if (pendingCount == 0) {
                "所有风险项已整改完成，适合演示复检通过结果。"
            } else {
                "当前仍有 $pendingCount 项风险待处理，可继续演示整改闭环。"
            }
            val latestEvent = if (pendingCount == 0) {
                DemoEvent("evt_review_pass", "09:36", "复检通过", "复检")
            } else {
                DemoEvent("evt_review_pending", "09:34", "风险项状态更新", "整改")
            }
            state.copy(
                findings = updatedFindings,
                latestRecordSummary = latestSummary,
                reportStatus = if (pendingCount == 0) "可生成" else state.reportStatus,
                demoEvents = listOf(latestEvent) + state.demoEvents.take(4)
            )
        }
    }

    fun createDemoRecord() {
        localState.update { state ->
            val unresolved = state.findings.count { !it.resolved }
            val status = if (unresolved == 0) "已闭环" else "待复检"
            val summary = "${state.vehiclePlate} / ${state.selectedTemplate.label} / 未整改 $unresolved 项"
            val record = DemoRecord(
                id = "record_${state.demoRecords.size + 1}",
                title = "演示记录 ${state.demoRecords.size + 1}",
                summary = summary,
                status = status
            )
            state.copy(
                demoRecords = listOf(record) + state.demoRecords.take(3),
                latestRecordSummary = "已生成本地演示记录：$summary",
                syncStatus = "待同步",
                demoEvents = listOf(
                    DemoEvent("evt_record_${state.demoRecords.size + 1}", "09:40", "生成演示记录", "留痕")
                ) + state.demoEvents.take(4)
            )
        }
    }
}

private fun buildSettingsSummary(state: ZhiGuUiState): String {
    val source = when {
        state.streamMode == StreamMode.DjiLive -> "DJI 实时"
        state.streamMode == StreamMode.DemoVideo && state.demoVideoUri != null -> "演示视频"
        state.streamMode == StreamMode.Auto && state.demoVideoUri != null -> "自动切换"
        else -> "本地演示"
    }
    val permission = "权限已准备"
    val extension = if (state.djiEnabled) "DJI 可扩展" else "DJI 预留"
    return "$source / $permission / $extension"
}

private fun defaultMissionLogs() = listOf(
    "步骤 1：固定牵引绳并确认起点",
    "讲解重点：说明起点选择逻辑和系固点要求",
    "演示建议：切到图传区域说明后续将叠加路径和进度"
)

private fun defaultPreflightChecks() = listOf(
    PreflightCheck("check_power", "电量与螺旋桨检查"),
    PreflightCheck("check_rope", "牵引绳与挂点检查"),
    PreflightCheck("check_path", "路径模板与禁行区检查"),
    PreflightCheck("check_video", "图传与备用视频检查")
)

private fun defaultRouteNodes(stage: MissionStage) = listOf(
    RouteNode("node_start", "前端左系固点", "起飞前固定起点", statusFor(stage, 0)),
    RouteNode("node_center", "重心区加固点", "重点讲解受力控制", statusFor(stage, 1)),
    RouteNode("node_tail", "后端右系固点", "尾部防移位处理", statusFor(stage, 2)),
    RouteNode("node_finish", "终点确认", "留痕并准备检测", statusFor(stage, 3))
)

private fun statusFor(stage: MissionStage, index: Int): RouteNodeStatus {
    val activeIndex = when (stage) {
        MissionStage.Prepare -> 0
        MissionStage.Takeoff -> 1
        MissionStage.Execute -> 2
        MissionStage.Finish -> 3
    }
    return when {
        index < activeIndex -> RouteNodeStatus.Completed
        index == activeIndex -> RouteNodeStatus.Active
        else -> RouteNodeStatus.Pending
    }
}

private fun defaultEvidenceItems() = listOf(
    EvidenceItem("evidence_before", "整改前截图", "待补充"),
    EvidenceItem("evidence_after", "整改后截图", "待补充"),
    EvidenceItem("evidence_note", "现场备注", "待补充")
)

private fun defaultDemoEvents() = listOf(
    DemoEvent("evt_1", "09:12", "设备完成自检", "系统"),
    DemoEvent("evt_2", "09:14", "载货参数已载入", "参数"),
    DemoEvent("evt_3", "09:16", "路径模板已切换", "路径"),
    DemoEvent("evt_4", "09:17", "检测模式同步完成", "检测")
)

private fun defaultCameraFeeds() = listOf(
    CameraFeed("主视角", "在线"),
    CameraFeed("云台特写", "在线"),
    CameraFeed("路径叠加", "准备中")
)

private fun defaultFindings() = listOf(
    InspectionFinding(
        id = "core_missing",
        title = "重心区点位缺失",
        level = "严重预警",
        action = "建议新增核心加固点并复检",
        color = Color(0xFFE85B5B)
    ),
    InspectionFinding(
        id = "angle_warning",
        title = "后侧绑带角度偏差",
        level = "一般预警",
        action = "建议微调至 30°-60°",
        color = Color(0xFFF3A63B)
    ),
    InspectionFinding(
        id = "material_review",
        title = "材料规格待确认",
        level = "待复核",
        action = "比赛版先展示推荐规格与对比图",
        color = Color(0xFF2A6CF6)
    ),
    InspectionFinding(
        id = "rear_shift",
        title = "尾部防移位约束不足",
        level = "重点关注",
        action = "建议补充尾部约束点并再次确认",
        color = Color(0xFF19B67A)
    )
)

private fun defaultDemoRecords() = listOf(
    DemoRecord(
        id = "record_1",
        title = "演示记录 1",
        summary = "粤A12345 / 基础交叉路径 / 未整改 2 项",
        status = "待复检"
    ),
    DemoRecord(
        id = "record_2",
        title = "演示记录 2",
        summary = "粤A12345 / 多点均匀路径 / 已完成整改演示",
        status = "已闭环"
    ),
    DemoRecord(
        id = "record_3",
        title = "演示记录 3",
        summary = "沪B9088 / 纵横组合路径 / 图传回放已保存",
        status = "已归档"
    )
)
