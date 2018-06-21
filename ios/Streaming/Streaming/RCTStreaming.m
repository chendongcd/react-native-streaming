//
//  Streaming.m
//  Streaming
//
//  Created by luo jian on 2017/10/17.
//  Copyright © 2017年 Seatell. All rights reserved.
//

#import "RCTStreaming.h"
#import "React/RCTBridgeModule.h"
#import "React/UIView+React.h"
#import "React/RCTEventDispatcher.h"
#import "AlivcParamModel.h"
#import <AlivcLibFace/AlivcLibFaceManager.h>
#import <ALivcLibBeauty/AlivcLibBeautyManager.h>
#define ScreenWidth [UIScreen mainScreen].bounds.size.width
#define ScreenHeight [UIScreen mainScreen].bounds.size.height
#define IPHONEX (ScreenWidth == 375.f && ScreenHeight == 812.f)
@interface RCTStreaming()<AlivcLivePusherInfoDelegate,AlivcLivePusherErrorDelegate,AlivcLivePusherNetworkDelegate,AlivcLivePusherCustomFilterDelegate>


@property (nonatomic, strong) AlivcLivePusher *livePusher;
@property (nonatomic, strong) AlivcLivePushConfig *pushConfig;
@property (nonatomic, strong) AlivcLivePushStatsInfo *info;
@property (nonatomic, strong) UIView *preview;
@property (nonatomic, copy) void(^segmentBlock)(int value);
/* 推流地址 */
@property (nonatomic, strong) NSString *url;
/* 摄像头方向记录 */
@property (nonatomic, assign) AlivcLivePushCameraType currentPosition;
/* 摄像头方向记录 */
@property (nonatomic, assign) AlivcLivePushResolution resolution;
/* 曝光度记录 */
@property (nonatomic, assign) CGFloat exposureValue;
// 调试
@property (nonatomic, strong) CTCallCenter *callCenter;
@property (nonatomic, strong) NSTimer *timer;
@property (nonatomic, strong) NSMutableArray *logArray;

@property (nonatomic, assign) BOOL isCTCallStateDisconnected;
@property (nonatomic, assign) CGFloat lastPinchDistance;

@end

@implementation RCTStreaming{
    RCTEventDispatcher *_eventDispatcher;
    dispatch_source_t _streamingTimer;
    BOOL _started;
    BOOL _muted;
    BOOL _focus;
    NSString *_camera;
    int _beauty;
    bool _orientation;
    bool _isFirstPush;
    bool _isFlash;
    //配置
    NSDictionary *_configurations;
    
}

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher
{
    if ((self = [super init])) {
        _eventDispatcher = eventDispatcher;
        _started = NO;
        _muted = NO;
        _focus = NO;
        _camera = @"front";
        _beauty = 0.0;
        _orientation = true;
        _isFirstPush = true;
        _isFlash = false;
        self.preview = [[UIView alloc] init];
        self.preview.backgroundColor = [UIColor clearColor];
        self.preview.frame =[self getFullScreenFrame];
        // 预览view
        [self addSubview:self.preview];
        int ret = [self setupPusher];
        if(ret == 0){
            [self.livePusher startPreview:self.preview];
        }
        [self addGesture];
    }
    return self;
};

- (int)setupPusher {
    self.pushConfig = [[AlivcLivePushConfig alloc] init];
        //self.pushConfig.externMainStream = false;
    self.pushConfig.resolution = AlivcLivePushResolution540P;//默认为540P，最大支持720P
    self.pushConfig.fps = AlivcLivePushFPS20; //建议用户使用20fps
    self.pushConfig.enableAutoBitrate = true; // 打开码率自适应，默认为true
    self.pushConfig.videoEncodeGop = AlivcLivePushVideoEncodeGOP_2;//默认值为2，关键帧间隔越大，延时越高。建议设置为1-2。
    
    self.pushConfig.connectRetryInterval = 2000; // 单位为毫秒，重连时长2s，重连间隔设置不小于1秒，建议使用默认值即可。
    self.pushConfig.previewMirror = false; // 默认为false，正常情况下都选择false即可。
    self.pushConfig.orientation =  AlivcLivePushOrientationPortrait; // 默认为竖屏，可设置home键向左或向右横屏
    self.pushConfig.qualityMode = AlivcLivePushQualityModeFluencyFirst;// 默认为清晰度优先模式，可设置为流畅度优先模式和自定义模式。
    self.pushConfig.enableAutoResolution = true;
    self.pushConfig.beautyOn = true; // 开启美颜
    self.pushConfig.beautyMode = AlivcLivePushBeautyModeProfessional;//设定为高级美颜
    self.pushConfig.beautyWhite = 70; // 美白范围0-100
    self.pushConfig.beautyBuffing = 40; // 磨皮范围0-100
    self.pushConfig.beautyRuddy = 40;// 红润设置范围0-100
    self.pushConfig.beautyBigEye = 30;// 大眼设置范围0-100
    self.pushConfig.beautyThinFace = 40;// 瘦脸设置范围0-100
    self.pushConfig.beautyShortenFace = 50;// 收下巴设置范围0-100
    self.pushConfig.beautyCheekPink = 15;// 腮红设置范围0-100
    self.livePusher = [[AlivcLivePusher alloc] initWithConfig:self.pushConfig];
    [self.livePusher setLogLevel:(AlivcLivePushLogLevelFatal)];
    if (!self.livePusher) {
        return -1;
    }
    [self.livePusher setInfoDelegate:self];
    [self.livePusher setErrorDelegate:self];
    [self.livePusher setNetworkDelegate:self];
    [self.livePusher setCustomFilterDelegate:self];
    //[self.livePusher setCustomDetectorDelegate:self];
    
    return 0;
}

- (CGRect)getFullScreenFrame {
    
    CGRect frame = CGRectMake(0, 0, ScreenWidth, ScreenHeight);
    if (IPHONEX) {
        // iPhone X UI适配
        frame = CGRectMake(0, 88, ScreenWidth, ScreenHeight-88-57);
    }
    if (self.pushConfig.orientation != AlivcLivePushOrientationPortrait) {
        CGFloat temSize = frame.size.height;
        frame.size.height = frame.size.width;
        frame.size.width = temSize;
        
        CGFloat temPoint = frame.origin.y;
        frame.origin.y = frame.origin.x;
        frame.origin.x = temPoint;
        
    }
    return frame;
}

- (void)dealloc{
    NSLog(@"清掉了推流");
    [self destroySession];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}
-(void)startPush{
    if(!_started&&self.url){
        NSLog(@"开始推流%@",self.url);
        if(_isFirstPush){
            // 开始推流
            int value = [self.livePusher startPushWithURL:self.url];
            if(value==0&&self.onConnectSuccess){
                self.onConnectSuccess(@{@"msg":@"success"});
            }
            [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
            _isFirstPush = false;
        }else{
            [self.livePusher resume];
        }
        _started = !_started;
    }
}
-(void)pausePush{
    if(_started){
        //暂停推流
        [self.livePusher pause];
        _started = !_started;
    }
}

-(void)setOrientation:(bool)orientation{
    _orientation = orientation;
    //int value = orientation?0:2;
    AlivcParamModel *orientationModel = [[AlivcParamModel alloc] init];
    orientationModel.segmentBlock = ^(int value){
        value =  orientation?0:2;
        self.pushConfig.orientation = value;
    };
    //self.config.orientation = _orientation;
    NSLog(@"横竖屏%d",orientation);
    //[self.liveSession]
}

-(void)setResolution:(int)resolution{
    //   AlivcParamModel *resolutionModel = [[AlivcParamModel alloc] init];
    // AlivcLivePushResolution resolute = AlivcLivePushResolution540P;
    if(resolution == 2){
        self.resolution = AlivcLivePushResolution240P;
    }
    if(resolution == 1){
        self.resolution = AlivcLivePushResolution540P;
    }
    if(resolution == 0){
        self.resolution = AlivcLivePushResolution720P;
    }
    self.pushConfig.resolution = self.resolution;
}

-(void)switchCamera{
    [self.livePusher switchCamera];
    // [self.liveSession alivcLiveVideoRotateCamera];
}

-(void)setBeautyWhite:(int) beauty{
    _beauty = beauty;
    [self.livePusher setBeautyWhite:beauty];
}
-(void)setBeautyOn:(BOOL) beautyOn{
    //_beauty = beauty;
    NSLog(@"美颜值%d",beautyOn);
    [self.livePusher setBeautyOn:beautyOn?true:false];
}

-(void)setMuted:(BOOL)muted{
    _muted = muted;
    [self.livePusher setMute:muted];
}

-(void)setExposure:(float) exposure{
    self.exposureValue = exposure;
    [self.livePusher setExposureValue:exposure];
}

- (void)setConfig:(NSDictionary *) config
{
    NSLog(@"设置了配置");
    _configurations = config;
    if(_configurations != nil){
        //        NSDictionary *video = _configurations[@"video"];
        //        NSDictionary *audio = _configurations[@"audio"];
        //        self.configuration.videoMaxBitRate = [video[@"maxBitRate"] integerValue] * 1000;
        //        self.configuration.videoBitRate = [video[@"bitRate"] integerValue] * 1000;
        //        self.configuration.videoMinBitRate = [video[@"mixBitRate"] integerValue] * 1000;
        //        self.configuration.audioBitRate = [audio[@"bitRate"] integerValue] * 1000;
        //        self.configuration.fps = [_configurations[@"fps"] integerValue];
        //        // 重连时长
        //        self.configuration.reconnectTimeout = [_configurations[@"timeOut"] integerValue];
        //        switch ([_configurations[@"quality"] integerValue]) {
        //            case 0:
        //                self.configuration.preset = AVCaptureSessionPresetLow;
        //                break;
        //            case 1:
        //                self.configuration.preset = AVCaptureSessionPresetMedium;
        //                break;
        //            case 2:
        //                self.configuration.preset = AVCaptureSessionPresetHigh;
        //                break;
        //
        //            default:
        //                break;
        //        }
    }
}

- (void) setRtmpURL:(NSString *)rtmpURL
{
    NSLog(@"设置了推流地址");
    //self.configuration.url = rtmpURL;
    self.url = rtmpURL;
}

-(void) setFlash
{
    _isFlash = !_isFlash;
    [self.livePusher setFlash:_isFlash];
    //[self.livePusher setFlash:_isFlash];
}



- (void)reconnectPush {
    
    if (!self.livePusher) {
        return;
    }
    
    int value = [self.livePusher reconnectPushAsync];
    if(value==0&&self.onConnectSuccess){
        self.onConnectSuccess(@{@"msg":@"success"});
    }
}

#pragma mark - AlivcLivePusherCustomFilterDelegate
/**
 通知外置滤镜创建回调
 */
- (void)onCreate:(AlivcLivePusher *)pusher context:(void*)context
{
    [[AlivcLibBeautyManager shareManager] create:context];
}

- (void)updateParam:(AlivcLivePusher *)pusher buffing:(float)buffing whiten:(float)whiten pink:(float)pink cheekpink:(float)cheekpink thinface:(float)thinface shortenface:(float)shortenface bigeye:(float)bigeye
{
    [[AlivcLibBeautyManager shareManager] setParam:buffing whiten:whiten pink:pink cheekpink:cheekpink thinface:thinface shortenface:shortenface bigeye:bigeye];
}

- (void)switchOn:(AlivcLivePusher *)pusher on:(bool)on
{
    [[AlivcLibBeautyManager shareManager] switchOn:on];
}
/**
 通知外置滤镜处理回调
 */
- (int)onProcess:(AlivcLivePusher *)pusher texture:(int)texture textureWidth:(int)width textureHeight:(int)height extra:(long)extra
{
    return [[AlivcLibBeautyManager shareManager] process:texture width:width height:height extra:extra];
}
/**
 通知外置滤镜销毁回调
 */
- (void)onDestory:(AlivcLivePusher *)pusher
{
    [[AlivcLibBeautyManager shareManager] destroy];
}

#pragma mark - AlivcLivePusherCustomDetectorDelegate
/**
 通知外置视频检测创建回调
 */
- (void)onCreateDetector:(AlivcLivePusher *)pusher
{
    [[AlivcLibFaceManager shareManager] create];
}
/**
 通知外置视频检测处理回调
 */
- (long)onDetectorProcess:(AlivcLivePusher *)pusher data:(long)data w:(int)w h:(int)h rotation:(int)rotation format:(int)format extra:(long)extra
{
    return [[AlivcLibFaceManager shareManager] process:data width:w height:h rotation:rotation format:format extra:extra];
}
/**
 通知外置视频检测销毁回调
 */
- (void)onDestoryDetector:(AlivcLivePusher *)pusher
{
    [[AlivcLibFaceManager shareManager] destroy];
}


#pragma mark - AlivcLivePusherErrorDelegate

- (void)onSystemError:(AlivcLivePusher *)pusher error:(AlivcLivePushError *)error {
    if(self.onConnectError){
        self.onConnectError(@{@"msg":@"连接失败，系统错误",@"error":@"0"});
    }
    //    [self showAlertViewWithErrorCode:error.errorCode
    //                            errorStr:error.errorDescription
    //                                 tag:kAlivcLivePusherVCAlertTag+11
    //                               title:NSLocalizedString(@"dialog_title", nil)
    //                             message:NSLocalizedString(@"system_error", nil)
    //                            delegate:self
    //                         cancelTitle:NSLocalizedString(@"exit", nil)
    //                   otherButtonTitles:NSLocalizedString(@"ok", nil),nil];
}


- (void)onSDKError:(AlivcLivePusher *)pusher error:(AlivcLivePushError *)error {
    if(self.onConnectError){
        self.onConnectError(@{@"msg":@"连接失败，SDK错误",@"error":@"1"});
    }
    [self.livePusher restartPush];
    NSLog(@"重连结果");
    //    [self showAlertViewWithErrorCode:error.errorCode
    //                            errorStr:error.errorDescription
    //                                 tag:kAlivcLivePusherVCAlertTag+12
    //                               title:NSLocalizedString(@"dialog_title", nil)
    //                             message:NSLocalizedString(@"sdk_error", nil)
    //                            delegate:self
    //                         cancelTitle:NSLocalizedString(@"exit", nil)
    //                   otherButtonTitles:NSLocalizedString(@"ok", nil),nil];
}

#pragma mark - AlivcLivePusherNetworkDelegate

- (void)onConnectFail:(AlivcLivePusher *)pusher error:(AlivcLivePushError *)error {
    
    if(self.onConnectError){
        self.onConnectError(@{@"msg":@"连接失败，网络错误",@"error":@"2"});
    }
    //[self reconnectPush];
    //    [self showAlertViewWithErrorCode:error.errorCode
    //                            errorStr:error.errorDescription
    //                                 tag:kAlivcLivePusherVCAlertTag+23
    //                               title:NSLocalizedString(@"dialog_title", nil)
    //                             message:NSLocalizedString(@"connect_fail", nil)
    //                            delegate:self
    //                         cancelTitle:NSLocalizedString(@"reconnect_button", nil)
    //                   otherButtonTitles:NSLocalizedString(@"exit", nil), nil];
    
}


- (void)onSendDataTimeout:(AlivcLivePusher *)pusher {
    if(self.onConnectError){
        self.onConnectError(@{@"msg":@"连接超时",@"error":@"2"});
    }
    //[self reconnectPush];
    //    [self showAlertViewWithErrorCode:0
    //                            errorStr:nil
    //                                 tag:0
    //                               title:NSLocalizedString(@"dialog_title", nil)
    //                             message:NSLocalizedString(@"senddata_timeout", nil)
    //                            delegate:nil
    //                         cancelTitle:NSLocalizedString(@"ok", nil)
    //                   otherButtonTitles:nil];
}

- (void)onSendSeiMessage:(AlivcLivePusher *)pusher {
    if(self.onConnectError){
        self.onConnectError(@{@"msg":@"连接超时",@"error":@"2"});
    }
    //[self.publisherView updateInfoText:NSLocalizedString(@"send message", nil)];
    
}

//网络恢复
- (void)onConnectRecovery:(AlivcLivePusher *)pusher {
    [self reconnectPush];
    //[self.publisherView updateInfoText:NSLocalizedString(@"connectRecovery_log", nil)];
}

//网络极差
- (void)onNetworkPoor:(AlivcLivePusher *)pusher {
    if(self.onNetworkSlow){
        self.onNetworkSlow(@{@"msg":@"当前网络环境较差",@"error":@"2"});
    }
    NSLog(@"网速过慢");
    //[self showAlertViewWithErrorCode:0 errorStr:nil tag:0 title:NSLocalizedString(@"dialog_title", nil) message:@"当前网速较慢，请检查网络状态" delegate:nil cancelTitle:NSLocalizedString(@"ok", nil) otherButtonTitles:nil];
}

//开始重连
- (void)onReconnectStart:(AlivcLivePusher *)pusher {
    
    // [self.publisherView updateInfoText:NSLocalizedString(@"reconnect_start", nil)];
}

//重连成功
- (void)onReconnectSuccess:(AlivcLivePusher *)pusher {
    if(self.onConnectSuccess){
        self.onConnectSuccess(@{@"msg":@"重连失败"});
    }
    //[self.publisherView updateInfoText:NSLocalizedString(@"reconnect_success", nil)];
}

//重连失败
- (void)onReconnectError:(AlivcLivePusher *)pusher error:(AlivcLivePushError *)error {
    if(self.onConnectError){
        self.onConnectError(@{@"msg":@"重连失败"});
    }
    //    [self showAlertViewWithErrorCode:error.errorCode
    //                            errorStr:error.errorDescription
    //                                 tag:kAlivcLivePusherVCAlertTag+22
    //                               title:NSLocalizedString(@"dialog_title", nil)
    //                             message:NSLocalizedString(@"reconnect_fail", nil)
    //                            delegate:self
    //                         cancelTitle:NSLocalizedString(@"reconnect_button", nil)
    //                   otherButtonTitles:NSLocalizedString(@"ok", nil), nil];
}
#pragma mark - 手势
- (void)addGesture {
    UITapGestureRecognizer *gesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapGesture:)];
    [self.preview addGestureRecognizer:gesture];
    
    UIPinchGestureRecognizer *pinch = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(pinchGesture:)];
    [self.preview addGestureRecognizer:pinch];
    
    UIPanGestureRecognizer *recognizer = [[UIPanGestureRecognizer alloc]
                                          initWithTarget:self action:@selector(handleSwipe:)];
    [self.preview addGestureRecognizer:recognizer];
}

- (void)tapGesture:(UITapGestureRecognizer *)gesture{
    CGPoint point = [gesture locationInView:self.preview];
    CGPoint percentPoint = CGPointZero;
    percentPoint.x = point.x / CGRectGetWidth(self.preview.bounds);
    percentPoint.y = point.y / CGRectGetHeight(self.preview.bounds);
    if (self.livePusher) {
        NSLog(@"进入到这里3");
        [self.livePusher focusCameraAtAdjustedPoint:percentPoint autoFocus:true];
    }
    // [self.liveSession alivcLiveVideoFocusAtAdjustedPoint:percentPoint autoFocus:YES];
    
}

- (void)pinchGesture:(UIPinchGestureRecognizer *)gesture {
    
    if (_currentPosition == AlivcLivePushCameraTypeFront) {
        return;
    }
    
    if (gesture.numberOfTouches != 2) {
        return;
    }
    CGPoint p1 = [gesture locationOfTouch:0 inView:self.preview];
    CGPoint p2 = [gesture locationOfTouch:1 inView:self.preview];
    CGFloat dx = (p2.x - p1.x);
    CGFloat dy = (p2.y - p1.y);
    CGFloat dist = sqrt(dx*dx + dy*dy);
    if (gesture.state == UIGestureRecognizerStateBegan) {
        _lastPinchDistance = dist;
    }
    
    CGFloat change = (dist - _lastPinchDistance)/3000;
    CGFloat max = [_livePusher getMaxZoom];
    [self.livePusher setZoom:MIN(change, max)];
    
}

- (void)handleSwipe:(UIPanGestureRecognizer *)swipe {
    
    if (swipe.state == UIGestureRecognizerStateChanged) {
        CGPoint translation = [swipe translationInView:self.preview];
        CGFloat absX = fabs(translation.x);
        CGFloat absY = fabs(translation.y);
        
        if (MAX(absX, absY) < 10) {
            return;
        }
        if (absY > absX) {
            if (translation.y<0) {
                self.exposureValue += 0.01;
                [self.livePusher setExposureValue:self.exposureValue];
                
            }else{
                self.exposureValue -= 0.01;
                [self.livePusher setExposureValue:self.exposureValue];
            }
        }
    }
}

#pragma mark - Notification
- (void)appResignActive{
    
    if (!self.livePusher) {
        return;
    }
    
    // 如果退后台不需要继续推流，则停止推流
    if ([self.livePusher isPushing]) {
        [self.livePusher stopPush];
    }
    //    // 监听电话
    //    _callCenter = [[CTCallCenter alloc] init];
    //    _isCTCallStateDisconnected = NO;
    //    _callCenter.callEventHandler = ^(CTCall* call) {
    //        if ([call.callState isEqualToString:CTCallStateDisconnected])
    //        {
    //            _isCTCallStateDisconnected = YES;
    //        }
    //        else if([call.callState isEqualToString:CTCallStateConnected])
    //
    //        {
    //            _callCenter = nil;
    //        }
    //    };
    
    NSLog(@"退入后台");
    
}

- (void)appBecomeActive{
    
    if (!self.livePusher) {
        return;
    }
    // 当前是推流模式，恢复推流
    [self.livePusher startPushWithURLAsync:self.url];
    
}
- (void)destroySession{
    [self.livePusher destory];
    self.livePusher = nil;
    [[UIApplication sharedApplication] setIdleTimerDisabled:NO];
}
- (void)restartPush{
    [self.livePusher restartPush];
}

@end




