package com.colman.dreamcatcher.model

import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class PollinationsImageGenerator : DreamImageGenerator {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    override fun generateImage(
        prompt: String,
        callback: (imageUrl: String?, error: String?) -> Unit
    ) {
        val encodedPrompt = URLEncoder.encode(prompt, "UTF-8").replace("+", "%20")
        // Use a random seed to avoid hitting the cache if the prompt is identical.
        val seed = (0..1000000).random()
        val url =
            "https://image.pollinations.ai/prompt/$encodedPrompt?width=768&height=768&model=flux&nologo=true&seed=$seed"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                // Return the actual URL of the generated image,
                // pollinations redirects to the generated image, so we return response.request.url if it redirects,
                // or just the original url
                callback(response.request.url.toString(), null)
            } else {
                callback(null, "Failed to generate image (${response.code})")
            }
        } catch (e: Exception) {
            callback(null, e.message ?: "Network error")
        }
    }
}
