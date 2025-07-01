package com.simi.pictureselector.interfaces;

import androidx.fragment.app.Fragment;

/**
 * @author：
 * @date：2021/12/1 8:48 下午
 * @describe：OnPermissionsInterceptListener
 */
public interface OnPermissionsInterceptListener {
    /**
     * Custom Permissions management
     *
     * @param fragment
     * @param permissionArray Permissions array
     * @param call
     */
    void requestPermission(Fragment fragment, String[] permissionArray, OnRequestPermissionListener call);

    /**
     * Verify permission application status
     *
     * @param fragment
     * @param permissionArray
     * @return
     */
    boolean hasPermissions(Fragment fragment, String[] permissionArray);
}
