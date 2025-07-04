require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "simi_picture_selector"
  s.version      = "1.0.0"
  s.summary      = "ImagePicker"
  s.homepage     = "https://github.com/CLChater/simi_picture_selector"
  s.license      = "MIT"
  s.authors      = "ImagePicker"

  s.source       = { :git => "https://github.com/CLChater/simi_picture_selector.git", :tag => "#{s.version}" }

  s.platforms    = { :ios => min_ios_version_supported }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.resources    = "ios/ZLPhotoBrowser.bundle"


  s.requires_arc = true
  s.swift_version = "5.0"


s.pod_target_xcconfig = {
  'SWIFT_OBJC_BRIDGING_HEADER' => '${PODS_ROOT}/../../node_modules/simi_picture_selector/ios/simi_picture_selector-Bridging-Header.h'
  'DEFINES_MODULE' => 'YES',
  'CLANG_ENABLE_MODULES' => 'YES'
}
  s.frameworks = 'UIKit', 'Photos'

  s.dependency "React"

  # s.vendored_framework = 'Eagleeye.xcframework'

  # Use install_modules_dependencies helper to install the dependencies if React Native version >=0.71.0.
  # See https://github.com/facebook/react-native/blob/febf6b7f33fdb4904669f99d795eba4c0f95d7bf/scripts/cocoapods/new_architecture.rb#L79.
  

  # if respond_to?(:install_modules_dependencies, true)
  #   install_modules_dependencies(s)
  # else
  #   s.dependency "React-Core"

  #   # Don't install the dependencies when we run `pod install` in the old architecture.
  #   if ENV['RCT_NEW_ARCH_ENABLED'] == '1' then
  #     s.compiler_flags = folly_compiler_flags + " -DRCT_NEW_ARCH_ENABLED=1"
  #     s.pod_target_xcconfig    = {
  #         "HEADER_SEARCH_PATHS" => "\"$(PODS_ROOT)/boost\"",
  #         "OTHER_CPLUSPLUSFLAGS" => "-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1",
  #         "CLANG_CXX_LANGUAGE_STANDARD" => "c++17"
  #     }
  #     s.dependency "React-Codegen"
  #     s.dependency "RCT-Folly"
  #     s.dependency "RCTRequired"
  #     s.dependency "RCTTypeSafety"
  #     s.dependency "ReactCommon/turbomodule/core"
  #   end
  # end
end
