package com.trdz.mapper.di

import com.trdz.mapper.R
import com.trdz.mapper.view.Navigation
import org.koin.dsl.module

val moduleMain = module {
	single<Navigation>() { Navigation(R.id.container_fragment_base) }
}


