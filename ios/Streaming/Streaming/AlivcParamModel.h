//
//  AlivcParamModel.h
//  Streaming
//
//  Created by chendong on 2018/4/24.
//  Copyright © 2018年 Seatell. All rights reserved.
//

#import <Foundation/Foundation.h>

static NSString *AlivcParamModelReuseCellInput = @"AlivcParamModelReuseCellInput";
static NSString *AlivcParamModelReuseCellSlider = @"AlivcParamModelReuseCellSlider";
static NSString *AlivcParamModelReuseCellSwitch = @"AlivcParamModelReuseCellSwitch";
static NSString *AlivcParamModelReuseCellSwitchButton = @"AlivcParamModelReuseCellSwitchButton";
static NSString *AlivcParamModelReuseCellSegment = @"AlivcParamModelReuseCellSegment";


@interface AlivcParamModel : NSObject

@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *placeHolder;
@property (nonatomic, copy) NSString *reuseId;
@property (nonatomic, copy) NSString *infoText;
@property (nonatomic, copy) NSArray *segmentTitleArray;
//@property (nonatomic, assign) CGFloat defaultValue;

@property (nonatomic, copy) NSString *titleAppose; // 并排显示 switch title
//@property (nonatomic, assign) CGFloat defaultValueAppose; // 并排显示 switch value

@property (nonatomic, copy) void(^valueBlock)(int value);
@property (nonatomic, copy) void(^switchBlock)(int index, BOOL open);
@property (nonatomic, copy) void(^sliderBlock)(int value);
@property (nonatomic, copy) void(^segmentBlock)(int value);
@property (nonatomic, copy) void(^switchButtonBlock)(void);
@property (nonatomic, copy) void(^stringBlock)(NSString *message);

@end
