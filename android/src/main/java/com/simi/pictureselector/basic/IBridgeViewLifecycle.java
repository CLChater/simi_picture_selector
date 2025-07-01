package com.simi.pictureselector.basic;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

/**
 * @author：
 * @date：2022/6/4 12:56 下午
 * @describe：IBridgeViewLifecycle
 */
public interface IBridgeViewLifecycle {
    /**
     * onViewCreated
     *
     * @param fragment
     * @param view
     * @param savedInstanceState
     */
    void onViewCreated(Fragment fragment, View view, Bundle savedInstanceState);

    /**
     * onDestroy
     *
     * @param fragment
     */
    void onDestroy(Fragment fragment);
}
