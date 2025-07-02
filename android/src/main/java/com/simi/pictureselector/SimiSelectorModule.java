package com.simi.pictureselector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.simi.pictureselector.basic.PictureSelector;
import com.simi.pictureselector.config.PictureMimeType;
import com.simi.pictureselector.config.SelectMimeType;
import com.simi.pictureselector.config.SelectModeConfig;
import com.simi.pictureselector.engine.CompressFileEngine;
import com.simi.pictureselector.entity.LocalMedia;
import com.simi.pictureselector.entity.MediaExtraInfo;
import com.simi.pictureselector.interfaces.OnKeyValueResultCallbackListener;
import com.simi.pictureselector.interfaces.OnResultCallbackListener;
import com.simi.pictureselector.interfaces.OnVideoThumbnailEventListener;
import com.simi.pictureselector.style.BottomNavBarStyle;
import com.simi.pictureselector.style.PictureSelectorStyle;
import com.simi.pictureselector.style.SelectMainStyle;
import com.simi.pictureselector.style.TitleBarStyle;
import com.simi.pictureselector.utils.DateUtils;
import com.simi.pictureselector.utils.DensityUtil;
import com.simi.pictureselector.utils.MediaUtils;
import com.simi.pictureselector.utils.PictureFileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import top.zibin.luban.Luban;
import top.zibin.luban.OnNewCompressListener;

public class SimiSelectorModule {
    private static final String TAG = "SimiSelectorModule";
    private static final boolean DEBUG = false;

    private final PictureSelectorStyle selectorStyle = new PictureSelectorStyle();
    private static final boolean DEFAULT_IS_SINGLE = false;
    private static final int DEFAULT_MAX_IMAGE_NUM = 6;
    private static final int DEFAULT_MAX_VIDEO_NUM = 1;
    private static final int DEFAULT_SELECT_MIME_TYPE = SelectMimeType.ofAll();//0: all , 1: image , 2: video , 3: audio
    private final ReactApplicationContext reactContext;

    public SimiSelectorModule(ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
        setCustomStyle(reactContext);
    }

    /**
     * 打开simi照片选择器
     * 默认模式
     *
     * @param promise 返回LocalMedia数组
     */
    public void openSelector(Promise promise) {
        openSelector(null, promise);
    }

    /**
     * 打开simi照片选择器
     * 自定义选择单个模式、图片张数、视频个数
     *
     * @param options {isSingle: false, maxImageNum: 6, int maxVideoNum: 1} 均为可选参数
     * @param promise 返回LocalMedia数组
     */
    public void openSelector(ReadableMap options, Promise promise) {
        try {
            boolean isSingle = DEFAULT_IS_SINGLE;
            int maxImageNum = DEFAULT_MAX_IMAGE_NUM;
            int maxVideoNum = DEFAULT_MAX_VIDEO_NUM;
            int selectMimeType = DEFAULT_SELECT_MIME_TYPE;

            if (options != null) {
                if (options.hasKey("isSingle")) {
                    isSingle = options.getBoolean("isSingle");
                }
                if (options.hasKey("maxImageNum")) {
                    maxImageNum = options.getInt("maxImageNum");
                }
                if (options.hasKey("maxVideoNum")) {
                    maxVideoNum = options.getInt("maxVideoNum");
                }
                if (options.hasKey("selectMimeType")) {
                    selectMimeType = options.getInt("selectMimeType");
                }
            }

            openSelector(isSingle, maxImageNum, maxVideoNum, selectMimeType, promise);
        } catch (Throwable e) {
            promise.reject("NATIVE_ERROR", e);
            Log.e(TAG, "openSelector: ", e);
        }
    }

    private void openSelector(boolean isSingleType, int maxSelectNum, int maxSelectVideoNum, int selectMimeType, Promise promise) {
        PictureSelector.create(reactContext.getCurrentActivity())
                .openGallery(selectMimeType)
                .setSelectorUIStyle(selectorStyle)
                .setSelectionMode(isSingleType ? SelectModeConfig.SINGLE : SelectModeConfig.MULTIPLE)
                .setImageEngine(GlideEngine.createGlideEngine())
                .setCompressEngine(new ImageFileCompressEngine())
                .setImageSpanCount(3)
                .isOriginalControl(true)
                .isPageStrategy(true)
                .isPageSyncAlbumCount(true)
                .setMaxSelectNum(maxSelectNum)
                .setMaxVideoSelectNum(maxSelectVideoNum)
                .setVideoThumbnailListener(getVideoThumbnailEventListener())
                .setQueryFilterListener(media -> false)
                .isDisplayCamera(false)
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        WritableArray medias = Arguments.createArray();
                        for (LocalMedia localMedia : result) {
                            WritableMap media = Arguments.createMap();
                            String mimeType = localMedia.getMimeType();
                            String path = localMedia.getPath();

                            media.putString("mediaType", mimeType);
                            String uri = localMedia.getCompressPath() != null ? localMedia.getCompressPath() : localMedia.getRealPath();
                            media.putString("uri", "file://" + uri);
                            media.putDouble("size", (double) localMedia.getSize());

                            setMediaDimensions(media, mimeType, path, localMedia.getWidth(), localMedia.getHeight());

                            if (PictureMimeType.isHasVideo(mimeType)) {
                                media.putString("videoImage", localMedia.getVideoThumbnailPath());
                            }
                            if (isSingleType) {
                                promise.resolve(media);
                                return;
                            }
                            medias.pushMap(media);
                        }
                        promise.resolve(medias);
                    }

                    @Override
                    public void onCancel() {
                        promise.reject("NATIVE_CANCEL", "User cancelled");
                    }
                });
    }

    private void setMediaDimensions(WritableMap media, String mimeType, String path, int width, int height) {
        if (PictureMimeType.isHasImage(mimeType)) {
            if (width == 0 || height == 0) {
                MediaExtraInfo info = MediaUtils.getImageSize(reactContext, path);
                media.putInt("width", info.getWidth());
                media.putInt("height", info.getHeight());
            } else {
                media.putInt("width", width);
                media.putInt("height", height);
            }
        } else if (PictureMimeType.isHasVideo(mimeType)) {
            if (width == 0 || height == 0) {
                MediaExtraInfo info = MediaUtils.getVideoSize(reactContext, path);
                media.putInt("videoImageWidth", info.getWidth());
                media.putInt("videoImageHeight", info.getHeight());
            } else {
                media.putInt("videoImageWidth", width);
                media.putInt("videoImageHeight", height);
            }
        }
    }

    private void setCustomStyle(Context context) {
        SelectMainStyle mainStyle = new SelectMainStyle();
        mainStyle.setSelectNumberStyle(true);
        mainStyle.setPreviewSelectNumberStyle(false);
        mainStyle.setPreviewDisplaySelectGallery(true);
        mainStyle.setSelectBackground(R.drawable.ps_default_num_selector);
        mainStyle.setPreviewSelectBackground(R.drawable.ps_preview_checkbox_selector);
        mainStyle.setSelectNormalBackgroundResources(R.drawable.ps_select_complete_normal_bg);
        mainStyle.setSelectNormalTextColor(ContextCompat.getColor(context, R.color.ps_color_aab2bd));
        mainStyle.setSelectNormalText(R.string.ps_send);
        mainStyle.setAdapterPreviewGalleryBackgroundResource(R.drawable.ps_preview_gallery_bg);
        mainStyle.setAdapterPreviewGalleryItemSize(DensityUtil.dip2px(context, 52));
        mainStyle.setPreviewSelectTextSize(14);
        mainStyle.setPreviewSelectTextColor(ContextCompat.getColor(context, R.color.ps_color_white));
        mainStyle.setPreviewSelectMarginRight(DensityUtil.dip2px(context, 6));
        mainStyle.setSelectBackgroundResources(R.drawable.ps_select_complete_bg);
        mainStyle.setSelectText(R.string.ps_send_num);
        mainStyle.setSelectTextColor(ContextCompat.getColor(context, R.color.ps_color_white));
        mainStyle.setMainListBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_black));
        mainStyle.setCompleteSelectRelativeTop(false);
        mainStyle.setPreviewSelectRelativeBottom(false);
        mainStyle.setAdapterItemIncludeEdge(false);

        TitleBarStyle titleStyle = new TitleBarStyle();
        titleStyle.setHideCancelButton(true);
        titleStyle.setAlbumTitleRelativeLeft(true);
        titleStyle.setTitleAlbumBackgroundResource(R.drawable.ps_album_bg);
        titleStyle.setTitleDrawableRightResource(R.drawable.ps_ic_grey_arrow);
        titleStyle.setPreviewTitleLeftBackResource(R.drawable.ps_ic_normal_back);

        BottomNavBarStyle navStyle = new BottomNavBarStyle();
        navStyle.setBottomPreviewNarBarBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_half_grey));
        navStyle.setBottomPreviewNormalText(R.string.ps_preview);
        navStyle.setBottomPreviewNormalTextColor(ContextCompat.getColor(context, R.color.ps_color_9b));
        navStyle.setBottomPreviewNormalTextSize(16);
        navStyle.setCompleteCountTips(false);
        navStyle.setBottomPreviewSelectText(R.string.ps_preview_num);
        navStyle.setBottomPreviewSelectTextColor(ContextCompat.getColor(context, R.color.ps_color_white));

        selectorStyle.setTitleBarStyle(titleStyle);
        selectorStyle.setBottomBarStyle(navStyle);
        selectorStyle.setSelectMainStyle(mainStyle);
    }

    private static class ImageFileCompressEngine implements CompressFileEngine {
        @Override
        public void onStartCompress(Context context, ArrayList<Uri> source, OnKeyValueResultCallbackListener call) {
            Luban.with(context)
                    .load(source)
                    .ignoreBy(100)
                    .setRenameListener(filePath -> {
                        int indexOf = filePath.lastIndexOf(".");
                        String postfix = indexOf != -1 ? filePath.substring(indexOf) : ".jpg";
                        return DateUtils.getCreateFileName("CMP_") + postfix;
                    })
                    .filter(path -> PictureMimeType.isUrlHasImage(path) && !PictureMimeType.isUrlHasGif(path))
                    .setCompressListener(new OnNewCompressListener() {
                        @Override
                        public void onStart() {
                        }

                        @Override
                        public void onSuccess(String source, File compressFile) {
                            if (call != null) {
                                call.onCallback(source, compressFile.getAbsolutePath());
                            }
                        }

                        @Override
                        public void onError(String source, Throwable e) {
                            if (call != null) {
                                call.onCallback(source, null);
                            }
                        }
                    })
                    .launch();
        }
    }

    private OnVideoThumbnailEventListener getVideoThumbnailEventListener() {
        return new MeOnVideoThumbnailEventListener(getVideoThumbnailDir());
    }

    private static class MeOnVideoThumbnailEventListener implements OnVideoThumbnailEventListener {
        private final String targetPath;

        public MeOnVideoThumbnailEventListener(String targetPath) {
            this.targetPath = targetPath;
        }

        @Override
        public void onVideoThumbnail(Context context, String videoPath, OnKeyValueResultCallbackListener call) {
            Glide.with(context).asBitmap().sizeMultiplier(0.6F).load(videoPath).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    resource.compress(Bitmap.CompressFormat.JPEG, 60, stream);

                    String result = null;
                    FileOutputStream fos = null;
                    try {
                        File targetFile = new File(targetPath, "thumbnails_" + System.currentTimeMillis() + ".jpg");
                        fos = new FileOutputStream(targetFile);
                        fos.write(stream.toByteArray());
                        fos.flush();
                        result = targetFile.getAbsolutePath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        PictureFileUtils.close(fos);
                        PictureFileUtils.close(stream);
                    }

                    if (call != null) {
                        call.onCallback(videoPath, result);
                    }
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    if (call != null) {
                        call.onCallback(videoPath, "");
                    }
                }
            });
        }
    }

    private String getVideoThumbnailDir() {
        File dir = reactContext.getExternalFilesDir("SimiThumbnail");
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        return dir != null ? dir.getAbsolutePath() + File.separator : "";
    }
}
