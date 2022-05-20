package com.starostinvlad.fan

import android.content.Context
import android.content.SharedPreferences
import com.starostinvlad.tsdapp.data.EntityId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Preferences @Inject constructor(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(FILE_NAME, 0)
    private val editor: SharedPreferences.Editor
        get() = preferences.edit()
    var token: String
        get() = preferences.getString(PREF_TOKEN, "").toString()
        set(data) {
            editor.putString(PREF_TOKEN, data).commit()
        }
    var refreshToken: String
        get() = preferences.getString(PREF_REFRESH_TOKEN, "").toString()
        set(data) {
            editor.putString(PREF_REFRESH_TOKEN, data).commit()
        }
    var host: String
        get() = preferences.getString(PREF_HOST, "").toString()
        set(data) {
            editor.putString(PREF_HOST, data).commit()
        }
    var port: Int
        get() = preferences.getInt(PREF_PORT, -1)
        set(data) {
            editor.putInt(PREF_PORT, data).commit()
        }

    companion object {
        const val FILE_NAME = "preferences"
        const val PREF_TOKEN = "token"
        const val PREF_HOST = "host"
        const val PREF_PORT = "port"
        const val PREF_REFRESH_TOKEN = "refreshToken"

    }

}