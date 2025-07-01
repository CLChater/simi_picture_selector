package com.simi.pictureselector.interfaces;

import android.view.View;

/**
 * @author：
 * @date：2020-03-26 10:50
 * @describe：OnItemClickListener
 */
public interface OnItemClickListener {
    /**
     * Item click event
     *
     * @param v
     * @param position
     */
    void onItemClick(View v, int position);
}
