
//#import <UIKit/UIKit.h>
//#import "React/RCTView.h"
//@class RCTEventDispatcher;
//
//@interface RCTPlayer : UIView
//
//- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher NS_DESIGNATED_INITIALIZER;
//@property (nonatomic, strong) UIImageView *placeHolderImgView;
//@property (nonatomic, copy )  NSString    *URL;
//@property (nonatomic, copy )  NSString    *placeHolder;
//- (void)play;
//- (void)shutdown;
//- (void)downloadImage;
//
//@end
//#import <UIKit/UIKit.h>
//#import "React/RCTView.h"
//#import "PLPlayer.h"
//#import <Masonry.h>
//
//@class RCTEventDispatcher;
//
//@interface RCTPlayer : UIView<PLPlayerDelegate>
//
//@property (nonatomic, assign) int reconnectCount;
//
//@property (nonatomic, copy)  RCTDirectEventBlock onLoading;
//@property (nonatomic, copy)  RCTDirectEventBlock onPaused;
//@property (nonatomic, copy)  RCTDirectEventBlock onShutdown;
//@property (nonatomic, copy)  RCTDirectEventBlock onError;
//@property (nonatomic, copy)  RCTDirectEventBlock onPlaying;
//- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher NS_DESIGNATED_INITIALIZER;
//
//
//@end
#import <UIKit/UIKit.h>
#import "React/RCTView.h"
#import "ZFPlayer.h"
@class RCTEventDispatcher;

@interface RCTPlayer : UIView
@property (nonatomic, strong) UIImageView *placeHolderImgView;
@property (nonatomic, copy )  NSString    *URL;
@property (nonatomic, copy )  NSString    *placeHolder;
@property (nonatomic, copy)  RCTBubblingEventBlock onFullScreen;
@property (nonatomic, copy)  RCTBubblingEventBlock onPlayState;
@property (nonatomic, copy)  RCTBubblingEventBlock onClick;
- (void)play;
- (void)shutdown;
- (void)downloadImage;
- (void)reload;
- (void)fullScreenAction;
- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher NS_DESIGNATED_INITIALIZER;
@end
