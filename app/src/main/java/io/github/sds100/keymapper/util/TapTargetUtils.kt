package io.github.sds100.keymapper.util

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.fragment.app.Fragment
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.ServiceLocator
import kotlinx.coroutines.flow.collect
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

/**
 * Created by sds100 on 17/01/21.
 */

sealed class TapTarget(private val key: Preferences.Key<Boolean>,
                       @StringRes val primaryText: Int,
                       @StringRes val secondaryText: Int) {

    /**
     * Only works on app bar items if called during or after
     * the resumed state in the view lifecycle.
     */
    suspend fun show(fragment: Fragment, @IdRes viewId: Int) {
        val dataStore = ServiceLocator.preferenceDataStore(fragment.requireContext())

        dataStore.get(key).collect { shown ->
            if (shown == true) return@collect

            MaterialTapTargetPrompt.Builder(fragment).apply {
                setTarget(viewId)

                focalColour = fragment.color(android.R.color.transparent)
                setPrimaryText(this@TapTarget.primaryText)
                setSecondaryText(this@TapTarget.secondaryText)
                backgroundColour = fragment.color(R.color.colorAccent)

                setPromptStateChangeListener { _, state ->
                    if (state == MaterialTapTargetPrompt.STATE_DISMISSED
                        || state == MaterialTapTargetPrompt.STATE_FINISHED) {

                        fragment.viewLifecycleScope.launchWhenCreated {
                            dataStore.set(key, true)
                        }
                    }
                }

                show()
            }
        }

    }
}

class QuickStartGuideTapTarget : TapTarget(
    preferencesKey("tap_target_quick_start_guide"),
    R.string.tap_target_quick_start_guide_primary,
    R.string.tap_target_quick_start_guide_secondary)