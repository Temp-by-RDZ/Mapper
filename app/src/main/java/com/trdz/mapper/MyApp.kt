package com.trdz.mapper

import android.app.Application
import com.trdz.mapper.di.moduleMain
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp: Application() {

	override fun onCreate() {
		super.onCreate()

		startKoin {
			androidContext(this@MyApp)
			modules(listOf(moduleMain))
		}
	}

}