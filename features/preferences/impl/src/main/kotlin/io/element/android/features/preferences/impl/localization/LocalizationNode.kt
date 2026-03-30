/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.localization

import io.element.android.features.preferences.impl.about.AboutPresenter
import io.element.android.features.preferences.impl.about.AboutView
import io.element.android.features.preferences.impl.about.ElementLegal
import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class LocalizationNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : Node(buildContext, plugins = plugins) {
    val languages = listOf(LocaleData(0, "Uzbek", false), LocaleData(1, "English", false), LocaleData(2, "Russian", false))

    @Composable
    override fun View(modifier: Modifier) {

//        var languages= remember<List<LocaleData>>(LocaleData(0, "Uzbek", false), LocaleData(1, "English", false), LocaleData(2, "Russian", false))
        LocalizationView(
            title = stringResource(R.string.language),
            onBackClick = ::navigateUp,
            languages = languages,
            onChecked = { checked, id ->
                for (i in 0 until languages.size) {
                    if (languages[i].id == id) {
                        languages[i].name = "aaaa"
                        languages[i].checked=checked
                    }else{
                        languages[i].checked=!checked
                    }
                }
            }
        )
    }
}

data class LocaleData(
    var id: Int,
    var name: String,
    var checked: Boolean
)
