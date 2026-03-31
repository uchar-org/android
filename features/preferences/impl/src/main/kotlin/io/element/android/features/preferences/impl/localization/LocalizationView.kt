package io.element.android.features.preferences.impl.localization

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasButtonText
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.Checkbox
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconToggleButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar

@Suppress("ParamsComparedByRef")
@Composable
fun LocalizationView(
    title: String,
    onBackClick: () -> Unit,
    languages: List<LocaleData>,
    onChecked: (checked: Boolean, id: Int, context: Context) -> Unit,
    onSaveLang: (context: Context) -> Unit,
    lang: String
) {
    val ctx = LocalContext.current

    languages.forEach {
        if (it.code == lang) onChecked(true, it.id, ctx)
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .statusBarsPadding()
            .imePadding(),
        content = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 76.dp)
                    .padding(horizontal = 12.dp),
                state = rememberLazyListState(),
            ) {
                items(languages) { lang ->
                    LocalizationItem(
                        data = lang,
                        onChecked = { checked, id, ctx ->
                            onChecked(checked, id, ctx)
                        },
                        context = ctx
                    )

                }
            }
        },
        topBar = {
            LocalizationTopAppBar(
                title = title,
                onBackClick = onBackClick,
                onSaveLang = {
                    onSaveLang(ctx)
                }
            )
        },
    )
}

@Suppress("ParamsComparedByRef")
@Composable
fun LocalizationItem(data: LocaleData, onChecked: (checked: Boolean, id: Int, context: Context) -> Unit, context: Context) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onChecked(!data.checked, data.id, context)
            },
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(data.name, style = TextStyle(fontSize = 16.sp))
//            Spacer(modifier = Modifier)
//            Checkbox(checked = data.checked, onCheckedChange = {
//                onChecked(it, data.id, context)
//            })
            IconToggleButton(checked = data.checked, onCheckedChange = {
                onChecked(it, data.id, context)
            }) {
                if (data.checked) Icon(
                    painter = painterResource(io.element.android.compound.R.drawable.ic_compound_check),
                    contentDescription = "Language",
//                    tint = Color(
//                        0xFF9B51E0
//                    )
                )
            }
        }
    }
}

@Composable
@PreviewsDayNight
@Preview
fun LocalizationViewPreview() {
    LocalizationView(
        title = "Language",
        onChecked = { a, b, ctx -> },
        onBackClick = {},
        languages = listOf(LocaleData(1, "English", "en", false), LocaleData(0, "Uzbek", "uz", false), LocaleData(2, "Russian", "ru", false)),
        lang = "uz",
        onSaveLang = { b ->

        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalizationTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    onSaveLang: () -> Unit,

    ) {
    TopAppBar(
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        title = {
            Text(
                modifier = Modifier.semantics {
                    heading()
                },
                text = title,
                style = ElementTheme.typography.aliasScreenTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .clickable {
                        onSaveLang()
                        onBackClick()
                    }) {
                Text(
                    stringResource(io.element.android.libraries.ui.strings.R.string.action_save),

                    style = ElementTheme.typography.aliasButtonText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    )
}
