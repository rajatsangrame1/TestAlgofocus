package com.algofocus.test.testalgofocus;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.login.LoginManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;

public class UserActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback {

    private ImageView mPicture;
    private TextView mId;
    private TextView mName;
    private TextView mEmail;
    private String id;
    private String first_name;
    private String last_name;
    private String email;
    private String picture_url;
    private Button logoutButton;
    private String TAG = getClass().getSimpleName();
    private LocationManager locationManager;
    private LatLng latLng;
    private GoogleMap googleMap;
    private MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Dummy LatLng in case GPS DATA is not available
        latLng = new LatLng(19.0760, 72.8777);

        // Mapping of Elements
        mPicture = findViewById(R.id.mPicture);
        mId = findViewById(R.id.mId);
        mName = findViewById(R.id.mName);
        mEmail = findViewById(R.id.mEmail);
        logoutButton = findViewById(R.id.logout_button);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        // Listeners & Callback Functions
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LoginManager.getInstance().logOut();
                SharedPreferences.Editor editor = getSharedPreferences("UserDetails", MODE_PRIVATE).edit();
                editor.clear();       // Clearing USER DATA from SharedPref on Logout Action
                editor.apply();

                startActivity(new Intent(UserActivity.this, LoginActivity.class));
                finish();
            }
        });
        mapFragment.getMapAsync(this);

        setFacebook();
        setGoogleMap();
    }

    private void setFacebook() {

        // Getting USER DATA from SharedPref
        SharedPreferences preferences = getSharedPreferences("UserDetails", MODE_PRIVATE);
        String jsonData = preferences.getString("json", "{}");

        try {
            JSONObject jsonObject = new JSONObject(jsonData);

            id = jsonObject.getString("id");
            email = jsonObject.get("email").toString();
            first_name = jsonObject.get("first_name").toString();
            last_name = jsonObject.get("last_name").toString();
            picture_url = jsonObject.getJSONObject("picture").getJSONObject("data").getString("url");

            Picasso.with(this).load(picture_url).into(mPicture);
            mId.setText(id);
            mName.setText(first_name + " " + last_name);
            mEmail.setText(email);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setGoogleMap() {

        // Wrapping data in a Try-Catch in case GPS DATA is not available
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
            onLocationChanged(location);
        } catch (Exception e) {
            Toast.makeText(this, "GPS is still loading, Try Again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        // Fetching GPS DATA info
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        Log.i(TAG, "onLocationChanged: " + latLng.toString());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    @Override
    public void onMapReady(GoogleMap map) {

        // Setting Google Map
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        try {
            googleMap.setMyLocationEnabled(true);
        } catch (SecurityException se) {
            se.printStackTrace();
        }

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        Marker placeMarker = googleMap.addMarker(new MarkerOptions().position(latLng) // LatLng updated in @onLocationChanged
                .title("Your Place"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 1000, null);
    }
}
