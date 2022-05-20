package com.starostinvlad.tsdapp

import android.app.Application
import android.content.Context
import com.starostinvlad.tsdapp.di.AppComponent
import com.starostinvlad.tsdapp.di.AppModule
import com.starostinvlad.tsdapp.di.DaggerAppComponent

class App : Application() {
    lateinit var appComponent: AppComponent
    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()
    }
}

val Context.appComponent: AppComponent
    get() =
        when (this) {
            is App -> appComponent
            else -> this.applicationContext.appComponent
        }