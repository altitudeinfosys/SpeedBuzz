package com.altitudeinfosys.speedbuzz.util;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.altitudeinfosys.speedbuzz.ui.AnalogActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import static android.transition.Fade.IN;
import static com.altitudeinfosys.speedbuzz.ui.AnalogActivity.latitude;
import static com.altitudeinfosys.speedbuzz.ui.AnalogActivity.longitude;

/**
 * Created by Tarek on 1/3/2017.
 */

public class LocationServiceAnalog extends Service implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final long INTERVAL = 1000 * 2;
    private static final long FASTEST_INTERVAL = 1000 * 1;
    private final String url = "http://route.st.nlp.nokia.com/routing/7.2/getlinkinfo.json?app_id=hJqvPuB3ckMdMO5CYqB5&app_code=VAi7QI3KE3vO3oOyiyO8uw&waypoint=";

    public static final String TAG = LocationServiceAnalog.class.getSimpleName();

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    LocationListener mLocationListener;

    Location mCurrentLocation, lStart, lEnd;
    static double distance = 0;
    double speed;
    double speedMph;


    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        return mBinder;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onConnected(Bundle bundle) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
        }
    }


    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        distance = 0;
    }


    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onLocationChanged(Location location) {
        AnalogActivity.locate.dismiss();

        Log.i(TAG,"Progress is off !");

        mCurrentLocation = location;
        if (lStart == null) {
            lStart = mCurrentLocation;
            lEnd = mCurrentLocation;
        } else
            lEnd = mCurrentLocation;

        //Calling the method below updates the  live values of distance and speed to the TextViews.
        updateUI();
        //calculating the speed with getSpeed method it returns speed in m/s so we are converting it into kmph
        speed = location.getSpeed() * 18 / 5;
        speedMph = speed / 1.609344;

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public class LocalBinder extends Binder {

        public LocationServiceAnalog getService() {
            return LocationServiceAnalog.this;
        }


    }

    //The live feed of Distance and Speed are being set in the method below .
    private void updateUI() {
        if (AnalogActivity.p == 0) {
            distance = distance + (lStart.distanceTo(lEnd) / 1000.00);
            AnalogActivity.endTime = System.currentTimeMillis();
            long diff = AnalogActivity.endTime - AnalogActivity.startTime;
            diff = TimeUnit.MILLISECONDS.toMinutes(diff);
            AnalogActivity.time.setText(diff + " minutes"); //total time
            String longitude="", latitude="";

            if (mCurrentLocation!=null) {
                longitude = String.valueOf(mCurrentLocation.getLongitude());
                latitude = String.valueOf(mCurrentLocation.getLatitude());
            }




            if (speed > 0.0) {
                //AnalogActivity.speedKPH = new DecimalFormat("#.##").format(speed);
                //AnalogActivity.speedMPH =  new DecimalFormat("#.##").format(speedMph);
                //AnalogActivity.speedLimit = "Speed Limit: ";

                AnalogActivity.longitude.setText(longitude);
                AnalogActivity.latitude.setText(latitude);
                AnalogActivity.speedometer.speedTo((int)speedMph);
                AnalogActivity.getSpeedLimit(url+latitude+","+longitude);
            }
            else{
                //AnalogActivity.speedKPH =  new DecimalFormat("#.##").format(speed);
                //AnalogActivity.speedMPH= new DecimalFormat("#.##").format(speedMph);
                //AnalogActivity.speedLimit.setText("Speed Limit: " + 0);
                AnalogActivity.longitude.setText(longitude);
                AnalogActivity.latitude.setText(latitude);
                AnalogActivity.speedometer.speedTo(0);
                AnalogActivity.getSpeedLimit(url+latitude+","+longitude);
            }


            AnalogActivity.dist.setText(new DecimalFormat("#.###").format(distance) + " Km's.");

            lStart = lEnd;

        }

    }


    @Override
    public boolean onUnbind(Intent intent) {
        stopLocationUpdates();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        lStart = null;
        lEnd = null;
        distance = 0;
        return super.onUnbind(intent);
    }
}
