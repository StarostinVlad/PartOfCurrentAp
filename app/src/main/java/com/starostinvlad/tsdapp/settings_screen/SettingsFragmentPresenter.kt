package com.starostinvlad.tsdapp.settings_screen

import android.util.Log
import android.util.Patterns
import com.starostinvlad.fan.Preferences
import com.starostinvlad.tsdapp.api.MQTTHelper
import com.starostinvlad.tsdapp.api.RequestInterceptor
import com.starostinvlad.tsdapp.base_mvp.BasePresenter
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.lang.Exception
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SettingsFragmentPresenter @Inject constructor(
    private val preferences: Preferences,
    private val interceptor: RequestInterceptor,
    private val client: OkHttpClient,
    private val mqttHelper: MQTTHelper
) :
    BasePresenter<SettingsFragmentContract>() {

    private val TAG = javaClass.simpleName

    override fun attachView(mvpView: SettingsFragmentContract) {
        super.attachView(mvpView)
        val httpUrl = preferences.host.toHttpUrlOrNull()
        val host = httpUrl?.host ?: ""
        val scheme = httpUrl?.scheme ?: ""
        val port = preferences.port
        val token = preferences.token
        view?.showCurrentSettings(scheme, host, port, token)
    }

    fun onSaveSettings(schema: String, domain: String, port: String, token: String) {
        launch {
            var hasError = false
            view?.hideErrors()
            view?.showLoading(true)
            if (port.isNotEmpty()) {
                preferences.port = port.toInt()
                try {
                    mqttHelper.init(domain, port.toInt())
                    mqttHelper.connect(token)
                } catch (e: Exception) {
                    view?.showPortError("mqqt not work!")
                    hasError = true
                }
            } else {
                view?.showPortError("Поле порт пустое")
                hasError = true
            }
            if (token.isNotEmpty()) {
                preferences.token = token
            } else {
                view?.showTokenError("Поле токен пустое")
                hasError = true
            }
            try {
                val url = "$schema://$domain/"
                if (url.isNotEmpty() && Patterns.WEB_URL.matcher(url).matches()) {
                    if (checkDomainAccessable(url)) {
                        preferences.host = url
                        interceptor.setInterceptor(url)
                    }
                } else {
                    view?.showHostError("Неверный формат")
                    hasError = true
                }
            } catch (e: SocketTimeoutException) {
                view?.showHostError("Превышено время ожидания")
                hasError = true
            } catch (e: ConnectException) {
                view?.showHostError("Неверный адрес, хост недоступен")
                hasError = true
            } catch (e: Exception) {
                view?.showHostError("Неизвестня ошибка!")
                hasError = true
            }
            view?.showLoading(false)
            if (!hasError) view?.hostSaved()
        }
    }


    private suspend fun checkDomainAccessable(domain: String) =
        suspendCancellableCoroutine<Boolean> {
            val request: Request = Request.Builder()
                .url(domain)
                .get()
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    it.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (it.isActive)
                        it.resume(response.isSuccessful)
                }

            })
        }

}

class InvalidHostException : Exception("Неверный адрес, хост недоступен")
class InvalidFormatHostException : Exception("Неверный адрес, хост недоступен")
class EmptyPortException : Exception("Поле порт пустое")
class EmptyTokenException : Exception("Поле токен пустое")
