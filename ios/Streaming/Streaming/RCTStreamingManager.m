//
//  RCTStreamingManager.m
//  Streaming
//
//  Created by luo jian on 2017/10/17.
//  Copyright © 2017年 Seatell. All rights reserved.
//

#import "RCTStreamingManager.h"
#import "RCTStreaming.h"
#import "UIViewController+PlayerRotation.h"
@interface RCTStreamingManager()

@property (strong,nonatomic) RCTStreaming  *streamView;
@end
@implementation RCTStreamingManager
RCT_EXPORT_MODULE();

@synthesize bridge = _bridge;

- (UIView *)view
{
    self.streamView = [[RCTStreaming alloc] initWithEventDispatcher:self.bridge.eventDispatcher];
    return self.streamView;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_VIEW_PROPERTY(rtmpURL, NSString);
RCT_EXPORT_VIEW_PROPERTY(exposure, float);
RCT_EXPORT_VIEW_PROPERTY(beauty, float);
RCT_EXPORT_VIEW_PROPERTY(orientation, BOOL);
RCT_EXPORT_VIEW_PROPERTY(muted, BOOL);
//RCT_EXPORT_VIEW_PROPERTY(zoom, NSNumber);
//RCT_EXPORT_VIEW_PROPERTY(focus, BOOL);
RCT_EXPORT_VIEW_PROPERTY(onConnectError, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onNetworkSlow, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onConnectTimeout, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onConnectSuccess, RCTBubblingEventBlock);
RCT_EXPORT_METHOD(closeStreaming){
    dispatch_async(dispatch_get_main_queue(), ^{
        //        UIViewController * app = [UIApplication sharedApplication].delegate;
        //        app.shouldChangeOrientation = 0;
        [self.streamView destroySession];
    });
}
RCT_EXPORT_METHOD(reconnectPush){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.streamView reconnectPush];
    });
}
RCT_EXPORT_METHOD(startPush){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.streamView startPush];
    });
}
RCT_EXPORT_METHOD(pausePush){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.streamView pausePush];
    });
}
RCT_EXPORT_METHOD(restartPush){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.streamView restartPush];
    });
}
RCT_EXPORT_METHOD(switchCamera){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.streamView switchCamera];
    });
}
RCT_EXPORT_METHOD(setFlash){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.streamView setFlash];
    });
}
RCT_EXPORT_METHOD(setResolution:(int) resolution){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.streamView setResolution:resolution];
    });
}
RCT_EXPORT_METHOD(setBeautyWhite:(int) beauty){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.streamView setBeautyWhite:beauty];
    });
}
RCT_EXPORT_METHOD(setBeautyOn:(BOOL) beautyOn){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.streamView setBeautyOn:beautyOn];
    });
}
@end

