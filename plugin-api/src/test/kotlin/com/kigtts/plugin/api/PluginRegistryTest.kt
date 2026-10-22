package com.kigtts.plugin.api

import kotlin.test.*

class PluginRegistryTest {
    private fun plugin(id: String, api: Int = PLUGIN_API_VERSION, actions: List<UiAction> = emptyList()) = object : KigPlugin {
        override val descriptor = PluginDescriptor(id, id, "test", apiVersion = api)
        override val contributions = PluginContributions(actions)
        override suspend fun execute(actionId: String, context: PluginContext) = PluginOutcome.Message(actionId)
    }
    @Test fun rejectsDuplicateAndIncompatiblePlugins() {
        assertFailsWith<IllegalArgumentException> { PluginRegistry(listOf(plugin("test.a"), plugin("test.a"))) }
        assertFailsWith<IllegalArgumentException> { PluginRegistry(listOf(plugin("test.a", api = 1))) }
    }
    @Test fun actionsRemainNamespacedAndDeterministicallyOrdered() {
        val registry = PluginRegistry(listOf(
            plugin("test.b", actions = listOf(UiAction("open", "B", UiSlot.TAB_END, order = 5))),
            plugin("test.a", actions = listOf(UiAction("open", "A", UiSlot.TAB_END, order = 1))),
        ))
        assertEquals(listOf("test.a/open", "test.b/open"), registry.actions(UiSlot.TAB_END).map { it.key })
        assertTrue(registry.actions(UiSlot.TAB_START).isEmpty())
    }
    @Test fun rejectsIncorrectMenuPlacementAndDuplicateActions() {
        assertFailsWith<IllegalArgumentException> { PluginRegistry(listOf(plugin("test.a", actions = listOf(UiAction("menu", "Menu", UiSlot.EDITOR_MENU))))) }
        val action = UiAction("open", "Open", UiSlot.HEADER_START)
        assertFailsWith<IllegalArgumentException> { PluginRegistry(listOf(plugin("test.a", actions = listOf(action, action)))) }
    }
    @Test fun validatesPcmFrameAlignment() {
        assertFailsWith<IllegalArgumentException> { PcmAudio(ByteArray(3)) }
        assertFailsWith<IllegalArgumentException> { PcmAudio(ByteArray(2), channels = 2) }
        assertFailsWith<IllegalArgumentException> { PcmAudio(ByteArray(4), sampleRate = 0) }
    }
    @Test fun dependentEntrypointsRequireAServiceAndDisappearAgainAfterItsRemoval() {
        val feature = object : KigPlugin {
            override val descriptor = PluginDescriptor("test.feature", "feature", "test", requires = listOf(PluginRequirement.RECOGNITION_SERVICE))
            override val contributions = PluginContributions(listOf(UiAction("listen", "聆听", UiSlot.EDITOR_TOP_START)),
                listOf(DrawerEntry("page", "page", "page", PluginIcon.MIC)), listOf(GestureAction("voice", "输入", PluginGesture.QUICK_TEXT_LONG_PRESS)))
            override suspend fun execute(actionId: String, context: PluginContext) = PluginOutcome.RefreshUi
        }
        fun recognizer(role: AsrRole) = object : KigPlugin {
            override val descriptor = PluginDescriptor("test.asr", "ASR", "test")
            override val asr = object : AsrEngine {
                override val info = AsrInfo(role = role)
                override suspend fun recognize(audio: PcmAudio, config: RecognitionConfig) = RecognitionResult("")
                override fun recognizeStream(audio: kotlinx.coroutines.flow.Flow<PcmAudio>, config: RecognitionConfig) = kotlinx.coroutines.flow.emptyFlow<RecognitionEvent>()
            }
            override suspend fun execute(actionId: String, context: PluginContext) = PluginOutcome.RefreshUi
        }
        val registry = PluginRegistry(listOf(feature, recognizer(AsrRole.PROVIDER)))
        assertEquals(listOf(PluginRequirement.RECOGNITION_SERVICE), registry.missingRequirements(feature))
        assertTrue(registry.actions(UiSlot.EDITOR_TOP_START).isEmpty()); assertTrue(registry.drawerEntries().isEmpty())
        assertTrue(registry.gestureActions(PluginGesture.QUICK_TEXT_LONG_PRESS, PluginContext("", "")).isEmpty())
        registry.replace(listOf(feature, recognizer(AsrRole.SERVICE)))
        assertTrue(registry.available(feature)); assertEquals(1, registry.actions(UiSlot.EDITOR_TOP_START).size)
        assertEquals(1, registry.gestureActions(PluginGesture.QUICK_TEXT_LONG_PRESS, PluginContext("", "")).size)
        registry.replace(listOf(feature)); assertFalse(registry.available(feature)); assertTrue(registry.actions(UiSlot.EDITOR_TOP_START).isEmpty())
    }
}
