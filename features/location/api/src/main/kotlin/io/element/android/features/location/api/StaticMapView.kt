/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.api

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import coil3.Extras
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.location.api.internal.StaticMapPlaceholder
import io.element.android.features.location.api.internal.StaticMapUrlBuilder
import io.element.android.features.location.api.internal.centerBottomEdge
import io.element.android.libraries.designsystem.components.LocationPin
import io.element.android.libraries.designsystem.components.PinVariant
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.location.DesiredAccuracy
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationPuckColors
import org.maplibre.compose.location.LocationPuckSizes
import org.maplibre.compose.location.UserLocationState
import org.maplibre.compose.location.rememberAndroidLocationProvider
import org.maplibre.compose.location.rememberNullLocationProvider
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Duration.Companion.minutes

/**
 * Shows a static map image downloaded via a third party service's static maps API.
 */
@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@Composable
fun StaticMapView(
    lat: Double,
    lon: Double,
    zoom: Double,
    pinVariant: PinVariant,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    darkMode: Boolean = !ElementTheme.isLightTheme,
) {
    // Using BoxWithConstraints to:
    // 1) Size the inner Image to the same Dp size of the outer BoxWithConstraints.
    // 2) Request the static map image of the exact required size in Px to fill the AsyncImage.
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val context = LocalContext.current
        var retryHash by remember { mutableIntStateOf(0) }




        val builder = remember { StaticMapUrlBuilder() }
        val painter = rememberAsyncImagePainter(
            model = if (constraints.isZero) {
                // Avoid building a URL if any of the size constraints is zero (else it will thrown an exception).
                null
            } else
            {
                ImageRequest.Builder(context)
                    .data(
                        builder.build(
                            lat = lat,
                            lon = lon,
                            zoom = zoom,
                            darkMode = darkMode,
                            width = constraints.maxWidth,
                            height = constraints.maxHeight,
                            density = LocalDensity.current.density,
                        )
                    )
                    .size(width = constraints.maxWidth, height = constraints.maxHeight)
                    .apply {
                        extras.set(Extras.Key("retry_hash"), retryHash).build()
                    }
                    .build()
            }
        )



        val initialPosition = remember {
                val firstLocation = Location(lat,lon)
                CameraPosition(
                    target = Position(latitude = firstLocation.lat, longitude = firstLocation.lon),
                    zoom = MapDefaults.DEFAULT_ZOOM
                )
        }
        val cameraState = rememberCameraState(firstPosition = initialPosition)



        val collectedState = painter.state.collectAsState()
        if (collectedState.value is AsyncImagePainter.State.Success) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.size(width = maxWidth, height = maxHeight),
                // The returned image can be smaller than the requested size due to the static maps API having
                // a max width and height of 2048 px. See buildStaticMapsApiUrl() for more details.
                // We apply ContentScale.Fit to scale the image to fill the AsyncImage should this be the case.
                contentScale = ContentScale.Fit,
            )
            LocationPin(variant = pinVariant, modifier = Modifier.centerBottomEdge(this))
        } else {
            StaticMapPlaceholder(
                showProgress = collectedState.value.isLoading(),
                canReload = builder.isServiceAvailable(),
                contentDescription = contentDescription,
                width = maxWidth,
                height = maxHeight,
                onLoadMapClick = { retryHash++ }
            )
        }
    }
}




private fun AsyncImagePainter.State.isLoading(): Boolean {
    return this is AsyncImagePainter.State.Empty ||
        this is AsyncImagePainter.State.Loading
}

@PreviewsDayNight
@Composable
internal fun StaticMapViewPreview() = ElementPreview {
    StaticMapView(
        lat = 0.0,
        lon = 0.0,
        zoom = 0.0,
        contentDescription = null,
        pinVariant = PinVariant.PinnedLocation,
        modifier = Modifier.size(400.dp),
    )
}
