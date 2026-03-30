package io.element.android.features.preferences.impl.localization

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar

@Composable
fun LocalizationView(
    title: String,
    onBackClick: () -> Unit,
) {
    val languages = listOf("Uzbek", "English", "Russian")
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = rememberLazyListState(),
                ) {

                    items(count = languages.size, itemContent = { item ->
                        LocalizationItem(name = item.toString())
                    })
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

@Composable
fun LocalizationItem(name: String) {
    Box() {
        Text("language")
    }
}

@Composable
@PreviewsDayNight
@Preview
fun LocalizationViewPreview() {
    LocalizationView(title = "Language", {})
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
