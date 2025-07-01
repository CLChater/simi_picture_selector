package com.simi.pictureselector.interfaces;

/**
 * @author：
 * @date：2020/4/24 11:48 AM
 * @describe：OnCallbackIndexListener
 */
public interface OnCallbackIndexListener<T> {
    /**
     * @param data
     * @param index
     */
    void onCall(T data, int index);
}
