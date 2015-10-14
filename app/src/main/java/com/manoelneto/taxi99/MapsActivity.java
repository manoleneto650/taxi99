package com.manoelneto.taxi99;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.manoelneto.taxi99.model.Driver;
import com.manoelneto.taxi99.rest.IClientAppRest;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.rest.RestService;

import java.util.HashMap;

@EActivity(R.layout.activity_maps)
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    @RestService
    protected IClientAppRest serviceRest;

    private Driver[] taxistas;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mapa;

    private HashMap<String, Marker> taxistasNoMapa;
    private LatLngInterpolator latLngInterpolator;


    @AfterViews
    void afterViews() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        taxistasNoMapa = new HashMap<>();


        final Handler manipulador = new Handler(Looper.getMainLooper());
        manipulador.postDelayed(new Runnable() {
            @Override
            public void run() {
                getTodosTaxistas();
                manipulador.postDelayed(this, 2000);
            }
        }, 3000);

    }


    public void getTodosTaxistas() {
        LatLngBounds atual = mapa.getProjection().getVisibleRegion().latLngBounds;
        new AsyncTask<LatLngBounds, Void, Object>() {
            @Override
            protected Object doInBackground(LatLngBounds... params) {
                String sw = String.valueOf(params[0].southwest.latitude) + "," + String.valueOf(params[0].southwest.longitude);
                String ne = String.valueOf(params[0].northeast.latitude) + "," + String.valueOf(params[0].northeast.longitude);
                return serviceRest.getTaxistasDisponiveis(sw, ne);
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                taxistas = (Driver[]) o;
                DrawTaxista(taxistas, mapa);
            }
        }.execute(atual);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        mapa = map;
    }

    private void DrawTaxista(Driver[] taxitas, GoogleMap map) {
        latLngInterpolator = new LatLngInterpolator.Linear();
        for (int i = 0; i < taxitas.length; i++) {
            Driver taxi = taxitas[i];
            String taxiId = String.valueOf(taxi.getDriverId());
            if (taxistasNoMapa.containsKey(taxiId)) {
                Marker markerExistente = taxistasNoMapa.get(taxiId);
                MarkerAnimation.animateMarkerToGB(markerExistente, new LatLng(taxi.getLatitude(), taxi.getLongitude()), latLngInterpolator);
            } else {
                Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(taxi.getLatitude(), taxi.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_taxi))
                        .title("Taxista: " + String.valueOf(taxi.getDriverId())));

                latLngInterpolator = new LatLngInterpolator.Linear();
                taxistasNoMapa.put(taxiId, marker);
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mapa.setMyLocationEnabled(true);

            LatLng current = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mapa.addMarker(new MarkerOptions().position(current)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_here))
                            .title("Eu")
                            .draggable(true)
            );

            mapa.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(marker.getPosition()).zoom(17).build();
                    mapa.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            });

            CameraPosition myPosition = new CameraPosition.Builder().target(current).zoom(17).build();
            mapa.animateCamera(CameraUpdateFactory.newCameraPosition(myPosition));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

}
