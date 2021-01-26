package com.cdap.safetyapp;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import com.cdap.safetyapp.inter.IOnLocationListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.cdap.safetyapp.inter.IOnLocationListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GeoQueryEventListener, IOnLocationListener {

    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentUser;
    private DatabaseReference myLocationRef;
    private GeoFire geoFire;
    private List<LatLng> dangerousArea;
    private com.cdap.safetyapp.inter.IOnLocationListener listener;

    private DatabaseReference myCity;
    private Location lastLocation;
    private GeoQuery geoQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

                        buildLocationRequest();
                        buildLocationCallback();
                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);



                        initArea();
                        settingGeoFire();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MapsActivity.this, "You must enable the permission", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

    }

    private void initArea() {

        myCity= FirebaseDatabase.getInstance()
                .getReference("DangerousArea")
                .child("MyCity");

        listener=this;
        //Load from firebase

       /* myCity .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<MyLatLng> latLngList= new ArrayList<>();
                        for (DataSnapshot locationSnapShot:snapshot.getChildren())
                        {
                            MyLatLng latLng = locationSnapShot.getValue(MyLatLng.class);
                            latLngList.add(latLng);

                        }
                        listener.onLoadLocationSuccess(latLngList);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onLoadLocationFailed(error.getMessage());

                    }
                });

        */
        myCity.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //update firebase
                List<MyLatLng> latLngList= new ArrayList<>();
                for (DataSnapshot locationSnapShot:snapshot.getChildren())
                {
                    MyLatLng latLng = locationSnapShot.getValue(MyLatLng.class);
                    latLngList.add(latLng);

                }
                listener.onLoadLocationSuccess(latLngList);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //After submit this area on firebase ,we will comment it
        /*



        FirebaseDatabase.getInstance()
                .getReference("DangerousArea")
                .child("MyCity")
                .setValue(dangerousArea)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MapsActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MapsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

         */
    }

    private void addUserMarker() {
        geoFire.setLocation("You", new GeoLocation(lastLocation.getLatitude(),
                lastLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (currentUser != null) currentUser.remove();
                currentUser = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lastLocation.getLatitude(),
                                lastLocation.getLongitude()))
                        .title("You"));

                //after add marker move camera
                mMap.animateCamera(CameraUpdateFactory
                        .newLatLngZoom(currentUser.getPosition(), 12.f));

            }
        });
    }

    private void settingGeoFire() {
        myLocationRef= FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire=new GeoFire(myLocationRef);


    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(final LocationResult locationResult) {
                if (mMap != null) {
                    lastLocation=locationResult.getLastLocation();

                    addUserMarker();



                }
            }
        };


    }

    private void buildLocationRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);


        if (fusedLocationProviderClient != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission( Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
            }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        //Add Circle for dangerous Area
        addCircleArea();

    }

    private void addCircleArea() {
        if (geoQuery != null)
        {
            geoQuery.removeGeoQueryEventListener(this);
            geoQuery.removeAllListeners();
        }

        for (LatLng latLng :dangerousArea)
        {
            mMap.addCircle(new CircleOptions().center(latLng)
                    .radius(500)//500m
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF) //22 is Transparent
                    .strokeWidth(5.0f)
            );

            //Create GeoQuery when user is dangerous are
            geoQuery=geoFire.queryAtLocation(new GeoLocation(latLng.latitude,latLng.longitude),0.5f);
            geoQuery.addGeoQueryEventListener(MapsActivity.this);
        }
    }


    @Override
    protected void onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        sendNotification("DID",String.format("%s Entered Dangerous Area",key));

    }



    @Override
    public void onKeyExited(String key) {
        sendNotification("DID",String.format("%s Leave Dangerous Area",key));

    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        sendNotification("DID",String.format("%s Move within Dangerous Area",key));

    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        Toast.makeText(this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();

    }

    private void sendNotification(String title, String content) {

        Toast.makeText(this, ""+content, Toast.LENGTH_SHORT).show();
        String NOTIFICATION_CHANNEL_ID ="DID_multiple_location";
        NotificationManager notificationManager =(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel=new NotificationChannel(NOTIFICATION_CHANNEL_ID,"My Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            //config

            notificationChannel.setDescription("Channel Description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentTitle(content)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));

        Notification notification= builder.build();
        notificationManager.notify(new Random().nextInt(),notification);
    }

    @Override
    public void onLoadLocationSuccess(List<MyLatLng> latLngs) {
        dangerousArea=new ArrayList<>();
        for (MyLatLng myLatLng :latLngs)
        {
            LatLng convert= new LatLng(myLatLng.getLatitude(),myLatLng.getLongitude());
            dangerousArea.add(convert);
        }
        //Now after dangerous area is have data we will call map display
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);


        //clear map and add Again
        if (mMap != null)
        {
            mMap.clear();
            //add user marker
            addUserMarker();
            //add Circle Danger area
            addCircleArea();
        }

    }

    @Override
    public void onLoadLocationFailed(String message) {
        Toast.makeText(this, ""+message, Toast.LENGTH_SHORT).show();

    }
}