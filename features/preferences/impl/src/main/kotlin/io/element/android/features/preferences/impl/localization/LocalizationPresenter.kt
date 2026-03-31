/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.localization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.edit
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import java.util.Locale

@Inject
class LocalizationPresenter : Presenter<LocalizationState> {

    private val languages = listOf(
        LocaleData(0, "Uzbek", "uz", false),
        LocaleData(1, "English", "en", false),
        LocaleData(2, "Russian", "ru", false)
    )
    var selectLang = "uz"

    fun setLangState(lang: String,) {
        selectLang = lang
    }

    fun saveLang(context: Context){
        setLocaleLang(selectLang, context)
    }
    fun setLocaleLang(lang: String, context: Context) {
        val locale = Locale.forLanguageTag(lang)
        Locale.setDefault(locale)
        selectLang = lang
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)

        context.getSharedPreferences("Settings", Context.MODE_PRIVATE).edit {
            putString("lang", lang)
        }
    }

    fun changeLanguage(checked: Boolean, id: Int, context: Context) {
        languages.forEach { item ->
            item.checked = item.id == id && checked
            if (item.id == id && checked) {
                setLangState(item.code)
//                setLocaleLang(item.code, context)
            }
        }
    }

    @Composable
    override fun present(): LocalizationState {
        return LocalizationState(languages, selectLang);
    }
}
