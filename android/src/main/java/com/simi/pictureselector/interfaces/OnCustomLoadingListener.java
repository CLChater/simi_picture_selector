package com.simi.pictureselector.interfaces;

import android.app.Dialog;
import android.content.Context;

/**
 * @author：
 * @date：2022/8/12 7:19 下午
 * @describe：OnCustomLoadingListener
 */
public interface OnCustomLoadingListener {
    /**
     * create loading dialog
     *
     * @param context
     */
    Dialog create(Context context);
}
