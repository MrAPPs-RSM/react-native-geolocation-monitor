package com.reactnativegeolocationmonitor;

public enum LocationError {

    PERMISSIONS_DENIED(1),
    LOCATION_NOT_ENABLED(2),
    GPS_PROVIDER_NOT_EXIST(3),
    INTERNAL_SERVICE_ERROR(-1);

    private int value;

    LocationError(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
