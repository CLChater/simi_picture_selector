package com.simi.pictureselector.config;

import android.content.pm.ActivityInfo;

import com.simi.pictureselector.basic.IBridgeLoaderFactory;
import com.simi.pictureselector.basic.IBridgeViewLifecycle;
import com.simi.pictureselector.basic.InterpolatorFactory;
import com.simi.pictureselector.engine.CompressEngine;
import com.simi.pictureselector.engine.CompressFileEngine;
import com.simi.pictureselector.engine.CropEngine;
import com.simi.pictureselector.engine.CropFileEngine;
import com.simi.pictureselector.engine.ExtendLoaderEngine;
import com.simi.pictureselector.engine.ImageEngine;
import com.simi.pictureselector.engine.SandboxFileEngine;
import com.simi.pictureselector.engine.UriToFileTransformEngine;
import com.simi.pictureselector.engine.VideoPlayerEngine;
import com.simi.pictureselector.entity.LocalMedia;
import com.simi.pictureselector.entity.LocalMediaFolder;
import com.simi.pictureselector.interfaces.OnBitmapWatermarkEventListener;
import com.simi.pictureselector.interfaces.OnCameraInterceptListener;
import com.simi.pictureselector.interfaces.OnCustomLoadingListener;
import com.simi.pictureselector.interfaces.OnExternalPreviewEventListener;
import com.simi.pictureselector.interfaces.OnGridItemSelectAnimListener;
import com.simi.pictureselector.interfaces.OnInjectActivityPreviewListener;
import com.simi.pictureselector.interfaces.OnInjectLayoutResourceListener;
import com.simi.pictureselector.interfaces.OnMediaEditInterceptListener;
import com.simi.pictureselector.interfaces.OnPermissionDeniedListener;
import com.simi.pictureselector.interfaces.OnPermissionDescriptionListener;
import com.simi.pictureselector.interfaces.OnPermissionsInterceptListener;
import com.simi.pictureselector.interfaces.OnPreviewInterceptListener;
import com.simi.pictureselector.interfaces.OnQueryFilterListener;
import com.simi.pictureselector.interfaces.OnRecordAudioInterceptListener;
import com.simi.pictureselector.interfaces.OnResultCallbackListener;
import com.simi.pictureselector.interfaces.OnSelectAnimListener;
import com.simi.pictureselector.interfaces.OnSelectFilterListener;
import com.simi.pictureselector.interfaces.OnSelectLimitTipsListener;
import com.simi.pictureselector.interfaces.OnVideoThumbnailEventListener;
import com.simi.pictureselector.language.LanguageConfig;
import com.simi.pictureselector.magical.BuildRecycleItemViewParams;
import com.simi.pictureselector.style.PictureSelectorStyle;
import com.simi.pictureselector.thread.PictureThreadUtils;
import com.simi.pictureselector.utils.FileDirMap;
import com.simi.pictureselector.utils.SdkVersionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author：
 * @date：2017-05-24 17:02
 * @describe：PictureSelector Config
 */

public final class SelectorConfig {
    public int chooseMode;
    public boolean isOnlyCamera;
    public boolean isDirectReturnSingle;
    public String cameraImageFormat;
    public String cameraVideoFormat;
    public String cameraImageFormatForQ;
    public String cameraVideoFormatForQ;
    public int requestedOrientation;
    public boolean isCameraAroundState;
    public int selectionMode;
    public int maxSelectNum;
    public int minSelectNum;
    public int maxVideoSelectNum;
    public int minVideoSelectNum;
    public int minAudioSelectNum;
    public int videoQuality;
    public int filterVideoMaxSecond;
    public int filterVideoMinSecond;
    public int selectMaxDurationSecond;
    public int selectMinDurationSecond;
    public int recordVideoMaxSecond;
    public int recordVideoMinSecond;
    public int imageSpanCount;
    public long filterMaxFileSize;
    public long filterMinFileSize;
    public long selectMaxFileSize;
    public long selectMinFileSize;
    public int language;
    public int defaultLanguage;
    public boolean isDisplayCamera;
    public boolean isDisplayAddSelect;
    public boolean isGif;
    public boolean isWebp;
    public boolean isBmp;
    public boolean isHeic;
    public boolean isEnablePreviewImage;
    public boolean isEnablePreviewVideo;
    public boolean isEnablePreviewAudio;
    public boolean isPreviewFullScreenMode;
    public boolean isPreviewZoomEffect;
    public boolean isOpenClickSound;
    public boolean isEmptyResultReturn;
    public boolean isHidePreviewDownload;
    public boolean isWithVideoImage;
    public List<String> queryOnlyImageList;
    public List<String> queryOnlyVideoList;
    public List<String> queryOnlyAudioList;
    public List<String> skipCropList;
    public boolean isCheckOriginalImage;
    public String outPutCameraImageFileName;
    public String outPutCameraVideoFileName;
    public String outPutAudioFileName;
    public String outPutCameraDir;
    public String outPutAudioDir;
    public String sandboxDir;
    public String originalPath;
    public String cameraPath;
    public String sortOrder;
    public String defaultAlbumName;
    public int pageSize;
    public boolean isPageStrategy;
    public boolean isFilterInvalidFile;
    public boolean isMaxSelectEnabledMask;
    public int animationMode;
    public boolean isAutomaticTitleRecyclerTop;
    public boolean isQuickCapture;
    public boolean isCameraRotateImage;
    public boolean isAutoRotating;
    public boolean isSyncCover;
    public int ofAllCameraType;
    public boolean isOnlySandboxDir;
    public boolean isCameraForegroundService;
    public boolean isResultListenerBack;
    public boolean isInjectLayoutResource;
    public boolean isActivityResultBack;
    public boolean isCompressEngine;
    public boolean isLoaderDataEngine;
    public boolean isLoaderFactoryEngine;
    public boolean isSandboxFileEngine;
    public boolean isOriginalControl;
    public boolean isDisplayTimeAxis;
    public boolean isFastSlidingSelect;
    public boolean isSelectZoomAnim;
    public boolean isAutoVideoPlay;
    public boolean isLoopAutoPlay;
    public boolean isFilterSizeDuration;
    public boolean isPageSyncAsCount;
    public boolean isPauseResumePlay;
    public boolean isSyncWidthAndHeight;
    public boolean isOriginalSkipCompress;
    public boolean isPreloadFirst;
    public boolean isUseSystemVideoPlayer;
    public boolean isNewKeyBackMode;
    public PictureSelectorStyle selectorStyle;

    public SelectorConfig() {
        initDefaultValue();
    }

    private void initDefaultValue() {
        chooseMode = SelectMimeType.ofImage();
        isOnlyCamera = false;
        selectionMode = SelectModeConfig.MULTIPLE;
        selectorStyle = new PictureSelectorStyle();
        maxSelectNum = 9;
        minSelectNum = 0;
        maxVideoSelectNum = 1;
        minVideoSelectNum = 0;
        minAudioSelectNum = 0;
        videoQuality = VideoQuality.VIDEO_QUALITY_HIGH;
        language = LanguageConfig.UNKNOWN_LANGUAGE;
        defaultLanguage = LanguageConfig.SYSTEM_LANGUAGE;
        filterVideoMaxSecond = 0;
        filterVideoMinSecond = 0;
        selectMaxDurationSecond = 0;
        selectMinDurationSecond = 0;
        filterMaxFileSize = 0;
        filterMinFileSize = 0;
        selectMaxFileSize = 0;
        selectMinFileSize = 0;
        recordVideoMaxSecond = 60;
        recordVideoMinSecond = 0;
        imageSpanCount = PictureConfig.DEFAULT_SPAN_COUNT;
        isCameraAroundState = false;
        isWithVideoImage = false;
        isDisplayCamera = true;
        isDisplayAddSelect = false;
        isGif = false;
        isWebp = true;
        isBmp = true;
        isHeic = true;
        isCheckOriginalImage = false;
        isDirectReturnSingle = false;
        isEnablePreviewImage = true;
        isEnablePreviewVideo = true;
        isEnablePreviewAudio = true;
        isHidePreviewDownload = false;
        isOpenClickSound = false;
        isEmptyResultReturn = false;
        cameraImageFormat = PictureMimeType.JPEG;
        cameraVideoFormat = PictureMimeType.MP4;
        cameraImageFormatForQ = PictureMimeType.MIME_TYPE_IMAGE;
        cameraVideoFormatForQ = PictureMimeType.MIME_TYPE_VIDEO;
        outPutCameraImageFileName = "";
        outPutCameraVideoFileName = "";
        outPutAudioFileName = "";
        queryOnlyImageList = new ArrayList<>();
        queryOnlyVideoList = new ArrayList<>();
        queryOnlyAudioList = new ArrayList<>();
        outPutCameraDir = "";
        outPutAudioDir = "";
        sandboxDir = "";
        originalPath = "";
        cameraPath = "";
        pageSize = PictureConfig.MAX_PAGE_SIZE;
        isPageStrategy = true;
        isFilterInvalidFile = false;
        isMaxSelectEnabledMask = false;
        animationMode = -1;
        isAutomaticTitleRecyclerTop = true;
        isQuickCapture = true;
        isCameraRotateImage = true;
        isAutoRotating = true;
        isSyncCover = !SdkVersionUtils.isQ();
        ofAllCameraType = SelectMimeType.ofAll();
        isOnlySandboxDir = false;
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        isCameraForegroundService = false;
        isResultListenerBack = true;
        isActivityResultBack = false;
        isCompressEngine = false;
        isLoaderDataEngine = false;
        isLoaderFactoryEngine = false;
        isSandboxFileEngine = false;
        isPreviewFullScreenMode = true;
        isPreviewZoomEffect = chooseMode != SelectMimeType.ofAudio();
        isOriginalControl = false;
        isInjectLayoutResource = false;
        isDisplayTimeAxis = true;
        isFastSlidingSelect = false;
        skipCropList = new ArrayList<>();
        sortOrder = "";
        isSelectZoomAnim = true;
        defaultAlbumName = "";
        isAutoVideoPlay = false;
        isLoopAutoPlay = false;
        isFilterSizeDuration = true;
        isPageSyncAsCount = false;
        isPauseResumePlay = false;
        isSyncWidthAndHeight = true;
        isOriginalSkipCompress = false;
        isPreloadFirst = true;
        isNewKeyBackMode = true;
        isUseSystemVideoPlayer = false;
    }

    /**
     * Callback listening
     */
    public ImageEngine imageEngine;
    public CompressEngine compressEngine;
    public CompressFileEngine compressFileEngine;
    public CropEngine cropEngine;
    public CropFileEngine cropFileEngine;
    public SandboxFileEngine sandboxFileEngine;
    public UriToFileTransformEngine uriToFileTransformEngine;
    public ExtendLoaderEngine loaderDataEngine;
    public VideoPlayerEngine videoPlayerEngine;
    public IBridgeViewLifecycle viewLifecycle;
    public IBridgeLoaderFactory loaderFactory;
    public InterpolatorFactory interpolatorFactory;
    public OnCameraInterceptListener onCameraInterceptListener;
    public OnSelectLimitTipsListener onSelectLimitTipsListener;
    public OnResultCallbackListener<LocalMedia> onResultCallListener;
    public OnExternalPreviewEventListener onExternalPreviewEventListener;
    public OnInjectActivityPreviewListener onInjectActivityPreviewListener;
    public OnMediaEditInterceptListener onEditMediaEventListener;
    public OnPermissionsInterceptListener onPermissionsEventListener;
    public OnInjectLayoutResourceListener onLayoutResourceListener;
    public OnPreviewInterceptListener onPreviewInterceptListener;
    public OnSelectFilterListener onSelectFilterListener;
    public OnPermissionDescriptionListener onPermissionDescriptionListener;
    public OnPermissionDeniedListener onPermissionDeniedListener;
    public OnRecordAudioInterceptListener onRecordAudioListener;
    public OnQueryFilterListener onQueryFilterListener;
    public OnBitmapWatermarkEventListener onBitmapWatermarkListener;
    public OnVideoThumbnailEventListener onVideoThumbnailEventListener;
    public OnGridItemSelectAnimListener onItemSelectAnimListener;
    public OnSelectAnimListener onSelectAnimListener;
    public OnCustomLoadingListener onCustomLoadingListener;

    /**
     * selected current album folder
     */
    public LocalMediaFolder currentLocalMediaFolder;

    /**
     * selected result
     */
    public final ArrayList<LocalMedia> selectedResult = new ArrayList<>();

    public synchronized ArrayList<LocalMedia> getSelectedResult() {
        return selectedResult;
    }

    public int getSelectCount() {
        return selectedResult.size();
    }

    public void addSelectResult(LocalMedia media) {
        selectedResult.add(media);
    }

    public void addAllSelectResult(ArrayList<LocalMedia> result) {
        selectedResult.addAll(result);
    }

    public String getResultFirstMimeType() {
        return selectedResult.size() > 0 ? selectedResult.get(0).getMimeType() : "";
    }

    /**
     * selected preview result
     */
    public final ArrayList<LocalMedia> selectedPreviewResult = new ArrayList<>();

    public void addSelectedPreviewResult(ArrayList<LocalMedia> list) {
        if (list != null) {
            selectedPreviewResult.clear();
            selectedPreviewResult.addAll(list);
        }
    }

    /**
     * all album data source
     */
    public final ArrayList<LocalMediaFolder> albumDataSource = new ArrayList<>();

    public void addAlbumDataSource(List<LocalMediaFolder> list) {
        if (list != null) {
            albumDataSource.clear();
            albumDataSource.addAll(list);
        }
    }

    /**
     * all data source
     */
    public final ArrayList<LocalMedia> dataSource = new ArrayList<>();

    public void addDataSource(ArrayList<LocalMedia> list) {
        if (list != null) {
            dataSource.clear();
            dataSource.addAll(list);
        }
    }

    /**
     * 释放监听器
     */
    public void destroy() {
        imageEngine = null;
        compressEngine = null;
        compressFileEngine = null;
        cropEngine = null;
        cropFileEngine = null;
        sandboxFileEngine = null;
        uriToFileTransformEngine = null;
        loaderDataEngine = null;
        onResultCallListener = null;
        onCameraInterceptListener = null;
        onExternalPreviewEventListener = null;
        onInjectActivityPreviewListener = null;
        onEditMediaEventListener = null;
        onPermissionsEventListener = null;
        onLayoutResourceListener = null;
        onPreviewInterceptListener = null;
        onSelectLimitTipsListener = null;
        onSelectFilterListener = null;
        onPermissionDescriptionListener = null;
        onPermissionDeniedListener = null;
        onRecordAudioListener = null;
        onQueryFilterListener = null;
        onBitmapWatermarkListener = null;
        onVideoThumbnailEventListener = null;
        viewLifecycle = null;
        loaderFactory = null;
        interpolatorFactory = null;
        onItemSelectAnimListener = null;
        onSelectAnimListener = null;
        videoPlayerEngine = null;
        onCustomLoadingListener = null;
        currentLocalMediaFolder = null;
        dataSource.clear();
        selectedResult.clear();
        albumDataSource.clear();
        selectedPreviewResult.clear();
        PictureThreadUtils.cancel(PictureThreadUtils.getIoPool());
        BuildRecycleItemViewParams.clear();
        FileDirMap.clear();
        LocalMedia.destroyPool();
    }
}
