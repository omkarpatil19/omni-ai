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
                            (0 until methods.length()).any { methods.
