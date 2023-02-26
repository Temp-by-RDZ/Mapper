package com.trdz.mapper.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.trdz.mapper.R
import com.trdz.mapper.utility.*

class Navigation(private var fastContainer: Int = 0) {

	fun returnTo(manager: FragmentManager, toId: Int = 0) {
		if (manager.backStackEntryCount <= toId) return
		val entry: FragmentManager.BackStackEntry = manager.getBackStackEntryAt(toId)
		manager.popBackStackImmediate(entry.id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
	}

	fun add(manager: FragmentManager, fragment: Fragment?, remember: Boolean = true, container: Int = fastContainer, effect: String = "NONE") {
		if (manager.isDestroyed) return
		manager.beginTransaction().apply {
			when (effect) {
				EFFECT_RISE -> setCustomAnimations(
					R.anim.slide_up,
					R.anim.slide_down,
				)
				EFFECT_FADE -> setCustomAnimations(
					R.anim.fade_in,
					R.anim.fade_out,
				)
				EFFECT_SLIDE -> setCustomAnimations(
					R.anim.slide_in,
					R.anim.slide_out,
				)
				EFFECT_DROP -> setCustomAnimations(
					R.anim.move_from_up,
					R.anim.fade_out,
				)
				EFFECT_MOVED -> setCustomAnimations(
					R.anim.move_from_up,
					R.anim.slide_down,
				)
				EFFECT_MOVEL -> setCustomAnimations(
					R.anim.slide_in,
					R.anim.move_to_left,
				)
				EFFECT_MOVER -> setCustomAnimations(
					R.anim.move_from_left,
					R.anim.slide_out,
				)
				EFFECT_MOVEU -> setCustomAnimations(
					R.anim.slide_up,
					R.anim.move_to_up,
				)
			}
			add(container, fragment!!)
			if (remember) addToBackStack("")
			commit()
		}
	}

	fun replace(manager: FragmentManager, fragment: Fragment?, remember: Boolean = true, container: Int = fastContainer, effect: String = "NONE") {
		if (manager.isDestroyed) return
		manager.beginTransaction().apply {
			when (effect) {
				EFFECT_RISE -> setCustomAnimations(
					R.anim.slide_up,
					R.anim.slide_down,
				)
				EFFECT_FADE -> setCustomAnimations(
					R.anim.fade_in,
					R.anim.fade_out,
				)
				EFFECT_SLIDE -> setCustomAnimations(
					R.anim.slide_in,
					R.anim.slide_out,
				)
				EFFECT_DROP -> setCustomAnimations(
					R.anim.move_from_up,
					R.anim.fade_out,
				)
				EFFECT_SHOW -> setCustomAnimations(
					R.anim.fade_in,
					R.anim.move_to_up,
				)
				EFFECT_MOVED -> setCustomAnimations(
					R.anim.move_from_up,
					R.anim.slide_down,
				)
				EFFECT_MOVEL -> setCustomAnimations(
					R.anim.slide_in,
					R.anim.move_to_left,
				)
				EFFECT_MOVER -> setCustomAnimations(
					R.anim.move_from_left,
					R.anim.slide_out,
				)
				EFFECT_MOVEU -> setCustomAnimations(
					R.anim.slide_up,
					R.anim.move_to_up,
				)
			}
			replace(container, fragment!!)
			setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
			if (remember) addToBackStack("")
			commit()
		}
	}

}