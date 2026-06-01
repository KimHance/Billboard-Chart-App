package com.hancekim.billboard.feature.agentchat.gemini

import com.hancekim.billboard.feature.agentchat.BuildConfig
import com.hancekim.billboard.feature.agentchat.ChatMessage
import com.hancekim.billboard.feature.agentchat.Role
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// Gemini REST API 호출 + tool calling round-trip 담당.
// 의존성이 없으므로 unscoped @Inject constructor 로 충분.
class GeminiAgentClient @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    private val client: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            builder.addInterceptor(logging)
        }
        builder.build()
    }

    /**
     * 채팅 1턴 진행.
     * - history 의 마지막 메시지가 user 의 새 prompt.
     * - 모델이 functionCall 을 반환하면 toolResolver 로 결과를 받아 2차 요청.
     * - 최종 텍스트 답변을 Assistant ChatMessage 로 반환.
     */
    suspend fun chat(
        history: ImmutableList<ChatMessage>,
        toolResolver: suspend (name: String, args: JsonObject) -> Map<String, Any?>,
    ): ChatMessage {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            return ChatMessage(
                role = Role.Error,
                text = "GEMINI_API_KEY not set in local.properties",
                id = newId(),
            )
        }

        // 1차 요청: history + tools 선언
        val initialContents = history.toGeminiContents()
        val request1 = GeminiRequest(
            contents = initialContents,
            tools = listOf(TOOL_DECLARATION),
        )
        val response1 = postGenerateContent(apiKey, request1)

        val candidate = response1.candidates?.firstOrNull()
            ?: return ChatMessage(Role.Error, "No candidates returned", newId())
        val parts = candidate.content?.parts.orEmpty()

        // functionCall 파트가 있으면 tool 실행 후 2차 요청.
        val functionCallPart = parts.firstOrNull { it.functionCall != null }
        if (functionCallPart != null) {
            val fc = functionCallPart.functionCall!!
            val args = fc.args ?: JsonObject(emptyMap())
            val toolResultMap = runCatching {
                toolResolver(fc.name, args)
            }.getOrElse { error ->
                Timber.e(error, "Tool resolver failed for ${fc.name}")
                return ChatMessage(
                    role = Role.Error,
                    text = "Tool '${fc.name}' failed: ${error.message ?: "unknown"}",
                    id = newId(),
                )
            }

            // 2차 요청: history + model turn(functionCall) + tool turn(functionResponse).
            val toolResultJson = mapToJsonObject(toolResultMap)
            val followUpContents = buildList {
                addAll(initialContents)
                add(
                    Content(
                        role = "model",
                        parts = listOf(Part(functionCall = fc)),
                    )
                )
                add(
                    Content(
                        role = "user",
                        parts = listOf(
                            Part(
                                functionResponse = FunctionResponse(
                                    name = fc.name,
                                    response = toolResultJson,
                                )
                            )
                        ),
                    )
                )
            }
            val request2 = GeminiRequest(
                contents = followUpContents,
                tools = listOf(TOOL_DECLARATION),
            )
            val response2 = postGenerateContent(apiKey, request2)
            val finalText = response2.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstNotNullOfOrNull { it.text }
                ?: return ChatMessage(Role.Error, "Empty final response", newId())
            return ChatMessage(Role.Assistant, finalText, newId())
        }

        // tool 호출 없이 텍스트 답변만 반환된 경우.
        val text = parts.firstNotNullOfOrNull { it.text }
            ?: return ChatMessage(Role.Error, "Empty text response", newId())
        return ChatMessage(Role.Assistant, text, newId())
    }

    private suspend fun postGenerateContent(
        apiKey: String,
        body: GeminiRequest,
    ): GeminiResponse = withContext(Dispatchers.IO) {
        val url = "$ENDPOINT?key=$apiKey"
        val bodyJson = json.encodeToString(GeminiRequest.serializer(), body)
        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toRequestBody(JSON_MEDIA_TYPE))
            .build()
        client.newCall(request).execute().use { resp ->
            val responseBody = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                Timber.e("Gemini API failed: ${resp.code} body=$responseBody")
                throw IllegalStateException("Gemini API error ${resp.code}: $responseBody")
            }
            json.decodeFromString(GeminiResponse.serializer(), responseBody)
        }
    }

    // ChatMessage history -> Gemini Content list 변환.
    // - User -> role="user"
    // - Assistant -> role="model"
    // - Tool / Error 는 모델 입력으로 부적합하므로 제외.
    private fun ImmutableList<ChatMessage>.toGeminiContents(): List<Content> {
        return this.mapNotNull { msg ->
            val role = when (msg.role) {
                Role.User -> "user"
                Role.Assistant -> "model"
                Role.Tool, Role.Error -> return@mapNotNull null
            }
            Content(role = role, parts = listOf(Part(text = msg.text)))
        }
    }

    private fun mapToJsonObject(map: Map<String, Any?>): JsonObject {
        return buildJsonObject {
            map.forEach { (k, v) ->
                put(k, anyToJsonElement(v))
            }
        }
    }

    private fun anyToJsonElement(value: Any?): JsonElement {
        return when (value) {
            null -> JsonPrimitive(null as String?)
            is String -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is Map<*, *> -> buildJsonObject {
                value.forEach { (k, v) -> put(k.toString(), anyToJsonElement(v)) }
            }
            else -> JsonPrimitive(value.toString())
        }
    }

    private fun newId(): String = UUID.randomUUID().toString()

    companion object {
        // gemini-2.5-flash: free tier 제공 + thought_signature 미요구.
        // (3-flash-preview 는 functionCall part 에 thought_signature round-trip 강제 → 400.
        //  2.0-flash 는 free tier limit:0 → 429.
        //  2.5-flash 가 GA + 무료 + tool calling 안정 지원.)
        private const val ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
        private const val TIMEOUT_SECONDS = 30L
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        // 두 가지 tool 노출:
        //  1) getCurrentHot100TopSong — 파라미터 없는 1위 조회 (Top 의도 명시용).
        //  2) getHot100SongByRank(rank: Int) — 1~100 임의 순위 조회.
        // LLM 이 사용자 질의를 보고 둘 중 적절한 것을 자율 선택.
        private val TOOL_DECLARATION = Tool(
            functionDeclarations = listOf(
                FunctionDeclaration(
                    name = "getCurrentHot100TopSong",
                    description = "Returns the current #1 song on Billboard Hot 100 chart.",
                    parameters = JsonObject(
                        mapOf(
                            "type" to JsonPrimitive("object"),
                            "properties" to JsonObject(emptyMap()),
                        )
                    ),
                ),
                FunctionDeclaration(
                    name = "getHot100SongByRank",
                    description = "Returns the Billboard Hot 100 song at a specific chart rank between 1 and 100.",
                    parameters = JsonObject(
                        mapOf(
                            "type" to JsonPrimitive("object"),
                            "properties" to JsonObject(
                                mapOf(
                                    "rank" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("integer"),
                                            "description" to JsonPrimitive(
                                                "Chart rank position to look up. Must be between 1 and 100 inclusive."
                                            ),
                                        )
                                    ),
                                )
                            ),
                            "required" to JsonArray(listOf(JsonPrimitive("rank"))),
                        )
                    ),
                ),
            )
        )
    }
}

// ----- Gemini REST 요청/응답 DTO (최소 필드만 정의) -----

@Serializable
internal data class GeminiRequest(
    val contents: List<Content>,
    val tools: List<Tool>? = null,
)

@Serializable
internal data class Content(
    val role: String? = null,
    val parts: List<Part> = emptyList(),
)

@Serializable
internal data class Part(
    val text: String? = null,
    @SerialName("functionCall")
    val functionCall: FunctionCall? = null,
    @SerialName("functionResponse")
    val functionResponse: FunctionResponse? = null,
)

@Serializable
internal data class FunctionCall(
    val name: String,
    val args: JsonObject? = null,
)

@Serializable
internal data class FunctionResponse(
    val name: String,
    val response: JsonObject,
)

@Serializable
internal data class Tool(
    @SerialName("function_declarations")
    val functionDeclarations: List<FunctionDeclaration>,
)

@Serializable
internal data class FunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: JsonObject,
)

@Serializable
internal data class GeminiResponse(
    val candidates: List<Candidate>? = null,
)

@Serializable
internal data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null,
)
