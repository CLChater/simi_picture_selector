package com.simi.pictureselector.app;

import android.content.Context;

import com.simi.pictureselector.engine.PictureSelectorEngine;

/**
 * @author：
 * @date：2019-12-03 15:14
 * @describe：IApp
 */
public interface IApp {
    /**
     * Application
     *
     * @return
     */
    Context getAppContext();

    /**
     * PictureSelectorEngine
     *
     * @return
     */
    PictureSelectorEngine getPictureSelectorEngine();
}
