package com.simi.pictureselector.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.simi.pictureselector.app.PictureAppMaster;
import com.simi.pictureselector.thread.PictureThreadUtils;

/**
 * @author：
 * @date：2022/1/8 3:29 下午
 * @describe：ToastUtils
 */
public class ToastUtils {
    /**
     * show toast content
     *
     * @param context
     * @param text
     */
    public static void showToast(Context context, String text) {
        if (isFastDoubleClick() && TextUtils.equals(text, mLastText)) {
            return;
        }
        Context appContext = PictureAppMaster.getInstance().getAppContext();
        if (appContext == null) {
            appContext = context.getApplicationContext();
        }
        if (PictureThreadUtils.isInUiThread()) {
            Toast.makeText(appContext, text, Toast.LENGTH_SHORT).show();
            mLastText = text;
        } else {
            PictureThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Context appContext = PictureAppMaster.getInstance().getAppContext();
                    if (appContext == null) {
                        appContext = context.getApplicationContext();
                    }
                    Toast.makeText(appContext, text, Toast.LENGTH_SHORT).show();
                    mLastText = text;
                }
            });
        }
    }

    private final static long TIME = 1000;
    private static long lastClickTime;
    private static String mLastText;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < TIME) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
