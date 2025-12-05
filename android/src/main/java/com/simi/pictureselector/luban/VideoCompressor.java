package com.simi.pictureselector.luban;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.effect.ScaleAndRotateTransformation;
import androidx.media3.transformer.Composition;
import androidx.media3.transformer.DefaultEncoderFactory;
import androidx.media3.transformer.EditedMediaItem;
import androidx.media3.transformer.Effects;
import androidx.media3.transformer.ExportException;
import androidx.media3.transformer.ExportResult;
import androidx.media3.transformer.Transformer;
import androidx.media3.transformer.VideoEncoderSettings;

import com.google.common.collect.ImmutableList;
import com.simi.pictureselector.utils.DateUtils;
import com.simi.pictureselector.utils.ValueOf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 视频压缩器 - 基于 Media3 Transformer。
 * 支持多文件顺序压缩、取消、进度回调。
 */
@UnstableApi
public class VideoCompressor {

    private static final String TAG = "SimiSelectorModule";
    private static final String DEFAULT_DISK_CACHE_DIR = "luban_disk_cache";

    private final Context context;
    private final List<Uri> inputUris;
    private final CompressListener listener;
    private final OnRenameListener renameListener;
    private final float quality;
    private final int outBitrate;
    private final int mLeastCompressSize;

    private final static int minBitrate = 2_000_000;
    private final static int maxBitrate = 8_000_000;

    private volatile boolean cancelled = false;
    private int currentIndex = 0;
    private Transformer currentTransformer;
    private final Object lock = new Object();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String targetDir;

    private VideoCompressor(Builder builder) {
        this.context = builder.context;
        this.inputUris = builder.inputUris;
        this.listener = builder.listener;
        this.renameListener = builder.renameListener;
        this.quality = builder.quality;
        this.outBitrate = builder.outBitrate;
        this.mLeastCompressSize = builder.mLeastCompressSize;
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    public void start() {
        if (inputUris.isEmpty()) {
            return;
        }
        cancelled = false;
        currentIndex = 0;
        processNext();
    }

    private void processNext() {
        if (cancelled || currentIndex >= inputUris.size()) {
            postToMain(() -> {
                if (cancelled && listener != null) listener.onCancelled();
            });
            return;
        }

        Uri uri = inputUris.get(currentIndex++);

        String source = Checker.isContent(uri.toString()) ? LubanUtils.getPath(context, uri) : uri.getPath();

        if (!Checker.SINGLE.needCompress(mLeastCompressSize, source)) {
            if (listener != null) {
                listener.onCompleted(uri.toString(), new File(source));
            }
            return;
        }

        ResolutionTarget target = getVideoTarget(context, uri);
        if (target == null) {
            postToMain(() -> {
                if (listener != null)
                    listener.onError(source, new RuntimeException("Failed to read video metadata"));
            });
            return;
        }

        // 宽高对齐为偶数
        target.targetWidth &= ~1;
        target.targetHeight &= ~1;

        try {
            if (TextUtils.isEmpty(source)) {
                postToMain(() -> {
                    if (listener != null)
                        listener.onError(source, new IllegalArgumentException("media source path fail"));
                });
                return;
            }

            int indexOf = source.lastIndexOf(".");
            String postfix = indexOf != -1 ? source.substring(indexOf) : ".mp4";
            String newName = DateUtils.getCreateFileName("CMP_") + postfix;

            File outFile = getVideoCustomFile(context, newName);

            DefaultEncoderFactory encoderFactory = new DefaultEncoderFactory.Builder(context)
                    .setRequestedVideoEncoderSettings(
                            new VideoEncoderSettings.Builder()
                                    .setBitrate(Math.max(minBitrate, Math.min(maxBitrate, target.targetBitrate)))
                                    .build()
                    )
                    .build();

            final Transformer transformerLocal = new Transformer.Builder(context)
                    .setVideoMimeType(MimeTypes.VIDEO_H264)
                    .setAudioMimeType(MimeTypes.AUDIO_AAC)
                    .setEncoderFactory(encoderFactory)
                    .addListener(new Transformer.Listener() {
                        @Override
                        public void onCompleted(@NonNull Composition composition,
                                                @NonNull ExportResult result) {
                            postToMain(() -> {
                                if (!cancelled && listener != null) {
                                    listener.onCompleted(uri.toString(), outFile);
                                } else if (cancelled && listener != null) {
                                    listener.onCancelled();
                                }
                                synchronized (lock) {
                                    currentTransformer = null;
                                }
                                processNext();
                            });
                        }

                        @Override
                        public void onError(@NonNull Composition composition,
                                            @NonNull ExportResult result,
                                            @NonNull ExportException exception) {
                            postToMain(() -> {
                                if (listener != null) listener.onError(source, exception);
                                synchronized (lock) {
                                    currentTransformer = null;
                                }
                                processNext();
                            });
                        }
                    }).build();

            synchronized (lock) {
                currentTransformer = transformerLocal;
            }

            MediaItem mediaItem = MediaItem.fromUri(uri);
            EditedMediaItem editedItem = new EditedMediaItem.Builder(mediaItem)
                    .setEffects(new Effects(
                            ImmutableList.of(),
                            ImmutableList.of(
                                    new ScaleAndRotateTransformation.Builder()
                                            .setScale(target.scale, target.scale)
                                            .build()
                            )
                    ))
                    .build();

            transformerLocal.start(editedItem, outFile.getAbsolutePath());

        } catch (Exception e) {
            Log.e(TAG, "Compression failed: " + e.getMessage(), e);
            postToMain(() -> {
                if (listener != null) listener.onError(source, e);
            });
            processNext();
        }
    }

    public void cancel() {
        cancelled = true;
        synchronized (lock) {
            if (currentTransformer != null) {
                try {
                    currentTransformer.cancel();
                } catch (Exception ignore) {
                }
                currentTransformer = null;
            }
        }
        postToMain(() -> {
            if (listener != null) listener.onCancelled();
        });
    }

    private void postToMain(Runnable r) {
        mainHandler.post(r);
    }

    private ResolutionTarget getVideoTarget(Context context, Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, uri);
            String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            int width, height;
            if ("90".equals(rotation) || "270".equals(rotation)) {
                height = ValueOf.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                width = ValueOf.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            } else {
                width = ValueOf.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                height = ValueOf.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            }

            float frameRate = 30f;
            try {
                String fpsStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE);
                if (fpsStr != null) frameRate = Float.parseFloat(fpsStr);
            } catch (Exception ignore) {
            }

            Log.i(TAG, "输入分辨率: " + width + "x" + height + ", fps=" + frameRate);
            return computeTargetParams(width, height, quality, frameRate);

        } catch (Exception e) {
            Log.e(TAG, "Metadata parse failed", e);
            return null;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignore) {
            }
        }
    }

    private ResolutionTarget computeTargetParams(int width, int height, float quality, float fps) {
        float baseBpp = 0.1f * quality;
        int bitrate = (int) (width * height * baseBpp * fps);
        float scale;
        if (quality >= 0.9f) scale = 1f;
        else if (quality >= 0.7f) scale = 0.85f;
        else if (quality >= 0.5f) scale = 0.7f;
        else if (quality >= 0.3f) scale = 0.55f;
        else scale = 0.4f;

        int targetWidth = Math.round(width * scale);
        int targetHeight = Math.round(height * scale);
        return new ResolutionTarget(targetWidth, targetHeight, bitrate, scale);
    }

    private File getVideoCacheFile(Context context, String suffix) {
        if (TextUtils.isEmpty(targetDir)) {
            targetDir = getVideoCacheDir(context).getAbsolutePath();
        }

        String cacheBuilder = targetDir + "/" +
                System.currentTimeMillis() +
                (int) (Math.random() * 1000) +
                (TextUtils.isEmpty(suffix) ? ".jpg" : suffix);

        return new File(cacheBuilder);
    }

    private File getVideoCustomFile(Context context, String filename) {
        if (TextUtils.isEmpty(targetDir)) {
            targetDir = getVideoCacheDir(context).getAbsolutePath();
        }

        String cacheBuilder = targetDir + "/" + filename;

        return new File(cacheBuilder);
    }

    private File getVideoCacheDir(Context context) {
        return getVideoCacheDir(context, DEFAULT_DISK_CACHE_DIR);
    }


    private static File getVideoCacheDir(Context context, String cacheName) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                // File wasn't able to create a directory, or the result exists but not a directory
                return null;
            }
            return result;
        }
        if (Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, "default disk cache dir is null");
        }
        return null;
    }

    // ===================== 内部类型定义 =====================

    public interface CompressListener {
        void onCompleted(String source, File compressed);

        void onError(String source, Exception e);

        void onCancelled();
    }

    public interface OnRenameListener {
        String rename(String sourcePath);
    }

    public static class ResolutionTarget {
        public int targetWidth;
        public int targetHeight;
        public final int targetBitrate;
        public final float scale;

        public ResolutionTarget(int w, int h, int bitrate, float scale) {
            this.targetWidth = w;
            this.targetHeight = h;
            this.targetBitrate = bitrate;
            this.scale = scale;
        }
    }

    // ===================== Builder =====================

    public static class Builder {
        private final Context context;
        private final List<Uri> inputUris = new ArrayList<>();
        private CompressListener listener;
        private OnRenameListener renameListener;
        private float quality = 0.8f;
        private int outBitrate = 2_000_000;
        private int mLeastCompressSize = 1000;

        Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        public Builder load(Uri uri) {
            inputUris.add(uri);
            return this;
        }

        public Builder load(List<Uri> uris) {
            inputUris.addAll(uris);
            return this;
        }

        public Builder setQuality(float q) {
            this.quality = q;
            return this;
        }

        public Builder setOutBitrate(int bitrate) {
            this.outBitrate = bitrate;
            return this;
        }

        public Builder setListener(CompressListener l) {
            this.listener = l;
            return this;
        }

        public Builder setRenameListener(OnRenameListener r) {
            this.renameListener = r;
            return this;
        }

        public Builder ignoreBy(int size) {
            this.mLeastCompressSize = size;
            return this;
        }

        public VideoCompressor build() {
            return new VideoCompressor(this);
        }

        public void start() {
            build().start();
        }
    }
}
