/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl

import android.app.Activity
import android.os.Parcelable
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.annotations.ContributesNode
import io.element.android.appnav.NotLoggedInFlowNode
import io.element.android.features.deactivation.api.AccountDeactivationEntryPoint
import io.element.android.features.home.api.HomeEntryPoint
import io.element.android.features.home.impl.HomeFlowNode.NavTarget.AnalyticsSettings
import io.element.android.features.home.impl.HomeFlowNode.NavTarget.BlockedUsers
import io.element.android.features.home.impl.HomeFlowNode.NavTarget.DeclineInviteAndBlockUser
import io.element.android.features.home.impl.HomeFlowNode.NavTarget.DeveloperSettings
import io.element.android.features.home.impl.HomeFlowNode.NavTarget.EditDefaultNotificationSetting
import io.element.android.features.home.impl.HomeFlowNode.NavTarget.Localization
import io.element.android.features.home.impl.HomeFlowNode.NavTarget.NavigateProfile
import io.element.android.features.home.impl.HomeFlowNode.NavTarget.NotificationSettings
import io.element.android.features.home.impl.HomeFlowNode.NavTarget.PushHistory
import io.element.android.features.home.impl.HomeFlowNode.NavTarget.ReportRoom
import io.element.android.features.home.impl.HomeFlowNode.NavTarget.Root
import io.element.android.features.home.impl.HomeFlowNode.NavTarget.SelectNewOwnersWhenLeavingRoom
import io.element.android.features.home.impl.HomeFlowNode.NavTarget.TroubleshootNotifications
import io.element.android.features.home.impl.components.RoomListMenuAction
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.features.home.impl.roomlist.RoomListEvent
import io.element.android.features.home.impl.user.editprofile.EditUserProfileNode
import io.element.android.features.home.impl.user.editprofile.EditUserProfileNode.Callback
import io.element.android.features.home.impl.user.editprofile.EditUserProfileNode.Inputs
import io.element.android.features.invite.api.InviteData
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteView
import io.element.android.features.invite.api.declineandblock.DeclineInviteAndBlockEntryPoint
import io.element.android.features.leaveroom.api.LeaveRoomRenderer
import io.element.android.features.licenses.api.OpenSourceLicensesEntryPoint
import io.element.android.features.linknewdevice.api.LinkNewDeviceEntryPoint
import io.element.android.features.lockscreen.api.LockScreenEntryPoint
import io.element.android.features.login.api.LoginParams
import io.element.android.features.logout.api.LogoutEntryPoint
import io.element.android.features.logout.api.direct.DirectLogoutView
import io.element.android.features.preferences.impl.about.AboutNode
import io.element.android.features.preferences.impl.advanced.AdvancedSettingsNode
import io.element.android.features.preferences.impl.analytics.AnalyticsSettingsNode
import io.element.android.features.preferences.impl.blockedusers.BlockedUsersNode
import io.element.android.features.preferences.impl.developer.DeveloperSettingsNode
import io.element.android.features.preferences.impl.labs.LabsNode
import io.element.android.features.preferences.impl.localization.LocalizationNode
import io.element.android.features.preferences.impl.notifications.NotificationSettingsNode
import io.element.android.features.preferences.impl.notifications.edit.EditDefaultNotificationSettingNode
import io.element.android.features.preferences.impl.root.PreferencesRootNode
import io.element.android.features.preferences.impl.root.PreferencesRootPresenter
import io.element.android.features.rageshake.api.bugreport.BugReportEntryPoint
import io.element.android.features.reportroom.api.ReportRoomEntryPoint
import io.element.android.features.rolesandpermissions.api.ChangeRoomMemberRolesEntryPoint
import io.element.android.features.rolesandpermissions.api.ChangeRoomMemberRolesListType
import io.element.android.features.securebackup.api.SecureBackupEntryPoint
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.appyx.canPop
import io.element.android.libraries.architecture.appyx.launchMolecule
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.deeplink.api.usecase.InviteFriendsUseCase
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.utils.DelayedVisibility
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.troubleshoot.api.NotificationTroubleShootEntryPoint
import io.element.android.libraries.troubleshoot.api.PushHistoryEntryPoint
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

@ContributesNode(SessionScope::class)
@AssistedInject
class HomeFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val matrixClient: MatrixClient,
    private val presenter: HomePresenter,
    private val inviteFriendsUseCase: InviteFriendsUseCase,
    private val analyticsService: AnalyticsService,
    private val acceptDeclineInviteView: AcceptDeclineInviteView,
    private val directLogoutView: DirectLogoutView,
    private val notificationTroubleShootEntryPoint: NotificationTroubleShootEntryPoint,
    private val reportRoomEntryPoint: ReportRoomEntryPoint,
    private val declineInviteAndBlockUserEntryPoint: DeclineInviteAndBlockEntryPoint,
    private val changeRoomMemberRolesEntryPoint: ChangeRoomMemberRolesEntryPoint,
    private val leaveRoomRenderer: LeaveRoomRenderer,
    private val pushHistoryEntryPoint: PushHistoryEntryPoint,
    private val lockScreenEntryPoint: LockScreenEntryPoint,
    private val openSourceLicensesEntryPoint: OpenSourceLicensesEntryPoint,
    private val logoutEntryPoint: LogoutEntryPoint,
    private val accountDeactivationEntryPoint: AccountDeactivationEntryPoint,
    private val linkNewDeviceEntryPoint: LinkNewDeviceEntryPoint,
    private val secureBackupEntryPoint: SecureBackupEntryPoint,
    private val bugReportEntryPoint: BugReportEntryPoint,

    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,

    ) : BaseFlowNode<HomeFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    private val callback: HomeEntryPoint.Callback = callback()
    private val stateFlow = launchMolecule { presenter.present() }

    private val preferencesRootNode by lazy {
        val prefCallback = object : PreferencesRootNode.Callback {
            override fun navigateToAddAccount() {
                backstack.push(NavTarget.NotLoggedInFlow(null))
            }

            override fun navigateToBugReport() = callback.navigateToBugReport()
            override fun navigateToSecureBackup() {
                backstack.push(NavTarget.SecureBackup(initialElement = SecureBackupEntryPoint.InitialTarget.Root))
            }

            override fun navigateToAnalyticsSettings() = backstack.push(AnalyticsSettings)
            override fun navigateToAbout() {
                backstack.push(NavTarget.About)
            }

            override fun navigateToLocalization() = backstack.push(Localization)
            override fun navigateToDeveloperSettings() = backstack.push(DeveloperSettings)
            override fun navigateToNotificationSettings() = backstack.push(NotificationSettings)
            override fun navigateToLockScreenSettings() {
                backstack.push(NavTarget.LockScreenSettings)
            }

            override fun navigateToAdvancedSettings() {
                backstack.push(NavTarget.AdvancedSettings)
            }

            override fun navigateToLabs() {
                backstack.push(NavTarget.Labs)
            }

            override fun navigateToLinkNewDevice() {
                backstack.push(NavTarget.LinkNewDevice)
            }

            override fun navigateToUserProfile(matrixUser: MatrixUser) = navigateToProfile(matrixUser)
            override fun navigateToBlockedUsers() = backstack.push(BlockedUsers)

            override fun startSignOutFlow() {
                backstack.push(NavTarget.SignOut)
            }

            override fun startAccountDeactivationFlow() {
                backstack.push(NavTarget.AccountDeactivation)
            }
        }
        createNode<PreferencesRootNode>(buildContext, listOf(prefCallback))
    }

    private val profileNode by lazy {
//        var uiState by mutableStateOf(stateFlow)
//        val inputs = Inputs(uiState.value.currentUserAndNeighbors.first())
        val matrixClientX = MutableStateFlow(matrixClient)
        var matrixUser = matrixClientX.value.userProfile.value
        val inputs = Inputs(matrixUser,true)

        val callback = object : Callback {
            override fun onDone() {
                backstack.pop()
            }
        }
        createNode<EditUserProfileNode>(buildContext, listOf(inputs, callback))

    }

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.Home))
            }
        )
        whenChildAttached {
            commonLifecycle: Lifecycle,
            changeRoomMemberRolesNode: ChangeRoomMemberRolesEntryPoint.NodeProxy,
            ->
            commonLifecycle.coroutineScope.launch {
                val isNewOwnerSelected = changeRoomMemberRolesNode.waitForCompletion()
                withContext(NonCancellable) {
                    backstack.pop()
                    if (isNewOwnerSelected) {
                        onNewOwnersSelected(changeRoomMemberRolesNode.roomId)
                    }
                }
            }
        }
    }

    sealed interface NavTarget : Parcelable {

        @Parcelize data object BugReport : NavTarget
        @Parcelize data class NotLoggedInFlow(
            val params: LoginParams?
        ) : NavTarget

        @Parcelize
        data class SecureBackup(
            val initialElement: SecureBackupEntryPoint.InitialTarget = SecureBackupEntryPoint.InitialTarget.Root
        ) : NavTarget

        @Parcelize
        data object LinkNewDevice : NavTarget

        @Parcelize
        data object AccountDeactivation : NavTarget

        @Parcelize
        data object SignOut : NavTarget

        @Parcelize
        data object AdvancedSettings : NavTarget

        @Parcelize
        data object Labs : NavTarget

        @Parcelize
        data object OssLicenses : NavTarget

        @Parcelize
        data object About : NavTarget

        @Parcelize
        data object LockScreenSettings : NavTarget

        @Parcelize
        data object PushHistory : NavTarget

        @Parcelize
        data object DeveloperSettings : NavTarget

        @Parcelize
        data object AnalyticsSettings : NavTarget

        @Parcelize
        data object BlockedUsers : NavTarget

        @Parcelize
        data class EditDefaultNotificationSetting(val isOneToOne: Boolean) : NavTarget

        @Parcelize
        data object TroubleshootNotifications : NavTarget

        @Parcelize
        data object Localization : NavTarget

        @Parcelize
        data object NotificationSettings : NavTarget
//        --------------------

        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data class ReportRoom(val roomId: RoomId) : NavTarget

        @Parcelize
        data class NavigateProfile(val matrixUser: MatrixUser) : NavTarget

        @Parcelize
        data class DeclineInviteAndBlockUser(val inviteData: InviteData) : NavTarget

        @Parcelize
        data class SelectNewOwnersWhenLeavingRoom(val roomId: RoomId) : NavTarget
    }

    private fun navigateToReportRoom(roomId: RoomId) {
        backstack.push(ReportRoom(roomId))
    }

    private fun navigateToProfile(matrixUser: MatrixUser) {
        backstack.push(NavigateProfile(matrixUser))
    }

    private fun navigateToDeclineInviteAndBlockUser(roomSummary: RoomListRoomSummary) {
        backstack.push(DeclineInviteAndBlockUser(roomSummary.toInviteData()))
    }

    private fun onMenuActionClick(activity: Activity, roomListMenuAction: RoomListMenuAction) {
        when (roomListMenuAction) {
            RoomListMenuAction.InviteFriends -> {
                inviteFriendsUseCase.execute(activity)
            }
            RoomListMenuAction.ReportBug -> {
                callback.navigateToBugReport()
            }
        }
    }

    private fun navigateToSelectNewOwnersWhenLeavingRoom(roomId: RoomId) {
        backstack.push(SelectNewOwnersWhenLeavingRoom(roomId))
    }

    private fun onNewOwnersSelected(roomId: RoomId) {
        stateFlow.value.roomListState.eventSink(RoomListEvent.LeaveRoom(roomId, needsConfirmation = false))
    }

    private fun rootNode(buildContext: BuildContext): Node {
        return node(buildContext) { modifier ->
            val state by stateFlow.collectAsState()
            val activity = requireNotNull(LocalActivity.current)

            val loadingJoinedRoomJob = remember { mutableStateOf<AsyncData<Job>>(AsyncData.Uninitialized) }
            if (loadingJoinedRoomJob.value.isLoading()) {
                DelayedVisibility(duration = 400.milliseconds) {
                    ProgressDialog(
                        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
                        onDismissRequest = {
                            loadingJoinedRoomJob.value.dataOrNull()?.cancel()
                            loadingJoinedRoomJob.value = AsyncData.Uninitialized
                        }
                    )
                }
            }

            fun navigateToRoom(
                roomId: RoomId,
            ) {
                if (!loadingJoinedRoomJob.value.isUninitialized()) {
                    Timber.w("Already loading a room, ignoring navigateToRoom for $roomId")
                    return
                }

                val job = sessionCoroutineScope.launch {
                    runCatchingExceptions {
                        matrixClient.getJoinedRoom(roomId)
                    }.fold(
                        onSuccess = { joinedRoom ->
                            if (isActive) {
                                callback.navigateToRoom(roomId, joinedRoom)
                                loadingJoinedRoomJob.value = AsyncData.Success(coroutineContext.job)
                                // Wait a bit before resetting the state to avoid allowing to open several rooms
                                delay(200.milliseconds)
                                loadingJoinedRoomJob.value = AsyncData.Uninitialized
                            }
                        },
                        onFailure = {
                            // If the operation wasn't cancelled, navigate without the room, using the room id
                            if (it !is CancellationException) {
                                callback.navigateToRoom(roomId, null)
                            }
                            loadingJoinedRoomJob.value = AsyncData.Failure(error = it, prevData = coroutineContext.job)
                            // Wait a bit before resetting the state to avoid allowing to open several rooms
                            delay(200.milliseconds)
                            loadingJoinedRoomJob.value = AsyncData.Uninitialized
                        }
                    )
                }
                loadingJoinedRoomJob.value = AsyncData.Loading(job)
            }


            HomeView(
                homeState = state,
                onRoomClick = ::navigateToRoom,
                navigateToProfileEdit = {
                    navigateToProfile(matrixUser = it)
                },
                onSettingsClick = callback::navigateToSettings,
                onStartChatClick = callback::navigateToCreateRoom,
                onCreateSpaceClick = callback::navigateToCreateSpace,
                onSetUpRecoveryClick = callback::navigateToSetUpRecovery,
                onConfirmRecoveryKeyClick = callback::navigateToEnterRecoveryKey,
                onRoomSettingsClick = callback::navigateToRoomSettings,
                onMenuActionClick = { onMenuActionClick(activity, it) },
                onReportRoomClick = ::navigateToReportRoom,
                onDeclineInviteAndBlockUser = ::navigateToDeclineInviteAndBlockUser,
                modifier = modifier,
                acceptDeclineInviteView = {
                    acceptDeclineInviteView.Render(
                        state = state.roomListState.acceptDeclineInviteState,
                        onAcceptInviteSuccess = ::navigateToRoom,
                        onDeclineInviteSuccess = { },
                        modifier = Modifier
                    )
                },
                leaveRoomView = {
                    leaveRoomRenderer.Render(
                        state = state.roomListState.leaveRoomState,
                        onSelectNewOwners = ::navigateToSelectNewOwnersWhenLeavingRoom,
                        modifier = Modifier
                    )
                },

                settingsView = { settingsModifier ->
                    preferencesRootNode.View(settingsModifier)
                },

                profileView={ profileModifier ->
                    profileNode.View(profileModifier)
                }
            )
            directLogoutView.Render(state.directLogoutState)
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.BugReport -> {
                val callback = object : BugReportEntryPoint.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }
                }
                bugReportEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = callback,
                )
            }
            is NavTarget.NotLoggedInFlow -> {
                val callback = object : NotLoggedInFlowNode.Callback {
                    override fun navigateToBugReport() {
                        backstack.push(NavTarget.BugReport)
                    }

                    override fun onDone() {
                        backstack.pop()
                    }
                }
                val params = NotLoggedInFlowNode.Params(
                    loginParams = navTarget.params,
                )
                createNode<NotLoggedInFlowNode>(buildContext, plugins = listOf(params, callback))
            }
            is NavTarget.SecureBackup -> {
                secureBackupEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = SecureBackupEntryPoint.Params(initialElement = navTarget.initialElement),
                    callback = object : SecureBackupEntryPoint.Callback {
                        override fun onDone() {
                            backstack.pop()
                        }
                    },
                )
            }
            NavTarget.LinkNewDevice -> {
                val callback = object : LinkNewDeviceEntryPoint.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }
                }
                linkNewDeviceEntryPoint.createNode(this, buildContext, callback)
            }
            NavTarget.AccountDeactivation -> {
                accountDeactivationEntryPoint.createNode(this, buildContext)
            }
            NavTarget.SignOut -> {
                val callBack: LogoutEntryPoint.Callback = object : LogoutEntryPoint.Callback {
                    override fun navigateToSecureBackup() {
//                        callback.navigateToSecureBackup()
                    }
                }
                logoutEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = callBack,
                )
            }
            NavTarget.AdvancedSettings -> {
                createNode<AdvancedSettingsNode>(buildContext)
            }
            NavTarget.Labs -> {
                val callback = object : LabsNode.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }
                }
                createNode<LabsNode>(buildContext, listOf(callback))
            }
            is NavTarget.OssLicenses -> {
                openSourceLicensesEntryPoint.createNode(this, buildContext)
            }
            NavTarget.About -> {
                val callback = object : AboutNode.Callback {
                    override fun navigateToOssLicenses() {
                        backstack.push(NavTarget.OssLicenses)
                    }
                }
                createNode<AboutNode>(buildContext, listOf(callback))
            }
            NavTarget.LockScreenSettings -> {
                lockScreenEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    navTarget = LockScreenEntryPoint.Target.Settings,
                    callback = object : LockScreenEntryPoint.Callback {
                        override fun onSetupDone() {
                            // No op
                        }
                    }
                )
            }
            PushHistory -> {
                pushHistoryEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = object : PushHistoryEntryPoint.Callback {
                        override fun onDone() {
                            if (backstack.canPop()) {
                                backstack.pop()
                            } else {
                                navigateUp()
                            }
                        }

                        override fun navigateToEvent(roomId: RoomId, eventId: EventId) {
//                            callback.navigateToEvent(roomId, eventId)
                        }
                    },
                )
            }
            DeveloperSettings -> {
                val developerSettingsCallback = object : DeveloperSettingsNode.Callback {
                    override fun navigateToPushHistory() {
                        backstack.push(PushHistory)
                    }

                    override fun onDone() {
                        if (backstack.canPop()) {
                            backstack.pop()
                        } else {
                            navigateUp()
                        }
                    }
                }
                createNode<DeveloperSettingsNode>(buildContext, listOf(developerSettingsCallback))
            }
            AnalyticsSettings -> {
                createNode<AnalyticsSettingsNode>(buildContext)
            }
            is NavigateProfile -> {
                val inputs = Inputs(navTarget.matrixUser,false)
                val callback = object : Callback {
                    override fun onDone() {
                        backstack.pop()
                    }
                }
                createNode<EditUserProfileNode>(buildContext, listOf(inputs, callback))
            }
            is ReportRoom -> {
                reportRoomEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    roomId = navTarget.roomId,
                )
            }
            is DeclineInviteAndBlockUser -> {
                declineInviteAndBlockUserEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    inviteData = navTarget.inviteData,
                )
            }
            is SelectNewOwnersWhenLeavingRoom -> {
                val room = runBlocking { matrixClient.getJoinedRoom(navTarget.roomId) } ?: error("Room ${navTarget.roomId} not found")
                changeRoomMemberRolesEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    room = room,
                    listType = ChangeRoomMemberRolesListType.SelectNewOwnersWhenLeaving,
                )
            }
            Root -> rootNode(buildContext)

            //----------------------------------------------------------------------------------------------

            Localization -> {
                createNode<LocalizationNode>(buildContext, listOf(callback))
            }
            NotificationSettings -> {
                val notificationSettingsCallback = object : NotificationSettingsNode.Callback {
                    override fun navigateToEditDefaultNotificationSetting(isOneToOne: Boolean) {
                        backstack.push(EditDefaultNotificationSetting(isOneToOne))
                    }

                    override fun navigateToTroubleshootNotifications() {
                        backstack.push(TroubleshootNotifications)
                    }
                }
                createNode<NotificationSettingsNode>(buildContext, listOf(notificationSettingsCallback))
            }
            TroubleshootNotifications -> {
                notificationTroubleShootEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = object : NotificationTroubleShootEntryPoint.Callback {
                        override fun onDone() {
                            if (backstack.canPop()) {
                                backstack.pop()
                            } else {
                                navigateUp()
                            }
                        }

                        override fun navigateToBlockedUsers() {
                            backstack.push(BlockedUsers)
                        }
                    },
                )
            }
            is EditDefaultNotificationSetting -> {
                val callback = object : EditDefaultNotificationSettingNode.Callback {
                    override fun navigateToRoomNotificationSettings(roomId: RoomId) {
//                        callback.navigateToRoomNotificationSettings(roomId)
                    }
                }
                val input = EditDefaultNotificationSettingNode.Inputs(navTarget.isOneToOne)
                createNode<EditDefaultNotificationSettingNode>(buildContext, plugins = listOf(input, callback))
            }
            BlockedUsers -> {
                createNode<BlockedUsersNode>(buildContext)
            }

        }
    }
}
