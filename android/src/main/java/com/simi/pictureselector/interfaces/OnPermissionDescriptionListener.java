package com.simi.pictureselector.interfaces;

import androidx.fragment.app.Fragment;

/**
 * @author：
 * @date：2021/12/1 8:48 下午
 * @describe：OnPermissionDescriptionListener
 */
public interface OnPermissionDescriptionListener {
    /**
     * Permission description
     *
     * @param fragment
     * @param permissionArray
     */
    void onPermissionDescription(Fragment fragment, String[] permissionArray);

    /**
     * onDismiss
     */
    void onDismiss(Fragment fragment);
}
