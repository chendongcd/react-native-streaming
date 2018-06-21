#import "RCTPlayer.h"
#import <IJKMediaFramework/IJKMediaFramework.h>
#import <Masonry.h>
#import "React/RCTEventDispatcher.h"
#import <AVFoundation/AVFoundation.h>
#import "UIViewController+PlayerRotation.h"
#import <MediaPlayer/MediaPlayer.h>


#define ScreenWidth [UIScreen mainScreen].bounds.size.width
#define ScreenHeight [UIScreen mainScreen].bounds.size.height

// 枚举值，包含水平移动方向和垂直移动方向
typedef NS_ENUM(NSInteger, PanDirection){
    PanDirectionHorizontalMoved, // 横向移动
    PanDirectionVerticalMoved    // 纵向移动
};

@interface RCTPlayer()
/** 直播播放器*/
@property (nonatomic, strong) IJKFFMoviePlayerController *moviePlayer;
/** 播放器属性*/
@property (nonatomic, strong) IJKFFOptions *options;
/** 是否全屏*/
@property (nonatomic, assign) BOOL                   isFullScreen;
/** 播放完了*/
@property (nonatomic, assign) BOOL                   playDidEnd;
/** 进入后台*/
@property (nonatomic, assign) BOOL                   didEnterBackground;
/** 是否锁定屏幕方向 */
@property (nonatomic, assign) BOOL                   isLocked;
/** 单击 */
@property (nonatomic, strong) UITapGestureRecognizer *singleTap;
/** 双击 */
@property (nonatomic, strong) UITapGestureRecognizer *doubleTap;
/** app ControlView实例 */
@property (nonatomic, strong) UIViewController *app;
/** 滑杆 */
@property (nonatomic, strong) UISlider               *volumeViewSlider;
/** 是否在调节音量*/
@property (nonatomic, assign) BOOL                   isVolume;
/** 亮度view */
@property (nonatomic, strong) ZFBrightnessView       *brightnessView;
/** 定义一个实例变量，保存枚举值 */
@property (nonatomic, assign) PanDirection           panDirection;
@end

@implementation RCTPlayer{
    RCTEventDispatcher *_eventDispatcher;
    bool _started;
    bool _muted;
}

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher
{
    self = [super init];
    if (self) {
        _eventDispatcher = eventDispatcher;
        //self.frame = CGRectMake(0, 0, ScreenWidth, ScreenHeight);
        [self addSubview:self.placeHolderImgView];
    }
    
    return self;
}

- (IJKFFMoviePlayerController *)moviePlayer {
    
    if (!_moviePlayer) {
        IJKFFMoviePlayerController *moviePlayer = [[IJKFFMoviePlayerController alloc] initWithContentURLString:self.URL withOptions:self.options];
        // 填充fill
        // moviePlayer.scalingMode = IJKMPMovieScalingModeNone;
        // 设置自动播放(必须设置为NO, 防止自动播放, 才能更好的控制直播的状态)
        moviePlayer.shouldAutoplay = NO;
        // 默认不显示
        moviePlayer.shouldShowHudView = NO;
        
        moviePlayer.scalingMode = IJKMPMovieScalingModeFill;
        [moviePlayer prepareToPlay];
        [moviePlayer setPauseInBackground:true];
        moviePlayer.view.frame = CGRectMake(0, 0,self.frame.size.width, self.frame.size.height);
        moviePlayer.view.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        _moviePlayer = moviePlayer;
    }
    _app = [UIApplication sharedApplication].delegate;
    //_app.orientation = currentOrientation;
    //self.app.orientation = UIInterfaceOrientationMaskPortrait;
    _app.shouldChangeOrientation = self.isLocked?0:2;
    [self createGesture];
    // 获取系统音量
    [self configureVolume];
    return _moviePlayer;
}

- (IJKFFOptions *)options {
    if (!_options) {
        IJKFFOptions *options = [IJKFFOptions optionsByDefault];
        [options setPlayerOptionIntValue:1  forKey:@"videotoolbox"];
        // 帧速率(fps) 非标准桢率会导致音画不同步，所以只能设定为15或者29.97
        [options setPlayerOptionIntValue:29.97 forKey:@"r"];
        // 置音量大小，256为标准  要设置成两倍音量时则输入512，依此类推
        [options setPlayerOptionIntValue:256 forKey:@"vol"];
        _options = options;
    }
    return _options;
}

-(UIImageView *)placeHolderImgView{
    if (!_placeHolderImgView) {
        _placeHolderImgView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, self.frame.size.width, self.frame.size.height)];
        _placeHolderImgView.contentMode = UIViewContentModeScaleAspectFill;
        _placeHolderImgView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        _placeHolderImgView.clipsToBounds = YES;
        
        UIBlurEffect *blurEffect = [UIBlurEffect effectWithStyle:UIBlurEffectStyleLight];
        UIVisualEffectView *effectView = [[UIVisualEffectView alloc] initWithEffect:blurEffect];
        effectView.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);
        //        effectView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        
        [_placeHolderImgView addSubview:effectView];
    }
    return _placeHolderImgView;
}

- (void)play{
    
    if (_moviePlayer) {
        [self shutdown];
    }
    
    [self addSubview:self.moviePlayer.view];
    [self bringSubviewToFront:self.placeHolderImgView];
    self.placeHolderImgView.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);
    [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
    //添加监听
    [self addObserveForMoviePlayer];
    
}

- (void)shutdown{
    //    UIViewController * app = [UIApplication sharedApplication].delegate;
    //    app.shouldChangeOrientation = NO;
    self.app.shouldChangeOrientation = 0;
    [_moviePlayer shutdown];
    [_moviePlayer.view removeFromSuperview];
    _moviePlayer = nil;
    [self removeMovieNotificationObservers];
    [[UIApplication sharedApplication] setIdleTimerDisabled:NO];
}

//rn 刷新直播播放器
- (void)reload{
    [self play];
}

- (void)downloadImage{
    
    NSURL *url = [NSURL URLWithString:self.placeHolder];
    NSURLRequest *reque = [NSURLRequest requestWithURL:url];
    NSURLSession *session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration ephemeralSessionConfiguration]];
    
    __weak typeof(self) weakSelf = self;
    NSURLSessionDownloadTask *task = [session downloadTaskWithRequest:reque completionHandler:^(NSURL *location, NSURLResponse *response, NSError *error) {
        dispatch_async(dispatch_get_main_queue(), ^{
            
            weakSelf.placeHolderImgView.image = [UIImage imageWithData:[NSData dataWithContentsOfURL:location]];
        });
    }];
    
    [task resume];
}

//rn props
- (void) setStarted:(BOOL) started{
    if(started != _started){
        if(started){
            [_moviePlayer play];
            _started = started;
        }else{
            [_moviePlayer pause];
            _started = started;
        }
    }
}

- (void) setMuted:(BOOL) muted {
    _muted = muted;
}
- (void) setLock:(BOOL) lock {
    _isLocked = lock;
    //self.app.orientation = currentOrientation;
    self.app.shouldChangeOrientation = _isLocked?3:2;
    //    UIInterfaceOrientation currentOrientation = [UIApplication sharedApplication].statusBarOrientation;
    //    if(currentOrientation== UIInterfaceOrientationPortrait){
    //        self.app.orientation = UIInterfaceOrientationMaskPortrait;
    //    }else if(currentOrientation==UIInterfaceOrientationLandscapeLeft){
    //        self.app.orientation = UIInterfaceOrientationMaskLandscapeLeft;
    //    }else{
    //        self.app.orientation = UIInterfaceOrientationMaskLandscapeRight;
    //    }
}


#pragma mark - Notification
- (void)addObserveForMoviePlayer {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(loadStateDidChange:)
                                                 name:IJKMPMoviePlayerLoadStateDidChangeNotification
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(moviePlayBackDidFinish)
                                                 name:IJKMPMoviePlayerPlaybackDidFinishNotification
                                               object:nil];
    // app退到后台
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(appDidEnterBackground) name:UIApplicationWillResignActiveNotification object:nil];
    // app进入前台
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(appDidEnterPlayground) name:UIApplicationDidBecomeActiveNotification object:nil];
    
    // 监听耳机插入和拔掉通知
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(audioRouteChangeListenerCallback:) name:AVAudioSessionRouteChangeNotification object:nil];
    
    // 监测设备方向
    [[UIDevice currentDevice] beginGeneratingDeviceOrientationNotifications];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(onDeviceOrientationChange)
                                                 name:UIDeviceOrientationDidChangeNotification
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(onStatusBarOrientationChange)
                                                 name:UIApplicationDidChangeStatusBarOrientationNotification
                                               object:nil];
}


- (void)removeMovieNotificationObservers {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)loadStateDidChange:(NSNotification*)notification {
    IJKMPMovieLoadState loadState = _moviePlayer.loadState;
    IJKMPMovieLoadState state = _moviePlayer.loadState;
    //IJKMPMoviePlaybackState state = _moviePlayer.playbackState;
    
    if ((loadState & IJKMPMovieLoadStatePlaythroughOK) != 0) { //shouldAutoplay 为yes 在这种状态下会自动开始播放
        if (!self.moviePlayer.isPlaying) {
            [self.moviePlayer play];
            [self bringSubviewToFront:self.moviePlayer.view];
        }
        if(self.onPlayState){
            self.onPlayState(@{@"state": @(state)});
        }
        NSLog(@"播放状态:  阔以播放 %d\n",(int)loadState);
    }else if ((loadState & IJKMPMovieLoadStateStalled) != 0) { //如果正在播放,会在此状态下暂停
        NSLog(@"播放状态: IJKMPMovieLoadStateStalled: %d\n", (int)loadState);
        if(self.onPlayState){
            self.onPlayState(@{@"state": @(state)});
        }
    }
    if(loadState ==  IJKMPMovieLoadStatePlaythroughOK){
        NSLog(@"播放状态:  播放成功");
    }
    if(state == IJKMPMovieLoadStateUnknown){
        NSLog(@"播放状态:  不知道原因");
    }else if(state == IJKMPMovieLoadStatePlayable){
        NSLog(@"播放状态:  可以播放");
    }else if(state ==  IJKMPMovieLoadStatePlaythroughOK){
        NSLog(@"播放状态:  播放成功");
    }else if(state == IJKMPMovieLoadStateStalled){
        NSLog(@"播放状态: -----");
    }
    
    NSLog(@"播放状态: ???: %d\n", (int)loadState);
    NSLog(@"loadStateDidChange: +++: %f\n", _moviePlayer.fpsInMeta);
    NSLog(@"loadStateDidChange: ---: %lld\n", _moviePlayer.numberOfBytesTransferred);
    //  NSLog(@"loadStateDidChange: +++: %f\n", _moviePlayer.fpsInMeta);
    //NSLog(@"loadStateDidChange: +++: %f\n", _moviePlayer.playbackRate);
}
-(void)moviePlayBackDidFinish{
    //IJKMPMovieLoadState loadState = _moviePlayer.loadState;
    int state = _moviePlayer.playbackState;
    if(self.onPlayState){
        self.onPlayState(@{@"state": @(state)});
    }
    
    if(state==0&&_moviePlayer){
        [_moviePlayer pause];
    }
    //    NSLog(@"当前播放器状态1:%d\n",(int)loadState);
    NSLog(@"当前播放器状态2:%d",state);
}
/**
 *  设置横屏的约束
 */
- (void)setOrientationLandscapeConstraint:(UIInterfaceOrientation)orientation {
    [self toOrientation:orientation];
    self.isFullScreen = YES;
}
/**
 *  设置竖屏的约束
 */
- (void)setOrientationPortraitConstraint {
    [self toOrientation:UIInterfaceOrientationPortrait];
    self.isFullScreen = NO;
}

- (void)toOrientation:(UIInterfaceOrientation)orientation {
    // 获取到当前状态条的方向
    UIInterfaceOrientation currentOrientation = [UIApplication sharedApplication].statusBarOrientation;
    // 判断如果当前方向和要旋转的方向一致,那么不做任何操作
    
    // 根据要旋转的方向,使用Masonry重新修改限制
    if (orientation != UIInterfaceOrientationPortrait) {
        // 这个地方加判断是为了从 全屏的一侧,直接到全屏的另一侧不用修改限制,否则会出错;
        if (currentOrientation == UIInterfaceOrientationPortrait) {
            ZFBrightnessView *brightnessView = [ZFBrightnessView sharedBrightnessView];
            [[UIApplication sharedApplication].keyWindow bringSubviewToFront:brightnessView];
            [[UIApplication sharedApplication].keyWindow insertSubview:self belowSubview:brightnessView];
        }
        if (currentOrientation != UIInterfaceOrientationPortrait) {
            //[self removeFromSuperview];
            [self mas_remakeConstraints:^(MASConstraintMaker *make) {
                make.width.equalTo(@(ScreenWidth));
                make.height.equalTo(@(ScreenHeight));
                make.center.equalTo([UIApplication sharedApplication].keyWindow);
            }];
        }
    }else {
        if (currentOrientation == UIInterfaceOrientationPortrait) {
            [self mas_remakeConstraints:^(MASConstraintMaker *make) {
                make.width.equalTo(@(ScreenWidth));
                make.height.equalTo(@(211));
                make.top.mas_equalTo(@(0));
                // make.center.equalTo([UIApplication sharedApplication].keyWindow);
            }];
        }
    }
    // iOS6.0之后,设置状态条的方法能使用的前提是shouldAutorotate为NO,也就是说这个视图控制器内,旋转要关掉;
    // 也就是说在实现这个方法的时候-(BOOL)shouldAutorotate返回值要为NO
    // [[UIApplication sharedApplication] setStatusBarOrientation:orientation animated:NO];
    // 获取旋转状态条需要的时间:
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationDuration:0.3];
    // 更改了状态条的方向,但是设备方向UIInterfaceOrientation还是正方向的,这就要设置给你播放视频的视图的方向设置旋转
    // 给你的播放视频的view视图设置旋转
    // self.transform = CGAffineTransformIdentity;
    // self.transform = [self getTransformRotationAngle];
    //    self.playerModel.fatherView.transform = CGAffineTransformIdentity;
    //    self.playerModel.fatherView.transform = [self getTransformRotationAngle];
    // 开始旋转
    //NSLog(@"进入这里5");
    [UIView commitAnimations];
}
/**
 * 获取变换的旋转角度
 *
 * @return 角度
 */
- (CGAffineTransform)getTransformRotationAngle {
    // 状态条的方向已经设置过,所以这个就是你想要旋转的方向
    UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
    // 根据要进行旋转的方向来计算旋转的角度
    if (orientation == UIInterfaceOrientationPortrait) {
        //return CGAffineTransformMakeRotation(M_PI_2);
        return CGAffineTransformIdentity;
    } else if (orientation == UIInterfaceOrientationLandscapeLeft){
        return CGAffineTransformMakeRotation(-M_PI_2);
    } else if(orientation == UIInterfaceOrientationLandscapeRight){
        return CGAffineTransformMakeRotation(M_PI_2);
    }
    return CGAffineTransformIdentity;
}
/** 手戳全屏 */
- (void)fullScreenAction {
    //NSLog(@"进入这里");
    if (_isLocked) {
        return;
    }
    NSLog(@"进入这里");
    if (self.isFullScreen) {
        [self interfaceOrientation:UIInterfaceOrientationPortrait];
        self.isFullScreen = NO;
        // return;
    } else {
        UIDeviceOrientation orientation = [UIDevice currentDevice].orientation;
        if (orientation == UIDeviceOrientationLandscapeRight) {
            [self interfaceOrientation:UIInterfaceOrientationLandscapeLeft];
        } else {
            [self interfaceOrientation:UIInterfaceOrientationLandscapeRight];
        }
        // self.isFullScreen = YES;
    }
    if(self.onFullScreen){
        self.onFullScreen(@{@"isfull": @(self.isFullScreen)});
    }
}

/**
 *  获取系统音量
 */
- (void)configureVolume {
    MPVolumeView *volumeView = [[MPVolumeView alloc] init];
    _volumeViewSlider = nil;
    for (UIView *view in [volumeView subviews]){
        if ([view.class.description isEqualToString:@"MPVolumeSlider"]){
            _volumeViewSlider = (UISlider *)view;
            break;
        }
    }
    
    // 使用这个category的应用不会随着手机静音键打开而静音，可在手机静音下播放声音
    NSError *setCategoryError = nil;
    BOOL success = [[AVAudioSession sharedInstance]
                    setCategory: AVAudioSessionCategoryPlayback
                    error: &setCategoryError];
    
    if (!success) { /* handle the error in setCategoryError */ }
}

#pragma mark - Getter
- (ZFBrightnessView *)brightnessView {
    if (!_brightnessView) {
        _brightnessView = [ZFBrightnessView sharedBrightnessView];
    }
    return _brightnessView;
}

#pragma mark - UIPanGestureRecognizer手势方法

/**
 *  pan手势事件
 *
 *  @param pan UIPanGestureRecognizer
 */
- (void)panDirection:(UIPanGestureRecognizer *)pan {
    //根据在view上Pan的位置，确定是调音量还是亮度
    CGPoint locationPoint = [pan locationInView:self];
    // 我们要响应水平移动和垂直移动
    // 根据上次和本次移动的位置，算出一个速率的point
    CGPoint veloctyPoint = [pan velocityInView:self];
    
    // 判断是垂直移动还是水平移动
    switch (pan.state) {
        case UIGestureRecognizerStateBegan: { // 开始移动
            // 使用绝对值来判断移动的方向
            CGFloat x = fabs(veloctyPoint.x);
            CGFloat y = fabs(veloctyPoint.y);
            if (x > y) { // 水平移动
                // 取消隐藏
                self.panDirection = PanDirectionHorizontalMoved;
            } else if (x < y) { // 垂直移动
                self.panDirection = PanDirectionVerticalMoved;
                // 开始滑动的时候,状态改为正在控制音量
                if (locationPoint.x > self.bounds.size.width / 2) {
                    self.isVolume = YES;
                }else { // 状态改为显示亮度调节
                    self.isVolume = NO;
                }
            }
            break;
        }
        case UIGestureRecognizerStateChanged: { // 正在移动
            switch (self.panDirection) {
                case PanDirectionVerticalMoved:{
                    [self verticalMoved:veloctyPoint.y]; // 垂直移动方法只要y方向的值
                    break;
                }
                default:
                    break;
            }
            break;
        }
        case UIGestureRecognizerStateEnded: { // 移动停止
            // 移动结束也需要判断垂直或者平移
            // 比如水平移动结束时，要快进到指定位置，如果这里没有判断，当我们调节音量完之后，会出现屏幕跳动的bug
            switch (self.panDirection) {
                case PanDirectionVerticalMoved:{
                    // 垂直移动结束后，把状态改为不再控制音量
                    self.isVolume = NO;
                    break;
                }
                default:
                    break;
            }
            break;
        }
        default:
            break;
    }
}

/**
 *  pan垂直移动的方法
 *
 *  @param value void
 */
- (void)verticalMoved:(CGFloat)value {
    self.isVolume ? (self.volumeViewSlider.value -= value / 10000) : ([UIScreen mainScreen].brightness -= value / 10000);
}
#pragma mark - NSNotification Action
/**
 *   轻拍方法
 *
 *  @param gesture UITapGestureRecognizer
 */
- (void)singleTapAction:(UIGestureRecognizer *)gesture {
    if (gesture.state == UIGestureRecognizerStateRecognized) {
        if(self.onClick){
            self.onClick(@{@"isSingle": @"true"});
        }
    }
}
- (void)createGesture {
    // 单击
    self.singleTap = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(singleTapAction:)];
    self.singleTap.delegate                = self;
    self.singleTap.numberOfTouchesRequired = 1; //手指数
    self.singleTap.numberOfTapsRequired    = 1;
    [self addGestureRecognizer:self.singleTap];
    
    // 双击(播放/暂停)
    //    self.doubleTap = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(doubleTapAction:)];
    //    self.doubleTap.delegate                = self;
    //    self.doubleTap.numberOfTouchesRequired = 1; //手指数
    //    self.doubleTap.numberOfTapsRequired    = 2;
    //    [self addGestureRecognizer:self.doubleTap];
    
    // 解决点击当前view时候响应其他控件事件
    [self.singleTap setDelaysTouchesBegan:YES];
    // [self.doubleTap setDelaysTouchesBegan:YES];
    // 双击失败响应单击事件
    //[self.singleTap requireGestureRecognizerToFail:self.doubleTap];
    
    UIPanGestureRecognizer *panRecognizer = [[UIPanGestureRecognizer alloc]initWithTarget:self action:@selector(panDirection:)];
    panRecognizer.delegate = self;
    [panRecognizer setMaximumNumberOfTouches:1];
    [panRecognizer setDelaysTouchesBegan:YES];
    [panRecognizer setDelaysTouchesEnded:YES];
    [panRecognizer setCancelsTouchesInView:YES];
    [self addGestureRecognizer:panRecognizer];
}
/**
 *  应用退到后台
 */
- (void)appDidEnterBackground {
    self.didEnterBackground     = YES;
    if(self.moviePlayer){
        [self.moviePlayer stop];
    }
}


/**
 *  应用进入前台
 */
- (void)appDidEnterPlayground {
    self.didEnterBackground  = NO;
    if(self.moviePlayer){
        [self.moviePlayer play];
        // [self play];
    }
}
/**
 *  屏幕转屏
 *
 *  @param orientation 屏幕方向
 */
- (void)interfaceOrientation:(UIInterfaceOrientation)orientation {
    if (orientation == UIInterfaceOrientationLandscapeRight || orientation == UIInterfaceOrientationLandscapeLeft) {
        // 设置横屏
        [self setOrientationLandscapeConstraint:orientation];
    } else if (orientation == UIInterfaceOrientationPortrait) {
        // 设置竖屏
        [self setOrientationPortraitConstraint];
    }
}
//设备方向发生变化
- (void)onDeviceOrientationChange {
    //锁屏状态不发生变化
    if(!_moviePlayer||self.isLocked||self.didEnterBackground){return;}
    UIDeviceOrientation orientation = [UIDevice currentDevice].orientation;
    UIInterfaceOrientation interfaceOrientation = (UIInterfaceOrientation)orientation;
    if (orientation == UIDeviceOrientationFaceUp || orientation == UIDeviceOrientationFaceDown || orientation == UIDeviceOrientationUnknown ) { return;}
    switch (interfaceOrientation) {
        case UIInterfaceOrientationPortraitUpsideDown:{
            return;
        }
            break;
        case UIInterfaceOrientationPortrait:{
            if (self.isFullScreen) {
                self.isFullScreen = NO;
                [self toOrientation:UIInterfaceOrientationPortrait];
            }
        }
            break;
        case UIInterfaceOrientationLandscapeLeft:{
            if (self.isFullScreen == NO) {
                [self toOrientation:UIInterfaceOrientationLandscapeLeft];
                self.isFullScreen = YES;
            } else {
                [self toOrientation:UIInterfaceOrientationLandscapeLeft];
            }
        }
            break;
        case UIInterfaceOrientationLandscapeRight:{
            if (self.isFullScreen == NO) {
                [self toOrientation:UIInterfaceOrientationLandscapeRight];
                self.isFullScreen = YES;
            } else {
                [self toOrientation:UIInterfaceOrientationLandscapeRight];
            }
        }
            break;
        default:
            break;
    }
    if(self.onFullScreen){
        //self.didEnterBackground?!self.isFullScreen:self.isFullScreen
        //NSLog(@"进入这里了 %@",self.didEnterBackground?@"Yes":@"No");
        //NSLog(@"进入这里了++++++ %@",self.isFullScreen?@"Yes":@"No");
        // BOOL isFull = self.didEnterBackground?true:self.isFullScreen;
        self.onFullScreen(@{@"isfull": @(self.isFullScreen)});
    }
}
// 状态条变化通知（在前台播放才去处理）
- (void)onStatusBarOrientationChange {
    if (!self.didEnterBackground) {
        // 获取到当前状态条的方向
        UIInterfaceOrientation currentOrientation = [UIApplication sharedApplication].statusBarOrientation;
        if (currentOrientation == UIInterfaceOrientationPortrait) {
            [self setOrientationPortraitConstraint];
            [self.brightnessView removeFromSuperview];
            [[UIApplication sharedApplication].keyWindow addSubview:self.brightnessView];
            [self.brightnessView mas_remakeConstraints:^(MASConstraintMaker *make) {
                make.width.height.mas_equalTo(155);
                make.leading.mas_equalTo((ScreenWidth-155)/2);
                make.top.mas_equalTo((ScreenHeight-155)/2);
            }];
        }else{
            [self.brightnessView removeFromSuperview];
            [self addSubview:self.brightnessView];
            [self.brightnessView mas_remakeConstraints:^(MASConstraintMaker *make) {
                make.center.mas_equalTo(self);
                make.width.height.mas_equalTo(155);
            }];
        }
    }
}
/**
 *  耳机插入、拔出事件
 */
- (void)audioRouteChangeListenerCallback:(NSNotification*)notification {
    
}

-(void)dealloc{
    [self removeMovieNotificationObservers];
}

@end
//#import "RCTPlayer.h"
//#import "React/RCTBridgeModule.h"
//#import "React/RCTEventDispatcher.h"
//#import "React/UIView+React.h"
//#define SCREEN_WIDTH  [UIScreen mainScreen].bounds.size.width
//#define SCREEN_HEIGHT [UIScreen mainScreen].bounds.size.height
//@interface RCTPlayer()
///** 是否全屏*/
//@property (nonatomic, assign) BOOL                   isFullScreen;
///** 播放完了*/
//@property (nonatomic, assign) BOOL                   playDidEnd;
///** 进入后台*/
//@property (nonatomic, assign) BOOL                   didEnterBackground;
///** 是否锁定屏幕方向 */
//@property (nonatomic, assign) BOOL                   isLocked;
//@end
//@implementation RCTPlayer{
//    RCTEventDispatcher *_eventDispatcher;
//    PLPlayer *_plplayer;
//    bool _started;
//    bool _muted;
//    UIImageView *placeHolderImgView;
//    NSString * holder;
//    NSString * url;
//    float viewWidth;
//    float viewHeight;
//}
//
//static NSString *status[] = {
//    @"PLPlayerStatusUnknow",
//    @"PLPlayerStatusPreparing",
//    @"PLPlayerStatusReady",
//    @"PLPlayerStatusCaching",
//    @"PLPlayerStatusPlaying",
//    @"PLPlayerStatusPaused",
//    @"PLPlayerStatusStopped",
//    @"PLPlayerStatusError"
//};
//
//
//- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher
//{
//    if ((self = [super init])) {
//        _eventDispatcher = eventDispatcher;
//        _started = YES;
//        _muted = NO;
//        [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:nil];
//        self.reconnectCount = 0;
//    }
//
//    return self;
//};
//
//- (void) setSource:(NSDictionary *)source
//{
//    url = source[@"uri"];
//    //bool backgroundPlay = source[@"backgroundPlay"] == nil ? false : source[@"backgroundPlay"];
//    holder = source[@"holder"];
//    PLPlayerOption *option = [PLPlayerOption defaultOption];
//
//    // 更改需要修改的 option 属性键所对应的值
//    [option setOptionValue:@15 forKey:PLPlayerOptionKeyTimeoutIntervalForMediaPackets];
//
//    if(_plplayer){
//        [_plplayer stop]; //TODO View 被卸载时 也要调用
//    }
//
//    _plplayer = [PLPlayer playerWithURL:[[NSURL alloc] initWithString:url] option:option];
//
//    _plplayer.delegate = self;
//    _plplayer.delegateQueue = dispatch_get_main_queue();
//    //placeHolderImgView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, self.frame.size.width, self.frame.size.height)];
//    _plplayer.launchView = self.placeHolderImgView;
//    _plplayer.backgroundPlayEnable = false;
////    if(backgroundPlay){
////        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(startPlayer) name:UIApplicationWillEnterForegroundNotification object:nil];
////    }
//    NSLog(@"+++++++++%@",url);
//    NSLog(@"+++++++++%@",_plplayer.launchView);
//    [self setupUI];
//
//    // 添加通知
//    [self addNotifications];
//
//    [self startPlayer];
//
//}
//-(UIImageView *)placeHolderImgView{
//    if (!placeHolderImgView) {
//        placeHolderImgView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, 211)];
//        placeHolderImgView.contentMode = UIViewContentModeScaleAspectFill;
//        placeHolderImgView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
//        placeHolderImgView.clipsToBounds = YES;
//
//        UIBlurEffect *blurEffect = [UIBlurEffect effectWithStyle:UIBlurEffectStyleLight];
//        UIVisualEffectView *effectView = [[UIVisualEffectView alloc] initWithEffect:blurEffect];
//        effectView.frame = CGRectMake(0, 0, SCREEN_WIDTH, 211);
//        //        effectView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
//        NSURL *holderUrl = [NSURL URLWithString: holder];
//        placeHolderImgView.image = [UIImage imageWithData:[NSData dataWithContentsOfURL:holderUrl]];
//        [placeHolderImgView addSubview:effectView];
//    }
//    NSLog(@"+++++++++---------%@",holder);
//    return placeHolderImgView;
//}
//- (void)setupUI {
//    if (_plplayer.status != PLPlayerStatusError) {
//        // add player view
//        UIView *playerView = _plplayer.playerView;
//        [self addSubview:playerView];
//        [playerView setTranslatesAutoresizingMaskIntoConstraints:NO];
//
//        NSLayoutConstraint *centerX = [NSLayoutConstraint constraintWithItem:playerView attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterX multiplier:1.0 constant:0];
//        NSLayoutConstraint *centerY = [NSLayoutConstraint constraintWithItem:playerView attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterY multiplier:1.0 constant:0];
//        NSLayoutConstraint *width = [NSLayoutConstraint constraintWithItem:playerView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeWidth multiplier:1.0 constant:0];
//        NSLayoutConstraint *height = [NSLayoutConstraint constraintWithItem:playerView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeHeight multiplier:1.0 constant:0];
//        viewHeight = SCREEN_HEIGHT;
//        viewWidth = SCREEN_WIDTH;
//        NSArray *constraints = [NSArray arrayWithObjects:centerX, centerY,width,height, nil];
//        [self addConstraints: constraints];
//    }
//
//}
//
//- (void) setStarted:(BOOL) started{
//    if(started != _started){
//        if(started){
//            [_plplayer resume];
//            _started = started;
//        }else{
//            [_plplayer pause];
//            _started = started;
//        }
//    }
//}
//
//- (void) setMuted:(BOOL) muted {
//    _muted = muted;
//    [_plplayer setMute:muted];
//
//}
//
//- (void)startPlayer {
//    [UIApplication sharedApplication].idleTimerDisabled = YES;
//    [_plplayer play];
//    _started = true;
//}
//
//#pragma mark - <PLPlayerDelegate>
//
//- (void)player:(nonnull PLPlayer *)player statusDidChange:(PLPlayerStatus)state {
//    switch (state) {
//        case PLPlayerStatusCaching:
//                self.onLoading(@{@"target": self.reactTag});
//            break;
//        case PLPlayerStatusPlaying:
//                self.onPlaying(@{@"target": self.reactTag});
//            break;
//        case PLPlayerStatusPaused:
//            self.onPaused(@{@"target": self.reactTag});
//            break;
//        case PLPlayerStatusStopped:
//            self.onShutdown(@{@"target": self.reactTag});
//            break;
//        case PLPlayerStatusError:
//            self.onError(@{@"target": self.reactTag});
//            break;
//        default:
//            break;
//    }
//    NSLog(@"1231231231232131231%@", status[state]);
//}
//
//- (void)player:(nonnull PLPlayer *)player stoppedWithError:(nullable NSError *)error {
//    [self tryReconnect:error];
//}
//
//- (void)tryReconnect:(nullable NSError *)error {
//    if (self.reconnectCount < 10) {
//        _reconnectCount ++;
//        //UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"错误" message:[NSString stringWithFormat:@"错误 %@，播放器将在%.1f秒后进行第 %d 次重连", error.localizedDescription,0.5 * pow(2, self.reconnectCount - 1), _reconnectCount] delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
//        //[alert show];
//        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * pow(2, self.reconnectCount) * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
//            [_plplayer play];
//        });
//    }else {
//        [UIApplication sharedApplication].idleTimerDisabled = NO;
//    }
//    NSLog(@"1231231231232131231%@", error);
//}
//#pragma mark - 观察者、通知
//
///**
// *  添加观察者、通知
// */
//- (void)addNotifications {
//    // app退到后台
//    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(appDidEnterBackground) name:UIApplicationWillResignActiveNotification object:nil];
//    // app进入前台
//    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(appDidEnterPlayground) name:UIApplicationDidBecomeActiveNotification object:nil];
//
//    // 监听耳机插入和拔掉通知
//    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(audioRouteChangeListenerCallback:) name:AVAudioSessionRouteChangeNotification object:nil];
//
//    // 监测设备方向
//    [[UIDevice currentDevice] beginGeneratingDeviceOrientationNotifications];
//    [[NSNotificationCenter defaultCenter] addObserver:self
//                                             selector:@selector(onDeviceOrientationChange)
//                                                 name:UIDeviceOrientationDidChangeNotification
//                                               object:nil];
//
//    [[NSNotificationCenter defaultCenter] addObserver:self
//                                             selector:@selector(onStatusBarOrientationChange)
//                                                 name:UIApplicationDidChangeStatusBarOrientationNotification
//                                               object:nil];
//}
//
///**
// *  设置横屏的约束
// */
//- (void)setOrientationLandscapeConstraint:(UIInterfaceOrientation)orientation {
//    [self toOrientation:orientation];
//    self.isFullScreen = YES;
//}
///**
// *  设置竖屏的约束
// */
//- (void)setOrientationPortraitConstraint {
//    [self toOrientation:UIInterfaceOrientationPortrait];
//    self.isFullScreen = NO;
//}
//- (void)toOrientation:(UIInterfaceOrientation)orientation {
//    // 获取到当前状态条的方向
//    UIInterfaceOrientation currentOrientation = [UIApplication sharedApplication].statusBarOrientation;
//    // 判断如果当前方向和要旋转的方向一致,那么不做任何操作
//    if (currentOrientation == orientation) { return; }
//    // 根据要旋转的方向,使用Masonry重新修改限制
//    if (orientation != UIInterfaceOrientationPortrait) {
//        // 这个地方加判断是为了从 全屏的一侧,直接到全屏的另一侧不用修改限制,否则会出错;
//        if (currentOrientation == UIInterfaceOrientationPortrait) {
//           // [self removeFromSuperview];
//            [self mas_remakeConstraints:^(MASConstraintMaker *make) {
//                make.width.equalTo(@(SCREEN_HEIGHT));
//                make.height.equalTo(@(SCREEN_WIDTH));
//                make.center.equalTo([UIApplication sharedApplication].keyWindow);
//            }];
//        }
//    }else {
//        if (currentOrientation != UIInterfaceOrientationPortrait) {
//            [self mas_remakeConstraints:^(MASConstraintMaker *make) {
//                make.width.equalTo(@(viewWidth));
//                make.height.equalTo(@(211));
//                make.top.mas_equalTo(@(0));
//               // make.center.equalTo([UIApplication sharedApplication].keyWindow);
//            }];
//        }
//    }
//    // iOS6.0之后,设置状态条的方法能使用的前提是shouldAutorotate为NO,也就是说这个视图控制器内,旋转要关掉;
//    // 也就是说在实现这个方法的时候-(BOOL)shouldAutorotate返回值要为NO
//    [[UIApplication sharedApplication] setStatusBarOrientation:orientation animated:NO];
//    // 获取旋转状态条需要的时间:
//    [UIView beginAnimations:nil context:nil];
//    [UIView setAnimationDuration:0.3];
//    // 更改了状态条的方向,但是设备方向UIInterfaceOrientation还是正方向的,这就要设置给你播放视频的视图的方向设置旋转
//    // 给你的播放视频的view视图设置旋转
//    self.transform = CGAffineTransformIdentity;
//    self.transform = [self getTransformRotationAngle];
//    //    self.playerModel.fatherView.transform = CGAffineTransformIdentity;
//    //    self.playerModel.fatherView.transform = [self getTransformRotationAngle];
//    // 开始旋转
//    [UIView commitAnimations];
//}
//
///**
// * 获取变换的旋转角度
// *
// * @return 角度
// */
//- (CGAffineTransform)getTransformRotationAngle {
//    // 状态条的方向已经设置过,所以这个就是你想要旋转的方向
//    UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
//    // 根据要进行旋转的方向来计算旋转的角度
//    if (orientation == UIInterfaceOrientationPortrait) {
//        return CGAffineTransformIdentity;
//        //return CGAffineTransformMakeRotation(M_PI_2);
//    } else if (orientation == UIInterfaceOrientationLandscapeLeft){
//        return CGAffineTransformMakeRotation(-M_PI_2);
//    } else if(orientation == UIInterfaceOrientationLandscapeRight){
//        return CGAffineTransformMakeRotation(M_PI_2);
//    }
//    return CGAffineTransformIdentity;
//}
///** 全屏 */
//- (void)_fullScreenAction {
//
//}
//
//#pragma mark - NSNotification Action
//
///**
// *  播放完了
// *
// *  @param notification 通知
// */
//- (void)moviePlayDidEnd:(NSNotification *)notification {
//
//}
//
///**
// *  应用退到后台
// */
//- (void)appDidEnterBackground {
//   self.didEnterBackground     = YES;
//}
//
///**
// *  应用进入前台
// */
//- (void)appDidEnterPlayground {
//    self.didEnterBackground     = NO;
//}
////设备方向发生变化
//- (void)onDeviceOrientationChange {
//    NSLog(@"设备方向发生了变化");
//    if(!_plplayer){return;}
//    UIDeviceOrientation orientation = [UIDevice currentDevice].orientation;
//    UIInterfaceOrientation interfaceOrientation = (UIInterfaceOrientation)orientation;
//    if (orientation == UIDeviceOrientationFaceUp || orientation == UIDeviceOrientationFaceDown || orientation == UIDeviceOrientationUnknown ) { return; }
//    switch (interfaceOrientation) {
//        case UIInterfaceOrientationPortraitUpsideDown:{
//        }
//            break;
//        case UIInterfaceOrientationPortrait:{
//            if (self.isFullScreen) {
//                [self toOrientation:UIInterfaceOrientationPortrait];
//            }
//        }
//            break;
//        case UIInterfaceOrientationLandscapeLeft:{
//            if (self.isFullScreen == NO) {
//                [self toOrientation:UIInterfaceOrientationLandscapeLeft];
//                self.isFullScreen = YES;
//            } else {
//                [self toOrientation:UIInterfaceOrientationLandscapeLeft];
//            }
//
//        }
//            break;
//        case UIInterfaceOrientationLandscapeRight:{
//            if (self.isFullScreen == NO) {
//                [self toOrientation:UIInterfaceOrientationLandscapeRight];
//                self.isFullScreen = YES;
//            } else {
//                [self toOrientation:UIInterfaceOrientationLandscapeRight];
//            }
//        }
//            break;
//        default:
//            break;
//    }
//
//}
//// 状态条变化通知（在前台播放才去处理）
//- (void)onStatusBarOrientationChange {
//    if (!self.didEnterBackground) {
//        // 获取到当前状态条的方向
//        UIInterfaceOrientation currentOrientation = [UIApplication sharedApplication].statusBarOrientation;
//        if (currentOrientation == UIInterfaceOrientationPortrait) {
//          [self setOrientationPortraitConstraint];
//    }
// }
//}
///**
// *  耳机插入、拔出事件
// */
//- (void)audioRouteChangeListenerCallback:(NSNotification*)notification {
//
//}
//@end
//
//
////#import "RCTPlayer.h"
////#import "React/RCTBridgeModule.h"
////#import "React/RCTEventDispatcher.h"
////#import "React/UIView+React.h"
////#import "React/RCTConvert.h"
////#import "PLPlayerKit.h"
////#import <IJKMediaFramework/IJKMediaFramework.h>
//
////#define SCREEN_WIDTH  [UIScreen mainScreen].bounds.size.width
////#define SCREEN_HEIGHT [UIScreen mainScreen].bounds.size.height
////@interface RCTPlayer()
/////** 直播播放器*/
////@property (nonatomic, strong) IJKFFMoviePlayerController *moviePlayer;
/////** 播放器属性*/
////@property (nonatomic, strong) IJKFFOptions *options;
////@end
////
////@implementation RCTPlayer{
////            RCTEventDispatcher *_eventDispatcher;
////            bool _started;
//////            NSString    *URL;
//////            NSString    *placeHolder;
////            bool _muted;
////    }
////
////- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher
////{
////    NSLog(@"初始化123123");
////    if ((self = [super init])) {
////        _eventDispatcher = eventDispatcher;
////        //self.frame = CGRectMake(0, 0, ScreenWidth, ScreenHeight);
////        [self addSubview:self.placeHolderImgView];
////    }
////    return self;
////}
//////{
//////    NSDictionary *para = [RCTConvert NSDictionary:json];
//////    self.player.URL = para[@"URL"];
//////    self.player.placeHolder = para[@"holder"];
//////    [self.player downloadImage];
//////    [self.player play];
//////}
////- (void)setLiveParameter:(NSDictionary *)live{
////        //NSDictionary *live = [RCTConvert NSDictionary:json];
////        self.URL = live[@"URL"];
////        self.placeHolder = live[@"holder"];
////        [self downloadImage];
////        [self play];
////}
////- (IJKFFMoviePlayerController *)moviePlayer {
////
////    if (!_moviePlayer) {
////        IJKFFMoviePlayerController *moviePlayer = [[IJKFFMoviePlayerController alloc] initWithContentURLString:self.URL withOptions:self.options];
////        // 填充fill
////        moviePlayer.scalingMode = IJKMPMovieScalingModeAspectFill;
////        // 设置自动播放(必须设置为NO, 防止自动播放, 才能更好的控制直播的状态)
////        moviePlayer.shouldAutoplay = NO;
////        // 默认不显示
////        moviePlayer.shouldShowHudView = NO;
////        [moviePlayer prepareToPlay];
////        NSLog(@"初始化123123%@",self.URL);
////        moviePlayer.view.frame = CGRectMake(0, 0,self.frame.size.width, self.frame.size.height);
////        moviePlayer.view.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
////
////        _moviePlayer = moviePlayer;
////    }
////    return _moviePlayer;
////}
////
////- (IJKFFOptions *)options {
////    if (!_options) {
////        IJKFFOptions *options = [IJKFFOptions optionsByDefault];
////        [options setPlayerOptionIntValue:1  forKey:@"videotoolbox"];
////        // 帧速率(fps) 非标准桢率会导致音画不同步，所以只能设定为15或者29.97
////        [options setPlayerOptionIntValue:29.97 forKey:@"r"];
////        // 置音量大小，256为标准  要设置成两倍音量时则输入512，依此类推
////        [options setPlayerOptionIntValue:256 forKey:@"vol"];
////        _options = options;
////    }
////    return _options;
////}
////
////-(UIImageView *)placeHolderImgView{
////    if (!_placeHolderImgView) {
////
////        _placeHolderImgView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, self.frame.size.width, self.frame.size.height)];
////        _placeHolderImgView.contentMode = UIViewContentModeScaleAspectFill;
////        _placeHolderImgView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
////        _placeHolderImgView.clipsToBounds = YES;
////
////        UIBlurEffect *blurEffect = [UIBlurEffect effectWithStyle:UIBlurEffectStyleLight];
////        UIVisualEffectView *effectView = [[UIVisualEffectView alloc] initWithEffect:blurEffect];
////        effectView.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);
////        //        effectView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
////
////        [_placeHolderImgView addSubview:effectView];
////    }
////    return _placeHolderImgView;
////}
////
////- (void)play{
////
////    if (_moviePlayer) {
////
////        [self shutdown];
////    }
////
////    [self addSubview:self.moviePlayer.view];
////    [self bringSubviewToFront:self.placeHolderImgView];
////    self.placeHolderImgView.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);
////
////    //添加监听
////    [self addObserveForMoviePlayer];
////
////}
////
////
////- (void)shutdown{
////
////    [_moviePlayer shutdown];
////    [_moviePlayer.view removeFromSuperview];
////    _moviePlayer = nil;
////    [self removeMovieNotificationObservers];
////}
////
////- (void)downloadImage{
////
////    NSURL *url = [NSURL URLWithString:self.placeHolder];
////    NSURLRequest *reque = [NSURLRequest requestWithURL:url];
////    NSURLSession *session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration ephemeralSessionConfiguration]];
////
////    __weak typeof(self) weakSelf = self;
////    NSURLSessionDownloadTask *task = [session downloadTaskWithRequest:reque completionHandler:^(NSURL *location, NSURLResponse *response, NSError *error) {
////        dispatch_async(dispatch_get_main_queue(), ^{
////
////            weakSelf.placeHolderImgView.image = [UIImage imageWithData:[NSData dataWithContentsOfURL:location]];
////        });
////    }];
////
////    [task resume];
////}
////
////#pragma mark - Notification
////- (void)addObserveForMoviePlayer {
////    [[NSNotificationCenter defaultCenter] addObserver:self
////                                             selector:@selector(loadStateDidChange:)
////                                                 name:IJKMPMoviePlayerLoadStateDidChangeNotification
////                                               object:nil];
////}
////
////
////- (void)removeMovieNotificationObservers {
////
////    [[NSNotificationCenter defaultCenter] removeObserver:self];
////}
////
////- (void)loadStateDidChange:(NSNotification*)notification {
////    IJKMPMovieLoadState loadState = _moviePlayer.loadState;
////
////    if ((loadState & IJKMPMovieLoadStatePlaythroughOK) != 0) { //shouldAutoplay 为yes 在这种状态下会自动开始播放
////        if (!self.moviePlayer.isPlaying) {
////            [self.moviePlayer play];
////            [self bringSubviewToFront:self.moviePlayer.view];
////        }
////    }else if ((loadState & IJKMPMovieLoadStateStalled) != 0) { //如果正在播放,会在此状态下暂停
////        NSLog(@"loadStateDidChange: IJKMPMovieLoadStateStalled: %d\n", (int)loadState);
////    } else {
////        NSLog(@"loadStateDidChange: ???: %d\n", (int)loadState);
////    }
////}
////
////-(void)dealloc{
////
////    [self removeMovieNotificationObservers];
////}
////@end
//
////static BOOL s_autoPlay = NO;
////@interface RCTPlayer ()
////@property (nonatomic,copy)NSString *tempPlayauth;
////@property (nonatomic, assign)BOOL isLock;
////
////@property (nonatomic,strong)NSTimer *timer;
////@property (nonatomic,assign)BOOL isStatusHidden;
////@property (nonatomic, strong) AliVcMediaPlayer* mediaPlayer;
////@property(nonatomic ,strong)UIView *playerView;
////
////@end
////@implementation RCTPlayer{
////         RCTEventDispatcher *_eventDispatcher;
////        bool _started;
////        bool _muted;
////}
////- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher
////{
////    if ((self = [super init])) {
////        _eventDispatcher = eventDispatcher;
////        _started = YES;
////        _muted = NO;
////        [self initView];
////    }
////    return self;
////};
////#pragma mark - 播放器初始化
////- (UIView *)playerView{
////    if (!_playerView) {
////        _playerView = [[UIView alloc] init];
////    }
////    return  _playerView;
////}
////-(void)initView{
////    /***************集成部分*******************/
////    self.mediaPlayer = [[AliVcMediaPlayer alloc] init];
////    [self.mediaPlayer create:self.playerView];
////    [self.mediaPlayer prepareToPlay:[NSURL URLWithString:@"http://hls-live-qn.xingyan.panda.tv/panda-xingyan/afb55a303e72c70ab2f5fa0b534b5193.m3u8"]];
////    self.mediaPlayer.mediaType = MediaType_AUTO;
////    self.mediaPlayer.timeout = 10000;//毫秒
////    //self.mediaPlayer.dropBufferDuration = [self.dropBufferDurationTextField.text intValue];
////    /****************************************/
////    self.playerView.frame= CGRectMake(0, 0, self.bounds.size.width, self.bounds.size.height);
////     [self setBackgroundColor:[UIColor whiteColor]];
////    [self addSubview:self.playerView];
////}
////@end
//
////@interface RCTPlayer ()<AliyunVodPlayerViewDelegate>
////@property (nonatomic,copy)NSString *tempPlayauth;
////@property (nonatomic, assign)BOOL isLock;
////
////@property (nonatomic,strong)NSTimer *timer;
////@property (nonatomic,assign)BOOL isStatusHidden;
////@property (nonatomic, strong) AliyunVodPlayerView *playerView;
////
////@end
////@implementation RCTPlayer{
////     RCTEventDispatcher *_eventDispatcher;
////    bool _started;
////    bool _muted;
////}
////- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher
////{
////    if ((self = [super init])) {
////        _eventDispatcher = eventDispatcher;
////        _started = YES;
////        _muted = NO;
////        [self initView];
////    }
////    return self;
////};
////-(void)initView{
////    [self setBackgroundColor:[UIColor whiteColor]];
////    CGFloat width = 0;
////    CGFloat height = 0;
////    UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
////    if (orientation == UIInterfaceOrientationPortrait ) {
////        width = self.bounds.size.width;
////        height = self.bounds.size.width * 9 / 16.0;
////    }else{
////        width = self.bounds.size.height;
////        height = self.bounds.size.height * 9 / 16.0;
////    }
////
////    /****************UI播放器集成内容**********************/
////    self.playerView = [[AliyunVodPlayerView alloc] initWithFrame:CGRectMake(0,44, width, height) andSkin:AliyunVodPlayerViewSkinRed];
////
////    //测试封面地址，请使用https 地址。
////    self.playerView.coverUrl = [NSURL URLWithString:@"https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=4046104436,1338839104&fm=27&gp=0.jpg"];
////    //    [self.playerView setTitle:@"1234567890"];
////    self.playerView.circlePlay = YES;
////    [self.playerView setDelegate:self];
////    //    [self.playerView setAutoPlay:YES];
////
////    /*
////     *备注：isLockScreen 会锁定，播放器界面尺寸。
////     isLockPortrait yes：竖屏全屏；no：横屏全屏;
////     isLockScreen对isLockPortrait无效。
////     - (void)aliyunVodPlayerView:(AliyunVodPlayerView *)playerView lockScreen:(BOOL)isLockScreen此方法在isLockPortrait==yes时，返回的islockscreen总是yes；
////     isLockScreen和isLockPortrait，因为播放器时UIView，是否旋转需要配合UIViewController来控制物理旋转。
////     假设：支持竖屏全屏
////     self.playerView.isLockPortrait = YES;
////     self.playerView.isLockScreen = NO;
////     self.isLock = self.playerView.isLockScreen||self.playerView.isLockPortrait?YES:NO;
////
////     支持横屏全屏
////     self.playerView.isLockPortrait = NO;
////     self.playerView.isLockScreen = NO;
////     self.isLock = self.playerView.isLockScreen||self.playerView.isLockPortrait?YES:NO;
////
////     锁定屏幕
////     self.playerView.isLockPortrait = NO;
////     self.playerView.isLockScreen = YES;
////     self.isLock = self.playerView.isLockScreen||self.playerView.isLockPortrait?YES:NO;
////
////     self.isLock时来判定UIViewController 是否支持物理旋转。如果viewcontroller在navigationcontroller中，需要添加子类重写navigationgController中的 以下方法，根据实际情况做判定 。
////     */
////    self.playerView.isLockScreen = NO;
////    self.playerView.isLockPortrait = 1;
////    self.isLock = self.playerView.isLockScreen||self.playerView.isLockPortrait?YES:NO;
////
////
////    //播放本地视频
////    //NSString *path = [[NSBundle mainBundle] pathForResource:@"set.mp4" ofType:nil];
////    //    [self.playerView playViewPrepareWithURL:[NSURL fileURLWithPath:path]];
////    //播放器播放方式
////    //[self.playerView playViewPrepareWithVid:self.videoId accessKeyId:self.accessKeyId accessKeySecret:self.accessKeySecret securityToken:self.securityToken];
////    [self.playerView playViewPrepareWithURL:[NSURL URLWithString:@"https://pl-hls3.live.panda.tv/live_panda/4555408772c0e0d0b76947dd823170b2.m3u8?sign=4102cbf1e3c7b978ffbdff02b4dc7793&ts=5a31ecf8&rid=115299958"]];
////    NSLog(@"%@",[self.playerView getSDKVersion]);
////    [self addSubview:self.playerView];
////    /**************************************/
////}
////
////#pragma mark - AliyunVodPlayerViewDelegate
////- (void)onBackViewClickWithAliyunVodPlayerView:(AliyunVodPlayerView *)playerView{
////    if (self.playerView != nil) {
////        [self.playerView stop];
////        [self.playerView releasePlayer];
////        [self.playerView removeFromSuperview];
////        self.playerView = nil;
////    }
////
////   // [self dismissViewControllerAnimated:YES completion:nil];
////}
////
////
////- (void)aliyunVodPlayerView:(AliyunVodPlayerView*)playerView onPause:(NSTimeInterval)currentPlayTime{
////
////}
////- (void)aliyunVodPlayerView:(AliyunVodPlayerView*)playerView onResume:(NSTimeInterval)currentPlayTime{
////
////}
////- (void)aliyunVodPlayerView:(AliyunVodPlayerView*)playerView onStop:(NSTimeInterval)currentPlayTime{
////
////}
////- (void)aliyunVodPlayerView:(AliyunVodPlayerView*)playerView onSeekDone:(NSTimeInterval)seekDoneTime{
////
////}
////
////- (void)aliyunVodPlayerView:(AliyunVodPlayerView *)playerView lockScreen:(BOOL)isLockScreen{
////    self.isLock = isLockScreen;
////}
////
////
////- (void)aliyunVodPlayerView:(AliyunVodPlayerView*)playerView onVideoQualityChanged:(AliyunVodPlayerVideoQuality)quality{
////
////}
////
////- (void)aliyunVodPlayerView:(AliyunVodPlayerView *)playerView fullScreen:(BOOL)isFullScreen{
////    NSLog(@"isfullScreen --%d",isFullScreen);
////
////    self.isStatusHidden = isFullScreen  ;
////    //[self setNeedsStatusBarAppearanceUpdate];
////
////}
////@end
//


