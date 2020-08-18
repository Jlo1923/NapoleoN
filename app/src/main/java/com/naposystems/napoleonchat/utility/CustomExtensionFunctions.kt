package com.naposystems.napoleonchat.utility

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import kotlin.math.sqrt

fun LatLng.toBounds(radiusInMeters: Double): LatLngBounds {
    val distanceFromCenterToCorner = radiusInMeters * sqrt(2.0)
    val southwestCorner: LatLng =
        SphericalUtil.computeOffset(this, distanceFromCenterToCorner, 225.0)
    val northeastCorner: LatLng =
        SphericalUtil.computeOffset(this, distanceFromCenterToCorner, 45.0)
    return LatLngBounds(southwestCorner, northeastCorner)
}
