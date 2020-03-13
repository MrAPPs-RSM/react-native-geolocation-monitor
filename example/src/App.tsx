import * as React from 'react';
import {
  StyleSheet,
  View,
  Text,
  EmitterSubscription,
  PermissionsAndroid,
} from 'react-native';
import {
  GeolocationMonitor,
  GeolocationNativeEventEmitter,
  Location,
  PositionError,
} from 'react-native-geolocation-monitor';

export default function App() {
  const [currentLocation, setCurrentLocation] = React.useState('searching...');
  const geolocationMonitorEmitter = new GeolocationNativeEventEmitter()
    .geolocationMonitorEmitter;
  let subscription: EmitterSubscription | null = null;
  let errorSubscription: EmitterSubscription | null = null;

  React.useEffect(() => {
    //To receive Location update until you stop
    // startTracking() | stopTracking()

    GeolocationMonitor.startTracking();

    // LocationUpdated event
    // callback LocationChangedCallback
    //
    subscription = geolocationMonitorEmitter.addListener(
      'LocationUpdated',
      location => setCurrentLocation(JSON.stringify(location))
    );

    // LocationUpdated event
    // callback LocationErrorCallback
    errorSubscription = geolocationMonitorEmitter.addListener(
      'LocationError',
      (code: PositionError, message: string) => setCurrentLocation(message)
    );

    //To receive only first location found
    // getCurrentLocation((location) => {})

    GeolocationMonitor.getCurrentLocation((location: Location) => {
      setCurrentLocation(JSON.stringify(location));
    });
  }, []);

  return (
    <View style={styles.container}>
      <Text>Current Location: {currentLocation}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
