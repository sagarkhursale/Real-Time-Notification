package com.sagar.real_time_notification;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import static android.content.Context.MODE_PRIVATE;


public class RuntimePermissionHandler {
    private final String TAG = RuntimePermissionHandler.class.getSimpleName();
    private static final int PLAY_SERVICES_REQUEST = 111;
    private static final int REQUEST_CHECK_SETTINGS = 112;
    private final int LOCATION_PERMISSION_CONSTANT = 113;
    private final int REQUEST_PERMISSION_SETTING = 114;

    private Context mContext;
    private SharedPreferences mPermissionStatus;


    public RuntimePermissionHandler(Context mContext) {
        this.mContext = mContext;
        mPermissionStatus = mContext.getSharedPreferences("permissionStatus", MODE_PRIVATE);
    }


    public boolean checkPlayServicesAvailability() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(mContext);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog((Activity) mContext, resultCode, PLAY_SERVICES_REQUEST).show();
            } else {
                Log.i(TAG, mContext.getString(R.string.play_services_not_available));
            }
            return false;
        }
        return true;
    }


    // END
}
