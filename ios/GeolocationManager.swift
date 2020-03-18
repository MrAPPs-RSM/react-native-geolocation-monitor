//
//  GeolocationManager.swift
//  GeolocationMonitor
//
//  Created by Samuele Mazza on 3/17/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation
import CoreLocation

public let LocationErrorPermissionsDenied = 1
public let LocationErrorLocationNotEnabled = 2
public let LocationErrorInternalError = 3

struct GeolocationError {
	var code = 0
	var message = ""
}

class GeolocationManager: NSObject,CLLocationManagerDelegate {
	var minAccuracyMeters: CLLocationAccuracy = 500
	var manager:CLLocationManager = CLLocationManager()
	var isTracking: Bool = false

	static let shared = GeolocationManager()

	var errorCallback:((GeolocationError) -> ())? = nil;
	var locationCallback:((CLLocation) -> ())? = nil;


	private override init(){
		super.init()
	}

	private func mapError(code: Int, message: String){
		if let errorCallback = errorCallback {
			let error = GeolocationError(code: code, message: message)
			errorCallback(error)
		}
	}

	private func askForAuthorizations(_ authorizationStatus: CLAuthorizationStatus) -> Bool{
		var canAsk: Bool = false

		switch authorizationStatus {
		case .restricted:
			self.mapError(code: LocationErrorPermissionsDenied, message: "geolocation_ios_permissions_restricted")
			break;
		case .denied:
			self.mapError(code: LocationErrorLocationNotEnabled, message: "geolocation_ios_permissions_denied")
			break;
		default:
			canAsk = true
		}

		return canAsk;
	}

	private func isGeolocationSupportedAndEnabled() -> Bool{
		return self.askForAuthorizations(CLLocationManager.authorizationStatus())
	}

	func startGeolocationRequest(_ locationCallback: @escaping (CLLocation) -> (),errorCallback: @escaping (GeolocationError) -> ()){
		manager.delegate = self
		manager.distanceFilter = minAccuracyMeters
		self.locationCallback = locationCallback
		self.errorCallback = errorCallback
		if(self.isGeolocationSupportedAndEnabled()){
			manager.requestAlwaysAuthorization()
		}
	}

	func stopGeolocationRequest(){
		manager.delegate = nil
		isTracking = false
		if(isTracking){
			manager.stopUpdatingLocation()
		}
	}


	/** CLLocationManagerDelegate **/

	func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
		switch status {
		case .notDetermined:
			//do nothing
			break;
		default:
			if(self.askForAuthorizations(status)){
				if(!isTracking){
					isTracking = true
					manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
					manager.allowsBackgroundLocationUpdates = true
					manager.showsBackgroundLocationIndicator = true
					manager.startUpdatingLocation()
				}
			}
		}
	}


	func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
		//self.mapError(code: LocationErrorInternalError, message: error.localizedDescription)
	}

	func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
		if let locationObj = locations.last {
			if locationObj.horizontalAccuracy < minAccuracyMeters {
				if let locationCallback = locationCallback {
					locationCallback(locationObj)
				}
			}
		}
	}
}

