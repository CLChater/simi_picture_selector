package com.simi.pictureselector.adapter.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.simi.pictureselector.R;
import com.simi.pictureselector.config.SelectMimeType;
import com.simi.pictureselector.config.SelectorProviders;
import com.simi.pictureselector.permissions.PermissionChecker;
import com.simi.pictureselector.style.SelectMainStyle;
import com.simi.pictureselector.utils.StyleUtils;

/**
 * @author：
 * @date：2021/11/20 3:54 下午
 * @describe：CameraViewHolder
 */
public class AddSelectViewHolder extends BaseRecyclerMediaHolder {

    public AddSelectViewHolder(View itemView) {
        super(itemView);
        ImageView tvAddSelect = itemView.findViewById(R.id.tvAddSelect);
        selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        SelectMainStyle adapterStyle = selectorConfig.selectorStyle.getSelectMainStyle();
        int background = adapterStyle.getAdapterCameraBackgroundColor();
        if (StyleUtils.checkStyleValidity(background)) {
            tvAddSelect.setBackgroundColor(background);
        }
    }

}
