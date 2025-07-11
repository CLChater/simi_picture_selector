package com.simi.pictureselector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.simi.pictureselector.basic.PictureCommonFragment;
import com.simi.pictureselector.entity.LocalMedia;
import com.simi.pictureselector.manager.SelectedManager;
import com.simi.pictureselector.permissions.PermissionChecker;
import com.simi.pictureselector.permissions.PermissionConfig;
import com.simi.pictureselector.utils.SdkVersionUtils;
import com.simi.pictureselector.utils.ToastUtils;

/**
 * @author：
 * @date：2021/11/22 2:26 下午
 * @describe：PictureOnlyCameraFragment
 */
public class PictureOnlyCameraFragment extends PictureCommonFragment {
    public static final String TAG = PictureOnlyCameraFragment.class.getSimpleName();

    public static PictureOnlyCameraFragment newInstance() {
        return new PictureOnlyCameraFragment();
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public int getResourceId() {
        return R.layout.ps_empty;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 这里只有非内存回收状态下才走，否则当内存不足Fragment被回收后会重复执行
        if (savedInstanceState == null) {
            openSelectedCamera();
        }
    }

    @Override
    public void dispatchCameraMediaResult(LocalMedia media) {
        int selectResultCode = confirmSelect(media, false);
        if (selectResultCode == SelectedManager.ADD_SUCCESS) {
            dispatchTransformResult();
        } else {
            onKeyBackFragmentFinish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            onKeyBackFragmentFinish();
        }
    }

    @Override
    public void handlePermissionSettingResult(String[] permissions) {
        onPermissionExplainEvent(false, null);
        boolean isHasPermissions;
        if (selectorConfig.onPermissionsEventListener != null) {
            isHasPermissions = selectorConfig.onPermissionsEventListener
                    .hasPermissions(this, permissions);
        } else {
            isHasPermissions = PermissionChecker.isCheckCamera(getContext());
            if (SdkVersionUtils.isQ()) {
            } else {
                isHasPermissions = PermissionChecker.isCheckWriteExternalStorage(getContext());
            }
        }
        if (isHasPermissions) {
            openSelectedCamera();
        } else {
            if (!PermissionChecker.isCheckCamera(getContext())) {
                ToastUtils.showToast(getContext(), getString(R.string.ps_camera));
            } else {
                if (!PermissionChecker.isCheckWriteExternalStorage(getContext())) {
                    ToastUtils.showToast(getContext(), getString(R.string.ps_jurisdiction));
                }
            }
            onKeyBackFragmentFinish();
        }
        PermissionConfig.CURRENT_REQUEST_PERMISSION = new String[]{};
    }
}
