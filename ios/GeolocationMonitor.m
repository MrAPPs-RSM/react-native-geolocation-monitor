#import "GeolocationMonitor.h"
#import "INTULocationManager.h"

@implementation GeolocationMonitor{
	long lastRequestId;
	CLLocationCoordinate2D lastLocation;
	RCTResponseSenderBlock lastLocationCallback;
	BOOL onlyOnce;
}

RCT_EXPORT_MODULE();

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

- (NSArray<NSString *> *)supportedEvents
{
  return @[@"LocationUpdated", @"LocationError"];
}

- (NSDictionary*) mapLocation: (CLLocationCoordinate2D) location{
	NSNumber* lat = [NSNumber numberWithFloat:location.latitude];
	NSNumber* lon = [NSNumber numberWithFloat:location.longitude];
	return @{
		@"latitude":lat,
		@"longitude":lon
	};
}

RCT_EXPORT_METHOD(startTracking)
{
	[self startTrackingOnlyOnce:false];
}

- (void) startTrackingOnlyOnce: (BOOL)onlyOnce
{
	self->onlyOnce = onlyOnce;
	if(self->lastRequestId == 0){
		INTULocationManager *locMgr = [INTULocationManager sharedInstance];
		self->lastRequestId = [locMgr subscribeToSignificantLocationChangesWithBlock:^(CLLocation *currentLocation, INTULocationAccuracy achievedAccuracy, INTULocationStatus status) {
			if (status == INTULocationStatusSuccess) {
				self->lastLocation = currentLocation.coordinate;
				[self sendEventWithName:@"LocationUpdated"
								   body:[self mapLocation:currentLocation.coordinate]
				 ];

				if(self->onlyOnce){
					[self stopTracking];
				}

				if(self->lastLocationCallback){
					self->lastLocationCallback(@[[self mapLocation:self->lastLocation]]);
					self->lastLocationCallback = nil;
				}
			}
			else {
				if(status == INTULocationServicesStateDenied
				   || status == INTULocationServicesStateRestricted
				   || status == INTULocationServicesStateDisabled){

					[self sendEventWithName:@"LocationError"
									   body:@{
										   @"code":@(1),
										   @"message":@"geolocation_ios_permissions_denied"
									   }
					];
				}
			}
		}];
	}
}

RCT_EXPORT_METHOD(stopTracking)
{
	if(self->lastRequestId != 0){
		INTULocationManager *locMgr = [INTULocationManager sharedInstance];
		[locMgr cancelLocationRequest:lastRequestId];
		self->lastRequestId = 0;
	}
}

RCT_EXPORT_METHOD(getCurrentLocation:(RCTResponseSenderBlock)callback)
{
	lastLocationCallback = callback;
	if(self->lastRequestId == 0){
		[self startTrackingOnlyOnce:true];
	}
}

@end
