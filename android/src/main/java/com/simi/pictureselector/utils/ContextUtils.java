package com.simi.pictureselector.utils;

import android.app.Application;
import android.content.Context;
import android.os.Build;


public class ContextUtils {

    private static Context appContext = null;

    public static void init(final Context context) {
        if (context == null) {
            return;
        }
        if (appContext == null) {
            appContext = context.getApplicationContext();
        }
    }

    public static Context getApp() {
        if (appContext != null) {
            return appContext;
        } else {
            return getContextImpl();
        }
    }

    /**
     * 内部实现，获取应用上下文对象
     *
     * @return 当前应用的上下文对象
     */
    private static Context getContextImpl() {
        try {
            if (appContext == null) {
                Object activityThread = ClazzUtils.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread");
                Application application = (Application) ClazzUtils.invokeObjectMethod(activityThread, "getApplication");

                if (application == null) {
                    application = (Application) ClazzUtils.invokeStaticMethod("android.app.AppGlobals", "getInitialApplication");
                }

                if (application != null) {
                    appContext = application.getApplicationContext();
                }

                if (appContext == null) {
                    appContext = (Context) ClazzUtils.invokeObjectMethod(activityThread, "getSystemContext");
                }

                if (appContext == null && Build.VERSION.SDK_INT >= 26) {
                    appContext = (Context) ClazzUtils.invokeObjectMethod(activityThread, "getSystemUiContext");
                }

                if (appContext == null && activityThread != null) {
                    appContext = (Context) ClazzUtils.invokeStaticMethod(
                            "android.app.ContextImpl",
                            "createSystemContext",
                            new Class[]{activityThread.getClass()},
                            new Object[]{activityThread});
                }
            }
        } catch (Throwable e) {
            //
        }

        return appContext;
    }

}
