package com.colman.dreamcatcher.model

interface DreamImageGenerator {
    fun generateImage(prompt: String, callback: (imageUrl: String?, error: String?) -> Unit)
}
