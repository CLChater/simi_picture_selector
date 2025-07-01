package com.simi.pictureselector.adapter.holder;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.simi.pictureselector.R;
import com.simi.pictureselector.config.SelectorConfig;
import com.simi.pictureselector.entity.LocalMedia;
import com.simi.pictureselector.style.SelectMainStyle;
import com.simi.pictureselector.utils.DateUtils;
import com.simi.pictureselector.utils.StyleUtils;

/**
 * @author：
 * @date：2021/11/20 3:59 下午
 * @describe：VideoViewHolder
 */
public class VideoViewHolder extends BaseRecyclerMediaHolder {
    private final TextView tvDuration;

    public VideoViewHolder(@NonNull View itemView, SelectorConfig config) {
        super(itemView, config);
        tvDuration = itemView.findViewById(R.id.tv_duration);
        SelectMainStyle adapterStyle = selectorConfig.selectorStyle.getSelectMainStyle();
        int drawableLeft = adapterStyle.getAdapterDurationDrawableLeft();
        if (StyleUtils.checkStyleValidity(drawableLeft)) {
            tvDuration.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableLeft, 0, 0, 0);
        }
        int textSize = adapterStyle.getAdapterDurationTextSize();
        if (StyleUtils.checkSizeValidity(textSize)) {
            tvDuration.setTextSize(textSize);
        }
        int textColor = adapterStyle.getAdapterDurationTextColor();
        if (StyleUtils.checkStyleValidity(textColor)) {
            tvDuration.setTextColor(textColor);
        }

        int shadowBackground = adapterStyle.getAdapterDurationBackgroundResources();
        if (StyleUtils.checkStyleValidity(shadowBackground)) {
            tvDuration.setBackgroundResource(shadowBackground);
        }

        int[] durationGravity = adapterStyle.getAdapterDurationGravity();
        if (StyleUtils.checkArrayValidity(durationGravity)) {
            if (tvDuration.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                ((RelativeLayout.LayoutParams) tvDuration.getLayoutParams()).removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                for (int i : durationGravity) {
                    ((RelativeLayout.LayoutParams) tvDuration.getLayoutParams()).addRule(i);
                }
            }
        }
    }

    @Override
    public void bindData(LocalMedia media, int position) {
        super.bindData(media, position);
        tvDuration.setText(DateUtils.formatDurationTime(media.getDuration()));
    }
}
