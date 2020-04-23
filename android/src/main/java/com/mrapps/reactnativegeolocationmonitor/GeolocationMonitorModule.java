package com.mrapps.reactnativegeolocationmonitor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import java.util.HashMap;

public class GeolocationMonitorModule extends ReactContextBaseJavaModule implements BackgroundLocationService.LocationMonitorListener, LifecycleEventListener {

  private final ReactApplicationContext mReactContext;
  private BackgroundLocationService mGpsService;

  private Location lastLocation;
  private Callback lastLocationCallback;
  private boolean onlyOnce;
  private boolean startTrackingRequested;
  private boolean initializing;


  public GeolocationMonitorModule(ReactApplicationContext reactContext) {
    super(reactContext);
    mReactContext = reactContext;
    mReactContext.addLifecycleEventListener(this);
    this.initializing = false;
    this.onlyOnce = false;
    this.startTrackingRequested = true;
  }

  private void init() {
    if (!this.initializing) {
      this.initializing = true;
      final Intent intent = new Intent(mReactContext, BackgroundLocationService.class);
      mReactContext.startService(intent);
      mReactContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
  }

  private ServiceConnection serviceConnection = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder service) {
      String name = className.getClassName();
      if (name.endsWith("BackgroundLocationService")) {
        mGpsService = ((BackgroundLocationService.LocationServiceBinder) service).getService();
        mGpsService.setLocationListener(GeolocationMonitorModule.this);
        initializing = false;
        if (startTrackingRequested) {
          startTrackingRequested = false;
          mGpsService.startTracking();
        }
      }
    }

    public void onServiceDisconnected(ComponentName className) {
      if (className.getClassName().equals("BackgroundLocationService")) {
        mGpsService = null;
        initializing = false;
        //TODO: verify if restart is needed
      }
    }
  };

  private void sendEvent(String eventName,
                         @Nullable WritableMap params) {
    mReactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

  @Override
  public String getName() {
    return "GeolocationMonitor";
  }

  private HashMap<String, Double> mapLocation(Location location) {
    HashMap<String, Double> mappedLocation = new HashMap<>();
    mappedLocation.put("latitude", location.getLatitude());
    mappedLocation.put("longitude", location.getLongitude());
    return mappedLocation;
  }

  private void startTrackingOnlyOnce(boolean onlyOnce) {
    this.onlyOnce = onlyOnce;
    // Check for permission
    if (LocationUtils.hasLocationPermission(mReactContext)) {

      if (mGpsService == null) {
        this.init();
      }

    } else if (!LocationUtils.isLocationEnabled(mReactContext)) {
      WritableMap params = Arguments.createMap();
      params.putInt("code", LocationError.LOCATION_NOT_ENABLED.getValue());
      params.putString("message", "geolocation_android_permissions_denied");
      sendEvent("LocationError", params);
    } else {


      WritableMap params = Arguments.createMap();
      params.putInt("code", LocationError.PERMISSIONS_DENIED.getValue());
      params.putString("message", "geolocation_android_permissions_denied");
      sendEvent("LocationError", params);
    }
  }

  @ReactMethod
  public void startTracking() {
    this.startTrackingOnlyOnce(false);
  }

  @ReactMethod
  public void stopTracking() {
    if (mGpsService != null) {
      mGpsService.stopTracking();
      mGpsService = null;
    }
  }

  @ReactMethod
  public void getCurrentLocation(Callback callback) {
    lastLocationCallback = callback;

    if (mGpsService == null) {
      this.startTrackingOnlyOnce(true);
    }
  }

  @Override
  public void onLocationChanged(Location location) {

    this.lastLocation = location;

    if (onlyOnce) {
      this.stopTracking();
    }

    if (this.lastLocationCallback != null) {
      this.lastLocationCallback.invoke(this.mapLocation(this.lastLocation));
      this.lastLocationCallback = null;
    } else if (!onlyOnce) {
      WritableMap params = Arguments.createMap();
      params.putString("latitude", String.valueOf(location.getLatitude()));
      params.putString("longitude", String.valueOf(location.getLongitude()));
      sendEvent("LocationUpdated", params);
    }
  }

  @Override
  public void onReady() {
  }

  @Override
  public void onError(int code, String errorMessage) {
    WritableMap params = Arguments.createMap();
    params.putInt("code", code);
    params.putString("message", errorMessage);
    sendEvent("LocationError", params);
  }

  @Override
  public void onHostResume() {

  }

  @Override
  public void onHostPause() {

  }

  @Override
  public void onHostDestroy() {

    final Intent intent = new Intent(mReactContext, BackgroundLocationService.class);
    mReactContext.stopService(intent);
    mReactContext.unbindService(serviceConnection);
  }
}
