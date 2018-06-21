#import "RCTPlayerManager.h"

@interface RCTPlayerManager()

@property (strong,nonatomic) RCTPlayer  *liveView;
@end

@implementation RCTPlayerManager
RCT_EXPORT_MODULE();
@synthesize bridge = _bridge;
//RCT_EXPORT_MODULE()
//
//RCT_EXTERN void RCTRegisterModule(RCTPlayerManager*);
//
//+ (void)load {RCTRegisterModule(self);}
//
//+ (NSString *)moduleName{return @"RCTPlayer";}


- (UIView *)view
{
    self.liveView = [[RCTPlayer alloc] initWithEventDispatcher:self.bridge.eventDispatcher];
    return self.liveView;
}
- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_CUSTOM_VIEW_PROPERTY(source, NSDictionary, RCTPlayerManager){
    NSDictionary *para = [RCTConvert NSDictionary:json];
    
    self.liveView.URL = para[@"uri"];
    self.liveView.placeHolder = para[@"holder"];
    [self.liveView downloadImage];
    [self.liveView play];
}
RCT_EXPORT_VIEW_PROPERTY(started, BOOL);
RCT_EXPORT_VIEW_PROPERTY(muted, BOOL);
RCT_EXPORT_VIEW_PROPERTY(lock, BOOL);
RCT_EXPORT_VIEW_PROPERTY(onFullScreen, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onPlayState, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onClick, RCTBubblingEventBlock);
RCT_EXPORT_METHOD(stopPlayer){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.liveView shutdown];
    });
}
RCT_EXPORT_METHOD(reload){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.liveView reload];
    });
}
RCT_EXPORT_METHOD(actionFull){
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.liveView fullScreenAction];
    });
}

-(RCTPlayer *)liveView{
    
    if (!_liveView) {
        _liveView = [[RCTPlayer alloc] initWithEventDispatcher:self.bridge.eventDispatcher];
    }
    return _liveView;
}

- (void)dealloc
{
    [self.liveView shutdown];
}

@end
//#import "RCTPlayerManager.h"
//#import "RCTPlayer.h"
//
//@implementation RCTPlayerManager
//RCT_EXPORT_MODULE();
//
//@synthesize bridge = _bridge;
//
//- (UIView *)view
//{
//    return [[RCTPlayer alloc] initWithEventDispatcher:self.bridge.eventDispatcher];
//}
//
////- (NSArray *)customDirectEventTypes
////{
////    return @[
////             @"onLoading",
////             @"onPaused",
////             @"onShutdown",
////             @"onError",
////             @"onPlaying"
////             ];
////}
//
//- (dispatch_queue_t)methodQueue
//{
//    return dispatch_get_main_queue();
//}
//
//RCT_EXPORT_VIEW_PROPERTY(source, NSDictionary);
//RCT_EXPORT_VIEW_PROPERTY(started, BOOL);
//RCT_EXPORT_VIEW_PROPERTY(muted, BOOL);
//RCT_EXPORT_VIEW_PROPERTY(onLoading,  RCTDirectEventBlock);
//RCT_EXPORT_VIEW_PROPERTY(onPaused,  RCTDirectEventBlock);
//RCT_EXPORT_VIEW_PROPERTY(onShutdown,  RCTDirectEventBlock);
//RCT_EXPORT_VIEW_PROPERTY(onError,  RCTDirectEventBlock);
//RCT_EXPORT_VIEW_PROPERTY(onPlaying,  RCTDirectEventBlock);
//
//@end
//
////#import "RCTPlayerManager.h"
////@implementation RCTPlayerManager
////RCT_EXPORT_MODULE();
////
////@synthesize bridge = _bridge;
////
////- (UIView *)view
////{
//////    if(!self.player){
//////        self.player = [[RCTPlayer alloc] initWithEventDispatcher:self.bridge.eventDispatcher];
//////    }
////     NSLog(@"初始化123123");
////    return [[RCTPlayer alloc] initWithEventDispatcher:self.bridge.eventDispatcher];
////}
////RCT_EXPORT_VIEW_PROPERTY(liveParameter, NSDictionary);
////RCT_EXPORT_METHOD(livePagePop){
////        [self.player shutdown];;
////}
////- (void)dealloc
////{
////    [self.player shutdown];
////}
////- (dispatch_queue_t)methodQueue
////{
////    return dispatch_get_main_queue();
////}
////@end
//
