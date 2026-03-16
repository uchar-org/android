/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarColors
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarHorizontalFabPosition
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.atoms.CounterAtom
import io.element.android.libraries.designsystem.atomic.atoms.RedIndicatorAtom
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.tooltip.PlainTooltip
import io.element.android.libraries.designsystem.components.tooltip.TooltipBox
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

/**
 * Ref: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=4457-1136
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HorizontalFloatingToolbar(
//    currentUserAndNeighbors: ImmutableList<MatrixUser>,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    floatingActionButton: (@Composable () -> Unit)? = null,
    colors: FloatingToolbarColors = FloatingToolbarDefaults.standardFloatingToolbarColors().copy(
        toolbarContainerColor = ElementTheme.colors.bgSubtleSecondary,
    ),
    contentPadding: PaddingValues = PaddingValues(
        vertical = 8.dp,
        horizontal = 12.dp,
    ),
    scrollBehavior: FloatingToolbarScrollBehavior? = null,
    shape: Shape = FloatingToolbarDefaults.ContainerShape,
    leadingContent: @Composable (RowScope.() -> Unit)? = null,
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
    floatingActionButtonPosition: FloatingToolbarHorizontalFabPosition =
        FloatingToolbarHorizontalFabPosition.End,
    animationSpec: FiniteAnimationSpec<Float> = FloatingToolbarDefaults.animationSpec(),
    expandedShadowElevation: Dp = 8.dp,
    collapsedShadowElevation: Dp = if (floatingActionButton == null) {
        FloatingToolbarDefaults.ContainerCollapsedElevation
    } else {
        FloatingToolbarDefaults.ContainerCollapsedElevationWithFab
    },
    content: @Composable RowScope.() -> Unit,
) {
    if (floatingActionButton == null) {
        androidx.compose.material3.HorizontalFloatingToolbar(
            expanded = expanded,
            modifier = modifier,
            colors = colors,
            contentPadding = contentPadding,
            scrollBehavior = scrollBehavior,
            shape = shape,
            leadingContent = leadingContent,
            trailingContent = trailingContent,
            expandedShadowElevation = expandedShadowElevation,
            collapsedShadowElevation = collapsedShadowElevation,
            content = content,
        )
    } else {
        androidx.compose.material3.HorizontalFloatingToolbar(
            expanded = expanded,
            floatingActionButton = floatingActionButton,
            modifier = modifier,
            colors = colors,
            contentPadding = contentPadding,
            scrollBehavior = scrollBehavior,
            shape = shape,
            floatingActionButtonPosition = floatingActionButtonPosition,
            animationSpec = animationSpec,
            expandedShadowElevation = expandedShadowElevation,
            collapsedShadowElevation = collapsedShadowElevation,
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalFloatingToolbarItem(
    icon: ImageVector,
    tooltipLabel: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    counter: Int? = null,
    forceRenderingTooltip: Boolean = false,
) {
    TooltipBox(
        positionProvider =
            TooltipDefaults.rememberTooltipPositionProvider(
                TooltipAnchorPosition.Above
            ),
        tooltip = { PlainTooltip { Text(tooltipLabel) } },
        state = rememberTooltipState(
            initialIsVisible = forceRenderingTooltip,
        ),
        modifier = modifier,
    ) {
        val colors = if (isSelected) {
            IconButtonDefaults.filledIconButtonColors().copy(
                containerColor = ElementTheme.colors.bgCanvasDefault,
                contentColor = ElementTheme.colors.iconPrimary,
            )
        } else {
            IconButtonDefaults.filledIconButtonColors().copy(
                containerColor = Color.Transparent,
                contentColor = ElementTheme.colors.iconSecondary,
            )
        }
        Box {
            FilledIconButton(
                modifier = Modifier.widthIn(min = 56.dp),
                colors = colors,
                onClick = onClick,
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = icon,
                    contentDescription = tooltipLabel,
                )
            }
            if (counter != null) {
                CounterAtom(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 3.dp),
                    count = counter,
                    textStyle = ElementTheme.typography.fontBodyXsMedium,
                )
            }
        }
    }
}

@Composable
fun HorizontalFloatingToolbarSeparator(modifier: Modifier = Modifier) {
    Spacer(modifier = modifier.width(16.dp))
}

//@Composable
//private fun AccountIcon(
//    matrixUser: MatrixUser,
//    isCurrentAccount: Boolean,
//    showAvatarIndicator: Boolean,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//) {
//    val testTag = if (isCurrentAccount) Modifier.testTag(TestTags.homeScreenSettings) else Modifier
//    IconButton(
//        modifier = modifier.then(testTag),
//        onClick = onClick,
//    ) {
//        Box {
//            val avatarData by remember(matrixUser) {
//                derivedStateOf {
//                    matrixUser.getAvatarData(size = AvatarSize.CurrentUserTopBar)
//                }
//            }
//            Avatar(
//                avatarData = avatarData,
//                avatarType = AvatarType.User,
//                contentDescription = if (isCurrentAccount) stringResource(CommonStrings.common_settings) else null,
//            )
//            if (showAvatarIndicator) {
//                RedIndicatorAtom(
//                    modifier = Modifier.align(Alignment.TopEnd)
//                )
//            }
//        }
//    }
//}

@PreviewsDayNight
@Composable
internal fun HorizontalFloatingToolbarPreview() = ElementPreview {
    ContentToPreview(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
            ) {
                Icon(
                    imageVector = CompoundIcons.Plus(),
                    contentDescription = null,
                )
            }
        }
    )
}

@PreviewsDayNight
@Composable
internal fun HorizontalFloatingToolbarNoFabPreview() = ElementPreview {
    ContentToPreview(
        floatingActionButton = null,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ContentToPreview(
    floatingActionButton: (@Composable () -> Unit)?,
) {
    HorizontalFloatingToolbar(
//        currentUserAndNeighbors: ImmutableList<MatrixUser>,
        modifier = Modifier.padding(28.dp),
        floatingActionButton = floatingActionButton,
    ) {
        val forEachIndexed = listOf(
            CompoundIcons.ChatSolid(),
            CompoundIcons.Space(),
        ).forEachIndexed { index, icon ->
            if (index > 0) {
                HorizontalFloatingToolbarSeparator()
            }
            HorizontalFloatingToolbarItem(
                icon = icon,
                tooltipLabel = "Label",
                isSelected = index == 0,
                counter = if (index == 0) 6 else null,
                forceRenderingTooltip = true,
                onClick = { },
            )
            HorizontalFloatingToolbarItem(
                icon = icon,
                tooltipLabel = "Label",
                isSelected = index == 0,
                counter = if (index == 0) 6 else null,
                forceRenderingTooltip = true,
                onClick = { },
            )

        }
    }
}

