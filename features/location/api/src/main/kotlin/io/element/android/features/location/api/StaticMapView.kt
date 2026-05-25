/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.api

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.maptiler.maptilersdk.annotations.MTMarker
import com.maptiler.maptilersdk.events.MTEvent
import com.maptiler.maptilersdk.map.LngLat
import com.maptiler.maptilersdk.map.MTMapOptions
import com.maptiler.maptilersdk.map.MTMapView
import com.maptiler.maptilersdk.map.MTMapViewController
import com.maptiler.maptilersdk.map.MTMapViewDelegate
import com.maptiler.maptilersdk.map.style.MTMapReferenceStyle
import com.maptiler.maptilersdk.map.style.MTMapStyleVariant
import com.maptiler.maptilersdk.map.types.MTData
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.location.api.internal.StaticMapPlaceholder
import io.element.android.features.location.api.internal.centerBottomEdge
import io.element.android.libraries.designsystem.components.LocationPin
import io.element.android.libraries.designsystem.components.PinVariant
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

@Composable
fun StaticMapView(
    location: Location?,
    zoom: Double,
    pinVariant: PinVariant,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    darkMode: Boolean = !ElementTheme.isLightTheme,
    onContentClick: (() -> Unit)?,
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            pinVariant is PinVariant.StaleLocation -> {
                StaleMapContent(
                    pinVariant = pinVariant,
                    contentDescription = contentDescription,
                    width = maxWidth,
                    height = maxHeight,
                )
            }
            location == null -> {
                StaticMapPlaceholder(
                    painter = painterResource(R.drawable.blurred_map),
                    canReload = false,
                    contentDescription = contentDescription,
                    width = maxWidth,
                    height = maxHeight,
                    onLoadMapClick = {}
                )
            }
            else -> LoadableMapContent(
                location = location,
                zoom = zoom,
                pinVariant = pinVariant,
                darkMode = darkMode,
                onContentClick = onContentClick,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.StaleMapContent(
    pinVariant: PinVariant,
    contentDescription: String?,
    width: Dp,
    height: Dp,
) {
    Box(contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.stale_map),
            contentDescription = contentDescription,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.size(width = width, height = height)
        )
        LocationPin(variant = pinVariant, modifier = Modifier.centerBottomEdge(this@StaleMapContent))
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@SuppressLint("UseKtx")
@Composable
private fun LoadableMapContent(
    location: Location,
    zoom: Double,
    pinVariant: PinVariant,
    darkMode: Boolean,
    onContentClick: (() -> Unit)?,
    modifier: Modifier
) {
    val context = LocalContext.current
    val controller = remember { MTMapViewController(context) }

    LaunchedEffect(controller, location) {
        controller.delegate = object : MTMapViewDelegate {
            override fun onMapViewInitialized() {
                val targetCoordinates = LngLat(location.lon, location.lat)

                val drawable = ContextCompat.getDrawable(
                    context,
                    io.element.android.compound.R.drawable.ic_compound_location_pin_solid
                )
                val mapTilerIcon = drawable?.toBitmap(width = 24, height = 24)

                if (mapTilerIcon != null) {
                    val locationMarker = MTMarker(targetCoordinates, mapTilerIcon)
                    controller.style?.addMarker(locationMarker)
                }

                try {
                    controller.setCenter(targetCoordinates)
                    controller.setZoom(zoom)
                } catch (e: Exception) {
                    //
                }
            }

            override fun onEventTriggered(event: MTEvent, data: MTData?) {
                // no-op
            }
        }
    }

    DisposableEffect(controller) {
        onDispose { controller.delegate = null }
    }

    val mapOptions = remember { MTMapOptions() }

    Box(modifier = Modifier.clickable {
        onContentClick?.invoke()
    }) {
        MTMapView(
            referenceStyle = MTMapReferenceStyle.DATAVIZ,
            options = mapOptions,
            controller = controller,
            modifier = Modifier
                .fillMaxSize()

                .combinedClickable(
                    onClick = onContentClick!!,
                    onLongClick = { },
                ),
            styleVariant = if (darkMode) MTMapStyleVariant.DARK else MTMapStyleVariant.DEFAULT_VARIANT,
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent)
                .pointerInteropFilter { motionEvent ->
                    if (motionEvent.action == MotionEvent.ACTION_UP) {
                        onContentClick()
                    }
                    true
                }
        )
    }
}

@PreviewsDayNight
@Composable
internal fun StaticMapViewPreview() = ElementPreview {
    StaticMapView(
        location = Location(0.0, 0.0),
        zoom = 0.0,
        contentDescription = null,
        pinVariant = PinVariant.PinnedLocation,
        modifier = Modifier.size(400.dp),
        onContentClick = {}
    )
}
