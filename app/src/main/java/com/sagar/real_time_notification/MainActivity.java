package com.sagar.real_time_notification;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private EditText editText_Current_Location;
    private EditText editText_Destination_Location;
    private AppCompatButton buttonEvent;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText_Current_Location = findViewById(R.id.edt_current_location);
        editText_Destination_Location = findViewById(R.id.edt_destination_location);
        buttonEvent = findViewById(R.id.buttonEvent);

        editText_Current_Location.addTextChangedListener(editTextWatcher);
        editText_Destination_Location.addTextChangedListener(editTextWatcher);

        buttonEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClickHandler();
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

            buttonEvent.setEnabled(!cur_location.isEmpty() && !dest_location.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    private void buttonClickHandler() {
        Toast.makeText(MainActivity.this, "dsdss", Toast.LENGTH_SHORT).show();
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
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // location update time

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
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
    }


    // END
}
