package il.ac.shenkar.octoid;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class GeoFencesActivity extends FragmentActivity
        implements ConnectionCallbacks, OnConnectionFailedListener, OnAddGeofencesResultListener
{

    private final static DecimalFormat DF = new DecimalFormat("#.##");

    private EditText mLocationIn;
    private TextView mLocationOut;
    private Geocoder mGeocoder;
    private GoogleMap mGoogleMap = null;
    private Marker marker = null;
    private Button mBtnDone;
    private LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geo_fences_activity);

       //bind to layout
        mLocationIn = (EditText) findViewById(R.id.location_input);
        mLocationOut = (TextView) findViewById(R.id.location_output);
        mBtnDone = (Button)findViewById(R.id.button_set_geo_fence);
        mGeocoder = new Geocoder(this);

        int gpsExists = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (gpsExists == ConnectionResult.SUCCESS)
        {
            mLocationClient = new LocationClient(this, this, this);
            mLocationClient.connect();
        }

        setUpMapIfNeeded();

        mLocationIn.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    lookUp(mLocationIn.getText().toString());
                }
                return false;
            }
        });

        mBtnDone.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle taskDetailsBundle = new Bundle();

                createGeoFence();

                taskDetailsBundle.putString("location", mLocationIn.getText().toString());
                intent.putExtras(taskDetailsBundle);
                setResult(RESULT_OK, intent);

                finish();
            }
        });

    }

    @Override
    public void onStart()
    {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
        // If adding the geofences was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            Toast.makeText(this, "Geofence was added", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to add Geofence", Toast.LENGTH_LONG).show();
        }

        mLocationClient.disconnect();
    }

    private void createGeoFence()
    {
        Geofence geofence = new Geofence.Builder()
                .setRequestId("location")
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setCircularRegion(marker.getPosition().latitude, marker.getPosition().longitude, 50)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();

        mLocationClient.addGeofences(Arrays.asList(geofence), getTransitionPendingIntent(), this);
    }

    private PendingIntent getTransitionPendingIntent()
    {
        // Create an explicit Intent
        Intent intent = new Intent(this, ReceiveTransitionsIntentService.class);
        /*
         * Return the PendingIntent
         */
        return PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationClient.connect();
        setUpMapIfNeeded();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationClient.disconnect();
    }

    @Override
    public void onConnected(Bundle arg0) {
        Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show();
    }
    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected", Toast.LENGTH_LONG).show();
    }

    /**
     * Lookup the address in input, format an output string and update map if possible
     */
    private void lookUp(String addressString) {
        String out;
        try {
            List<Address> addresses = mGeocoder.getFromLocationName(addressString, 1);
            if (addresses.size() >= 1) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                out = address.getAddressLine(0) + " ("
                        + DF.format(address.getLatitude()) + " , "
                        + DF.format(address.getLongitude()) + ")";
                updateMap(latLng);
            } else {
                out = "Not found";
            }
        } catch (IOException e) {
            out = "Not available";
        }
        mLocationOut.setText(out);
    }

    /**
     * Display a marker on the map and reposition the camera according to location
     * @param latLng
     */
    private void updateMap(LatLng latLng){
        if (mGoogleMap==null){
            return; //no play services
        }

        if (marker!=null){
            marker.remove();
        }

        marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng));

        //reposition camera
        CameraPosition newPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(15)
                .build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newPosition));
    }

    private void setUpMapIfNeeded()
    {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mGoogleMap == null) {
            mGoogleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mGoogleMap != null) {
                // The Map is verified. It is now safe to manipulate the map.

            }
        }
    }
}