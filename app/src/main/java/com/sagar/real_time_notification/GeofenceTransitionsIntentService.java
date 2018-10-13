package com.sagar.real_time_notification;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;


public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "gfservice";


    public GeofenceTransitionsIntentService() {
        super(TAG);
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    // END
}
