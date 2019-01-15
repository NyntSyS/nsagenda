package com.dam.nestor_samuel.nsagenda;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class ActivityMapbox extends AppCompatActivity {

    private String nick;

    private MapView mapView;
    private MapboxMap mbm;
    private CameraPosition cameraPosition;
    private TextView tv_error;
    private Button btn_actualizar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoibnN5cyIsImEiOiJjanBqdTNjc3gwOXQ2M3dxZGV2eGM5ZnRoIn0.fVMUE1Uu9n9yhfYYqwFLbA");
        setContentView(R.layout.activity_mapbox);

        Bundle bundle = getIntent().getExtras();
        nick = bundle.getString("Nick");

        btn_actualizar = findViewById(R.id.aMapBox_btn_actualizar);
        tv_error = findViewById(R.id.aMapBox_tv_error);
        mapView = (MapView) findViewById(R.id.aMapBox_mapbox);
        mapView.onCreate(savedInstanceState);

        if(ActivityMain.gpsAccesible) {
            btn_actualizar.setEnabled(true);
            posicionInicial();
        }
        else {
            mapView.setVisibility(View.GONE);
            tv_error.setVisibility(View.VISIBLE);
        }


        btn_actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(!ActivityMain.gpsAccesible) {    //  Si no está el GPS activado no hace nada
                        btn_actualizar.setEnabled(false);
                        mapView.setVisibility(View.GONE);
                        tv_error.setVisibility(View.VISIBLE);
                        return;
                    }

                    btn_actualizar.setEnabled(true);
                    mapView.setVisibility(View.VISIBLE);
                    tv_error.setVisibility(View.GONE);

                    cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(ActivityMain.latitud, ActivityMain.longitud))
                            .zoom(10)
                            .tilt(20)
                            .build();

                    mbm.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000);

                    mbm.removeAnnotations();
                    mbm.addMarker(new MarkerOptions()
                            .position(new LatLng(ActivityMain.latitud, ActivityMain.longitud))
                            .title(nick)
                            .snippet("Ahora estoy aquí"));
                }
                catch (SecurityException se) { }
            }
        });
    }

    private void posicionInicial() {

        try {
            cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(ActivityMain.latitud, ActivityMain.longitud))
                    .zoom(10)
                    .tilt(20)
                    .build();

            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapboxMap mapboxMap) {
                    mbm = mapboxMap;

                    mbm.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000);

                    mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(ActivityMain.latitud, ActivityMain.longitud))
                            .title(nick)
                            .snippet("Posición inicial"));
                }
            });

        }
        catch (SecurityException se) { }

    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
