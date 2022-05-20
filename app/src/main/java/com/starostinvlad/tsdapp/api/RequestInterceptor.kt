package com.starostinvlad.tsdapp.api

import android.util.Log
import com.starostinvlad.fan.Preferences
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.http.Header
import javax.inject.Singleton

import javax.inject.Inject


@Singleton
class RequestInterceptor @Inject constructor() : Interceptor {
    private var scheme: String = ""
    private var host: String = ""
    private var token = ""
    private val TAG = javaClass.simpleName
    fun setInterceptor(url: String?) {
        val httpUrl = url!!.toHttpUrlOrNull()
        scheme = httpUrl!!.scheme
        host = httpUrl.host
    }

    fun setToken(token: String) {
        this.token = token
        Log.d(TAG, "setToken: ${this.token}")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var original: Request = chain.request()
        val builder = original.newBuilder()

        if (scheme.isNotEmpty() && host.isNotEmpty()) {
            val newUrl: HttpUrl = original.url.newBuilder()
                .scheme(scheme)
                .host(host)
                .build()

            builder.url(newUrl)
        }
        if (token.isNotEmpty()) {
            builder.header("X-Authorization", "Bearer $token")
        }
        original = builder.build()
//        Log.d(TAG, "intercept: headers: ${original.headers}")
        return chain.proceed(original)
    }
}
