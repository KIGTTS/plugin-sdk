package com.kigtts.plugin.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.test.*

class PluginNotificationTest {
    @Test fun notificationCommandsRoundTripWithProgressAndScopedActions() {
        val commands = listOf(AppCommand.Notifications(), AppCommand.Notifications("cancel", id = "task-1"),
            AppCommand.Notifications("post", PluginNotification("task-1", "正在执行", kind = PluginNotificationKind.PROGRESS,
                ongoing = true, live = true, indeterminate = true, symbol = "robot_2",
                contentAction = PluginNotificationAction("查看任务", pageId = "chat"),
                actions = listOf(PluginNotificationAction("停止", actionId = "stop")))))
        commands.forEach { assertEquals(it, PluginJson.decodeFromString<AppCommand>(PluginJson.encodeToString<AppCommand>(it))) }
    }
}
