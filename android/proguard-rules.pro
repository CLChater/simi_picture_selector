# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 忽略警告
-dontwarn com.sc.hawkeye.**

# 优化移除未使用的代码
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# 保留类名和方法名以支持反射（视项目需求保留）
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

# 压缩后保留序列化对象
#-keepclassmembers class * implements java.io.Serializable {
#    static final long serialVersionUID;
#    private static final java.io.ObjectStreamField[] serialPersistentFields;
#    private void writeObject(java.io.ObjectOutputStream);
#    private void readObject(java.io.ObjectInputStream);
#    Object readResolve();
#    Object writeReplace();
#}

# 保留本地 JNI 方法
-keepclasseswithmembers class * {
    native <methods>;
}

# 保留枚举类型
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


-keep class com.sc.hawkeye.track.HawkeyeDataAPI {
    *;
}

-keep class com.sc.hawkeye.HawkeyeModule {
    *;
#    public  initialize(...);
#    public  onCatalystInstanceDestroy(...);
#    public  initT(...);
#    public  userProperties(...);
#    public  cleanUserProperties(...);
#    public  trackEvent(...);
#    public  trackViewClick(...);
#    public  saveViewProperties(...);
}

