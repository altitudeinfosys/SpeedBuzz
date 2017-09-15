package com.altitudeinfosys.speedbuzz.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;



import com.altitudeinfosys.speedbuzz.R;
import com.altitudeinfosys.speedbuzz.ui.base.BaseActivity;
import com.altitudeinfosys.speedbuzz.util.LocationServiceAnalog;
import com.github.anastr.speedviewlib.Speedometer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.R.attr.data;
import static android.R.color.black;

public class AnalogActivity extends BaseActivity {




    LocationServiceAnalog myService;
    LocationManager locationManager;


    static public Speedometer speedometer;
    static public TextView dist, time, speedKPH, speedMPH, speedLimit, longitude, latitude;
    static public boolean status;
    static public long startTime, endTime;
    static public ProgressDialog locate;
    static public int p = 0;
    static public boolean paused = false;

    ImageButton start, pause, stop , playpause;
    ImageView image;


    public static final String TAG = AnalogActivity.class.getSimpleName();

/*    @BindView(R.id.longitudeText) public static TextView tv_longitude;
    @BindView(R.id.latitudeText) public static TextView tv_latitude;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analog);



        dist = (TextView) findViewById(R.id.distancetext);
        time = (TextView) findViewById(R.id.timetext);
        /*
        speedKPH = (TextView) findViewById(R.id.speedtextKMH);
        speedMPH = (TextView) findViewById(R.id.speedtextMPH);
        speedLimit = (TextView) findViewById(R.id.speedLimit);
        image = (ImageView) findViewById(R.id.image);
        */
// Show the Up button in the action bar.
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        longitude = (TextView) findViewById(R.id.longitudeText);
        latitude = (TextView) findViewById(R.id.latitudeText);
        speedLimit = (TextView) findViewById(R.id.speedLimit);
        speedometer = (Speedometer)findViewById(R.id.speedView);
        start = (ImageButton) findViewById(R.id.start);
        pause = (ImageButton) findViewById(R.id.pause);
        stop = (ImageButton) findViewById(R.id.stop);
        playpause = (ImageButton)findViewById(R.id.play_pause);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setupToolbar();

        ButterKnife.bind(this);
    }


   /* @OnClick(R.id.btnSpeed)
    public void showSpeed(View view) {
        // TODO submit data to server...
        speedometer.setUnit("M/h");
        speedometer.setSpeedTextSize(105f);
        speedometer.setMinSpeed(0);
        speedometer.setMaxSpeed(120);
        speedometer.setWithTremble(true);
        speedometer.speedTo(50);
    }*/

    @OnClick(R.id.start)
    public void start(View view) {
        // TODO submit data to server...

        //The method below checks if Location is enabled on device or not. If not, then an alert dialog box appears with option
        //to enable gps.
        checkGps();
        requestGpsPermission();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            return;
        }


        if (status == false)
            //Here, the Location Service gets bound and the GPS Speedometer gets Active.
            bindService();

        locate = new ProgressDialog(AnalogActivity.this);
        locate.setIndeterminate(true);
        locate.setCancelable(true);
        locate.setMessage("Getting Location...");
        locate.show();
        Log.i(TAG,"Progress is on !");
        start.setVisibility(View.GONE);
        pause.setVisibility(View.VISIBLE);
        //pause.setText("Pause");
        playpause.setVisibility(View.GONE);
        stop.setVisibility(View.VISIBLE);

        speedometer.setUnit("M/h");
        speedometer.setSpeedTextSize(105f);
        speedometer.setMinSpeed(0);
        speedometer.setMaxSpeed(120);
        speedometer.setWithTremble(true);

       /* if (speedMPH!=null)
        if (!(speedMPH.getText().toString().isEmpty())) {
            Log.i(TAG," Speed is " + speedMPH);
            speedometer.speedTo(Integer.parseInt(speedMPH));

        }
        else{
            speedometer.speedTo(0);
            Log.i(TAG," Speed is 0");
        }
*/
        final Animation animation = new TranslateAnimation(0,100,0,0);
        // set Animation for 5 sec
        animation.setDuration(5000);
        //for button stops in the new position.
        animation.setFillAfter(true);
        start.startAnimation(animation);

    }

    @OnClick(R.id.pause)
    public void pause(View view) {
        // TODO submit data to server...

        if (p==0)
        {
            p=1;
            pause.setVisibility(View.GONE);
            playpause.setVisibility(View.VISIBLE);
            stop.setVisibility(View.VISIBLE);

        }
        else if (p==1)
        {
            checkGps();
            requestGpsPermission();
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(getApplicationContext(), "GPS is Enabled in your devide", Toast.LENGTH_LONG).show();
                return;
            }
            pause.setVisibility(View.VISIBLE);
            playpause.setVisibility(View.GONE);
            stop.setVisibility(View.VISIBLE);
            p = 0;

        }

        /*if (pause.getText().toString().equalsIgnoreCase("pause")) {
            pause.setText("Resume");
            p = 1;

        } else if (pause.getText().toString().equalsIgnoreCase("Resume")) {
            checkGps();
            requestGpsPermission();
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(getApplicationContext(), "GPS is Enabled in your devide", Toast.LENGTH_LONG).show();
                return;
            }
            pause.setText("Pause");
            p = 0;

        }*/
    }

    @OnClick(R.id.stop)
    public void stop(View view) {
        // TODO submit data to server...
        if (status == true)
            unbindService();
        start.setVisibility(View.VISIBLE);
        //pause.setText("Pause");
        pause.setVisibility(View.GONE);
        stop.setVisibility(View.GONE);
        p = 0;
    }

    public static void getSpeedLimit(String url)
    {
        GetSpeedLimit limit = new GetSpeedLimit();
        limit.execute(url);

    }
    public static class GetSpeedLimit extends AsyncTask<String, Void, String> {

        @Override
        protected String t(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);

                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {

                    char current = (char) data;

                    result += current;

                    data = reader.read();

                }

                return result;

            } catch (Exception e) {

                Log.i(TAG, "couldn't retrieve speed limit");

            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {

                String message = "";
                String currentSpeedLimit = "";

                JSONObject jsonObject = new JSONObject(result);

                JSONObject jsonPart = jsonObject.getJSONObject("response");

                JSONArray extraInfo = jsonPart.getJSONArray("link");

                for(int i=0;i<extraInfo.length();++i) {
                    JSONObject jsonSpeedLimit = extraInfo.getJSONObject(i);
                    currentSpeedLimit = jsonSpeedLimit.getString("speedLimit");

                }

                float speedLimitNow = Float.parseFloat(currentSpeedLimit);

                speedLimitNow = Math.round(speedLimitNow * 2.23f);

                currentSpeedLimit = Float.toString(speedLimitNow);

                Log.i("Speed Limit Retrieved", currentSpeedLimit);

                speedLimit.setText("Speed Limit : " + (currentSpeedLimit));

                speedLimit.setTextSize(20f);



            } catch (JSONException e) {

                Log.i(TAG, "Could not find sped limit");

            }



        }
    }

    //Requesting permission
    private void requestGpsPermission(){

        // If device is running SDK < 23

        if (Build.VERSION.SDK_INT < 23) {

            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // ask for permission

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


            }

        }
    }

    //This method leads you to the alert dialog box.
    void checkGps() {


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {


            showGPSDisabledAlertToUser();
        } else
            Toast.makeText(getApplicationContext(), "GPS is Enabled in your devide", Toast.LENGTH_LONG).show();


    }

    //This method configures the Alert Dialog box.
    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Enable GPS to use application")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    private void setupToolbar() {
        final ActionBar ab = getActionBarToolbar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_samples;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }




    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationServiceAnalog.LocalBinder binder = (LocationServiceAnalog.LocalBinder) service;
            myService = binder.getService();
            status = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            status = false;
        }
    };

    void bindService() {
        if (status == true)
            return;
        Intent i = new Intent(getApplicationContext(), LocationServiceAnalog.class);
        bindService(i, sc, BIND_AUTO_CREATE);
        status = true;
        startTime = System.currentTimeMillis();
    }

    void unbindService() {
        if (status == false)
            return;
        Intent i = new Intent(getApplicationContext(), LocationServiceAnalog.class);
        unbindService(sc);
        status = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (status == true)
            unbindService();
    }

    @Override
    public void onBackPressed() {
        if (status == false)
            super.onBackPressed();
        else
            moveTaskToBack(true);
    }



}