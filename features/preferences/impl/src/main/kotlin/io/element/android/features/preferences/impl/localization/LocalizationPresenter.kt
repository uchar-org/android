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
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter

@Inject
class LocalizationPresenter : Presenter<LocalizationState> {

    private val languages = listOf(
        LocaleData(0, "Uzbek", "uz", false),
        LocaleData(1, "English", "en", false),
        LocaleData(2, "Russian", "ru", false)
    )
    var selectLang = "uz"

    fun setLangState(lang: String) {
        selectLang = lang
    }

    fun saveLang(context: Context) {
        setLocaleLang(selectLang, context)
    }

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

    fun setLocaleLang(lang: String, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
                ?.applicationLocales = LocaleList.forLanguageTags(lang)
        }
    }

    fun changeLanguage(checked: Boolean, id: Int) {
        languages.forEach { item ->
            item.checked = item.id == id && checked
            if (item.id == id && checked) {
                setLangState(item.code)
            }
        }
    }

    @Composable
    override fun present(): LocalizationState {
        return LocalizationState(languages, selectLang);
    }
}
