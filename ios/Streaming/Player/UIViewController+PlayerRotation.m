//
//  UIViewController+PlayerRotation.m
//  Streaming
//
//  Created by luo jian on 2018/1/3.
//  Copyright © 2018年 Seatell. All rights reserved.
//

#import "UIViewController+PlayerRotation.h"

@implementation UIViewController (PlayerRotation)
- (UIInterfaceOrientationMask)application:(UIApplication *)application supportedInterfaceOrientationsForWindow:(UIWindow *)window {
    
    
    if (self.shouldChangeOrientation == 0) {
        return UIInterfaceOrientationMaskPortrait;
        //return UIInterfaceOrientationMaskLandscapeLeft;
        //return UIInterfaceOrientationMaskAllButUpsideDown;
    }
    else if(self.shouldChangeOrientation == 1)
    {
        return UIInterfaceOrientationMaskLandscapeLeft;
    }else if(self.shouldChangeOrientation == 2){
        //    return UIInterfaceOrientationMaskAll;
        return UIInterfaceOrientationMaskAllButUpsideDown;
    }
    UIInterfaceOrientation currentOrientation = [UIApplication sharedApplication].statusBarOrientation;
    if(currentOrientation== UIInterfaceOrientationPortrait){
        return UIInterfaceOrientationMaskPortrait;
    }else if(currentOrientation==UIInterfaceOrientationLandscapeLeft){
        return UIInterfaceOrientationMaskLandscapeLeft;
    }else{
        return UIInterfaceOrientationMaskLandscapeRight;
    }
}
///**
// * 默认所有都不支持转屏,如需个别页面支持除竖屏外的其他方向，请在viewController重新下边这三个方法
// */
//
//// 是否支持自动转屏
//- (BOOL)shouldAutorotate {
//    return NO;
//}
//
//// 支持哪些屏幕方向
//- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
//    return UIInterfaceOrientationMaskPortrait;
//}
//
//// 默认的屏幕方向（当前ViewController必须是通过模态出来的UIViewController（模态带导航的无效）方式展现出来的，才会调用这个方法）
//- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation {
//    return UIInterfaceOrientationPortrait;
//}
//
//- (UIStatusBarStyle)preferredStatusBarStyle {
//    return UIStatusBarStyleDefault; // your own style
//}
//
//- (BOOL)prefersStatusBarHidden {
//    return NO; // your own visibility code
//}
//- (UIInterfaceOrientationMask)application:(UIApplication *)application supportedInterfaceOrientationsForWindow:(UIWindow *)window {
//    if (self.shouldChangeOrientation == YES) {
//        return UIInterfaceOrientationMaskAllButUpsideDown;
//    }
//    else
//    {
//        return UIInterfaceOrientationMaskPortrait;
//    }
//    //    return UIInterfaceOrientationMaskAll;
//}

//- (UIInterfaceOrientationMask)supportedInterfaceOrientationsForWindow:(UIInterfaceOrientationMask)oritention{
//    return oritention;
//    //    return UIInterfaceOrientationMaskAll;
//}

@end
