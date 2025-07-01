package com.simi.pictureselector.engine;

import android.content.Context;
import android.net.Uri;

import com.simi.pictureselector.entity.LocalMedia;
import com.simi.pictureselector.interfaces.OnKeyValueResultCallbackListener;

import java.util.ArrayList;

/**
 * @author：
 * @date：2021/5/19 9:36 AM
 * @describe：CompressFileEngine
 */
public interface CompressFileEngine {
    /**
     * Custom compression engine
     * <p>
     * Users can implement this interface, and then access their own compression framework to plug
     * the compressed path into the {@link LocalMedia} object;
     * </p>
     *
     * @param context
     * @param source
     */
    void onStartCompress(Context context, ArrayList<Uri> source, OnKeyValueResultCallbackListener call);
}
