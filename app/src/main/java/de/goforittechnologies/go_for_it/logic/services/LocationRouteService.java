package de.goforittechnologies.go_for_it.logic.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;


public class LocationRouteService extends Service implements LocationListener {

    private static final String TAG = "LocationRouteService";

    private LocationManager mLocationManager;
    private ArrayList<LocationParcel> mRoute = new ArrayList<>();

    public LocationRouteService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "onCreate: Start");
        
        createNotification();

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (mLocationManager != null) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            }

            HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
            handlerThread.start();
            // Now get the Looper from the HandlerThread
            // NOTE: This call will block until the HandlerThread gets control and initializes its Looper
            Looper looper = handlerThread.getLooper();
            // Request location updates to be called back on the HandlerThread
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this, looper);
            Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            LocationParcel locationParcel = new LocationParcel(lastKnownLocation);
            mRoute.add(locationParcel);
            sendLocationMessageToActivity(mRoute);

        }

        mRoute.clear();

    }

    @Override
    public void onDestroy() {

        mLocationManager.removeUpdates(this);

        super.onDestroy();

    }


    @Override
    public void onLocationChanged(Location location) {

        Log.i(TAG, "Thread id: " + Thread.currentThread().getId());
        LocationParcel locationParcel = new LocationParcel(location);

        mRoute.add(locationParcel);
        sendLocationMessageToActivity(mRoute);

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

    private void sendLocationMessageToActivity(ArrayList<LocationParcel> route) {

        Intent locationIntent = new Intent("LocationUpdate");
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("Location", route);
        locationIntent.putExtra("Location", bundle);
        LocalBroadcastManager.getInstance(LocationRouteService.this).sendBroadcast(locationIntent);

    }

    private void createNotification() {

        Intent notificationIntent = new Intent(this, LocationRouteService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            String NOTIFICATION_CHANNEL_ID = "de.goforittechnologies.go_for_it";
            String channelName = "My Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(chan);


            notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("Test")
                    .setContentText("Background location service in foreground")
                    .setContentIntent(pendingIntent)
                    .setTicker("2")
                    .build();
        }

        startForeground(12345678, notification);

    }

}
