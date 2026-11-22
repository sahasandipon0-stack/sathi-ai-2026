package com.example.model

import org.json.JSONObject

enum class AppTab(val labelBn: String, val routeId: String) {
    CHAT("সাথী চ্যাট", "chat"),
    STUDIO("স্ক্রিপ্ট স্টুডিও", "studio"),
    COMMANDS("স্মার্ট কমান্ড", "commands")
}

enum class DeviceActionType(val wireValue: String, val labelBn: String) {
    OPEN_YOUTUBE_SEARCH("open_youtube_search", "YouTube সার্চ"),
    OPEN_APP("open_app", "অ্যাপ ওপেন"),
    MAKE_CALL("make_call", "ফোন কল"),
    NONE("none", "কথোপকথন");

    companion object {
        fun fromWireValue(value: String?): DeviceActionType {
            return entries.firstOrNull { it.wireValue.equals(value?.trim(), ignoreCase = true) }
                ?: NONE
        }
    }
}

data class DeviceAction(
    val action: DeviceActionType,
    val target: String,
    val spokenReply: String
) {
    fun toFormattedJson(): String {
        val obj = JSONObject().apply {
            put("action", action.wireValue)
            put("target", target)
            put("spoken_reply", spokenReply)
        }
        return obj.toString(2)
    }
}

data class YouTubeScript(
    val title: String,
    val hook: String,
    val mainBody: String,
    val outro: String,
    val topicIdeas: List<String> = emptyList()
) {
    fun toShareableText(): String = buildString {
        appendLine("🎬 শিরোনাম: $title")
        if (topicIdeas.isNotEmpty()) {
            appendLine()
            appendLine("💡 বিকল্প টপিক:")
            topicIdeas.forEachIndexed { index, idea ->
                appendLine("${index + 1}. $idea")
            }
        }
        appendLine()
        appendLine("🪝 হুক (Hook):")
        appendLine(hook)
        appendLine()
        appendLine("📝 মূল অংশ (Main Content):")
        appendLine(mainBody)
        appendLine()
        appendLine("👋 আউটরো (Outro):")
        appendLine(outro)
    }.trim()
}

data class ChatMessage(
    val id: String,
    val isFromUser: Boolean,
    val text: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val deviceAction: DeviceAction? = null,
    val youtubeScript: YouTubeScript? = null,
    val actionAttempted: Boolean = false,
    val actionSucceeded: Boolean = false,
    val actionStatusMessage: String? = null,
    val needsContactPicker: Boolean = false,
    val isError: Boolean = false
)

enum class ScriptTone(val labelBn: String, val promptInstruction: String) {
    FRIENDLY("বন্ধুত্বপূর্ণ", "Warm, conversational, approachable Bengali"),
    ENERGETIC("প্রাণবন্ত", "High-energy, engaging, lively YouTube creator Bengali"),
    INFORMATIVE("তথ্যবহুল", "Clear, well-structured, educational Bengali"),
    STORYTELLING("গল্পের মতো", "Emotional, narrative storytelling Bengali")
}

enum class VideoFormat(val labelBn: String, val promptInstruction: String) {
    LONG("লং ভিডিও (৫-৮ মিনিট)", "Full-length YouTube video (5-8 minutes)"),
    SHORTS("Shorts (৬০ সেকেন্ড)", "Fast-paced 60-second YouTube Shorts")
}

data class InstalledAppInfo(
    val appName: String,
    val packageName: String
)
