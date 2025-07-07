package com.simi.pictureselector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

public class PictureSelectorModule extends ReactContextBaseJavaModule {
    private final SimiSelectorModule simiSelectorModule;

    public PictureSelectorModule(@Nullable ReactApplicationContext reactContext) {
        super(reactContext);
        simiSelectorModule = new SimiSelectorModule(reactContext);
    }

    @NonNull
    @Override
    public String getName() {
        return "SimiSelector";
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    /**
     * 默认模式（图片：6 or 视频：1）
     */
    @ReactMethod
    public void openSelector(Promise promise) {
        simiSelectorModule.openSelector(promise);
    }

    /**
     * 打开simi照片选择器
     * 自定义选择参数
     *
     * @param options 可选
     * @param promise 返回LocalMedia数组
     */
    @ReactMethod
    public void openSelector(ReadableMap options, Promise promise) {
        simiSelectorModule.openSelector(options, promise);
    }

}
