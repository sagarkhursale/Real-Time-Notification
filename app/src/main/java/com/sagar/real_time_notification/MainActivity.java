package com.sagar.real_time_notification;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import static com.sagar.real_time_notification.RuntimePermissionHandler.LOCATION_PERMISSION_CONSTANT;
import static com.sagar.real_time_notification.RuntimePermissionHandler.REQUEST_CHECK_SETTINGS;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int AUTO_PLACES_REQUEST = 101;

    private EditText editText_Current_Location;
    private EditText editText_Destination_Location;
    private AppCompatButton addGeofenceButton;

    private GoogleApiClient mGoogleApiClient;
    protected Geofence mGeofence;
    private static LocationRequest mLocationRequest;
    private RuntimePermissionHandler mPermissionHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText_Current_Location = findViewById(R.id.edt_current_location);
        editText_Destination_Location = findViewById(R.id.edt_destination_location);
        addGeofenceButton = findViewById(R.id.addGeoFence);

        editText_Current_Location.addTextChangedListener(editTextWatcher);
        editText_Destination_Location.addTextChangedListener(editTextWatcher);

        addGeofenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addGeofencesButtonHandler(view);
            }
        });

        mPermissionHandler = new RuntimePermissionHandler(this);

        buildGoogleApiClient();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mPermissionHandler.check_Request_Permissions()) {
                mPermissionHandler.locationSettingRequest(mLocationRequest);
            }
        } else {
            mPermissionHandler.locationSettingRequest(mLocationRequest);
        }

        // end
    }


    private TextWatcher editTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String cur_location = editText_Current_Location.getText().toString().trim();
            String dest_location = editText_Destination_Location.getText().toString().trim();

            addGeofenceButton.setEnabled(!cur_location.isEmpty() && !dest_location.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    public void addGeofencesButtonHandler(View view) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    /*pending intent is used to generate an intent when a matched geofence
                    transition is observed.**/
                    getGeofencingPendingIntent()).setResultCallback(this); // Result processed in onResult()
        } else {
            Log.i(TAG, "Permission Denied");
        }
    }


    private synchronized void buildGoogleApiClient() {
        // api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // location request
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // location update time

        // ends here..
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient onConnected");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                Log.i(TAG, "Last Location : " + mLastLocation.toString());
            }

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspended.");
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection has failed.");
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, location.toString());
        editText_Current_Location.setText(mPermissionHandler.getCurrentAddress(location.getLatitude(), location.getLongitude()));
    }


    public void editTextClickHandler(View view) {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(MainActivity.this);
            startActivityForResult(intent, AUTO_PLACES_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "Exception : " + e.toString());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case AUTO_PLACES_REQUEST:
                if (resultCode == RESULT_OK) {
                    Place autoCompletePlace = PlaceAutocomplete.getPlace(MainActivity.this, data);
                    editText_Destination_Location.setText(autoCompletePlace.getAddress());
                    populateGeofence(autoCompletePlace.getLatLng());

                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(MainActivity.this, data);
                    Log.i(TAG, status.getStatus().toString());
                }
                break;

            case RuntimePermissionHandler.REQUEST_PERMISSION_SETTING:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mPermissionHandler.locationSettingRequest(mLocationRequest);
                }
                break;

            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case AppCompatActivity.RESULT_OK:
                        Log.i(TAG, "location settings are satisfied!");
                        break;

                    case AppCompatActivity.RESULT_CANCELED:
                        Toast.makeText(this, "Please, enable the Gps!", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

        }
    }


    private void populateGeofence(LatLng latLng) {
        Pair<String, LatLng> geofencePair = new Pair<>("Destination Fence", latLng);

        mGeofence = new Geofence.Builder()
                .setRequestId(geofencePair.first)
                .setCircularRegion(geofencePair.second.latitude,
                        geofencePair.second.longitude,
                        Constants.GEOFENCE_RADIUS_IN_METERS)
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }


    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(mGeofence);
        return builder.build();
    }


    private PendingIntent getGeofencingPendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Toast.makeText(this, "Geofence Added.", Toast.LENGTH_SHORT).show();
            addGeofenceButton.setEnabled(false);
        } else {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CONSTANT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // got Permissions, just Go
                mPermissionHandler.locationSettingRequest(mLocationRequest);

            } else {

                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Permissions!");
                    builder.setMessage("This app requires Location permission.");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CONSTANT);
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
                    Toast.makeText(this, "Unable to get Permission", Toast.LENGTH_SHORT).show();
                }

            }
        }
        // end
    }


    // END
}
