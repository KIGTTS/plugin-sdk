package com.kigtts.plugin.api

import kotlin.test.*

class QuickTextProtocolTest {
    @Test fun externalJsonUsesStableOperationTagsAndPortableValues() {
        val command=PluginJson.decodeFromString<QuickTextCommand>("""{"type":"query","offset":0,"limit":10,"includeContent":false}""")
        assertEquals(QuickTextCommand.Query(limit=10,includeContent=false),command)
        val request:QuickTextCommand=QuickTextCommand.Prepare("id",4,listOf(QuickTextEdit.PutGroup(QuickGroup(name="工具"))))
        val json=PluginJson.encodeToString(request);assertTrue(json.contains("\"type\":\"prepare\""));assertTrue(json.contains("\"type\":\"group.put\""))
        assertEquals(request,PluginJson.decodeFromString<QuickTextCommand>(json))
        val reply:QuickTextReply=QuickTextReply.Prepared(QuickBatchPreview("token",4,1,0,0,0,listOf("计算器"),false),PluginResource("resources/mapping.json","test.agent"))
        assertEquals(reply,PluginJson.decodeFromString<QuickTextReply>(PluginJson.encodeToString(reply)))
    }
    @Test fun oldEntriesAndBundlesUseSafeDefaultsWithoutAbsolutePaths() {
        val json="""{"schema":1,"groups":[{"id":"g","name":"常用"}],"entries":[{"id":"e","groupId":"g","text":"你好"}]}"""
        val value=PluginJson.decodeFromString<QuickCardsBundle>(json)
        assertEquals(QuickCardKind.TEXT,value.entries.single().kind);assertTrue(value.entries.single().metadata.isEmpty());assertTrue(value.media.isEmpty())
    }
}
