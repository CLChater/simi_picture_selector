package com.simi.pictureselector.interfaces;

import com.simi.pictureselector.entity.LocalMedia;

/**
 * @author：
 * @date：2022/3/12 9:00 下午
 * @describe：OnSelectFilterListener
 */
public interface OnSelectFilterListener {
    /**
     * You need to filter out the content that does not meet the selection criteria
     *
     * @param media current select {@link LocalMedia}
     * @return the boolean result
     */
    boolean onSelectFilter(LocalMedia media);
}
