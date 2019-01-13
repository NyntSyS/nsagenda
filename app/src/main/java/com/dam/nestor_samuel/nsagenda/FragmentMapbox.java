package com.dam.nestor_samuel.nsagenda;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class FragmentMapbox extends Fragment {

    private OnFragmentInteractionListener mListener;
    private String nick;

    private MapView mapView;
    private CameraPosition position;
    private MapboxMap mbm;
    private LocationManager lm;

    private TextView tv_error;
    private Button btn_actualizar;

    public FragmentMapbox() {
        // Required empty public constructor
    }

    public static FragmentMapbox newInstance(String nick) {

        FragmentMapbox fragment = new FragmentMapbox();
        Bundle args = new Bundle();
        args.putString("Nick", nick);
        fragment.setArguments(args);

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nick = getArguments().getString("Nick");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Mapbox.getInstance(getContext(), "pk.eyJ1IjoibnN5cyIsImEiOiJjanBqdTNjc3gwOXQ2M3dxZGV2eGM5ZnRoIn0.fVMUE1Uu9n9yhfYYqwFLbA");
        View view = inflater.inflate(R.layout.fragment_mapbox, container, false);

        tv_error = view.findViewById(R.id.fMapBox_tv_error);
        btn_actualizar = view.findViewById(R.id.fMapBox_btn_actualizar);
        mapView = (MapView) view.findViewById(R.id.fMapBox_mapbox);
        mapView.onCreate(savedInstanceState);

        if(gpsActivado()) {
            mostrarPosicion();
        }
        else {
            mapView.setVisibility(View.GONE);
            tv_error.setVisibility(View.VISIBLE);
        }

        btn_actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!gpsActivado()) {    //  Si no está el GPS activado no hace nada
                    mapView.setVisibility(View.GONE);
                    tv_error.setVisibility(View.VISIBLE);
                    return;
                }

                mapView.setVisibility(View.VISIBLE);
                tv_error.setVisibility(View.GONE);

                Location location;
                double latitud;
                double longitud;

                try {
                    location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    latitud = location.getLatitude();
                    longitud = location.getLongitude();

                    position = new CameraPosition.Builder()
                            .target(new LatLng(latitud, longitud))
                            .zoom(10)
                            .tilt(20)
                            .build();

                    mbm.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2000);
                }
                catch (SecurityException se) { }

            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private boolean gpsActivado() {

        boolean gpsActivado = false;
        boolean networkActivada = false;
        lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        try {
            gpsActivado = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            networkActivada = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        return (gpsActivado && networkActivada);

    }

    private void mostrarPosicion() {

        Location location;
        double latitud;
        double longitud;

        try {
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            latitud = location.getLatitude();
            longitud = location.getLongitude();

            position = new CameraPosition.Builder()
                    .target(new LatLng(latitud, longitud))
                    .zoom(10)
                    .tilt(20)
                    .build();

            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapboxMap mapboxMap) {
                    mbm = mapboxMap;

                    mbm.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2000);

                    mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitud, longitud))
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
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
