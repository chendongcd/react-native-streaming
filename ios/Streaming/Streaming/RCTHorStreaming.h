//
//  Streaming.h
//  Streaming
//
//  Created by luo jian on 2017/10/17.
//  Copyright © 2017年 Seatell. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AlivcLivePusher/AlivcLivePusherHeader.h>
#import <CoreTelephony/CTCallCenter.h>
#import <CoreTelephony/CTCall.h>
#import "React/RCTView.h"

@class AlivcLivePushStatsInfo,AlivcLivePushConfig,RCTEventDispatcher;

@interface RCTHorStreaming : UIView

@property (nonatomic, strong) dispatch_queue_t sessionQueue;
@property (nonatomic, strong) NSString *rtmpURL;

@property(nonatomic,copy) RCTBubblingEventBlock onNetworkSlow;
@property(nonatomic,copy) RCTBubblingEventBlock onConnectError;
@property(nonatomic,copy) RCTBubblingEventBlock onConnectSuccess;
@property(nonatomic,copy) RCTBubblingEventBlock onConnectTimeout;
@property(nonatomic,copy) RCTBubblingEventBlock onSetting;
- (void)destroySession;
- (void)reconnectPush;
- (void)restartPush;
- (void)switchCamera;
- (void)startPush;
- (void)pausePush;
- (void)setFlash;
- (void)setBeautyOn:(BOOL) beautyOn;
- (void)setResolution:(int) resolution;
- (void)setBeautyWhite:(int) beauty;
- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher NS_DESIGNATED_INITIALIZER;

@end


