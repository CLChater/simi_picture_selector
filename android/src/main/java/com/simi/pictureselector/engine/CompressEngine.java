package com.simi.pictureselector.engine;

import android.content.Context;

import com.simi.pictureselector.entity.LocalMedia;
import com.simi.pictureselector.interfaces.OnCallbackListener;

import java.util.ArrayList;

/**
 * @author：
 * @date：2021/5/19 9:36 AM
 * @describe：CompressEngine Please use {@link CompressFileEngine}
 */
@Deprecated
public interface CompressEngine {
    /**
     * Custom compression engine
     * <p>
     * Users can implement this interface, and then access their own compression framework to plug
     * the compressed path into the {@link LocalMedia} object;
     *
     * </p>
     *
     * <p>
     * 1、LocalMedia media = new LocalMedia();
     * media.setCompressed(true);
     * media.setCompressPath("Your compressed path");
     * </p>
     * <p>
     * 2、listener.onCall( "you result" );
     * </p>
     *
     * @param context
     * @param list
     * @param listener
     */
    void onStartCompress(Context context, ArrayList<LocalMedia> list, OnCallbackListener<ArrayList<LocalMedia>> listener);
}
