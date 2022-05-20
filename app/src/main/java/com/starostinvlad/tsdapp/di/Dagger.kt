package com.starostinvlad.tsdapp.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.starostinvlad.fan.Preferences
import com.starostinvlad.tsdapp.App
import com.starostinvlad.tsdapp.acceptance_screen.InputChassisNumberFragment
import com.starostinvlad.tsdapp.api.RequestInterceptor
import com.starostinvlad.tsdapp.main_screen.MainActivity
import com.starostinvlad.tsdapp.api.ThingsBoardApi
import com.starostinvlad.tsdapp.attach_tag_screen.AttachTagFragment
import com.starostinvlad.tsdapp.checklist_screen.CheckListFragment
import com.starostinvlad.tsdapp.confirm_location_screen.ConfirmLocationFragment
import com.starostinvlad.tsdapp.confirm_site_row_screen.ConfirmSiteRowFragment
import com.starostinvlad.tsdapp.current_task_fragment.CurrentTaskFragment
import com.starostinvlad.tsdapp.defects_screen.DefectsFragment
import com.starostinvlad.tsdapp.login_screen.LoginFragment
import com.starostinvlad.tsdapp.operations_screen.OperationsFragment
import com.starostinvlad.tsdapp.search_defect_dialog.SearchFragment
import com.starostinvlad.tsdapp.settings_screen.SettingsFragment
import com.starostinvlad.tsdapp.tasklist_screen.TaskListFragment
import com.starostinvlad.tsdapp.workflow_screen.WorkFlowFragment
import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)
    fun inject(fragment: LoginFragment)
    fun inject(fragment: WorkFlowFragment)
    fun inject(fragment: CheckListFragment)
    fun inject(fragment: SettingsFragment)
    fun inject(fragment: OperationsFragment)
    fun inject(fragment: CurrentTaskFragment)
    fun inject(fragment: AttachTagFragment)
    fun inject(fragment: InputChassisNumberFragment)
    fun inject(fragment: TaskListFragment)
    fun inject(fragment: DefectsFragment)
    fun inject(fragment: SearchFragment)
    fun inject(fragment: ConfirmSiteRowFragment)
    fun inject(fragment: ConfirmLocationFragment)
}

@Module(includes = [NetworkModule::class, StorageModule::class])
class AppModule(private val application: App) {
    @Singleton
    @Provides
    fun provideAppContext(): Context {
        return application.applicationContext
    }
}

@Module
class StorageModule {
}

@Module
class NetworkModule {
    //    private val BASE_URL = "https://tbdemo.ardins.ru/api/"
//    private val BASE_URL = "https://things.demo.7id.com/api/"

    @Singleton
    @Provides
    fun provideLocationService(context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Singleton
    @Provides
    fun provideGson(): Gson {
        return Gson()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(interceptor: RequestInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideNetworkService(
        preferences: Preferences,
        okHttpClient: OkHttpClient, gson: Gson
    ): ThingsBoardApi {
        return Retrofit.Builder()
            .baseUrl(preferences.host)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ThingsBoardApi::class.java)
    }
}