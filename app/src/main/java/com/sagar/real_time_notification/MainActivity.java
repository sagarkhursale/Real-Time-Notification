package com.sagar.real_time_notification;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextUtils;
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
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int AUTO_PLACES_REQUEST = 101;

    private EditText editText_Current_Location;
    private EditText editText_Destination_Location;
    private AppCompatButton addGeofenceButton;

    private GoogleApiClient mGoogleApiClient;
    protected Geofence mGeofence;


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


        // googleApiClient
        buildGoogleApiClient();


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
                    /*A pending intent that that is reused when calling removeGeofences(). This
                    pending intent is used to generate an intent when a matched geofence
                    transition is observed.**/
                    getGeofencingPendingIntent());
        } else {
            Log.i(TAG, "Permission Denied");
        }
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient onConnected");
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // location update time

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                Log.i(TAG, "Last Location : " + mLastLocation.toString());
                //updateUI(mLastLocation);
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
        editText_Current_Location.setText(getCurrentAddress(location.getLatitude(), location.getLongitude()));
    }


    private Address getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

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
        }
    }


    private void populateGeofence(LatLng latLng) {
        Pair<String, LatLng> geofencePair = new Pair<>("destination-fence", latLng);

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

    // END
}
