package com.deepmedi.cnhp_ipcam

import android.app.Application
import android.content.Context
import com.deepmedi.deepmedialgorithms.DeepMediAlgorithm
import com.deepmedi.deepmedimeasure.di.recordModule
import com.deepmedi.deepmedimeasure.di.recordViewModule
import com.deepmedi.deepmedimeasure.utils.SaveUtil
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this

        DeepMediAlgorithm.init()

        SaveUtil.filePath =
            context().externalCacheDir!!

        startKoin {
            androidContext(this@App)
            modules(listOf(
                recordModule,
                recordViewModule)
            )
        }

    }

    fun context(): Context = applicationContext

    companion object {
        lateinit var instance: App
    }
}