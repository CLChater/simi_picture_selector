package com.simi.pictureselector.interfaces;

/**
 * @author：
 * @date：2020-04-14 18:44
 * @describe：OnRecyclerViewScrollStateListener
 */
public interface OnRecyclerViewScrollStateListener {

    /**
     * RecyclerView Scroll Fast
     */
    void onScrollFast();

    /**
     * RecyclerView Scroll Slow
     */
    void onScrollSlow();
}
