#import <Foundation/Foundation.h>

	// GeolocationMonitor.m
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
@interface RCT_EXTERN_MODULE(GeolocationMonitor, RCTEventEmitter)
RCT_EXTERN_METHOD(startTracking)
RCT_EXTERN_METHOD(stopTracking)
RCT_EXTERN_METHOD(getCurrentLocation: (RCTResponseSenderBlock)callback)

- (dispatch_queue_t)methodQueue
{
	return dispatch_get_main_queue();
}

@end

