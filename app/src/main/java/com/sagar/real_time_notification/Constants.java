package com.sagar.real_time_notification;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;


public final class Constants {

    public Constants() {
    }


    private static final String PACKAGE_NAME = "com.sagar.real_time_notification";


    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";


    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";


    /**
     * Used to set an expiration time for a geofence.
     */
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;


    /**
     * For this sample, geofences expire after twelve hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    //public static final float GEOFENCE_RADIUS_IN_METERS = 1609; // 1 mile, 1.6 km
    public static final float GEOFENCE_RADIUS_IN_METERS = 1000; // 1 mile, 1.6 km


    // end
}
