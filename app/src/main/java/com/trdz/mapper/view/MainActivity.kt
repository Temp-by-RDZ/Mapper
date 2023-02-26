package com.trdz.mapper.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.trdz.mapper.R
import com.trdz.mapper.utility.stopToast
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

	//region Injected

	private val navigation: Navigation by inject()

	//endregion

	//region Elements


	//endregion

	//region Base realization

	override fun onDestroy() {
		stopToast()
		super.onDestroy()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		if (savedInstanceState == null) {
			Log.d("@@@", "Start program")
			navigation.add(supportFragmentManager, WindowStart(), false, R.id.container_fragment_primal)
		}
		if (savedInstanceState == null) navigation.replace(supportFragmentManager, WindowStart.newInstance(), false)
	}

	//endregion

}