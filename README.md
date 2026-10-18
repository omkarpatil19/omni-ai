package com.omni.ai.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class GeminiProvider : AiProvider {

    override val displayName = "Google Gemini"

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"

    override suspend fun testConnection(apiKey: String): Result<List<String>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val json = request("GET", "$baseUrl/models", apiKey, null)
                val arr = json.optJSONArray("models")
                    ?: throw AiException("Google कडून अनपेक्षित उत्तर मिळाले.")
                buildList {
                    for (i in 0 until arr.length()) {
                        val model = arr.getJSONObject(i)
                        val methods = model.optJSONArray("supportedGenerationMethods")
                        val supportsChat = methods != null &&
                            (0 until methods.length()).any { methods.getString(it) == "generateContent" }
                        if (supportsChat) add(model.getString("name").removePrefix("models/"))
                    }
                }
            }
        }

    override suspend fun generate(
        config: GenerationConfig,
        history: List<ChatMessage>,
        userText: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val contents = JSONArray()
            history.forEach { msg ->
                contents.put(content(if (msg.role == ChatMessage.Role.USER) "user" else "model", msg.text))
            }
            contents.put(content("user", userText))

            val body = JSONObject()
                .put(
                    "systemInstruction",
                    JSONObject().put("parts", JSONArray().put(JSONObject().put("text", config.systemPrompt)))
                )
                .put("contents", contents)
                .put(
                    "generationConfig",
                    JSONObject().put("temperature", config.temperature.toDouble())
                )

            val json = request(
                "POST",
                "$baseUrl/models/${config.model}:generateContent",
                config.apiKey,
                body,
            )

            val parts = json.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")

            val text = buildString {
                if (parts != null) {
                    for (i in 0 until parts.length()) append(parts.getJSONObject(i).optString("text"))
                }
            }

            if (text.isBlank()) {
                throw AiException("AI ने रिकामे उत्तर दिले. कदाचित सुरक्षा फिल्टरमुळे. प्रश्न बदलून पहा.")
            }
            text
        }
    }

    private fun content(role: String, text: String): JSONObject =
        JSONObject()
            .put("role", role)
            .put("parts", JSONArray().put(JSONObject().put("text", text)))

    private fun request(method: String, url: String, apiKey: String, body: JSONObject?): JSONObject {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 60_000
            doOutput = body != null
            setRequestProperty("x-goog-api-key", apiKey)
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
        try {
            if (body != null) {
                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw AiException(mapError(code, text))
            return JSONObject(text)
        } catch (e: SocketTimeoutException) {
            throw AiException("वेळ संपली. इंटरनेट कनेक्शन तपासा आणि पुन्हा प्रयत्न करा.")
        } catch (e: IOException) {
            throw AiException("नेटवर्क त्रुटी. इंटरनेट कनेक्शन तपासा.")
        } finally {
            conn.disconnect()
        }
    }

    private fun mapError(code: Int, body: String): String {
        val apiMessage = runCatching {
            JSONObject(body).getJSONObject("error").optString("message")
        }.getOrNull().orEmpty()

        return when (code) {
            400 -> "विनंती चुकीची आहे. मॉडेल किंवा सेटिंग्ज तपासा. ($apiMessage)"
            401, 403 -> "API key चुकीची आहे किंवा परवानगी नाही. Settings मध्ये key तपासा."
            404 -> "निवडलेले मॉडेल उपलब्ध नाही. Settings मधून दुसरे मॉडेल निवडा."
            429 -> "वापराची मर्यादा (rate limit) संपली. थोडा वेळ थांबून पुन्हा प्रयत्न करा."
            in 500..599 -> "Gemini सर्व्हर सध्या उपलब्ध नाही. नंतर पुन्हा प्रयत्न करा."
            else -> "अनपेक्षित त्रुटी (HTTP $code)."
        }
    }
}
