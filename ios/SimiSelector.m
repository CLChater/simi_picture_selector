//
//  SimiSelector.m
//  SimiTalk
//
//  Created by edy on 2025/7/4.
//

#import "SimiSelector.h"
#import <Photos/Photos.h>
#import <React/RCTConvert.h>
#import <React/RCTEventDispatcher.h>
#import <ZLPhotoBrowser/ZLPhotoBrowser-Swift.h>

#define DEFAULT_IS_SINGLE NO
#define DEFAULT_CROP NO
#define DEFAULT_MAX_IMAGE_NUM 6
#define DEFAULT_MAX_VIDEO_NUM 1
#define DEFAULT_SELECT_MIME_TYPE 0
#define DEFAULT_LANGUAGE ZLLanguageTypeChineseSimplified
#define DEFAULT_MIX_SELECT NO
#define DEFAULT_IMAGE_SIZE_LIMIT 0
#define DEFAULT_VIDEO_SIZE_LIMIT 0

@implementation SimiSelector

RCT_EXPORT_MODULE();

- (instancetype)init {
  if (self = [super init]) {
  }
  return self;
}

RCT_EXPORT_METHOD(openSelector
                  : (NSDictionary *)options resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  @try {

    // 默认值
    BOOL isSingle = DEFAULT_IS_SINGLE;
    BOOL isCrop = DEFAULT_CROP;
    int maxImageNum = DEFAULT_MAX_IMAGE_NUM;
    int maxVideoNum = DEFAULT_MAX_VIDEO_NUM;
    int selectMimeType = DEFAULT_SELECT_MIME_TYPE;
    int selectLanguage = DEFAULT_LANGUAGE;
    BOOL isMixSelect = DEFAULT_MIX_SELECT;
    int imageSizeLimit = DEFAULT_IMAGE_SIZE_LIMIT;
    int videoSizeLimit = DEFAULT_VIDEO_SIZE_LIMIT;

    if (options != nil && [options isKindOfClass:[NSDictionary class]]) {
      if (options[@"isSingle"]) {
        isSingle = [options[@"isSingle"] boolValue];
      }
      if (options[@"maxImageNum"]) {
        maxImageNum = [options[@"maxImageNum"] intValue];
      }
      if (options[@"maxVideoNum"]) {
        maxVideoNum = [options[@"maxVideoNum"] intValue];
      }
      if (options[@"selectMimeType"]) {
        selectMimeType = [options[@"selectMimeType"]
            intValue]; // 0: all , 1: image , 2: video , 3: audio
      }
      if (options[@"selectLanguage"]) {
        NSString *lang = [options[@"selectLanguage"] lowercaseString];
        if ([lang containsString:@"en"]) {
          selectLanguage = ZLLanguageTypeEnglish;
        }
      }
      if (options[@"isCrop"]) {
        isCrop = [options[@"isCrop"] boolValue];
      }
      if (options[@"isMixSelect"]) {
        isMixSelect = [options[@"isMixSelect"] boolValue];
      }
      if (options[@"imageSizeLimit"]) {
        imageSizeLimit = [options[@"imageSizeLimit"] intValue];
      }
      if (options[@"videoSizeLimit"]) {
        videoSizeLimit = [options[@"videoSizeLimit"] intValue];
      }
    }

    int maxCount = maxImageNum + maxVideoNum;

    BOOL canSelectVideo = NO;
    if (selectMimeType == 0 || selectMimeType == 2) {
      canSelectVideo = YES;
    }

    if (isSingle) {
      maxCount = 1;
      maxImageNum = 1;
    }

    if (!isMixSelect) {
      maxCount = maxImageNum;
    }

    // 调用封装逻辑
    [self openPhotoPickerAllowMixSelect:isMixSelect
                               isSingle:isSingle
                         canSelectVideo:canSelectVideo
                               maxCount:maxCount
                          maxVideoCount:maxVideoNum
                               language:selectLanguage
                         allowEditImage:isCrop
                         imageSizeLimit:imageSizeLimit
                         videoSizeLimit:videoSizeLimit
                                resolve:resolve
                               rejecter:reject];

  } @catch (NSException *exception) {
    reject(@"NATIVE_ERROR", @"openSelector exception", nil);
  }
}

- (void)openPhotoPickerAllowMixSelect:(BOOL)allowMixSelect
                             isSingle:(BOOL)isSingle
                       canSelectVideo:(BOOL)canSelectVideo
                             maxCount:(int)maxCount
                        maxVideoCount:(int)maxVideoCount
                             language:(ZLLanguageType)language
                       allowEditImage:(BOOL)allowEditImage
                       imageSizeLimit:(int)imageSizeLimit
                       videoSizeLimit:(int)videoSizeLimit
                              resolve:(RCTPromiseResolveBlock)resolve
                             rejecter:(RCTPromiseRejectBlock)reject {

  dispatch_async(dispatch_get_main_queue(), ^{
    self.images = [NSMutableArray array];
    self.assets = [NSMutableArray array];
    [self.selectedMedias removeAllObjects];
    // 获取viewController
    UIViewController *rootVC =
        [UIApplication sharedApplication].delegate.window.rootViewController;

    ZLPhotoUIConfiguration *uiConfig = [ZLPhotoUIConfiguration default];
    uiConfig.languageType = language;
    uiConfig.minimumLineSpacing = 6;
    uiConfig.minimumInteritemSpacing = 6;
    uiConfig.columnCount = 3;
    uiConfig.cellCornerRadio = 16;
    uiConfig.themeColor = [UIColor colorWithRed:16.0 / 255.0
                                          green:175.0 / 255.0
                                           blue:255.0 / 255.0
                                          alpha:1];

    ZLPhotoConfiguration *config = [ZLPhotoConfiguration default];
    config.allowSelectImage = YES;
    config.allowSelectVideo =
        canSelectVideo ? (self.images.count == 0 ? YES : NO) : NO;
    config.allowSelectGif = NO;
    config.allowSelectLivePhoto = YES;
    config.allowSelectOriginal = YES;
    config.allowMixSelect = allowMixSelect;
    config.maxSelectCount = maxCount;
    config.maxVideoSelectCount = maxVideoCount;
    config.allowTakePhotoInLibrary = NO;
    config.allowEditImage = allowEditImage;
    config.showSelectBtnWhenSingleSelect = YES;

    //        config.canSelectAsset = ^BOOL(PHAsset * _Nonnull asset) {
    //
    //            NSNumber *sizeStr = [SimiSelector
    //            fetchFormattedAssetSize:asset];
    //
    //            NSString *imageSizeLimitStr = [NSString
    //            stringWithFormat:@"%f",(float)imageSizeLimit/1024/1024];
    //            NSString *videoSizeLimitStr = [NSString
    //            stringWithFormat:@"%f",(float)videoSizeLimit/1024/1024];
    //
    //            if (asset.mediaType == PHAssetMediaTypeImage) {
    //                NSString *tip = (language ==
    //                ZLLanguageTypeChineseSimplified) ? [NSString
    //                stringWithFormat:@"选择图片不能大于%@", imageSizeLimitStr]
    //                : [NSString stringWithFormat:@"Select an image that cannot
    //                be lager than %@", imageSizeLimitStr]; if (imageSizeLimit
    //                > 0 && sizeStr.intValue > imageSizeLimit) {
    //                    dispatch_async(dispatch_get_main_queue(), ^{
    //
    //
    //                    });
    //
    //                    return NO;
    //                }
    //                return YES;
    //            }else if (asset.mediaType == PHAssetMediaTypeVideo) {
    //                NSString *tip = (language ==
    //                ZLLanguageTypeChineseSimplified) ? [NSString
    //                stringWithFormat:@"选择视频不能大于%@", videoSizeLimitStr]
    //                : [NSString stringWithFormat:@"Select an video that cannot
    //                be lager than %@", videoSizeLimitStr]; if (videoSizeLimit
    //                > 0 && sizeStr.intValue > videoSizeLimit) {
    //                    dispatch_async(dispatch_get_main_queue(), ^{
    //
    //                    });
    //
    //                    return NO;
    //                }
    //                return YES;
    //            }else {
    //                return YES;
    //            }
    //            return YES;
    //        };

    ZLPhotoPicker *picker = [[ZLPhotoPicker alloc] init];
    __weak typeof(self) weakSelf = self;

    picker.selectImageBlock =
        ^(NSArray<ZLResultModel *> *_Nonnull results, BOOL isOriginal) {
          [weakSelf handlePickedResults:results
                               isSingle:isSingle
                             completion:^(id result) {
                               resolve(result);
                             }];
        };

    picker.cancelBlock = ^{
      reject(@"-1", @"cancelAction", nil);
    };

    [picker showPhotoLibraryWithSender:rootVC];
  });
}

- (void)handlePickedResults:(NSArray<ZLResultModel *> *)results
                   isSingle:(BOOL)isSingle
                 completion:(void (^)(id result))completion {
  NSMutableArray<UIImage *> *selectedImages = [NSMutableArray array];
  NSMutableArray<PHAsset *> *selectedAssets = [NSMutableArray array];
  NSMutableArray<NSDictionary *> *tempMedias = [NSMutableArray array];

  dispatch_group_t group = dispatch_group_create();

  for (ZLResultModel *result in results) {
    if (result.image) {
      [selectedImages addObject:result.image];
    }

    if (result.asset) {
      [selectedAssets addObject:result.asset];
      [self processAsset:result group:group toArray:tempMedias];
    }
  }

  __weak typeof(self) weakSelf = self;
  dispatch_group_notify(group, dispatch_get_main_queue(), ^{
    [weakSelf.images addObjectsFromArray:selectedImages];
    [weakSelf.assets addObjectsFromArray:selectedAssets];
    weakSelf.hasSelectVideo =
        selectedAssets.firstObject.mediaType == PHAssetMediaTypeVideo;
    [weakSelf.selectedMedias
        addObjectsFromArray:[NSMutableArray arrayWithArray:tempMedias]];

    if (isSingle) {
      if (tempMedias.count == 1) {
        completion(weakSelf.selectedMedias.firstObject);
      } else {
        completion(@[]);
      }
    } else {
      if (tempMedias.count >= 1) {
        completion(weakSelf.selectedMedias);
      } else {
        completion(@[]);
      }
    }
  });
}

- (void)processAsset:(ZLResultModel *)result
               group:(dispatch_group_t)group
             toArray:(NSMutableArray<NSDictionary *> *)mediaArray {

  PHAsset *asset = result.asset;
  NSString *mediaType = @"";
  if (asset.mediaType == PHAssetMediaTypeImage) {
    mediaType = @"image";
  } else if (asset.mediaType == PHAssetMediaTypeVideo) {
    mediaType = @"video";
  } else if (asset.mediaType == PHAssetMediaTypeAudio) {
    mediaType = @"audio";
  } else {
    mediaType = @"unknown";
  }

  NSMutableDictionary *media = [@{
    @"mediaType" : mediaType,
    @"width" : @(asset.pixelWidth),
    @"height" : @(asset.pixelHeight)
  } mutableCopy];

  dispatch_group_enter(group);
  [ZLPhotoManager
      fetchAssetFilePathFor:asset
                 completion:^(NSString *_Nullable path) {
                   if (!path) {
                     dispatch_group_leave(group);
                     return;
                   }

                   // 根据类型做压缩
                   if (asset.mediaType == PHAssetMediaTypeImage) {
                     // 图片压缩
                     dispatch_group_enter(group);
                     [self compressImageAtPath:path
                                    completion:^(NSString *compressedPath) {
                                      media[@"uri"] = compressedPath ?: path;
                                      dispatch_group_leave(group);
                                    }];

                   } else if (asset.mediaType == PHAssetMediaTypeVideo) {
                     // 视频压缩
                     dispatch_group_enter(group);
                     [self
                         compressVideoAtPath:path
                                  completion:^(NSString *compressedPath) {
                                    media[@"uri"] = compressedPath ?: path;
                                    // 生成缩略图
                                    dispatch_group_enter(group);
                                    [self
                                        generateVideoThumbnailForAsset:asset
                                                                 group:group
                                                            completion:^(
                                                                NSString
                                                                    *_Nullable thumbnailPath) {
                                                              if (thumbnailPath) {
                                                                media[@"videoIm"
                                                                      @"age"] =
                                                                    thumbnailPath;
                                                              }
                                                              dispatch_group_leave(
                                                                  group);
                                                            }];
                                    dispatch_group_leave(group);
                                  }];

                   } else {
                     // 其他类型直接使用原路径
                     media[@"uri"] = path;
                   }

                   // 4. **在这里**再去读处理后文件的名称和大小
                   //    注意：compressedPath 已经写到
                   //    media[@"uri"]，我们直接用这个路径去构造资源
                   dispatch_async(dispatch_get_global_queue(
                                      DISPATCH_QUEUE_PRIORITY_DEFAULT, 0),
                                  ^{
                                    NSString *finalPath = media[@"uri"];
                                    // 4.1 文件名
                                    media[@"fileName"] =
                                        [finalPath lastPathComponent];
                                    // 4.2 大小
                                    NSError *err = nil;
                                    NSDictionary *attrs =
                                        [[NSFileManager defaultManager]
                                            attributesOfItemAtPath:finalPath
                                                             error:&err];
                                    if (!err && attrs) {
                                      media[@"size"] = attrs[NSFileSize];
                                    }

                                    // 5. 加入数组 & 最外层 leave
                                    @synchronized(mediaArray) {
                                      [mediaArray addObject:media];
                                    }
                                    dispatch_group_leave(group);
                                  });
                 }];
}

//- (void)processAsset:(ZLResultModel *)result
//               group:(dispatch_group_t)group
//             toArray:(NSMutableArray<NSDictionary *> *)mediaArray {
//
//    PHAsset *asset = result.asset;
//    NSString *mediaType;
//    switch (asset.mediaType) {
//        case PHAssetMediaTypeImage: mediaType = @"image"; break;
//        case PHAssetMediaTypeVideo: mediaType = @"video"; break;
//        case PHAssetMediaTypeAudio: mediaType = @"audio"; break;
//        default:                      mediaType = @"unknown"; break;
//    }
//
//    // 基础字段（先不填 uri/fileName/size）
//    NSMutableDictionary *media = [@{
//        @"mediaType": mediaType,
//        @"width": @(asset.pixelWidth),
//        @"height": @(asset.pixelHeight)
//    } mutableCopy];
//
//    // 最外层 enter
//    dispatch_group_enter(group);
//    [ZLPhotoManager fetchAssetFilePathFor:asset completion:^(NSString *
//    _Nullable origPath) {
//        if (!origPath) {
//            dispatch_group_leave(group);
//            return;
//        }
//
//        // 压缩后写入临时文件的回调
//        void (^onCompressed)(NSString *finalPath) = ^(NSString *finalPath) {
//            // 1. uri
//            media[@"uri"] = finalPath;
//            // 2. fileName
//            media[@"fileName"] = [finalPath lastPathComponent];
//            // 3. size
//            NSError *attrErr = nil;
//            NSDictionary *attrs = [[NSFileManager defaultManager]
//                                    attributesOfItemAtPath:finalPath
//                                                   error:&attrErr];
//            if (!attrErr && attrs) {
//                media[@"size"] = attrs[NSFileSize];
//            }
//            // 4. push 到数组
//            @synchronized(mediaArray) {
//                [mediaArray addObject:media];
//            }
//            // 5. leave
//            dispatch_group_leave(group);
//        };
//
//        // 根据类型压缩
//        if (asset.mediaType == PHAssetMediaTypeImage) {
//            dispatch_group_enter(group);
//            [self compressImageAtPath:origPath completion:^(NSString
//            *compressedPath) {
//                NSString *p = compressedPath ?: origPath;
//                onCompressed(p);
//                dispatch_group_leave(group);
//            }];
//
//        } else if (asset.mediaType == PHAssetMediaTypeVideo) {
//            dispatch_group_enter(group);
//            [self compressVideoAtPath:origPath completion:^(NSString
//            *compressedPath) {
//                NSString *p = compressedPath ?: origPath;
//                // 你也可以在这里再生成缩略图...
//                onCompressed(p);
//                dispatch_group_leave(group);
//            }];
//
//        } else {
//            // 其他类型直接复用原始文件
//            onCompressed(origPath);
//        }
//    }];
//}

#pragma mark - 图片压缩

- (void)compressImageAtPath:(NSString *)originalPath
                 completion:(void (^)(NSString *compressedPath))completion {
  dispatch_async(
      dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        UIImage *image = [UIImage imageWithContentsOfFile:originalPath];
        if (!image) {
          completion(nil);
          return;
        }
        // 按 0.7 质量压缩
        NSData *jpegData = UIImageJPEGRepresentation(image, 0.7);
        NSString *tmp = [NSTemporaryDirectory()
            stringByAppendingPathComponent:
                [NSString stringWithFormat:@"img_%@.jpg",
                                           [[NSUUID UUID] UUIDString]]];
        if ([jpegData writeToFile:tmp atomically:YES]) {
          completion(tmp);
        } else {
          completion(nil);
        }
      });
}

#pragma mark - 视频压缩

- (void)compressVideoAtPath:(NSString *)inputPath
                 completion:(void (^)(NSString *compressedPath))completion {
  NSURL *sourceURL = [NSURL fileURLWithPath:inputPath];
  AVURLAsset *asset = [AVURLAsset URLAssetWithURL:sourceURL options:nil];
  AVAssetExportSession *exporter = [[AVAssetExportSession alloc]
      initWithAsset:asset
         presetName:AVAssetExportPresetHighestQuality];
  NSString *outputPath = [NSTemporaryDirectory()
      stringByAppendingPathComponent:[NSString
                                         stringWithFormat:@"vid_%@.mp4",
                                                          [[NSUUID UUID]
                                                              UUIDString]]];
  exporter.outputURL = [NSURL fileURLWithPath:outputPath];
  exporter.outputFileType = AVFileTypeMPEG4;
  exporter.shouldOptimizeForNetworkUse = YES;

  [exporter exportAsynchronouslyWithCompletionHandler:^{
    if (exporter.status == AVAssetExportSessionStatusCompleted) {
      completion(outputPath);
    } else {
      NSLog(@"Video compression failed: %@", exporter.error);
      completion(nil);
    }
  }];
}

//- (void)processAsset:(ZLResultModel *)result
//               group:(dispatch_group_t)group
//             toArray:(NSMutableArray<NSDictionary *> *)mediaArray {
//
//    PHAsset *asset = result.asset;
//    NSString *mediaType = @"";
//    if (asset.mediaType == PHAssetMediaTypeImage) {
//        mediaType = @"image";
//    } else if (asset.mediaType == PHAssetMediaTypeVideo) {
//        mediaType = @"video";
//    } else if (asset.mediaType == PHAssetMediaTypeAudio) {
//        mediaType = @"audio";
//    } else {
//        mediaType = @"unknown";
//    }
//
//    NSMutableDictionary *media = [@{
//        @"mediaType" : mediaType,
//        @"width" : @(asset.pixelWidth),
//        @"height" : @(asset.pixelHeight)
//    } mutableCopy];
//
//    dispatch_group_enter(group);
//    [ZLPhotoManager
//     fetchAssetFilePathFor:asset
//     completion:^(NSString *_Nullable path) {
//        if (path) {
//            media[@"uri"] = path;
//        }
//
//        // 名称
//        NSArray<PHAssetResource *> *resources =
//        [PHAssetResource assetResourcesForAsset:result.asset];
//        PHAssetResource *resource = resources.firstObject;
//        if (resource) {
//            // 文件名
//            media[@"fileName"] = resource.originalFilename;
//            NSLog(@"--------name：%@", media[@"fileName"]);
//        }
//
//        // 大小
//        NSNumber *sizeStr =
//        [SimiSelector fetchFormattedAssetSize:asset];
//        if (sizeStr) {
//            media[@"size"] = sizeStr;
//            NSLog(@"--------size：%@", media[@"size"]);
//        }
//
//        // 视频处理
//        if (asset.mediaType == PHAssetMediaTypeVideo) {
//            dispatch_group_enter(group);
//            [self
//             generateVideoThumbnailForAsset:asset
//             group:group
//             completion:^(
//                          NSString
//                          *_Nullable thumbnailPath) {
//                              if (thumbnailPath) {
//                                  media[@"videoImage"] =
//                                  thumbnailPath;
//                              }
//                              dispatch_group_leave(group);
//                          }];
//        }
//
//        dispatch_async(dispatch_get_global_queue(
//                                                 DISPATCH_QUEUE_PRIORITY_DEFAULT,
//                                                 0),
//                       ^{
//            @synchronized(mediaArray) {
//                [mediaArray addObject:media];
//            }
//        });
//
//        dispatch_group_leave(group);
//    }];
//}

+ (nullable NSNumber *)fetchFormattedAssetSize:(PHAsset *)asset {
  PHAssetResource *resource =
      [PHAssetResource assetResourcesForAsset:asset].firstObject;
  if (!resource)
    return nil;

  @try {
    NSNumber *fileSize = [resource valueForKey:@"fileSize"];
    if ([fileSize isKindOfClass:[NSNumber class]]) {

      return fileSize;
    }
  } @catch (NSException *exception) {
    NSLog(@"❌ Failed to get fileSize : %@", exception);
  }
  return nil;
}

- (void)generateVideoThumbnailForAsset:(PHAsset *)asset
                                 group:(dispatch_group_t)group
                            completion:
                                (void (^)(NSString *_Nullable thumbnailPath))
                                    completion {

  CGFloat maxSide = 300.0;
  CGFloat width = asset.pixelWidth;
  CGFloat height = asset.pixelHeight;
  CGFloat scale = MIN(maxSide / width, maxSide / height);
  CGSize thumbnailSize = CGSizeMake(width * scale, height * scale);

  [ZLPhotoManager fetchImageFor:asset
                           size:thumbnailSize
                       progress:nil
                     completion:^(UIImage *_Nullable image, BOOL isDegraded) {
                       if (image && !isDegraded) {
                         NSString *path = [SimiSelector
                             saveThumbnailToTemporaryDirectory:image];
                         completion(path);
                       } else {
                         completion(nil);
                       }
                     }];
}

+ (NSString *)saveThumbnailToTemporaryDirectory:(UIImage *)image {
  if (!image)
    return nil;

  // 将 UIImage 压缩为 JPEG 格式的数据
  NSData *imageData = UIImageJPEGRepresentation(image, 0.8);
  if (!imageData)
    return nil;

  // 创建临时文件路径
  NSString *tempDirectory = NSTemporaryDirectory();
  NSString *fileName =
      [[NSUUID UUID].UUIDString stringByAppendingString:@".jpg"];
  NSString *filePath = [tempDirectory stringByAppendingPathComponent:fileName];

  // 写入数据到文件
  NSError *error = nil;
  BOOL success = [imageData writeToFile:filePath
                                options:NSDataWritingAtomic
                                  error:&error];

  if (success) {
    return filePath;
  } else {
    NSLog(@"❌ 保存缩略图失败: %@", error);
    return nil;
  }
}

RCT_EXPORT_METHOD(clearPhotos
                  : (BOOL)clearPhotos clearVideos
                  : (BOOL)clearVideos resolve
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  // 清空数组
}

- (NSMutableArray *)selectedMedias {
  if (_selectedMedias == nil) {
    _selectedMedias = [NSMutableArray array];
  }
  return _selectedMedias;
}

@end
