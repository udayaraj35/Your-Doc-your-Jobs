package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    // Check if the API key has been added
    fun getApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        return if (key == "MY_GEMINI_API_KEY" || key.isEmpty()) "" else key
    }

    suspend fun generateContent(prompt: String, systemInstruction: String? = null): Result<String> {
        return generateContentUnified(
            prompt = prompt,
            systemInstruction = systemInstruction,
            apiKeyType = "google",
            customApiKey = ""
        )
    }

    suspend fun generateContentUnified(
        prompt: String,
        systemInstruction: String? = null,
        apiKeyType: String = "google",
        customApiKey: String = "",
        openaiModel: String = "gpt-4o-mini"
    ): Result<String> = withContext(Dispatchers.IO) {
        val activeKey = customApiKey.trim().ifEmpty { 
            if (apiKeyType == "google") getApiKey() else ""
        }
        
        if (activeKey.isEmpty()) {
            return@withContext Result.failure(Exception("AI API Key is missing. Please enter your custom ${if (apiKeyType == "openai") "OpenAI" else "Google Gemini"} API key under Settings & API Key Manager."))
        }

        if (apiKeyType == "openai") {
            try {
                val root = JSONObject()
                root.put("model", openaiModel)
                
                val messagesArray = JSONArray()
                if (systemInstruction != null) {
                    val sysMsg = JSONObject().apply {
                        put("role", "system")
                        put("content", systemInstruction)
                    }
                    messagesArray.put(sysMsg)
                }
                val userMsg = JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                }
                messagesArray.put(userMsg)
                root.put("messages", messagesArray)

                val body = root.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer $activeKey")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errString = response.body?.string() ?: ""
                        val errMessage = try {
                            JSONObject(errString).getJSONObject("error").getString("message")
                        } catch (e: Exception) {
                            "OpenAI HTTP error: ${response.code}"
                        }
                        return@withContext Result.failure(Exception(errMessage))
                    }
                    val respStr = response.body?.string() ?: ""
                    val content = JSONObject(respStr)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    Result.success(content)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            // Google Gemini Flow
            try {
                val root = JSONObject()
                val contentsArray = JSONArray()
                val contentObj = JSONObject()
                val partsArray = JSONArray()
                val partObj = JSONObject()
                partObj.put("text", prompt)
                partsArray.put(partObj)
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
                root.put("contents", contentsArray)

                if (systemInstruction != null) {
                    val sysObj = JSONObject()
                    val sysParts = JSONArray()
                    val sysPart = JSONObject()
                    sysPart.put("text", systemInstruction)
                    sysParts.put(sysPart)
                    sysObj.put("parts", sysParts)
                    root.put("systemInstruction", sysObj)
                }

                val url = "${BASE_URL}v1beta/models/gemini-3.5-flash:generateContent?key=$activeKey"
                val body = root.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errString = response.body?.string() ?: ""
                        val errMessage = try {
                            JSONObject(errString).getJSONObject("error").getString("message")
                        } catch (e: Exception) {
                            "Gemini HTTP error: ${response.code}"
                        }
                        return@withContext Result.failure(Exception(errMessage))
                    }

                    val responseBodyStr = response.body?.string() ?: ""
                    val jsonObj = JSONObject(responseBodyStr)
                    val text = jsonObj.optJSONArray("candidates")
                        ?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text")

                    if (text != null) {
                        Result.success(text)
                    } else {
                        Result.failure(Exception("AI did not return any text. Please try again."))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
