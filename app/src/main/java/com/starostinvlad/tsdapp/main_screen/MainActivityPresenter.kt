package com.starostinvlad.tsdapp.main_screen

import android.os.Build
import android.util.Log
import com.google.gson.JsonParser
import com.starostinvlad.fan.Preferences
import com.starostinvlad.tsdapp.api.MQTTHelper
import com.starostinvlad.tsdapp.base_mvp.BasePresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.*
import javax.inject.Inject

class MainActivityPresenter @Inject constructor(
    preferences: Preferences,
    private val client: OkHttpClient,
    private val mqttHelper: MQTTHelper
) :
    BasePresenter<MainActivityContract>() {
    private var token = preferences.token
    private var domain = preferences.host
    private var port = preferences.port
    private val TAG = javaClass.simpleName
    private var tagId = ""


    fun onLoaded() = launch {
        try {
            Log.d(TAG, "domain:$domain")
            if (domain.isNotEmpty()) {
                Log.d(TAG, "domain is not empty")
                if (!isDomainAccessable(domain)) {
                    view?.openSettingScreen()
                    return@launch
                }
            } else {
                Log.d(TAG, "domain is empty")
                view?.openSettingScreen()
                return@launch
            }
            val url = domain.toHttpUrlOrNull()
            url?.let {
                try {
                    mqttHelper.init(it.host, port)
                    mqttHelper.connect(token)
                } catch (e: java.lang.Exception) {
                    view?.showError(e.message)
                }
                view?.openLoginScreen()
            }
        } catch (e: Exception) {
            view?.showError(e.message)
        }
    }

    private suspend fun isDomainAccessable(domain: String): Boolean =
        withContext(Dispatchers.IO) {
            val request: Request = Request.Builder()
                .url(domain)
                .build()
            val call: Call = client.newCall(request)
            try {
                val response: Response = call.execute()
                return@withContext response.isSuccessful
            } catch (e: Exception) {
                return@withContext false
            }

        }

    private fun tokenIsValid(token: String): Boolean {
        val payload = decodeToken(token)
        Log.d(TAG, "onLoginSubmit: token payload:$payload")

        val exp: Long =
            JsonParser.parseString(payload).asJsonObject.get("exp").asLong * 1000
        val expDate = Date(exp)
        val nowDate = Date(System.currentTimeMillis())

        Log.d(
            TAG,
            "onLoginSubmit: currTime: ${nowDate}, token exp:$expDate, delta: ${
                expDate.after(
                    nowDate
                )
            }"
        )
        return expDate.after(
            nowDate
        )
    }

    private fun decodeToken(jwt: String): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "Requires SDK 26"
        val parts = jwt.split(".")
        return try {
            val charset = charset("UTF-8")
            val payload =
                String(Base64.getUrlDecoder().decode(parts[1].toByteArray(charset)), charset)
            "$payload"
        } catch (e: Exception) {
            "Error parsing JWT: $e"
        }
    }

    fun onTagIdRead(tagId: ByteArray) {
        this.tagId = tagId.slice(0..4).reversed().toByteArray().toHexString()
        Log.d("TagId", this.tagId)
    }

    private fun ByteArray.toHexString(): String {
        return this.joinToString("") {
            java.lang.String.format("%02x", it).uppercase()
        }
    }
}