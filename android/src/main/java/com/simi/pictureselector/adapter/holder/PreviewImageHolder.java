package com.simi.pictureselector.adapter.holder;

import android.view.View;

import androidx.annotation.NonNull;

import com.simi.pictureselector.config.PictureConfig;
import com.simi.pictureselector.entity.LocalMedia;
import com.simi.pictureselector.photoview.OnViewTapListener;

/**
 * @author：
 * @date：2021/12/15 5:11 下午
 * @describe：PreviewImageHolder
 */
public class PreviewImageHolder extends BasePreviewHolder {

    public PreviewImageHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    protected void findViews(View itemView) {
    }

    @Override
    protected void loadImage(LocalMedia media, int maxWidth, int maxHeight) {
        if (selectorConfig.imageEngine != null) {
            String availablePath = media.getAvailablePath();
            if (maxWidth == PictureConfig.UNSET && maxHeight == PictureConfig.UNSET) {
                selectorConfig.imageEngine.loadImage(itemView.getContext(), availablePath, coverImageView);
            } else {
                selectorConfig.imageEngine.loadImage(itemView.getContext(), coverImageView, availablePath, maxWidth, maxHeight);
            }
        }
    }

    @Override
    protected void onClickBackPressed() {
        coverImageView.setOnViewTapListener(new OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                if (mPreviewEventListener != null) {
                    mPreviewEventListener.onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onLongPressDownload(LocalMedia media) {
        coverImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mPreviewEventListener != null) {
                    mPreviewEventListener.onLongPressDownload(media);
                }
                return false;
            }
        });
    }
}
