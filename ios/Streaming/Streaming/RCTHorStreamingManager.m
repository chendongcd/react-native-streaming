//
//  RCTStreamingManager.m
//  Streaming
//
//  Created by luo jian on 2017/10/17.
//  Copyright © 2017年 Seatell. All rights reserved.
//

#import "RCTHorStreamingManager.h"
#import "RCTHorStreaming.h"

@interface RCTHorStreamingManager()

@property (strong,nonatomic) RCTHorStreaming  *horStreamView;
@end
@implementation RCTHorStreamingManager
RCT_EXPORT_MODULE();

@synthesize bridge = _bridge;

- (UIView *)view
{
    self.horStreamView = [[RCTHorStreaming alloc] initWithEventDispatcher:self.bridge.eventDispatcher];
    return self.horStreamView;
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
RCT_EXPORT_VIEW_PROPERTY(onSetting, RCTBubblingEventBlock);
RCT_EXPORT_METHOD(closeStreaming){
    dispatch_async(dispatch_get_main_queue(), ^{
        //        UIViewController * app = [UIApplication sharedApplication].delegate;
        //        app.shouldChangeOrientation = 0;
        [self.horStreamView destroySession];
    });
}
RCT_EXPORT_METHOD(reconnectPush){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.horStreamView reconnectPush];
    });
}
RCT_EXPORT_METHOD(startPush){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.horStreamView startPush];
    });
}
RCT_EXPORT_METHOD(pausePush){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.horStreamView pausePush];
    });
}
RCT_EXPORT_METHOD(restartPush){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.horStreamView restartPush];
    });
}
RCT_EXPORT_METHOD(switchCamera){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.horStreamView switchCamera];
    });
}
RCT_EXPORT_METHOD(setFlash){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.horStreamView setFlash];
    });
}
RCT_EXPORT_METHOD(setResolution:(int) resolution){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.horStreamView setResolution:resolution];
    });
}
RCT_EXPORT_METHOD(setBeautyWhite:(int) beauty){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.horStreamView setBeautyWhite:beauty];
    });
}
RCT_EXPORT_METHOD(setBeautyOn:(BOOL) beautyOn){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.horStreamView setBeautyOn:beautyOn];
    });
}
@end




