package com.starostinvlad.tsdapp.login_screen

import com.google.gson.Gson
import com.starostinvlad.fan.Preferences
import com.starostinvlad.tsdapp.api.MQTTHelper
import com.starostinvlad.tsdapp.api.MqttMessageListener
import com.starostinvlad.tsdapp.api.RequestInterceptor
import com.starostinvlad.tsdapp.base_mvp.BasePresenter
import com.starostinvlad.tsdapp.data.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class LoginFragmentPresenter @Inject constructor(
    private val api: ThingsboardApiHelper,
    private val mqttHelper: MQTTHelper,
    private val interceptor: RequestInterceptor,
    private val gson: Gson
) : BasePresenter<LoginFragmentContract?>(), MqttMessageListener {
    private val TAG = this.javaClass.simpleName


    fun loginDataChanged(username: String, password: String) {
        view?.enableSubmit(isValid(username, password) || isAdmin(username, password))
    }

    private fun isAdmin(username: String, password: String): Boolean {
        return username == "admin" && password == "admin"
    }

    override fun attachView(mvpView: LoginFragmentContract?) {
        mqttHelper.addListener(this)
        super.attachView(mvpView)
    }

    override fun detachView() {
        mqttHelper.removeListener(this)
        super.detachView()
    }

    fun onLoginSubmit(username: String, password: String) {
        view?.showLoading(true)
        if (isAdmin(username, password)) {
            view?.openSettingScreen()
        } else
            if (isValid(username, password)) {
                launch {
                    try {
                        val response = api.login(username, password)
                        if (response.isSuccessful) {
                            try {
                                val token = response.body()!!

                                parseJwtToken(token)

                                setInterceptorToken(token)

                                mqttHelper.username = username

                                mqttHelper.attachDeviceToUser(username)

                            } catch (e: Exception) {
                                e.printStackTrace()

                            }
                        } else {
                            val error = gson.fromJson(
                                response.errorBody()!!.string(),
                                AttachResult::class.java
                            )
                            view?.showLoginFailed(error.params.message)
                        }

                    } catch (e: Exception) {
                        view?.showLoginFailed(e.localizedMessage!!)
                    }
                }
            }
        view?.showLoading(false)
    }

    private fun parseJwtToken(token: JsonToken) {
        val chunks: List<String> = token.token.split(".")
        val decoder: Base64.Decoder = Base64.getUrlDecoder()
        val payload = String(decoder.decode(chunks[1]))
        val jwtToken = gson.fromJson(payload, JwtToken::class.java)
        api.userId = EntityId("USER", jwtToken.userId)
    }

    private fun setInterceptorToken(body: JsonToken) {
        interceptor.setToken(body.token)
    }

    private fun isValid(username: String, password: String): Boolean {
        return username.isNotEmpty() &&
                password.isNotEmpty() &&
                username.contains("@") &&
                password.length >= 4
    }

    override fun messageArrived(result: AttachResult, topic: String?) {
        view?.showLoading(false)
        if (result.params.success) {
            if (result.method == MqttRpcCommandMethod.AttachDeviceToUser) {
                result.params.additionalInfo.let {
                    api.additionalInfo = it
                }
                view?.updateUiWithUser()
            }
        } else if (!result.params.success) {
            view?.showLoginFailed(result.params.message)
        }

    }
}