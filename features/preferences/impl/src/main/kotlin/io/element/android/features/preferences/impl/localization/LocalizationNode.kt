/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.localization

import android.annotation.SuppressLint
import android.app.LocaleManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class LocalizationNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: LocalizationPresenter,
) : Node(buildContext, plugins = plugins) {

    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val context1 = LocalContext.current
//        val shared = context1.getSharedPreferences("Settings", Context.MODE_PRIVATE)
//        val lang = shared.getString("lang", "uz")
//For setOverrideLocaleConfig
//        val localeManager = applicationContext?.getSystemService(LocaleManager::class.java)
//        localeManager?.overrideLocaleConfig = LocaleConfig(
//            LocaleList.forLanguageTags("en-US,ja-JP,zh-Hans-SG,uz,ru")
//        )

        fun getLanguageCode(context: Context): String {
            val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getSystemService(LocaleManager::class.java)
                    ?.applicationLocales
                    ?.get(0)
            } else {
                AppCompatDelegate.getApplicationLocales().get(0)
            }
            return locale?.language ?: "uz"
        }

        val lang = getLanguageCode(context1)

        LocalizationView(
            title = stringResource(R.string.language),
            onBackClick = ::navigateUp,
            languages = state.languages,
            onChecked = { checked, id, ctx ->
                presenter.changeLanguage(checked, id, )
            },
            lang = lang,
            onSaveLang = { ctx ->
                presenter.saveLang(ctx)
            }
        )
    }
}

class LocaleData(
    var id: Int,
    var name: String,
    var code: String,
    checked: Boolean
) {
    var checked by mutableStateOf(checked)
}
