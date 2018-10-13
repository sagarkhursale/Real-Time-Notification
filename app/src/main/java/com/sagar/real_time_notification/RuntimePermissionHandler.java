package com.sagar.real_time_notification;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;


public class RuntimePermissionHandler {
    private final String TAG = RuntimePermissionHandler.class.getSimpleName();
    private static final int PLAY_SERVICES_REQUEST = 111;
    static final int REQUEST_CHECK_SETTINGS = 112;
    static final int LOCATION_PERMISSION_CONSTANT = 113;
    static final int REQUEST_PERMISSION_SETTING = 114;

    private Context mContext;
    private SharedPreferences mPermissionStatus;


    RuntimePermissionHandler(Context mContext) {
        this.mContext = mContext;
        mPermissionStatus = mContext.getSharedPreferences("permissionStatus", MODE_PRIVATE);
    }


    protected boolean checkPlayServicesAvailability() {
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


    public boolean isInternetAvailable() {
        ConnectivityManager conn_manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (conn_manager != null)
            networkInfo = conn_manager.getActiveNetworkInfo();

        return networkInfo != null;
    }


    public boolean check_Request_Permissions() {

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Permissions!");
                builder.setMessage("This app requires Location permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (mPermissionStatus.getBoolean(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Permissions!");
                builder.setMessage("This app requires Location permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
                        intent.setData(uri);
                        ((Activity) mContext).startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                // just request the permission
                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CONSTANT);
            }
            //if
            SharedPreferences.Editor editor = mPermissionStatus.edit();
            editor.putBoolean(Manifest.permission.ACCESS_FINE_LOCATION, true);
            editor.apply();
        } else {
            // just go ahead ..
            return true;
        }

        return false;
        // end
    }


    private Address getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(mContext, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            return addresses.get(0);
        } catch (IOException e) {
            Log.e("Exception : ", e.toString());
        }
        return null;
    }


    public String getCurrentAddress(double latitude, double longitude) {
        Address location_address = getAddress(latitude, longitude);

        if (location_address != null) {
            String locality = location_address.getLocality();
            String subLocality = location_address.getSubLocality();
            if (!TextUtils.isEmpty(subLocality))
                return subLocality.trim() + "," + locality.trim();
        }
        return null;
    }


    public void locationSettingRequest(LocationRequest locationRequest) {

        // Get location settings
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(mContext).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    assert response != null;
                    Log.i(TAG, "Test 1 : " + response.getLocationSettingsStates());
                    Log.i(TAG, "All location settings are satisfied. The client can initialize location");

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult((Activity) mContext, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the Error ..
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Log.i(TAG, "Test 12 : Location settings are not satisfied, Unsupported device.");
                            break;
                    }
                }
            }
        });

    }


    // END
}
