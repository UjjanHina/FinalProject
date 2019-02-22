package com.example.mahnoor.project1;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomerMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private ProgressDialog loadingbar;
    private EditText foodtype;
    private Button customerOrder;
    private Button searchNearestshopBtn;
    LocationRequest locationRequest;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    Marker customerMarker;
    LatLng customerLoc;
    int a = 1;
    private Boolean shopFound = false;
    private String shopFoundID;
    private int radius = 1;
    Location location2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        loadingbar = new ProgressDialog(this);
        foodtype = (EditText) findViewById(R.id.foodtypeTxt);
        foodtype.setVisibility(View.INVISIBLE);


        customerOrder = (Button) findViewById(R.id.order);
        customerOrder.setVisibility(View.INVISIBLE);
        customerOrder.setEnabled(false);
        searchNearestshopBtn = (Button) findViewById(R.id.searchBtn);
        searchNearestshopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingbar.setTitle("Searching for Resturant");
                loadingbar.setMessage("plz Wait, Search in progress");
                loadingbar.show();
                getNearestResturantAddress();
                searchNearestshopBtn.setVisibility(View.INVISIBLE);
            }
        });

        customerOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeCustomerOrderAndLocation();
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
        // Add a marker in Sydney and move the camera
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

            customerLocation();
        }
    }

    public void customerLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {


                                if (a > 1) {

                                    customerMarker.remove();

                                    a -= 1;
                                }
                                a += 1;


                                customerLoc = new LatLng(location.getLatitude(), location.getLongitude());
                                customerMarker = mMap.addMarker(new MarkerOptions().position(customerLoc).title("You are here"));


                            }
                        }
                );


    }

    private void getNearestResturantAddress() {


        final String customer_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference DriverLocationRef = FirebaseDatabase.getInstance().getReference().child("Admin data");


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        GeoFire geoFire = new GeoFire(DriverLocationRef);
                        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(customerLoc.latitude, customerLoc.longitude), radius);
                        geoQuery.removeAllListeners();
                        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                            @Override
                            public void onKeyEntered(String key, GeoLocation location) {
                                if (!shopFound) {


                                    shopFound = true;
                                    shopFoundID = key;


                                    loadingbar.dismiss();


                                    gettingShopLocation(shopFoundID, customerLoc);

                                    customerOrder.setVisibility(View.VISIBLE);
                                    customerOrder.setEnabled(true);
                                    foodtype.setVisibility(View.VISIBLE);

                                }
                            }

                            @Override
                            public void onKeyExited(String key) {

                            }

                            @Override
                            public void onKeyMoved(String key, GeoLocation location) {

                            }

                            @Override
                            public void onGeoQueryReady() {
                                if (!shopFound) {
                                    radius = radius + 1;


                                    getNearestResturantAddress();

                                }
                            }

                            @Override
                            public void onGeoQueryError(DatabaseError error) {

                            }
                        });

                    }
                });
    }

    private void gettingShopLocation(final String shopAddressFoundkey, final LatLng customersLoc) {
        DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference().child("Admin data");
        driverLocationRef.child(shopAddressFoundkey).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            //if (a > 1) {

                            // shopMarker.remove();

                            //   a -= 1;
                            // }
                            // a += 1;


                            List<Object> ResurantLocationMap = (List<Object>) dataSnapshot.getValue();

                            double LocationLat = 0;
                            double LocationLng = 0;


                            if (ResurantLocationMap.get(0) != null) {
                                LocationLat = Double.parseDouble(ResurantLocationMap.get(0).toString());
                            }
                            if (ResurantLocationMap.get(1) != null) {
                                LocationLng = Double.parseDouble(ResurantLocationMap.get(1).toString());
                            }

                            final LatLng ShopLatLng = new LatLng(LocationLat, LocationLng);

                            Location location1 = new Location("");

                            location1.setLatitude(customersLoc.latitude);
                            location1.setLongitude(customersLoc.longitude);
                            location2 = new Location("");
                            location2.setLatitude(ShopLatLng.latitude);
                            location2.setLongitude(ShopLatLng.longitude);
                            float Distance = location1.distanceTo(location2);
                            searchNearestshopBtn.setVisibility(View.VISIBLE);
                            float distance = Distance / 1000;

                            searchNearestshopBtn.setText(" " + distance + "Km");
                            Marker shopMarkers = mMap.addMarker(new MarkerOptions().position(ShopLatLng).title("Nearest Shop is Here"));
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    public void storeCustomerOrderAndLocation() {
        final String customer_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FusedLocationProviderClient mFusedLocationClient;
        final DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Customer Order");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {

                            final GeoFire geoFire = new GeoFire(customerRef);

                            geoFire.setLocation(customer_ID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {


                                @Override
                                public void onComplete(String key, DatabaseError error) {

                                    DatabaseReference driversRef = FirebaseDatabase.getInstance().getReference().child("Customer Order").child(customer_ID);
                                    HashMap driverMap = new HashMap();
                                    String foodType = foodtype.getText().toString();
                                    driverMap.put("foodOrder", foodType);
                                    driversRef.updateChildren(driverMap);
                                    customerOrder.setVisibility(View.INVISIBLE);
                                    foodtype.setVisibility(View.INVISIBLE);
                                    Toast.makeText(CustomerMapsActivity.this, "Food Ordered Sussesfully", Toast.LENGTH_LONG).show();
                                    searchNearestshopBtn.setVisibility(View.INVISIBLE);

                                    if (error != null) {
                                    } else {
                                    }


                                }

                            });

                        }
                    }

                });
    }
}






