package com.magmacoin.leap.core.navigation

import androidx.fragment.app.FragmentManager
import com.magmacoin.leap.R
import com.magmacoin.leap.presentation.game.GameFragment
import com.magmacoin.leap.presentation.gameover.GameOverFragment
import com.magmacoin.leap.presentation.menu.MenuFragment
import com.magmacoin.leap.presentation.splash.SplashFragment

object Navigator {

    const val TAG_SPLASH = "splash"
    const val TAG_MENU = "menu"
    const val TAG_GAME = "game"
    const val TAG_RESULT = "result"

    fun showSplash(manager: FragmentManager) {
        try {
            manager.beginTransaction()
                .replace(R.id.fragment_container, SplashFragment(), TAG_SPLASH)
                .commitAllowingStateLoss()
        } catch (e: Exception) {
        }
    }

    fun toMenu(manager: FragmentManager) {
        try {
            manager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, MenuFragment(), TAG_MENU)
                .commitAllowingStateLoss()
        } catch (e: Exception) {
        }
    }

    fun toGame(manager: FragmentManager) {
        try {
            manager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_up,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.scale_out
                )
                .replace(R.id.fragment_container, GameFragment(), TAG_GAME)
                .addToBackStack(TAG_GAME)
                .commitAllowingStateLoss()
        } catch (e: Exception) {
        }
    }

    fun toResult(
        manager: FragmentManager,
        altitude: Int,
        tokens: Int,
        leaps: Int,
        newBest: Boolean
    ) {
        popTag(manager, TAG_GAME)
        try {
            manager.beginTransaction()
                .setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.scale_out,
                    R.anim.fade_in,
                    R.anim.scale_out
                )
                .replace(
                    R.id.fragment_container,
                    GameOverFragment.newInstance(altitude, tokens, leaps, newBest),
                    TAG_RESULT
                )
                .addToBackStack(TAG_RESULT)
                .commitAllowingStateLoss()
        } catch (e: Exception) {
        }
    }

    fun replayFromResult(manager: FragmentManager) {
        popTag(manager, TAG_RESULT)
        toGame(manager)
    }

    fun backToMenu(manager: FragmentManager) {
        popTag(manager, TAG_RESULT)
    }

    fun back(manager: FragmentManager) {
        try {
            manager.popBackStackImmediate()
        } catch (e: Exception) {
        }
    }

    private fun popTag(manager: FragmentManager, tag: String) {
        try {
            manager.popBackStackImmediate(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } catch (e: Exception) {
        }
    }
}
