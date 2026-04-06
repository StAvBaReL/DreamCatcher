package com.colman.dreamcatcher.model

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.colman.dreamcatcher.BuildConfig
import com.colman.dreamcatcher.base.DreamCatcherApplication
import java.util.UUID

class CloudinaryStorageModel {

    companion object {
        private const val TAG = "CloudinaryStorageModel"

        val cloudinary = Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME,
                "api_key", BuildConfig.CLOUDINARY_API_KEY,
                "api_secret", BuildConfig.CLOUDINARY_API_SECRET,
                "secure", true
            )
        )
    }

    fun uploadProfileImageBytes(bytes: ByteArray, callback: (Uri?, String?) -> Unit) {
        requireUser({ error -> callback(null, error) }) { user ->
            try {
                val publicId = "profile_${user.uid}_${UUID.randomUUID()}"
                val options = ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", "dreamcatcher/profiles"
                )

                // Upload the file synchronously (this should be called on a background thread)
                val result = cloudinary.uploader().upload(bytes, options)
                val secureUrl = result["secure_url"] as? String

                if (secureUrl != null) {
                    callback(secureUrl.toUri(), null)
                } else {
                    callback(null, "Upload succeeded but secure_url is missing")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Upload failed: ${e.message}", e)
                callback(null, "Upload failed: ${e.message}")
            }
        }
    }

    fun uploadProfileImage(uri: Uri, callback: (Uri?, String?) -> Unit) {
        try {
            val contentResolver = DreamCatcherApplication.appContext?.contentResolver
            val inputStream = contentResolver?.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null) {
                uploadProfileImageBytes(bytes, callback)
            } else {
                callback(null, "Could not read image data")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image URI", e)
            callback(null, "Error processing image: ${e.message}")
        }
    }
}
