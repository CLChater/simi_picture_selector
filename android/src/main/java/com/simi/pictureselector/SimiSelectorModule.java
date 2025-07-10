package com.simi.pictureselector;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import com.simi.pictureselector.config.SelectLimitType;
import com.simi.pictureselector.config.SelectMimeType;
import com.simi.pictureselector.config.SelectModeConfig;
import com.simi.pictureselector.config.SelectorConfig;
import com.simi.pictureselector.engine.CompressFileEngine;
import com.simi.pictureselector.engine.CropFileEngine;
import com.simi.pictureselector.entity.LocalMedia;
import com.simi.pictureselector.entity.MediaExtraInfo;
import com.simi.pictureselector.interfaces.OnKeyValueResultCallbackListener;
import com.simi.pictureselector.interfaces.OnMediaEditInterceptListener;
import com.simi.pictureselector.interfaces.OnResultCallbackListener;
import com.simi.pictureselector.interfaces.OnSelectFilterListener;
import com.simi.pictureselector.interfaces.OnSelectLimitTipsListener;
import com.simi.pictureselector.interfaces.OnVideoThumbnailEventListener;
import com.simi.pictureselector.language.LanguageConfig;
import com.simi.pictureselector.style.BottomNavBarStyle;
import com.simi.pictureselector.style.PictureSelectorStyle;
import com.simi.pictureselector.style.SelectMainStyle;
import com.simi.pictureselector.style.TitleBarStyle;
import com.simi.pictureselector.utils.DateUtils;
import com.simi.pictureselector.utils.DensityUtil;
import com.simi.pictureselector.utils.ImageLoaderUtils;
import com.simi.pictureselector.utils.MediaUtils;
import com.simi.pictureselector.utils.PictureFileUtils;
import com.simi.pictureselector.utils.StyleUtils;
import com.simi.pictureselector.utils.ToastUtils;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropImageEngine;

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
    private static final boolean DEFAULT_CROP = false;
    private static final boolean DEFAULT_MIX_SELECT = false;//视频、图片混选
    private static final int DEFAULT_MAX_IMAGE_NUM = 6;
    private static final int DEFAULT_MAX_VIDEO_NUM = 1;
    private static final int DEFAULT_LANGUAGE = LanguageConfig.CHINESE; //zh 简体中文 en 英文
    private static final int DEFAULT_SELECT_MIME_TYPE = SelectMimeType.ofAll();//0: all , 1: image , 2: video , 3: audio
    private static final String DEFAULT_SELECT_MIME_TYPE_LIST = "jpg,jepg,gif,png,mov,mp4";
    private static final long DEFAULT_IMAGE_SIZE_LIMIT = 0;// 照片限制
    private static final long DEFAULT_VIDEO_SIZE_LIMIT = 0;// 视频限制
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
            boolean isCrop = DEFAULT_CROP;
            int maxImageNum = DEFAULT_MAX_IMAGE_NUM;
            int maxVideoNum = DEFAULT_MAX_VIDEO_NUM;
            int selectMimeType = DEFAULT_SELECT_MIME_TYPE;
            int selectLanguage = DEFAULT_LANGUAGE;
            boolean isMixSelect = DEFAULT_MIX_SELECT;
            long imageSizeLimit = DEFAULT_IMAGE_SIZE_LIMIT;
            long videoSizeLimit = DEFAULT_VIDEO_SIZE_LIMIT;


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
                if (options.hasKey("selectLanguage")) {
                    String language = options.getString("selectLanguage");
                    if (language != null && language.toLowerCase().contains("en")) {
                        selectLanguage = LanguageConfig.ENGLISH;
                    }
                }
                if (options.hasKey("isCrop")) {
                    isCrop = options.getBoolean("isCrop");
                }
                if (options.hasKey("isMixSelect")) {
                    isMixSelect = options.getBoolean("isMixSelect");
                }
                if (options.hasKey("imageSizeLimit")) {
                    imageSizeLimit = options.getInt("imageSizeLimit");
                }
                if (options.hasKey("videoSizeLimit")) {
                    videoSizeLimit = options.getInt("videoSizeLimit");
                }
            }

            openSelector(isSingle, maxImageNum, maxVideoNum, selectMimeType, selectLanguage, isCrop,
                    isMixSelect, imageSizeLimit, videoSizeLimit, promise);
        } catch (Throwable e) {
            promise.reject("NATIVE_ERROR", e);
            Log.e(TAG, "openSelector: ", e);
        }
    }

    private void openSelector(boolean isSingleType, int maxSelectNum, int maxSelectVideoNum, int selectMimeType,
                              int selectLanguage, boolean isCrop, boolean isMixSelect, long imageSizeLimit,
                              long videoSizeLimit, Promise promise) {
        PictureSelector.create(reactContext.getCurrentActivity())
                .openGallery(selectMimeType)
                .setSelectorUIStyle(selectorStyle)
                .setLanguage(selectLanguage)
                .setSelectionMode(isSingleType ? SelectModeConfig.SINGLE : SelectModeConfig.MULTIPLE)
                .isWithSelectVideoImage(isSingleType || isMixSelect)
                .setImageEngine(GlideEngine.createGlideEngine())
//                .setCropEngine(new ImageFileCropEngine())
                .setCompressEngine(new ImageFileCompressEngine())
                .setEditMediaInterceptListener(isCrop ? new MeOnMediaEditInterceptListener(getSandboxPath(), buildOptions()) : null)
                .setImageSpanCount(3)
                .isOriginalControl(true)
                .isPageStrategy(true)
                .isPageSyncAlbumCount(true)
                .setMaxSelectNum(maxSelectNum)
                .setMaxVideoSelectNum(maxSelectVideoNum)
                .isWebp(false)
                .isMaxSelectEnabledMask(true)
                .setVideoThumbnailListener(getVideoThumbnailEventListener())
                .setSelectFilterListener(media -> {
                    String mimeType = media.getMimeType();
                    long mediaSize = media.getSize();
                    if (PictureMimeType.isHasImage(mimeType) && imageSizeLimit != 0) {
                        return mediaSize > imageSizeLimit;
                    } else if (PictureMimeType.isHasVideo(mimeType) && imageSizeLimit != 0) {
                        return mediaSize > videoSizeLimit;
                    }
                    return false;
                })
                .setSelectLimitTipsListener(new OnSelectLimitTipsListener() {
                    @Override
                    public boolean onSelectLimitTips(Context context, @Nullable LocalMedia media, SelectorConfig config, int limitType) {
                        Log.d(TAG, "onSelectLimitTips: limitType = " + limitType);
                        if (limitType == SelectLimitType.SELECT_NOT_SUPPORT_SELECT_LIMIT) {
                            String mimeType = media != null ? media.getMimeType() : null;
                            if (PictureMimeType.isHasImage(mimeType)) {
                                ToastUtils.showToast(reactContext, reactContext.getString(R.string.ps_select_image_size_limit,
                                        PictureFileUtils.formatFileSize(imageSizeLimit)));
                            } else if (PictureMimeType.isHasVideo(mimeType)) {
                                ToastUtils.showToast(reactContext, reactContext.getString(R.string.ps_select_video_size_limit,
                                        PictureFileUtils.formatFileSize(videoSizeLimit)));
                            }
                            return true;
                        }
                        return false;
                    }
                })
                .setQueryFilterListener(media -> {
                    String[] split = media.getRealPath().split("\\.");
                    String aCase = split[split.length - 1].toLowerCase();
                    return !DEFAULT_SELECT_MIME_TYPE_LIST.contains(aCase);
                })
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
                            media.putString("fileName", localMedia.getFileName());
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
                media.putInt("width", info.getWidth());
                media.putInt("height", info.getHeight());
            } else {
                media.putInt("width", width);
                media.putInt("height", height);
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

    /**
     * 自定义编辑
     */
    private static class MeOnMediaEditInterceptListener implements OnMediaEditInterceptListener {
        private final String outputCropPath;
        private final UCrop.Options options;

        public MeOnMediaEditInterceptListener(String outputCropPath, UCrop.Options options) {
            this.outputCropPath = outputCropPath;
            this.options = options;
        }

        @Override
        public void onStartMediaEdit(Fragment fragment, LocalMedia currentLocalMedia, int requestCode) {
            String currentEditPath = currentLocalMedia.getAvailablePath();
            Uri inputUri = PictureMimeType.isContent(currentEditPath)
                    ? Uri.parse(currentEditPath) : Uri.fromFile(new File(currentEditPath));
            Uri destinationUri = Uri.fromFile(
                    new File(outputCropPath, DateUtils.getCreateFileName("CROP_") + ".jpeg"));
            UCrop uCrop = UCrop.of(inputUri, destinationUri);
            options.setHideBottomControls(false);
            uCrop.withOptions(options);
            uCrop.setImageEngine(new UCropImageEngine() {
                @Override
                public void loadImage(Context context, String url, ImageView imageView) {
                    if (!ImageLoaderUtils.assertValidRequest(context)) {
                        return;
                    }
                    Glide.with(context).load(url).override(180, 180).into(imageView);
                }

                @Override
                public void loadImage(Context context, Uri url, int maxWidth, int maxHeight, OnCallbackListener<Bitmap> call) {
                    Glide.with(context).asBitmap().load(url).override(maxWidth, maxHeight).into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            if (call != null) {
                                call.onCall(resource);
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            if (call != null) {
                                call.onCall(null);
                            }
                        }
                    });
                }
            });
            uCrop.startEdit(fragment.requireActivity(), fragment, requestCode);
        }
    }

//    /**
//     * 自定义裁剪
//     */
//    private class ImageFileCropEngine implements CropFileEngine {
//
//        @Override
//        public void onStartCrop(Fragment fragment, Uri srcUri, Uri destinationUri, ArrayList<String> dataSource, int requestCode) {
//            UCrop.Options options = buildOptions();
//            UCrop uCrop = UCrop.of(srcUri, destinationUri, dataSource);
//            uCrop.withOptions(options);
//            uCrop.setImageEngine(new UCropImageEngine() {
//                @Override
//                public void loadImage(Context context, String url, ImageView imageView) {
//                    if (!ImageLoaderUtils.assertValidRequest(context)) {
//                        return;
//                    }
//                    Glide.with(context).load(url).override(180, 180).into(imageView);
//                }
//
//                @Override
//                public void loadImage(Context context, Uri url, int maxWidth, int maxHeight, OnCallbackListener<Bitmap> call) {
//                    Glide.with(context).asBitmap().load(url).override(maxWidth, maxHeight).into(new CustomTarget<Bitmap>() {
//                        @Override
//                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                            if (call != null) {
//                                call.onCall(resource);
//                            }
//                        }
//
//                        @Override
//                        public void onLoadCleared(@Nullable Drawable placeholder) {
//                            if (call != null) {
//                                call.onCall(null);
//                            }
//                        }
//                    });
//                }
//            });
//            uCrop.start(fragment.requireActivity(), fragment, requestCode);
//        }
//    }

    /**
     * 配制UCrop，可根据需求自我扩展
     *
     * @return
     */
    private UCrop.Options buildOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setHideBottomControls(true);
        options.setFreeStyleCropEnabled(false);
        options.setShowCropFrame(true);
        options.setShowCropGrid(true);
        options.setCircleDimmedLayer(false);
        options.isCropDragSmoothToCenter(false);
        options.isForbidCropGifWebp(false);
        options.isForbidSkipMultipleCrop(true);
        options.setMaxScaleMultiplier(100);
        int color_blue = ContextCompat.getColor(reactContext, R.color.ps_color_10AFFF);
        options.setToolbarColor(color_blue);
        options.setToolbarWidgetColor(color_blue);
        options.setActiveControlsWidgetColor(color_blue);
        if (selectorStyle.getSelectMainStyle().getStatusBarColor() != 0) {
            SelectMainStyle mainStyle = selectorStyle.getSelectMainStyle();
            boolean isDarkStatusBarBlack = mainStyle.isDarkStatusBarBlack();
            int statusBarColor = mainStyle.getStatusBarColor();
            options.isDarkStatusBarBlack(isDarkStatusBarBlack);
            if (StyleUtils.checkStyleValidity(statusBarColor)) {
                options.setStatusBarColor(statusBarColor);
                options.setToolbarColor(statusBarColor);
            } else {
                options.setStatusBarColor(ContextCompat.getColor(reactContext, R.color.ps_color_grey));
                options.setToolbarColor(ContextCompat.getColor(reactContext, R.color.ps_color_grey));
            }
            TitleBarStyle titleBarStyle = selectorStyle.getTitleBarStyle();
            if (StyleUtils.checkStyleValidity(titleBarStyle.getTitleTextColor())) {
                options.setToolbarWidgetColor(titleBarStyle.getTitleTextColor());
            } else {
                options.setToolbarWidgetColor(ContextCompat.getColor(reactContext, R.color.ps_color_white));
            }
        } else {
            options.setStatusBarColor(ContextCompat.getColor(reactContext, R.color.ps_color_grey));
            options.setToolbarColor(ContextCompat.getColor(reactContext, R.color.ps_color_grey));
            options.setToolbarWidgetColor(ContextCompat.getColor(reactContext, R.color.ps_color_white));
        }
        return options;
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

    /**
     * 自定义视频封面图输出目录
     *
     * @return
     */
    private String getVideoThumbnailDir() {
        File dir = reactContext.getExternalFilesDir("SimiThumbnail");
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        return dir != null ? dir.getAbsolutePath() + File.separator : "";
    }

    /**
     * 创建自定义输出目录
     *
     * @return
     */
    private String getSandboxPath() {
        File externalFilesDir = reactContext.getExternalFilesDir("");
        File customFile = new File(externalFilesDir.getAbsolutePath(), "SimiSandbox");
        if (!customFile.exists()) {
            customFile.mkdirs();
        }
        return customFile.getAbsolutePath() + File.separator;
    }
}
