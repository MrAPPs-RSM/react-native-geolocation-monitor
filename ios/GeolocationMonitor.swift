//
//  GeolocationMonitor.swift
//  GeolocationMonitor
//
//  Created by Samuele Mazza on 3/17/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation
import CoreLocation

@objc(GeolocationMonitor)
class GeolocationMonitor: RCTEventEmitter {
	var lastLocationCallback:((NSObject) -> ())? = nil;

	override func supportedEvents() -> [String]! {
		return ["LocationUpdated","LocationError"]
	}

	@objc func startTracking() {
		self.startTracking(onlyOnce: false)
	}

	@objc func stopTracking() {
		GeolocationManager.shared.stopGeolocationRequest()
	}

	@objc func getCurrentLocation(_ callback: @escaping (NSObject) -> ()) {
		self.lastLocationCallback = callback
		self.startTracking(onlyOnce: true)
	}

	func startTracking(onlyOnce: Bool) -> Void {
		GeolocationManager.shared.startGeolocationRequest({ (location: CLLocation) in
			if(onlyOnce){
				GeolocationManager.shared.stopGeolocationRequest()
			}
			self.sendEvent(withName: "LocationUpdated", body: self.mapLocation(location.coordinate))
		}) { (error: GeolocationError) in
			self.sendEvent(withName: "LocationError", body: ["code": error.code, "message": error.message])
		}
	}

	private func mapLocation(_ location: (CLLocationCoordinate2D)) -> Dictionary<String, Any>{
		return ["latitude": location.latitude, "longitude": location.longitude]
	}

}

