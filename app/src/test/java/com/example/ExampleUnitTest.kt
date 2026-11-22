package com.example

import com.example.model.DeviceActionType
import com.example.model.YouTubeScript
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `device action type wire mapping works`() {
        assertEquals(DeviceActionType.OPEN_YOUTUBE_SEARCH, DeviceActionType.fromWireValue("open_youtube_search"))
        assertEquals(DeviceActionType.OPEN_APP, DeviceActionType.fromWireValue("open_app"))
        assertEquals(DeviceActionType.MAKE_CALL, DeviceActionType.fromWireValue("make_call"))
        assertEquals(DeviceActionType.NONE, DeviceActionType.fromWireValue("none"))
        assertEquals(DeviceActionType.NONE, DeviceActionType.fromWireValue("unknown"))
    }

    @Test
    fun `youtube script shareable text includes hook body and outro`() {
        val script = YouTubeScript(
            title = "সহজে এআই শেখা",
            hook = "প্রথম ১০ সেকেন্ডের চমক!",
            mainBody = "মূল আলোচনার ধাপসমূহ।",
            outro = "সাবস্ক্রাইব করতে ভুলবেন না!",
            topicIdeas = listOf("টপিক ১", "টপিক ২")
        )
        val formatted = script.toShareableText()
        assertTrue(formatted.contains("সহজে এআই শেখা"))
        assertTrue(formatted.contains("প্রথম ১০ সেকেন্ডের চমক!"))
        assertTrue(formatted.contains("মূল আলোচনার ধাপসমূহ।"))
        assertTrue(formatted.contains("সাবস্ক্রাইব করতে ভুলবেন না!"))
    }
}
