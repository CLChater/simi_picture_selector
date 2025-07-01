package com.simi.pictureselector.basic;

import com.simi.pictureselector.loader.IBridgeMediaLoader;

/**
 * @author：
 * @date：2022/6/10 9:37 上午
 * @describe：IBridgeLoaderFactory
 */
public interface IBridgeLoaderFactory {
    /**
     * CreateLoader
     */
    IBridgeMediaLoader onCreateLoader();
}
