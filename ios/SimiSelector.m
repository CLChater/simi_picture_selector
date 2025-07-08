//
//  SimiSelector.m
//  SimiTalk
//
//  Created by edy on 2025/7/4.
//

#import "SimiSelector.h"
#import "UIView+LCToast.h"
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

    config.canSelectAsset = ^BOOL(PHAsset *_Nonnull asset) {
      NSNumber *sizeStr = [SimiSelector fetchFormattedAssetSize:asset];

      NSString *imageSizeLimitStr = [NSString
          stringWithFormat:@"%.2f", (float)imageSizeLimit / 1024 / 1024];
      NSString *videoSizeLimitStr = [NSString
          stringWithFormat:@"%.2f", (float)videoSizeLimit / 1024 / 1024];

      if (asset.mediaType == PHAssetMediaTypeImage) {
        NSString *tip =
            (language == ZLLanguageTypeChineseSimplified)
                ? [NSString stringWithFormat:@"选择图片不能大于%@ MB",
                                             imageSizeLimitStr]
                : [NSString
                      stringWithFormat:
                          @"Select an image that cannot be lager than %@ MB",
                          imageSizeLimitStr];
        if (imageSizeLimit > 0 && sizeStr.intValue > imageSizeLimit) {

          dispatch_async(dispatch_get_main_queue(), ^{
            [[UIApplication sharedApplication].delegate.window
                lc_showToast:tip];
          });

          return NO;
        }
        return YES;
      } else if (asset.mediaType == PHAssetMediaTypeVideo) {
        NSString *tip =
            (language == ZLLanguageTypeChineseSimplified)
                ? [NSString stringWithFormat:@"选择视频不能大于%@ MB",
                                             videoSizeLimitStr]
                : [NSString
                      stringWithFormat:
                          @"Select an video that cannot be lager than %@ MB",
                          videoSizeLimitStr];
        if (videoSizeLimit > 0 && sizeStr.intValue > videoSizeLimit) {

          dispatch_async(dispatch_get_main_queue(), ^{
            [[UIApplication sharedApplication].delegate.window
                lc_showToast:tip];
          });
          return NO;
        }
        return YES;
      } else {
        return YES;
      }
      return YES;
    };

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
                   if (path) {
                     media[@"uri"] = path;
                   }

                   // 名称
                   NSArray<PHAssetResource *> *resources =
                       [PHAssetResource assetResourcesForAsset:result.asset];
                   PHAssetResource *resource = resources.firstObject;
                   if (resource) {
                     // 文件名
                     media[@"fileName"] = resource.originalFilename;
                     NSLog(@"--------name：%@", media[@"fileName"]);
                   }

                   // 大小
                   NSNumber *sizeStr =
                       [SimiSelector fetchFormattedAssetSize:asset];
                   if (sizeStr) {
                     media[@"size"] = sizeStr;
                     NSLog(@"--------size：%@", media[@"size"]);
                   }

                   // 视频处理
                   if (asset.mediaType == PHAssetMediaTypeVideo) {
                     dispatch_group_enter(group);
                     [self
                         generateVideoThumbnailForAsset:asset
                                                  group:group
                                             completion:^(
                                                 NSString
                                                     *_Nullable thumbnailPath) {
                                               if (thumbnailPath) {
                                                 media[@"videoImage"] =
                                                     thumbnailPath;
                                               }
                                               dispatch_group_leave(group);
                                             }];
                   }

                   dispatch_async(dispatch_get_global_queue(
                                      DISPATCH_QUEUE_PRIORITY_DEFAULT, 0),
                                  ^{
                                    @synchronized(mediaArray) {
                                      [mediaArray addObject:media];
                                    }
                                  });

                   dispatch_group_leave(group);
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
//    [ZLPhotoManager fetchAssetFilePathFor:asset completion:^(NSString
//    *_Nullable path) {
//        if (path) {
//            media[@"uri"] = path;
//        }
//
//        // 文件名
//        NSArray<PHAssetResource *> *resources = [PHAssetResource
//        assetResourcesForAsset:result.asset]; PHAssetResource *resource =
//        resources.firstObject; if (resource) {
//            media[@"fileName"] = resource.originalFilename;
//            NSLog(@"--------name：%@", media[@"fileName"]);
//        }
//
//        // 大小
//        NSNumber *sizeStr = [SimiSelector fetchFormattedAssetSize:asset];
//        if (sizeStr) {
//            media[@"size"] = sizeStr;
//            NSLog(@"--------size：%@", media[@"size"]);
//        }
//
//        /// ✅ 图片压缩
//        if (asset.mediaType == PHAssetMediaTypeImage) {
//            dispatch_group_enter(group);
//            PHImageRequestOptions *options = [[PHImageRequestOptions alloc]
//            init]; options.resizeMode = PHImageRequestOptionsResizeModeFast;
//            options.deliveryMode =
//            PHImageRequestOptionsDeliveryModeHighQualityFormat;
//            options.networkAccessAllowed = YES;
//
//            CGSize targetSize = CGSizeMake(asset.pixelWidth,
//            asset.pixelHeight);
//            [[PHImageManager defaultManager] requestImageForAsset:asset
//                                                        targetSize:targetSize
//                                                       contentMode:PHImageContentModeDefault
//                                                           options:options
//                                                     resultHandler:^(UIImage *
//                                                     _Nullable image,
//                                                     NSDictionary * _Nullable
//                                                     info) {
//                if (image) {
//                    NSData *jpegData = UIImageJPEGRepresentation(image, 0.8);
//                    if (jpegData) {
//                        NSString *fileName = [NSString
//                        stringWithFormat:@"%@.jpg", [[NSUUID UUID]
//                        UUIDString]]; NSString *tempPath =
//                        [NSTemporaryDirectory()
//                        stringByAppendingPathComponent:fileName]; if
//                        ([jpegData writeToFile:tempPath atomically:YES]) {
//                            if (tempPath) {
//                                media[@"uri"] = tempPath;
//
//                                // ✅ 用压缩文件获取文件名
//                                media[@"fileName"] = [tempPath
//                                lastPathComponent];
//
//                                // ✅ 获取文件大小
//                                NSDictionary *attrs = [[NSFileManager
//                                defaultManager]
//                                attributesOfItemAtPath:tempPath error:nil];
//                                NSNumber *fileSize = attrs[NSFileSize];
//                                if (fileSize) {
//                                    media[@"size"] = fileSize;
//                                }
//
//                                NSLog(@"压缩图片成功：%@, size: %@", tempPath,
//                                fileSize);
//
//                            }
//
//                            NSLog(@"压缩图片成功：%@", tempPath);
//                        }
//                    }
//                }
//                dispatch_group_leave(group);
//            }];
//        }
//
//        /// ✅ 视频压缩导出
//        if (asset.mediaType == PHAssetMediaTypeVideo) {
//            dispatch_group_enter(group);
//            AVAsset *avAsset = [AVAsset assetWithURL:[NSURL
//            URLWithString:media[@"uri"]]]; AVAssetExportSession *exportSession
//            = [[AVAssetExportSession alloc] initWithAsset:avAsset
//                                                                                    presetName:AVAssetExportPresetMediumQuality];
//            NSString *fileName = [NSString stringWithFormat:@"%@.mp4",
//            [[NSUUID UUID] UUIDString]]; NSString *outputPath =
//            [NSTemporaryDirectory() stringByAppendingPathComponent:fileName];
//            NSURL *outputURL = [NSURL fileURLWithPath:outputPath];
//
//            exportSession.outputURL = outputURL;
//            exportSession.outputFileType = AVFileTypeMPEG4;
//            exportSession.shouldOptimizeForNetworkUse = YES;
//
//            [exportSession exportAsynchronouslyWithCompletionHandler:^{
//                if (exportSession.status ==
//                AVAssetExportSessionStatusCompleted) {
//
//                    media[@"uri"] = outputPath;
//
//                    // ✅ 用压缩文件获取文件名
//                    media[@"fileName"] = [outputPath lastPathComponent];
//
//                    // ✅ 获取文件大小
//                    NSDictionary *attrs = [[NSFileManager defaultManager]
//                    attributesOfItemAtPath:outputPath error:nil]; NSNumber
//                    *fileSize = attrs[NSFileSize]; if (fileSize) {
//                        media[@"size"] = fileSize;
//                    }
//
//                    NSLog(@"导出视频成功：%@", outputPath);
//
//                } else {
//                    NSLog(@"导出视频失败：%@", exportSession.error);
//                }
//                dispatch_group_leave(group);
//            }];
//        }
//
//        /// ✅ 视频缩略图处理
//        if (asset.mediaType == PHAssetMediaTypeVideo) {
//            dispatch_group_enter(group);
//            [self generateVideoThumbnailForAsset:asset group:group
//            completion:^(NSString *_Nullable thumbnailPath) {
//                if (thumbnailPath) {
//                    media[@"videoImage"] = thumbnailPath;
//                }
//                dispatch_group_leave(group);
//            }];
//        }
//
//        /// ✅ 最后加入数组
//        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT,
//        0), ^{
//            @synchronized(mediaArray) {
//                [mediaArray addObject:media];
//            }
//        });
//
//        dispatch_group_leave(group); // fetchAssetFilePath
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
