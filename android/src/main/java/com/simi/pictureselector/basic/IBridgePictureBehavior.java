package com.simi.pictureselector.basic;

/**
 * @author：
 * @date：2022/1/12 9:32 上午
 * @describe：IBridgePictureBehavior
 */
public interface IBridgePictureBehavior {
    /**
     * finish activity
     *
     * @param result data
     */
    void onSelectFinish(PictureCommonFragment.SelectorResult result);
}
