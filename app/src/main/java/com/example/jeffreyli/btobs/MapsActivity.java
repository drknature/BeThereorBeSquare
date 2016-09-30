package com.example.jeffreyli.btobs;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.graphics.Color;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolygonOptions;
import android.location.LocationManager;
import android.location.Location;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import android.os.CountDownTimer;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, ConnectionCallbacks, OnConnectionFailedListener {

    private GoogleMap mMap;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    protected final static String LOCATION_KEY = "location-key";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);
        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();
        generateLevelOne();
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i("TAG", "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
        }
    }

    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //DO OP WITH LOCATION SERVICE
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i("TAG", "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();

        super.onStop();
    }


    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i("TAG", "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //DO OP WITH LOCATION SERVICE
            }
        }
        startLocationUpdates();
    }

    /**
     * Callback that fires when the location changes.
     */

    ArrayList<LatLng> coordList = new ArrayList<LatLng>(){{
        add(new LatLng(40.443525,-79.940813));
        add(new LatLng(40.443458,-79.940503));
        add(new LatLng(40.443392,-79.940193));
        add(new LatLng(40.443325,-79.939883));
        add(new LatLng(40.443258,-79.939573));
        add(new LatLng(40.443315,-79.940891));
        add(new LatLng(40.443249,-79.940582));
        add(new LatLng(40.443182,-79.940274));
        add(new LatLng(40.443116,-79.939964));
        add(new LatLng(40.443049,-79.939653));
        add(new LatLng(40.443106,-79.940969));
        add(new LatLng(40.443039,-79.94066));
        add(new LatLng(40.442973,-79.940351));
        add(new LatLng(40.442906,-79.940043));
        add(new LatLng(40.44284,-79.939734));
    }};

    List<LatLng> rect1 = Arrays.asList(coordList.get(0), coordList.get(1), coordList.get(6), coordList.get(5), coordList.get(0));
    List<LatLng> rect2 = Arrays.asList(coordList.get(1), coordList.get(2), coordList.get(7), coordList.get(6), coordList.get(1));
    List<LatLng> rect3 = Arrays.asList(coordList.get(2), coordList.get(3), coordList.get(8), coordList.get(7), coordList.get(2));
    List<LatLng> rect4 = Arrays.asList(coordList.get(3), coordList.get(4), coordList.get(9), coordList.get(8), coordList.get(3));
    List<LatLng> rect5 = Arrays.asList(coordList.get(5), coordList.get(6), coordList.get(11), coordList.get(10), coordList.get(5));
    List<LatLng> rect6 = Arrays.asList(coordList.get(6), coordList.get(7), coordList.get(12), coordList.get(11), coordList.get(6));
    List<LatLng> rect7 = Arrays.asList(coordList.get(7), coordList.get(8), coordList.get(13), coordList.get(12), coordList.get(7));
    List<LatLng> rect8 = Arrays.asList(coordList.get(8), coordList.get(9), coordList.get(14), coordList.get(13), coordList.get(8));

    List<List<LatLng>> rects = new ArrayList<List<LatLng>>(){{
        add(rect1);
        add(rect2);
        add(rect3);
        add(rect4);
        add(rect5);
        add(rect6);
        add(rect7);
        add(rect8);
    }};


    int count = 0;

    public boolean check(Location location){

        double lat1 = 40.443512;
        double lon1 = -79.944821;

        double lat2 = 40.443503;
        double lon2 = -79.944635;

        double lat3 = 40.443356;
        double lon3 = -79.944639;

        double lat4 = 40.443369;
        double lon4 = -79.944826;
        if (count == 0) {

            /*mMap.addPolygon(new PolygonOptions()
                    .add(new LatLng(lat1, lon1))
                    .add(new LatLng(lat2, lon2))
                    .add(new LatLng(lat3, lon3))
                    .add(new LatLng(lat4, lon4))
                    .strokeColor(Color.RED)
                    .fillColor(0x330000FF)
                    .strokeWidth(2)); */

            count ++;
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            if (latitude > lat1 || latitude > lat2 || latitude < lat3 || latitude < lat4){
                return true;
            }
            if (longitude > lon2 || longitude > lon3 || longitude < lon1 || longitude < lon4) {
                return true;
            }
        } else {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            if (latitude > lat1 || latitude > lat2 || latitude < lat3 || latitude < lat4){
                return true;
            }
            if (longitude > lon2 || longitude > lon3 || longitude < lon1 || longitude < lon4) {
                return true;
            }
        }
        return false;
    }

    int checkLevel = 0;

    ArrayList<BitmapDescriptor> imageCount = new ArrayList<BitmapDescriptor>(){{
        add(BitmapDescriptorFactory.fromResource(R.drawable.ten));
        add(BitmapDescriptorFactory.fromResource(R.drawable.nine));
        add(BitmapDescriptorFactory.fromResource(R.drawable.ten));
        add(BitmapDescriptorFactory.fromResource(R.drawable.nine));
        add(BitmapDescriptorFactory.fromResource(R.drawable.ten));
        add(BitmapDescriptorFactory.fromResource(R.drawable.nine));
        add(BitmapDescriptorFactory.fromResource(R.drawable.ten));
        add(BitmapDescriptorFactory.fromResource(R.drawable.nine));
        add(BitmapDescriptorFactory.fromResource(R.drawable.ten));
        add(BitmapDescriptorFactory.fromResource(R.drawable.nine));
    }};

    public void generateLevelOne() {

        checkLevel = 1;

        List<List<LatLng>> temprects = rects;
        Random rand = new Random();
        for (int i = 0; i < 4; i ++) {
            int randomInt = rand.nextInt(temprects.size());
            mMap.addPolygon(new PolygonOptions()
                    .strokeColor(Color.TRANSPARENT)
                    .fillColor(Color.rgb(197, 239, 247))
                    .addAll(temprects.get(randomInt)));
            temprects.remove(temprects.get(randomInt));
        }

        for (int i = 0; i < 4; i ++) {
            int randomInt = rand.nextInt(temprects.size());
            mMap.addPolygon(new PolygonOptions()
                    .strokeColor(Color.TRANSPARENT)
                    .fillColor(Color.rgb(44, 62, 80))
                    .addAll(temprects.get(randomInt)));
            temprects.remove(temprects.get(randomInt));
        }

        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {

                GroundOverlayOptions countdown = new GroundOverlayOptions();
                int num = 10 - ((int) millisUntilFinished / 1000);
                countdown.image(imageCount.get(num));
                countdown.anchor(0, 1);
                countdown.transparency(0.8f);
                countdown.position(new LatLng(40.443639, -79.940084), 100f, 100f);
                mMap.addGroundOverlay(countdown);
            }

            public void onFinish() {
                generateLevelTwo();
            }
        }.start();
    }

    public void generateLevelTwo(){

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        double latitude = mCurrentLocation.getLatitude();
        double longitude = mCurrentLocation.getLongitude();
        LatLng current = new LatLng(latitude, longitude);
        Log.d("TAG", latitude + " " + longitude);
        if (checkLevel == 1){
            boolean checking = check(mCurrentLocation);
            if (checking){
                count ++;
                //mMap.addMarker(new MarkerOptions().position(current).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).title(String.valueOf(latitude) + "," + String.valueOf(longitude)));
            }else {
                count ++;
                //mMap.addMarker(new MarkerOptions().position(current).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(String.valueOf(latitude) + "," + String.valueOf(longitude)));
            }
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i("TAG", "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i("TAG", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*
        double l1x = 40.443512;
        double l1y = -79.944821;
        LatLng l1 = new LatLng(l1x, l1y);
        mMap.addMarker(new MarkerOptions().position(l1).title("suh"));
        Marker test1 = mMap.addMarker(new MarkerOptions().position(l1).title("suh"));

        double l2x = 40.443503;
        double l2y = -79.944635;
        LatLng l2 = new LatLng(l2x, l2y);
        mMap.addMarker(new MarkerOptions().position(l2).title("suh"));
        Marker test2 = mMap.addMarker(new MarkerOptions().position(l2).title("suh"));

        double l3x = 40.443356;
        double l3y = -79.944639;
        LatLng l3 = new LatLng(l3x, l3y);
        mMap.addMarker(new MarkerOptions().position(l3).title("suh"));
        Marker test3 = mMap.addMarker(new MarkerOptions().position(l3).title("suh"));

        double l4x = 40.443369;
        double l4y = -79.944826;
        LatLng l4 = new LatLng(l4x, l4y);
        mMap.addMarker(new MarkerOptions().position(l4).title("suh"));
        Marker test4 = mMap.addMarker(new MarkerOptions().position(l4).title("suh"));
        */



        /*new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                String provider;
                provider = locationManager.getBestProvider(criteria,true);

                Location myLocation = locationManager.getLastKnownLocation(provider);
                myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //DO OP WITH LOCATION SERVICE
                }

                double latitude = myLocation.getLatitude();
                double longitude = myLocation.getLongitude();
                LatLng current = new LatLng(latitude, longitude);
                Log.d("TAG", latitude + " " + longitude);
                mMap.addMarker(new MarkerOptions().position(current).title(String.valueOf(latitude) + "," + String.valueOf(longitude)));
            }

            public void onFinish() {

            }
        }.start();*/

        /*

        LatLng point0 = new LatLng(40.443525, -79.940813);
        mMap.addMarker(new MarkerOptions().position(point0).title("suh"));
        Marker marker1 = mMap.addMarker(new MarkerOptions().position(point0).title("suh"));

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(point1));

        /*mMap.addCircle(new CircleOptions()
                .center(point0)
                .radius(100)
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE)); */

        /*
        LatLng point1 = new LatLng(40.443258,-79.939573);
        mMap.addMarker(new MarkerOptions().position(point1).title("dood"));
        Marker marker2 = mMap.addMarker(new MarkerOptions().position(point1).title("dood"));

        LatLng point2 = new LatLng(40.443106,-79.940969);
        mMap.addMarker(new MarkerOptions().position(point2).title("lol"));
        Marker marker3 = mMap.addMarker(new MarkerOptions().position(point2).title("dood"));

        LatLng point3 = new LatLng(40.44284,-79.939734);
        mMap.addMarker(new MarkerOptions().position(point3).title("jeff"));

        mMap.addPolygon(new PolygonOptions()
                .add(marker1.getPosition())
                .add(marker2.getPosition())
                .add(marker3.getPosition())
                .strokeColor(Color.RED)
                .fillColor(0x330000FF)
                .strokeWidth(2));

        LatLng point4 = new LatLng(40.443392,-79.940193 );
        mMap.addMarker(new MarkerOptions().position(point4).title("jeff hella weird lol"));

        LatLng point5 = new LatLng(40.443315,-79.940891);
        mMap.addMarker(new MarkerOptions().position(point5).title("goog maps bruh"));

        LatLng point6 = new LatLng(40.443182,-79.940274);
        mMap.addMarker(new MarkerOptions().position(point6).title("send help it's 3:41 am"));

        LatLng point7 = new LatLng(40.442973,-79.940351);
        mMap.addMarker(new MarkerOptions().position(point7).title("poopoo code"));

        LatLng point8 = new LatLng(40.443049,-79.939653);
        mMap.addMarker(new MarkerOptions().position(point8).title("omg i hate this"));

        LatLng point9 = new LatLng(40.443458,-79.940503 );
        mMap.addMarker(new MarkerOptions().position(point9).title("almost there"));

        LatLng point10 = new LatLng(40.443325,-79.939883);
        mMap.addMarker(new MarkerOptions().position(point10).title("current position works lmao"));

        LatLng point11 = new LatLng(40.443039,-79.94066);
        mMap.addMarker(new MarkerOptions().position(point11).title("end me bruh"));

        LatLng point12 = new LatLng(40.442906,-79.940043);
        mMap.addMarker(new MarkerOptions().position(point12).title("suh?"));

        LatLng point13 = new LatLng(40.443249,-79.940582);
        mMap.addMarker(new MarkerOptions().position(point13).title("vvvvvvvvvvvvvvvvvv"));

        LatLng point14 = new LatLng(40.443116,-79.939964);
        mMap.addMarker(new MarkerOptions().position(point14).title("made it"));

        */
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        mMap.setMyLocationEnabled(true);
    }

}
