/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.element.android.appconfig.RoomListConfig
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.home.impl.HomeNavigationBarItem
import io.element.android.features.home.impl.R
import io.element.android.features.home.impl.filters.RoomListFilter
import io.element.android.features.home.impl.filters.RoomListFiltersEvent
import io.element.android.features.home.impl.filters.RoomListFiltersState
import io.element.android.features.home.impl.spacefilters.SpaceFiltersEvent
import io.element.android.features.home.impl.spacefilters.SpaceFiltersState
import io.element.android.libraries.designsystem.modifiers.backgroundVerticalGradient
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.DropdownMenu
import io.element.android.libraries.designsystem.theme.components.DropdownMenuItem
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    selectedNavigationItem: HomeNavigationBarItem,
    showAvatarIndicator: Boolean,
    areSearchResultsDisplayed: Boolean,
    onToggleSearch: () -> Unit,
    onMenuActionClick: (RoomListMenuAction) -> Unit,
    onOpenSettings: () -> Unit,
    onAccountSwitch: (SessionId) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    canReportBug: Boolean,
    displayFilters: Boolean,
    filtersState: RoomListFiltersState,
    spaceFiltersState: SpaceFiltersState,
    modifier: Modifier = Modifier,
    selectedTabIndex: State<Int>,
    scope: CoroutineScope,
    pagerState: PagerState
) {
    Column(modifier) {
        TopAppBar(
            modifier = Modifier
                .backgroundVerticalGradient(
                    isVisible = !areSearchResultsDisplayed,
                )
                .statusBarsPadding(),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
            ),
            title = {
//                val displayTitle = when (selectedNavigationItem) {
//                    HomeNavigationBarItem.Chats -> "Uchar"
//                    HomeNavigationBarItem.Spaces -> "Uchar"
//                    HomeNavigationBarItem.Profile -> ""
//                }
                Text(
                    modifier = Modifier.semantics {
                        heading()
                    },
                    style = ElementTheme.typography.aliasScreenTitle,
                    text = "Uchar",
                )
            },
            actions = {
                if (selectedNavigationItem == HomeNavigationBarItem.Chats) {
                    RoomListMenuItems(
                        onToggleSearch = onToggleSearch,
                        onMenuActionClick = onMenuActionClick,
                        canReportBug = canReportBug,
                        spaceFiltersState = spaceFiltersState,
                    )
                }
            },
            windowInsets = WindowInsets(left = 4.dp),
        )

        val scrollState = rememberScrollState()

        if (displayFilters)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                PrimaryScrollableTabRow(
                    containerColor = Color.Gray.copy(alpha = 0.3f),
                    edgePadding = 10.dp,
                    scrollState = scrollState,
                    selectedTabIndex = selectedTabIndex.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(vertical = 4.dp, horizontal = 0.dp)
                        .clip(shape = RoundedCornerShape(50))
                ) {
                    HomeTabs.entries.forEachIndexed { index, currentTab ->
                        Tab(
                            modifier = if (selectedTabIndex.value == index) Modifier
                                .clip(RoundedCornerShape(50))
                                .height(36.dp)
                            else Modifier
                                .clip(RoundedCornerShape(50))
                                .height(50.dp),
                            selected = selectedTabIndex.value == index,
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                scope.launch {
                                    pagerState.scrollToPage(currentTab.ordinal)
                                    when (HomeTabs.entries[selectedTabIndex.value]) {
                                        HomeTabs.All -> {
                                            filtersState.eventSink(RoomListFiltersEvent.ClearSelectedFilters)
                                        }
                                        HomeTabs.Unread -> {
                                            filtersState.eventSink(RoomListFiltersEvent.ClearSelectedFilters)
                                            filtersState.eventSink(RoomListFiltersEvent.ToggleFilter(RoomListFilter.Unread))
                                        }
                                        HomeTabs.People -> {
                                            filtersState.eventSink(RoomListFiltersEvent.ClearSelectedFilters)
                                            filtersState.eventSink(RoomListFiltersEvent.ToggleFilter(RoomListFilter.People))
                                        }
                                        else -> {
                                            filtersState.eventSink(RoomListFiltersEvent.ClearSelectedFilters)
                                            filtersState.eventSink(RoomListFiltersEvent.ToggleFilter(RoomListFilter.Favourites))
                                        }
                                    }
                                }
                            },
                            text = {
                                androidx.compose.material3.Text(text = stringResource(currentTab.text))
                            },
                        )
                    }
                }
            }
    }
}

@Composable
private fun RoomListMenuItems(
    onToggleSearch: () -> Unit,
    onMenuActionClick: (RoomListMenuAction) -> Unit,
    canReportBug: Boolean,
    spaceFiltersState: SpaceFiltersState,
) {
    IconButton(
        onClick = onToggleSearch,
    ) {
        Icon(
            imageVector = CompoundIcons.Search(),
            contentDescription = stringResource(CommonStrings.action_search),
        )
    }
    SpaceFilterButton(spaceFiltersState = spaceFiltersState)
    if (RoomListConfig.HAS_DROP_DOWN_MENU) {
        var showMenu by remember { mutableStateOf(false) }
        IconButton(
            onClick = { showMenu = !showMenu }
        ) {
            Icon(
                imageVector = CompoundIcons.OverflowVertical(),
                contentDescription = null,
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            if (RoomListConfig.SHOW_INVITE_MENU_ITEM) {
                DropdownMenuItem(
                    onClick = {
                        showMenu = false
                        onMenuActionClick(RoomListMenuAction.InviteFriends)
                    },
                    text = { Text(stringResource(id = CommonStrings.action_invite)) },
                    leadingIcon = {
                        Icon(
                            imageVector = CompoundIcons.ShareAndroid(),
                            tint = ElementTheme.colors.iconSecondary,
                            contentDescription = null,
                        )
                    }
                )
            }
            if (RoomListConfig.SHOW_REPORT_PROBLEM_MENU_ITEM && canReportBug) {
                DropdownMenuItem(
                    onClick = {
                        showMenu = false
                        onMenuActionClick(RoomListMenuAction.ReportBug)
                    },
                    text = { Text(stringResource(id = CommonStrings.common_report_a_problem)) },
                    leadingIcon = {
                        Icon(
                            imageVector = CompoundIcons.ChatProblem(),
                            tint = ElementTheme.colors.iconSecondary,
                            contentDescription = null,
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SpaceFilterButton(
    spaceFiltersState: SpaceFiltersState,
) {
    if (spaceFiltersState == SpaceFiltersState.Disabled) return

    fun onClick() {
        when (spaceFiltersState) {
            is SpaceFiltersState.Unselected -> spaceFiltersState.eventSink(SpaceFiltersEvent.Unselected.ShowFilters)
            is SpaceFiltersState.Selected -> spaceFiltersState.eventSink(SpaceFiltersEvent.Selected.ClearSelection)
            else -> Unit
        }
    }

    val isSelected = spaceFiltersState is SpaceFiltersState.Selected
    IconButton(
        onClick = ::onClick,
        colors = if (isSelected) {
            IconButtonDefaults.iconButtonColors(
                containerColor = ElementTheme.colors.bgActionPrimaryRest,
                contentColor = ElementTheme.colors.iconOnSolidPrimary,
            )
        } else {
            IconButtonDefaults.iconButtonColors()
        },
    ) {
        Icon(
            imageVector = CompoundIcons.Filter(),
            contentDescription = stringResource(R.string.screen_roomlist_your_spaces),
        )
    }
}
