package io.element.android.features.preferences.impl.localization

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.Checkbox
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import kotlin.toString

@Suppress("ParamsComparedByRef")
@Composable
fun LocalizationView(
    title: String,
    onBackClick: () -> Unit,
    languages: List<LocaleData>,
    onChecked: (checked: Boolean, id: Int) -> Unit
) {
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
                        onChecked = { checked, id ->
                            onChecked(checked, id)
                        },
                    )

                }
            }
        },
        topBar = {
            LocalizationTopAppBar(
                title = title,
                onBackClick = onBackClick,
            )
        },
    )
}

@Suppress("ParamsComparedByRef")
@Composable
fun LocalizationItem(data: LocaleData, onChecked: (checked: Boolean, id: Int) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(data.name, color = Color.Black, style = TextStyle(fontSize = 16.sp))
            Checkbox(checked = data.checked, onCheckedChange = {
                onChecked(it, data.id)
            })
        }
    }
}

@Composable
@PreviewsDayNight
@Preview
fun LocalizationViewPreview() {
    LocalizationView(
        title = "Language",
        onChecked = { a, b -> },
        onBackClick = {},
        languages = listOf(LocaleData(1, "English", false), LocaleData(0, "Uzbek", false), LocaleData(2, "Russian", false))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalizationTopAppBar(
    title: String,
    onBackClick: () -> Unit,
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
        }
    )
}
