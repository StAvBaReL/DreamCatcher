package com.colman.dreamcatcher.model

import android.net.Uri

class StorageModel {

    enum class StorageAPI {
        CLOUDINARY
    }

    private val cloudinaryStorage = CloudinaryStorageModel()

    fun uploadProfileImageBytes(api: StorageAPI, bytes: ByteArray, callback: (Uri?, String?) -> Unit) {
        when (api) {
            StorageAPI.CLOUDINARY -> cloudinaryStorage.uploadProfileImageBytes(bytes, callback)
        }
    }

    fun uploadProfileImage(api: StorageAPI, uri: Uri, callback: (Uri?, String?) -> Unit) {
        when (api) {
            StorageAPI.CLOUDINARY -> cloudinaryStorage.uploadProfileImage(uri, callback)
        }
    }
}

