package com.simi.pictureselector.interfaces;

import android.content.Context;

/**
 * @author：
 * @date：2022/4/3 5:37 下午
 * @describe：OnVideoThumbnailEventListener
 */
public interface OnVideoThumbnailEventListener {
    /**
     * video thumbnail
     *
     * @param context
     * @param videoPath
     */
    void onVideoThumbnail(Context context, String videoPath, OnKeyValueResultCallbackListener call);
}
