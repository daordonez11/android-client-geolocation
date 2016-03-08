package com.example.danielordonez.labredes5;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.rey.material.widget.Button;
import com.rey.material.widget.EditText;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, LocationListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final long ONE_MIN = 1000 * 60;
    private static final long TWO_MIN = ONE_MIN * 2;
    private static final long FIVE_MIN = ONE_MIN * 5;
    private static final long POLLING_FREQ = 1000 * 30;
    private static final long FASTEST_UPDATE_FREQ = 1000 * 5;
    private static final float MIN_ACCURACY = 25.0f;
    private static final float MIN_LAST_READ_ACCURACY = 500.0f;
    private ThreadComunicacion tc;
    private Button emp, fin, limp;
    private RadioButton tcp, udp;
    private TextView log;
    private  GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mBestReading;
    private EditText puerto, ipadd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        log = (TextView)findViewById(R.id.tvMessage);
        emp = (Button)findViewById(R.id.btnStart);
        fin = (Button)findViewById(R.id.btnEnd);
        limp = (Button)findViewById(R.id.btnLimpiar);
        tcp = (RadioButton)findViewById(R.id.btnTCP);
        udp = (RadioButton)findViewById(R.id.btnUDP);
        puerto = (EditText)findViewById(R.id.etPort);
        ipadd = (EditText)findViewById(R.id.etServerIP);
        emp.setOnClickListener(this);
        fin.setOnClickListener(this);
        limp.setOnClickListener(this);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(POLLING_FREQ);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_FREQ);
        if (mGoogleApiClient == null) {
    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();
}



    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btnStart)
        {
            if(tcp.isChecked())
            {
                log.setText("Comienza proceso TCP");
                tc = new ThreadComunicacion("TCP", ipadd.getText().toString(),Integer.parseInt(puerto.getText().toString()),findViewById(R.id.acciones),mGoogleApiClient);
                tc.start();
            }
            else
            {

            }
        }
        if(v.getId()==R.id.btnEnd)
        {
            if(tcp.isChecked())
            {
                log.setText(log.getText()+"\nTerminando conexión");
                tc.terminarConexion();
                tc.interrupt();
            }
            else
            {

            }
        }
        if(v.getId()==R.id.btnLimpiar)
        {
            log.setText("");
            //if (ContextCompat.checkSelfPermission(this,
              //      Manifest.permission.ACCESS_FINE_LOCATION)
                //    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?


                    // No explanation needed, we can request the permission.

                 //   ActivityCompat.requestPermissions(this,
                   //         new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                     //       1);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.

          //  }
         //   LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
           // try{
             //   Location a =   mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
               // if(a!=null)
                //{
                 //   Snackbar snackbar = Snackbar
                 //           .make(findViewById(R.id.acciones), "Prueba: "+a.getLatitude(), Snackbar.LENGTH_SHORT);

                  //  snackbar.show();
//                }

            //   }
            //catch(SecurityException e)
            //{
            //    Snackbar snackbar = Snackbar
            //            .make(findViewById(R.id.acciones), e.getMessage(), Snackbar.LENGTH_SHORT);

//                snackbar.show();
  //          }
        }
    }
    public void setMessage(final String mess)
    {
        log.setText(log.getText() + "mess");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
   protected void onStart() {
    mGoogleApiClient.connect();
    super.onStart();
    }
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {

        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.acciones), "Conectado a google", Snackbar.LENGTH_SHORT);

        snackbar.show();

        if (servicesAvailable()) {
            // Get best last location measurement meeting criteria
            mBestReading = bestLastKnownLocation(MIN_LAST_READ_ACCURACY, FIVE_MIN);

            if (null == mBestReading
                    || mBestReading.getAccuracy() > MIN_LAST_READ_ACCURACY
                    || mBestReading.getTime() < System.currentTimeMillis() - TWO_MIN) {
                try{
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

                }
                catch (SecurityException e)
                {

                }

                // Schedule a runnable to unregister location listeners
                Executors.newScheduledThreadPool(1).schedule(new Runnable() {

                    @Override
                    public void run() {
                        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MainActivity.this);
                    }

                }, ONE_MIN, TimeUnit.MILLISECONDS);
            }
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
private Location bestLastKnownLocation(float minAccuracy, long minTime) {
    Location bestResult = null;
    float bestAccuracy = Float.MAX_VALUE;
    long bestTime = Long.MIN_VALUE;

    // Get the best most recent location currently available
    Location mCurrentLocation = null;
    try {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    } catch (SecurityException e) {

    }


    if (mCurrentLocation != null) {
        float accuracy = mCurrentLocation.getAccuracy();
        long time = mCurrentLocation.getTime();

        if (accuracy < bestAccuracy) {
            bestResult = mCurrentLocation;
            bestAccuracy = accuracy;
            bestTime = time;
        }
    }

    // Return best reading or null
    if (bestAccuracy > minAccuracy || bestTime < minTime) {
        return null;
    } else {
        return bestResult;
    }
}
    private boolean servicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        }
        else {
            GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0).show();
            return false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }


}
