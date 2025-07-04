//
//  SimiSelector.h
//  SimiTalk
//
//  Created by edy on 2025/7/4.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

NS_ASSUME_NONNULL_BEGIN

@interface SimiSelector : NSObject<RCTBridgeModule>

@property (nonatomic, strong) NSMutableArray *images;

@property (nonatomic, strong) NSMutableArray *assets;

@property (nonatomic, assign) BOOL hasSelectVideo;

@property (nonatomic, strong) NSMutableArray <NSMutableDictionary *>*selectedMedias;


@end

NS_ASSUME_NONNULL_END
