package com.example.mahnoor.project1;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.Task;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.annotation.Retention;
import java.util.List;

public class AdminMapsActivity extends FragmentActivity implements OnMapReadyCallback ,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener

{

    //private GoogleMap mMap;
    //private FusedLocationProviderClient fusedLocationProviderClient;
    //private Boolean mLocationPermissionGranted = false;





    private GoogleMap mMap;

    private Button recieveCustOrder;
    private Button orderRecieved;
    private Button createDeliveryBoyAccount;
    LatLng adminLoc;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    Location location2;
    private String customerKey = "";
    private String custId2 = "";
    private int checkCustId2=0;
    private String foodOrdered = "";
    private LatLng customerLatLng;
    LocationRequest locationRequest;
    private ProgressDialog loadingbar;
    private FusedLocationProviderClient mFusedLocationClient;
    Marker adminMarker;
    private FirebaseAuth mAuth;
    int a = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        recieveCustOrder=(Button)findViewById(R.id.recieveOrder);
        recieveCustOrder.setVisibility(View.INVISIBLE);
        recieveCustOrder.setEnabled(false);
        createDeliveryBoyAccount=(Button)findViewById(R.id.createDlvrboyAccount);
        createDeliveryBoyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerDeliveryBoyAlertDialog();
            }
        });
        recieveCustOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFoodOrder();
                getCustomerLocation();
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

            return;
        }
        mMap.setMyLocationEnabled(true);


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


    protected synchronized void buildGoogleApiClient() {

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();


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

            adminLocation();
            storeAdminLocation();
        }
    }





    public void storeAdminLocation() {

//In this line we get the device uid code

        final String adminId_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final DatabaseReference adminAvailibilityRef = FirebaseDatabase.getInstance().getReference().child("Admin data");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {


                        if (location != null) {

                            final GeoFire geoFire = new GeoFire(adminAvailibilityRef);

                            geoFire.setLocation(adminId_ID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {


                                @Override
                                public void onComplete(String key, DatabaseError error) {


                                    if (error != null) {
                                    } else {
                                    }


                                }
                            });



                        }
                    }
                });


    }


    public void adminLocation() {


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {


                                if (a > 1) {

                                    adminMarker.remove();

                                    a -= 1;
                                }
                                a += 1;


                                adminLoc = new LatLng(location.getLatitude(), location.getLongitude());

                                adminMarker = mMap.addMarker(new MarkerOptions().position(adminLoc).title("You are here"));


                            }
                        }
                );


    }

    private void getCustomerOrder(){

        final DatabaseReference AssignedLowestCustomerKeyRef = FirebaseDatabase.getInstance().getReference().child("Customer order");
        AssignedLowestCustomerKeyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                String key=dataSnapshot.getValue().toString();
                //Toast.
                String[] keys=key.split("=",2);

                String customersKey=keys[0];
                String custkey=customersKey.replaceAll("[\\(\\)\\=\\[\\]\\{\\}]", "");
                customerKey = custkey;

                if(! customersKey.equals(" ")){
                    recieveCustOrder.setEnabled(true);
                    recieveCustOrder.setVisibility(View.VISIBLE);
                }
                    if (checkCustId2 == 0) {
                        custId2 = customerKey;


                        checkCustId2 = 1;
                    }


                    if (!customerKey.equals(custId2)) {

                        custId2 = customerKey;
                        checkCustId2 = 0;


                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void getFoodOrder() {

        final DatabaseReference AssignedLowestCustomerRef2 = FirebaseDatabase.getInstance().getReference().child("Customer Order").child(customerKey).child("foodOrder");
        AssignedLowestCustomerRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {


                    foodOrdered = dataSnapshot.getValue().toString();


                    Toast.makeText(AdminMapsActivity.this, "   = " + foodOrdered, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getCustomerLocation() {


        final DatabaseReference customerLoc = FirebaseDatabase.getInstance().getReference().child("Customer Order");
        customerLoc.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    List<Object> customerLocationMap = (List<Object>) dataSnapshot.getValue();


                    double LocationLat = 0;
                    double LocationLng = 0;


                    if (customerLocationMap.get(0) != null) {
                        LocationLat = Double.parseDouble(customerLocationMap.get(0).toString());


                    }
                    if (customerLocationMap.get(1) != null) {
                        LocationLng = Double.parseDouble(customerLocationMap.get(1).toString());


                    }

                    customerLatLng = new LatLng(LocationLat, LocationLng);
                    Marker custLocMarker = mMap.addMarker(new MarkerOptions().position(customerLatLng).title("Customer is here"));

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void registerDeliveryBoyAlertDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setCancelable(false);
        alertDialog.setTitle("My App");
        alertDialog.setMessage("Registeration");



        final EditText password = new EditText(this);
        password.setInputType(InputType.TYPE_CLASS_PHONE);
        password.setRawInputType(Configuration.KEYBOARD_12KEY);
        password.setHint("Enter password");
        alertDialog.setView(password);

        alertDialog.setButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                registerDeliveryBoy("sajbjfb@gamil.com", "ssss");
            }
        });

        alertDialog.show();
    }

    private void registerDeliveryBoy(String email, String password) {
        if (TextUtils.isEmpty(email)) {

            Toast.makeText(AdminMapsActivity.this, "Plz write email...", Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(AdminMapsActivity.this, "Plz write password...", Toast.LENGTH_LONG).show();


        } else {


            loadingbar.setTitle(" Driver  Registring...");
            loadingbar.setMessage("plz Wait ,Driver is Registring....");
            loadingbar.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        Toast.makeText(AdminMapsActivity.this, "successfully registered...", Toast.LENGTH_LONG).show();

                        loadingbar.dismiss();

                    } else {

                        Toast.makeText(AdminMapsActivity.this, "ergisteration failed try again...", Toast.LENGTH_LONG).show();
                        loadingbar.dismiss();
                    }
                }
            });


        }

    }









    }
