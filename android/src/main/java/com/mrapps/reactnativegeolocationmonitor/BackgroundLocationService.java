package com.mrapps.reactnativegeolocationmonitor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.RequiresApi;

import android.util.Log;
import android.widget.Toast;

public class BackgroundLocationService extends Service {

  private final LocationServiceBinder mBinder = new LocationServiceBinder();
  private static final String TAG = "BackgroundLocService";
  private LocationListener mLocationListener;
  private LocationManager mLocationManager;
  public LocationMonitorListener mLocationMonitorListener;

  public BackgroundLocationService() {

  }

  public interface LocationMonitorListener {
    void onLocationChanged(Location location);

    void onReady();

    void onError(int code, String error);
  }

  private final int LOCATION_INTERVAL = 500;
  private final int LOCATION_DISTANCE = 10;

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }

  private class LocationListener implements android.location.LocationListener {
    private final String TAG = "LocationListener";
    private Location mLastLocation;
    private LocationMonitorListener mMonitorListener;

    public LocationListener(String provider, LocationMonitorListener monitorListener) {
      mLastLocation = new Location(provider);
      mMonitorListener = monitorListener;
      mMonitorListener.onLocationChanged(mLastLocation);
    }

    @Override
    public void onLocationChanged(Location location) {
      mLastLocation = location;
      mMonitorListener.onLocationChanged(location);
    }

    @Override
    public void onProviderDisabled(String provider) {
      Log.e(TAG, "onProviderDisabled: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
      Log.e(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      Log.e(TAG, "onStatusChanged: " + status);
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    return START_NOT_STICKY;
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public void onCreate() {
    Log.i(TAG, "onCreate");
    startForeground(12345678, getNotification());
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mLocationManager != null) {
      try {
        mLocationManager.removeUpdates(mLocationListener);
      } catch (Exception ex) {
        Log.i(TAG, "fail to remove location listeners, ignore", ex);
      }
    }
  }

  private void initializeLocationManager() {
    if (mLocationManager == null) {
      mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
      mLocationListener = new LocationListener(LocationManager.GPS_PROVIDER, mLocationMonitorListener);
    }
  }

  public void startTracking() {
    initializeLocationManager();

    try {
      mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListener);
      mLocationMonitorListener.onReady();
    } catch (java.lang.SecurityException ex) {
      Log.i(TAG, "Fail to request location update, ignore", ex);
      mLocationMonitorListener.onError(LocationError.INTERNAL_SERVICE_ERROR.getValue(), ex.getMessage());
    } catch (IllegalArgumentException ex) {
      Log.d(TAG, "GPS provider does not exist " + ex.getMessage());
      mLocationMonitorListener.onError(LocationError.GPS_PROVIDER_NOT_EXIST.getValue(), ex.getMessage());
    }

  }

  public void setLocationListener(LocationMonitorListener locationMonitorListener) {
    this.mLocationMonitorListener = locationMonitorListener;
  }

  public void stopTracking() {
    this.onDestroy();
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private Notification getNotification() {

    NotificationChannel channel = new NotificationChannel("channel_01", "Background location Service", NotificationManager.IMPORTANCE_DEFAULT);

    NotificationManager notificationManager = getSystemService(NotificationManager.class);
    notificationManager.createNotificationChannel(channel);

    Notification.Builder builder = new Notification.Builder(getApplicationContext(), "channel_01").setAutoCancel(true);
    return builder.build();
  }


  public class LocationServiceBinder extends Binder {
    public BackgroundLocationService getService() {
      return BackgroundLocationService.this;
    }
  }
}
