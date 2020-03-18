import { NativeModules, NativeEventEmitter } from 'react-native';

export enum PositionError {
  PERMISSIONS_DENIED = 1,
  LOCATION_NOT_ENABLED = 2,
  GPS_PROVIDER_NOT_EXIST = 3,
  INTERNAL_SERVICE_ERROR = -1
}

export interface Location {
  latitude: number,
  longitude: number
}

export type LocationChangedCallback = (location: Location) => void;
export type LocationErrorCallback = (code: PositionError, message: string) => void;

interface GeolocationMonitorType {
  startTracking(): void;
  getCurrentLocation(locationChangedCallback: LocationChangedCallback)
  stopTracking(): void;
}

export const {GeolocationMonitor} = NativeModules as GeolocationMonitorType;

export class GeolocationNativeEventEmitter {
  public geolocationMonitorEmitter: NativeEventEmitter;

  constructor() {
    this.geolocationMonitorEmitter =  new NativeEventEmitter(GeolocationMonitor);
  }
}
