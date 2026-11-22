package com.example.data

import android.util.Log
import com.example.model.DeviceAction
import com.example.model.DeviceActionType
import com.example.model.ScriptTone
import com.example.model.VideoFormat
import com.example.model.YouTubeScript
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "SathiAiRepository"
private const val MODEL_NAME = "gemini-3.8-flash"
private const val AI_TIMEOUT_MS = 60_000L

data class SathiTurnResult(
    val replyText: String,
    val deviceAction: DeviceAction?,
    val youtubeScript: YouTubeScript?
)

class SathiAiRepository {

    private val sathiSystemInstruction = content {
        text(
            """
            তুমি "সাথী এআই" (Sathi AI) — ব্যবহারকারীর একজন আপন, বুদ্ধিমান ও বন্ধুসুলভ বাংলা এআই অ্যাসিস্ট্যান্ট।
            তোমার ব্যক্তিত্ব একটি সহায়ক, মার্জিত ও প্রাণবন্ত মেয়ের মতো। তুমি সবসময় সহজ, স্বাভাবিক ও আন্তরিক বাংলায় কথা বলবে (প্রয়োজনে প্রচলিত ইংরেজি শব্দ যেমন YouTube, call, app, script, hook, outro স্বাভাবিকভাবে ব্যবহার করতে পারো)।

            তোমার মূল কাজগুলো:
            ১. সাধারণ কথোপকথন বা YouTube ভিডিওর টপিক ও স্ক্রিপ্ট চাইলে সুন্দরভাবে গুছিয়ে উত্তর দেবে, আকর্ষণীয় টপিক দেবে এবং হুক (Hook), মূল অংশ ও আউটরো (Outro)-সহ ভিডিও স্ক্রিপ্ট লিখে দেবে (is_youtube_script = true সেট করে script_title, script_hook, script_body, script_outro এবং topic_ideas ফিল্ডগুলো পূরণ করবে)।
            ২. ব্যবহারকারী যদি কোনো ডিভাইস কমান্ড দেয় (যেমন—YouTube খোলা বা গান/ভিডিও চালানো, কোনো অ্যাপ ওপেন করা, বা কাউকে ফোন/কল করা), তুমি বন্ধুর মতো ইতিবাচক উত্তর দেবে এবং action ও target সঠিকভাবে নির্ধারণ করবে:
               - YouTube-এ কিছু খুঁজতে বা গান/ভিডিও চালাতে বললে: action = "open_youtube_search", target = সার্চ কুয়েরি।
               - কোনো অ্যাপ খুলতে বললে: action = "open_app", target = অ্যাপের নাম (যেমন WhatsApp, Facebook, Camera, Settings, YouTube, Chrome ইত্যাদি)।
               - কাউকে ফোন/কল করতে বললে: action = "make_call", target = নাম বা ফোন নম্বর।
               - কোনো ডিভাইস অ্যাকশন না লাগলে: action = "none", target = ""।
            ৩. spoken_reply ফিল্ডে সবসময় সংক্ষিপ্ত, প্রাণবন্ত ও স্বাভাবিক বাংলা বাক্য লিখবে যা তুমি মুখে বলবে।
            """.trimIndent()
        )
    }

    private val chatResponseSchema = Schema.obj(
        properties = mapOf(
            "reply_text" to Schema.string(
                description = "Warm, natural, well-structured Bengali reply for the user."
            ),
            "spoken_reply" to Schema.string(
                description = "Short, friendly spoken Bengali line for TTS."
            ),
            "action" to Schema.enumeration(
                values = listOf("open_youtube_search", "open_app", "make_call", "none"),
                description = "Device action type to perform."
            ),
            "target" to Schema.string(
                description = "Search query, app name, phone number/contact name, or empty string if action is none."
            ),
            "is_youtube_script" to Schema.boolean(
                description = "True when user asked for YouTube video topic ideas or a script."
            ),
            "script_title" to Schema.string(
                description = "Catchy YouTube video title in Bengali",
                nullable = true
            ),
            "script_hook" to Schema.string(
                description = "Opening Hook (প্রথম ১০-১৫ সেকেন্ড) in Bengali",
                nullable = true
            ),
            "script_body" to Schema.string(
                description = "Structured Main Content (মূল অংশ) in Bengali",
                nullable = true
            ),
            "script_outro" to Schema.string(
                description = "Closing Outro & CTA (আউটরো) in Bengali",
                nullable = true
            ),
            "topic_ideas" to Schema.array(
                items = Schema.string(),
                description = "3 catchy YouTube topic ideas in Bengali",
                nullable = true
            )
        ),
        optionalProperties = listOf(
            "script_title",
            "script_hook",
            "script_body",
            "script_outro",
            "topic_ideas"
        )
    )

    private val studioResponseSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Best primary YouTube video title in Bengali"),
            "topic_ideas" to Schema.array(
                items = Schema.string(),
                description = "3 catchy alternative Bengali YouTube video titles"
            ),
            "hook" to Schema.string(description = "High-retention opening Hook in Bengali"),
            "main_body" to Schema.string(description = "Detailed structured main script in Bengali"),
            "outro" to Schema.string(description = "Warm Outro with Subscribe/Comment CTA in Bengali")
        )
    )

    private val chatModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = chatResponseSchema
            },
            systemInstruction = sathiSystemInstruction
        )
    }

    private val studioModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = studioResponseSchema
            },
            systemInstruction = sathiSystemInstruction
        )
    }

    private var activeChat: Chat? = null

    private fun getOrStartChat(): Chat {
        return activeChat ?: chatModel.startChat().also { activeChat = it }
    }

    fun resetChat() {
        activeChat = null
    }

    suspend fun sendMessage(userMessage: String): SathiTurnResult {
        try {
            val response = withTimeout(AI_TIMEOUT_MS) {
                getOrStartChat().sendMessage(userMessage)
            }
            val rawJson = response.text?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("সাথী কোনো উত্তর পাঠাতে পারেনি। আবার চেষ্টা করুন।")

            val json = JSONObject(rawJson)
            val replyText = json.optString("reply_text").ifBlank {
                json.optString("spoken_reply", "হ্যালো! আমি শুনেছি, কীভাবে সাহায্য করতে পারি?")
            }
            val spokenReply = json.optString("spoken_reply", replyText)
            val actionType = DeviceActionType.fromWireValue(json.optString("action"))
            val target = json.optString("target", "").trim()

            val deviceAction = if (actionType != DeviceActionType.NONE || target.isNotEmpty()) {
                DeviceAction(
                    action = actionType,
                    target = target,
                    spokenReply = spokenReply
                )
            } else {
                // Still keep spokenReply accessible when action is NONE via a lightweight object or null
                DeviceAction(
                    action = DeviceActionType.NONE,
                    target = "",
                    spokenReply = spokenReply
                )
            }

            val isScript = json.optBoolean("is_youtube_script", false)
            val hook = json.optString("script_hook", "").trim()
            val body = json.optString("script_body", "").trim()
            val outro = json.optString("script_outro", "").trim()
            val title = json.optString("script_title", "").trim()
            val topicsJson = json.optJSONArray("topic_ideas")
            val topicIdeas = if (topicsJson != null) {
                (0 until topicsJson.length()).mapNotNull { i ->
                    topicsJson.optString(i).takeIf { it.isNotBlank() }
                }
            } else {
                emptyList()
            }

            val script = if (isScript && (hook.isNotEmpty() || body.isNotEmpty() || outro.isNotEmpty())) {
                YouTubeScript(
                    title = title.ifBlank { "YouTube ভিডিও স্ক্রিপ্ট" },
                    hook = hook,
                    mainBody = body,
                    outro = outro,
                    topicIdeas = topicIdeas
                )
            } else {
                null
            }

            return SathiTurnResult(
                replyText = replyText,
                deviceAction = deviceAction,
                youtubeScript = script
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Chat generation timed out", e)
            throw IllegalStateException("উত্তর আসতে একটু বেশি সময় লাগছে (৬০ সেকেন্ড পেরিয়ে গেছে)। আবার চেষ্টা করুন।", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate Sathi response", e)
            throw IllegalStateException(
                e.localizedMessage ?: "এই মুহূর্তে সাথীর সাথে সংযোগ করা যায়নি।",
                e
            )
        }
    }

    suspend fun generateYouTubeScript(
        topicOrNiche: String,
        tone: ScriptTone,
        format: VideoFormat
    ): YouTubeScript {
        val prompt = """
            বিষয়/নিশ: "$topicOrNiche"
            ফরম্যাট: ${format.promptInstruction}
            টোন/স্টাইল: ${tone.promptInstruction}

            এই বিষয়ের ওপর বাংলায় ৩টি আকর্ষণীয় YouTube ভিডিও টপিক/টাইটেল দাও এবং সেরা টপিকটির জন্য একটি পূর্ণাঙ্গ YouTube ভিডিও স্ক্রিপ্ট লেখো যেখানে থাকবে:
            - আকর্ষণীয় ও ক্লিকেবল মূল শিরোনাম (title)
            - দর্শক ধরে রাখার মতো দারুণ হুক (hook)
            - সাজানো, সহজবোধ্য মূল অংশ (main_body)
            - লাইক, কমেন্ট ও সাবস্ক্রাইব বলার আন্তরিক আউটরো (outro)
        """.trimIndent()

        try {
            val response = withTimeout(AI_TIMEOUT_MS) {
                studioModel.generateContent(prompt)
            }
            val rawJson = response.text?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("স্ক্রিপ্ট তৈরি করা যায়নি। আবার চেষ্টা করুন।")

            val json = JSONObject(rawJson)
            val title = json.optString("title", topicOrNiche)
            val hook = json.optString("hook", "")
            val mainBody = json.optString("main_body", "")
            val outro = json.optString("outro", "")
            val topicsArr = json.optJSONArray("topic_ideas")
            val topics = if (topicsArr != null) {
                (0 until topicsArr.length()).mapNotNull { idx ->
                    topicsArr.optString(idx).takeIf { it.isNotBlank() }
                }
            } else {
                emptyList()
            }

            return YouTubeScript(
                title = title,
                hook = hook,
                mainBody = mainBody,
                outro = outro,
                topicIdeas = topics
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Studio generation timed out", e)
            throw IllegalStateException("স্ক্রিপ্ট তৈরিতে সময় শেষ হয়ে গেছে। আবার চেষ্টা করুন।", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate YouTube script", e)
            throw IllegalStateException(
                e.localizedMessage ?: "স্ক্রিপ্ট তৈরি করতে সমস্যা হয়েছে।",
                e
            )
        }
    }
}
