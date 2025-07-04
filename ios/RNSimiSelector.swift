import Foundation
import UIKit
import Photos
import ZLPhotoBrowser
import React

@objc(RNSimiSelector)
class RNSimiSelector: NSObject {
  
  var images: [UIImage] = []
  var assets: [PHAsset] = []
  var hasSelectVideo: Bool = false
  var selectedMedias: [[String: Any]] = []
}

extension RNSimiSelector: RCTBridgeModule {
  static func moduleName() -> String! {
    return "SimiSelector"
  }

  static func requiresMainQueueSetup() -> Bool {
    return true
  }

  @objc
  func openSelector(_ options: NSDictionary,
                    resolver resolve: @escaping RCTPromiseResolveBlock,
                    rejecter reject: @escaping RCTPromiseRejectBlock) {
    DispatchQueue.main.async {
      // 默认配置
      var isSingle = true
      var isCrop = false
      var maxImageNum = 6
      var maxVideoNum = 1
      var selectMimeType = 0
      var language = ZLLanguageType.chineseSimplified
      var isMixSelect = false

      if let dict = options as? [String: Any] {
        if let val = dict["isSingle"] as? Bool { isSingle = val }
        if let val = dict["isCrop"] as? Bool { isCrop = val }
        if let val = dict["maxImageNum"] as? Int { maxImageNum = val }
        if let val = dict["maxVideoNum"] as? Int { maxVideoNum = val }
        if let val = dict["selectMimeType"] as? Int { selectMimeType = val }
        if let val = dict["isMixSelect"] as? Bool { isMixSelect = val }
        if let lang = dict["selectLanguage"] as? String, lang.lowercased().contains("en") {
          language = .english
        }
      }

      let maxCount = isSingle ? 1 : maxImageNum + maxVideoNum
      let canSelectVideo = (selectMimeType == 0 || selectMimeType == 2)

      self.openPhotoPicker(allowMixSelect: isMixSelect,
                           canSelectVideo: canSelectVideo,
                           maxCount: maxCount,
                           maxVideoCount: maxVideoNum,
                           language: language,
                           allowEditImage: isCrop,
                           resolve: resolve,
                           reject: reject)
    }
  }

  private func openPhotoPicker(allowMixSelect: Bool,
                                canSelectVideo: Bool,
                                maxCount: Int,
                                maxVideoCount: Int,
                                language: ZLLanguageType,
                                allowEditImage: Bool,
                                resolve: @escaping RCTPromiseResolveBlock,
                                reject: @escaping RCTPromiseRejectBlock) {

    self.images.removeAll()
    self.assets.removeAll()
    self.selectedMedias.removeAll()

    guard let rootVC = UIApplication.shared.delegate?.window??.rootViewController else {
      reject("NO_ROOT", "No root view controller", nil)
      return
    }

    let uiConfig = ZLPhotoUIConfiguration.default()
    uiConfig.languageType = language
    uiConfig.columnCount = 3
    uiConfig.themeColor = UIColor(red: 16/255, green: 175/255, blue: 255/255, alpha: 1.0)

    let config = ZLPhotoConfiguration.default()
    config.allowSelectImage = true
    config.allowSelectVideo = canSelectVideo && self.images.isEmpty
    config.allowSelectGif = false
    config.allowSelectLivePhoto = true
    config.allowSelectOriginal = true
    config.allowMixSelect = allowMixSelect
    config.maxSelectCount = maxCount
    config.maxVideoSelectCount = maxVideoCount
    config.allowTakePhotoInLibrary = false
    config.allowEditImage = allowEditImage

    let picker = ZLPhotoPicker()

    picker.selectImageBlock = { results, _ in
      self.handlePickedResults(results) { result in
        resolve(result)
      }
    }

    picker.cancelBlock = {
      reject("-1", "User cancelled", nil)
    }

    picker.showPhotoLibrary(sender: rootVC)
  }

  private func handlePickedResults(_ results: [ZLResultModel],
                                   completion: @escaping (Any) -> Void) {
    var selectedImages: [UIImage] = []
    var selectedAssets: [PHAsset] = []
    var tempMedias: [[String: Any]] = []
    let group = DispatchGroup()

    for result in results {
      if let img = result.image {
        selectedImages.append(img)
      }
      if let asset = result.asset {
        selectedAssets.append(asset)
        processAsset(result, group: group, mediaArray: &tempMedias)
      }
    }

    group.notify(queue: .main) {
      self.images.append(contentsOf: selectedImages)
      self.assets.append(contentsOf: selectedAssets)
      self.hasSelectVideo = selectedAssets.first?.mediaType == .video
      self.selectedMedias.append(contentsOf: tempMedias)

      if tempMedias.count > 1 {
        completion(tempMedias)
      } else if tempMedias.count == 1 {
        completion(tempMedias[0])
      } else {
        completion([])
      }
    }
  }

  private func processAsset(_ result: ZLResultModel,
                            group: DispatchGroup,
                            mediaArray: inout [[String: Any]]) {
    guard let asset = result.asset else { return }

    var media: [String: Any] = [
      "mediaType": mediaType(of: asset),
      "width": asset.pixelWidth,
      "height": asset.pixelHeight
    ]

    group.enter()
    ZLPhotoManager.fetchAssetFilePath(for: asset) { path in
      if let path = path {
        media["uri"] = path
      }

      if let resource = PHAssetResource.assetResources(for: asset).first {
        media["fileName"] = resource.originalFilename
      }

      media["size"] = self.fetchFormattedAssetSize(asset)

      if asset.mediaType == .video {
        group.enter()
        self.generateVideoThumbnail(for: asset) { thumbnailPath in
          if let path = thumbnailPath {
            media["videoImage"] = path
          }
          group.leave()
        }
      }

      DispatchQueue.global().async {
        mediaArray.append(media)
      }

      group.leave()
    }
  }

  private func generateVideoThumbnail(for asset: PHAsset,
                                      completion: @escaping (String?) -> Void) {
    let maxSide: CGFloat = 300
    let width = CGFloat(asset.pixelWidth)
    let height = CGFloat(asset.pixelHeight)
    let scale = min(maxSide / width, maxSide / height)
    let size = CGSize(width: width * scale, height: height * scale)

    ZLPhotoManager.fetchImage(for: asset, size: size, progress: nil) { image, isDegraded in
      if let image = image, !isDegraded {
        let path = self.saveThumbnailToTemp(image)
        completion(path)
      } else {
        completion(nil)
      }
    }
  }

  private func saveThumbnailToTemp(_ image: UIImage) -> String? {
    guard let data = image.jpegData(compressionQuality: 0.8) else { return nil }
    let fileName = UUID().uuidString + ".jpg"
    let filePath = NSTemporaryDirectory().appending(fileName)
    let url = URL(fileURLWithPath: filePath)

    do {
      try data.write(to: url)
      return filePath
    } catch {
      print("❌ 保存缩略图失败: \(error)")
      return nil
    }
  }

  private func fetchFormattedAssetSize(_ asset: PHAsset) -> String? {
    guard let resource = PHAssetResource.assetResources(for: asset).first else { return nil }
    if let fileSize = resource.value(forKey: "fileSize") as? NSNumber {
      let kb = fileSize.doubleValue / 1024
      if kb >= 1024 {
        return String(format: "%.2f MB", kb / 1024)
      } else {
        return String(format: "%.0f KB", kb)
      }
    }
    return nil
  }

  private func mediaType(of asset: PHAsset) -> String {
    switch asset.mediaType {
    case .image: return "image"
    case .video: return "video"
    case .audio: return "audio"
    default: return "unknown"
    }
  }

  @objc
  func clearPhotos(_ clearPhotos: Bool, clearVideos: Bool, resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
    // 预留接口，暂不处理
    resolve(nil)
  }
}

