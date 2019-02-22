package com.example.mahnoor.project1;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DeliveryBoyMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private FirebaseAnalytics mFirebaseAnalytics;
    LocationRequest LocationRequest;
    Location lastLocation;
    Marker dbMarker;
    LatLng dbLoc;
    int a = 1;

    ArrayList list = new ArrayList();
    List<Address> addresses;
    String currentAddress;
    double longi;
    double lati;
    private Button start;
    private Button end;

    boolean drawing = false;
    Polyline line;
    LatLng deliveryboyLoc;
    Marker deliveryBoyMarker;
    LatLng compareLatLang;

    private Button showFirebaseData;
    private float distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_boy_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
          start = (Button) findViewById(R.id.button2);
          end = (Button) findViewById(R.id.button6);

        start.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
             drawing = true;
         }
     });
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
        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(30000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (getApplicationContext() != null) {

            lastLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
//            routes.add(location.getLatitude());
//            routes.add(location.getLongitude());
            startDrawingRoute();



        }
    }

    private void getAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(DeliveryBoyMapsActivity.this, Locale.getDefault());
        try {

             addresses = null;


            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            Address obj = addresses.get(0);

            String add = obj.getAddressLine(0);
            currentAddress = obj.getSubAdminArea() + "," + obj.getAdminArea();


            Toast.makeText(DeliveryBoyMapsActivity.this,""+add+"     "+currentAddress,Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void startDrawingRoute() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(final Location location) {


                        if (a > 1) {

                            dbMarker.remove();

                            a -= 1;
                        }
                        a += 1;

                        dbLoc = new LatLng(location.getLatitude(), location.getLongitude());
                        dbMarker = mMap.addMarker(new MarkerOptions().position(dbLoc).title("Current Location"));


                                if (drawing == true && deliveryboyLoc == null)
                               {

                                        deliveryboyLoc = new LatLng(location.getLatitude(), location.getLongitude());
                                       deliveryBoyMarker = mMap.addMarker(new MarkerOptions().position(dbLoc).title("Starting point"));
                                       Toast.makeText(DeliveryBoyMapsActivity.this,"this is done",Toast.LENGTH_SHORT).show();
                                       getAddress(location.getLatitude(), location.getLongitude());
                                   }
                        try
                        {
                            if (compareLatLang != null)
                            {

                                line = mMap.addPolyline(new PolylineOptions()
                                        .add(new LatLng(dbLoc.latitude, dbLoc.longitude),
                                                new LatLng(compareLatLang.latitude, compareLatLang.longitude))
                                        .width(5).color(Color.BLUE).geodesic(true));

                            }
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        compareLatLang = new LatLng(location.getLatitude(), location.getLongitude());


                        lati = location.getLatitude();
                        longi = location.getLongitude();


                        list.add(longi);
                        list.add(lati);

//                                   else{
//                                    compareLatLang = new LatLng(location.getLatitude(), location.getLongitude());
//                                    Toast.makeText(DeliveryBoyMapsActivity.this,"this is not done",Toast.LENGTH_SHORT).show();
//
//                                    line = mMap.addPolyline(new PolylineOptions()
//                                                   .add(new LatLng(dbLoc.latitude, dbLoc.longitude),
//                                                           new LatLng(compareLatLang.latitude, compareLatLang.longitude))
//                                                    .width(5).color(Color.BLUE).geodesic(true));
//
//                                    lati = location.getLatitude();
//                                   longi = location.getLongitude();
//
//
//                                    list.add(longi);
//                                    list.add(lati);
//
//                              }
                        end.setOnClickListener(new View.OnClickListener()
                               {
                                    @Override
                                   public void onClick(View v)
                                    {
                                       try
                                       {

                                            final String adminId_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                            final DatabaseReference latLangRef = FirebaseDatabase.getInstance().getReference().child(currentAddress);


                                            latLangRef.setValue(list);

//                                            getAddress(location.getLatitude(),location.getLongitude());

                                           drawing=false;
                                           Toast.makeText(DeliveryBoyMapsActivity.this,"this is database done",Toast.LENGTH_SHORT).show();


                                       }catch (Exception e)
                                       {
                                           e.printStackTrace();
                                        }

                                    }
                                });
                    }
                });
    }

//
//                                    try
//                                    {
//                                        if (compareLatLang != null)
//                                        {
//
//                                            line = mMap.addPolyline(new PolylineOptions()
//                                                    .add(new LatLng(dbLoc.latitude, dbLoc.longitude),
//                                                            new LatLng(compareLatLang.latitude, compareLatLang.longitude))
//                                                    .width(5).color(Color.BLUE).geodesic(true));
//
//
//                                            Location location1 = new Location("");
//                                            location1.setLatitude(dbLoc.latitude);
//                                            location1.setLongitude(dbLoc.longitude);
//
//                                            Location location2 = new Location("");
//                                            location1.setLatitude(compareLatLang.latitude);
//                                            location1.setLongitude(compareLatLang.longitude);
//
//
//                                            float Distance = location1.distanceTo(location2);
//                                            distance += Distance/1000;
//                                            Toast.makeText(DeliveryBoyMapsActivity.this,"distance  "+distance,Toast.LENGTH_LONG).show();
//
//                                        }
//                                    } catch (Exception e)
//                                    {
//                                        e.printStackTrace();
//                                    }
//
//
//
//                                    compareLatLang = new LatLng(location.getLatitude(), location.getLongitude());
//
//
//                                    lati = location.getLatitude();
//                                    longi = location.getLongitude();
//
//
//                                    list.add(longi);
//                                    list.add(lati);
//
//
//                                }
//
//
//
//
//
//                                end.setOnClickListener(new View.OnClickListener()
//                                {
//                                    @Override
//                                    public void onClick(View v)
//                                    {
//                                        try
//                                        {
//
//
//                                            final String adminId_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                                            final DatabaseReference latLangRef = FirebaseDatabase.getInstance().getReference().child("latLanging");
//
//
//                                            latLangRef.setValue(list);
//
//                                            getAddress(location.getLatitude(),location.getLongitude());
//
//                                            drawing=false;
//
//
//                                        }catch (Exception e)
//                                        {
//                                            e.printStackTrace();
//                                        }
//
//                                    }
//                                });
//
//
//                            }
//
//                        }
//                );
//
//        showFirebaseData.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//
//            }
//        });
//    }
//
//   private void getAddress(double latitude, double longitude) {
//       Geocoder geocoder = new Geocoder(DeliveryBoyMapsActivity.this, Locale.getDefault());
//        try {
//
//            List<Address> addresses = null;
//
//
//           addresses = geocoder.getFromLocation(latitude, longitude, 1);
//            Address obj = addresses.get(0);
//
//            String add = obj.getAddressLine(0);
//            String currentAddress = obj.getSubAdminArea() + "," + obj.getAdminArea();
//
//
//           Toast.makeText(DeliveryBoyMapsActivity.this,""+add+"     "+currentAddress,Toast.LENGTH_LONG).show();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//                    }



}



