import * as React from 'react';
import {
  StyleSheet,
  View,
  Text,
  EmitterSubscription,
  PermissionsAndroid,
  TouchableOpacity
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

    //GeolocationMonitor.startTracking();

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

    /*
    GeolocationMonitor.getCurrentLocation((location: Location) => {
      setCurrentLocation(JSON.stringify(location));
    });

     */
  }, []);

  const onStartPress = () => GeolocationMonitor.startTracking();
  const onStopPress = () => GeolocationMonitor.stopTracking();

  return (
    <View style={styles.container}>
      <View style={styles.content}>
        <Text>Current Location: {currentLocation}</Text>
        <TouchableOpacity style={styles.button} onPress={onStartPress}>
          <Text style={styles.buttonTitle}>Start</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={onStopPress}>
          <Text style={styles.buttonTitle}>Stop</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  content: {
    padding: 32,
    justifyContent: "center",
    alignItems: "center"
  },
  button: {
    marginTop: 20,
    width: 200,
    height: 50,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "red"
  },
  buttonTitle: {
    color: "white",
    fontSize: 16,
    fontWeight: "bold"
  },
  stop: {
    marginTop: 16
  }
});
