package com.example.farmhelper.ui.weather.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

object AppLocationProvider {

    private const val TAG = "AppLocationProvider"

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        // 1. Get best last known location immediately
        val lastKnownGps = try { locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: SecurityException) { null }
        val lastKnownNetwork = try { locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: SecurityException) { null }

        val bestLastKnown = when {
            lastKnownGps != null && lastKnownNetwork != null -> {
                if (lastKnownGps.time > lastKnownNetwork.time) lastKnownGps else lastKnownNetwork
            }
            lastKnownGps != null -> lastKnownGps
            else -> lastKnownNetwork
        }

        // If the last known location is very fresh (less than 1 minute old), return it immediately
        if (bestLastKnown != null && (System.currentTimeMillis() - bestLastKnown.time) < TimeUnit.MINUTES.toMillis(1)) {
            Log.d(TAG, "Using fresh last-known location: Lat=${bestLastKnown.latitude}, Lng=${bestLastKnown.longitude}")
            return bestLastKnown
        }

        // 2. Request from Fused Location Provider Client (Google Play Services)
        val freshLocation = try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            withTimeoutOrNull(8000) {
                suspendCancellableCoroutine<Location?> { continuation ->
                    val request = CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .build()

                    fusedLocationClient.getCurrentLocation(request, cts.token)
                        .addOnSuccessListener { loc ->
                            if (continuation.isActive) {
                                continuation.resume(loc)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "FusedLocationProviderClient failed: ${exception.message}")
                            if (continuation.isActive) {
                                continuation.resume(null)
                            }
                        }

                    continuation.invokeOnCancellation {
                        cts.cancel()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fused location provider not available or threw exception: ${e.message}")
            null
        }

        if (freshLocation != null) {
            Log.d(TAG, "Retrieved location from FusedLocationProviderClient: Lat=${freshLocation.latitude}, Lng=${freshLocation.longitude}")
            return freshLocation
        }

        // 3. Fallback to LocationManager fresh request (prefer GPS over Network)
        val fallbackLocation = getFreshLocationFromLocationManager(context)
        if (fallbackLocation != null) {
            Log.d(TAG, "Retrieved location from LocationManager fallback: Lat=${fallbackLocation.latitude}, Lng=${fallbackLocation.longitude}")
            return fallbackLocation
        }

        if (bestLastKnown != null) {
            Log.d(TAG, "Falling back to older last-known location: Lat=${bestLastKnown.latitude}, Lng=${bestLastKnown.longitude}")
        } else {
            Log.w(TAG, "Unable to resolve any location.")
        }
        return bestLastKnown
    }

    @SuppressLint("MissingPermission")
    private suspend fun getFreshLocationFromLocationManager(context: Context): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!gpsEnabled && !networkEnabled) {
            return null
        }

        // Prefer GPS over Network for higher accuracy
        val provider = if (gpsEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER

        return withTimeoutOrNull(6000) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationManager.removeUpdates(this)
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                try {
                    locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                } catch (e: SecurityException) {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }

                continuation.invokeOnCancellation {
                    try {
                        locationManager.removeUpdates(listener)
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
        }
    }
}
