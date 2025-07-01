package com.simi.pictureselector.utils;

import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ClazzUtils {
//    private static Method forName = null;
//    private static Method getDeclaredMethod = null;
//    private static Method getMethod = null;
//    private static Method getDeclaredField = null;
//    private static Method getField = null;
//    private static Method getDeclaredConstructor = null;
//    private static Method getConstructor = null;
//    private static Method newInstance = null;

    static {
        if (Build.VERSION.SDK_INT > 27) {
            try {
                call(findClass("dalvik.system.VMDebug"), "allowHiddenApiReflectionFrom", null,
                        new Class[]{Class.class}, new Object[]{ClazzUtils.class});
            } catch (Throwable e) {
                //
            }
        }

//        if (Build.VERSION.SDK_INT > 27) {
//            try {
//                forName = Class.class.getDeclaredMethod("forName", String.class);
//                // invoke = Method.class.getMethod("invoke", Object.class, Object[].class);
//                // 反射获取方法
//                getDeclaredMethod =
//                        Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
//                getMethod = Class.class.getDeclaredMethod("getMethod", String.class, Class[].class);
//
//                // 反射获取变量
//                getDeclaredField = Class.class.getDeclaredMethod("getDeclaredField", String.class);
//                getField = Class.class.getDeclaredMethod("getField", String.class);
//
//                // 反射实例化代码
//                getDeclaredConstructor =
//                        Class.class.getDeclaredMethod("getDeclaredConstructor", Class[].class);
//                getConstructor = Class.class.getDeclaredMethod("getConstructor", Class[].class);
//                newInstance = Constructor.class.getDeclaredMethod("newInstance", Object[].class);
//            } catch (Throwable e) {
//            }
//            /*
//             * 设置豁免所有hide api
//             * http://androidxref.com/9.0.0_r3/xref/art/test/674-hiddenapi/src-art/Main.java#100
//             * VMRuntime.getRuntime().setHiddenApiExemptions(new String[]{"L"});
//             */
//            try {
//                Class<?> vmRuntimeClass =
//                        (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
//                Method getRuntime =
//                        (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
//                Method setHiddenApiExemptions =
//                        (Method) getDeclaredMethod.invoke(vmRuntimeClass,
//                                "setHiddenApiExemptions", new Class[]{String[].class});
//                if (setHiddenApiExemptions != null) {
//                    Object sVmRuntime = getRuntime.invoke((Object) null);
//                    setHiddenApiExemptions.invoke(sVmRuntime, new Object[]{new String[]{"L"}});
//                }
//
//            } catch (Throwable igone) {
//            }
//        }
    }


    public static Object invokeStaticMethod(String clazzName, String methodName) {
        if (TextUtils.isEmpty(clazzName) || TextUtils.isEmpty(methodName)) {
            return null;
        }
        return call(findClass(clazzName), methodName, null, null, null);
    }

    public static Object invokeStaticMethod(Class clazz, String methodName) {
        if (clazz == null || TextUtils.isEmpty(methodName)) {
            return null;
        }
        return call(clazz, methodName, null, null, null);
    }

    public static Object invokeStaticMethod(String clazzName, String methodName, Class<?>[] argsClass, Object[] args) {
        if (TextUtils.isEmpty(clazzName) || TextUtils.isEmpty(methodName)) {
            return null;
        }
        return call(findClass(clazzName), methodName, null, argsClass, args);
    }

    public static Object invokeStaticMethod(Class clazz, String methodName, Class<?>[] argsClass, Object[] args) {
        if (clazz == null || TextUtils.isEmpty(methodName)) {
            return null;
        }
        return call(clazz, methodName, null, argsClass, args);
    }

    public static Object invokeObjectMethod(Object o, String methodName) {
        if (o == null || TextUtils.isEmpty(methodName)) {
            return null;
        }
        return call(o.getClass(), methodName, o, null, null);
    }

    public static Object invokeObjectMethod(Object o, String methodName, String[] argsClassNames, Object[] args) {
        if (o == null || TextUtils.isEmpty(methodName)) {
            return null;
        }
        return call(o.getClass(), methodName, o, converStringToClass(argsClassNames), args);
    }

    public static Object invokeObjectMethod(Object o, String methodName, Class<?>[] argsClass, Object[] args) {
        if (o == null || TextUtils.isEmpty(methodName)) {
            return null;
        }

        return call(o.getClass(), methodName, o, argsClass, args);
    }

    public static Object call(String className, String methodName, Object instance, Class<?>[] types, Object[] values) {
        return call(findClass(className), methodName, instance, types, values);
    }

    public static Object call(Class clazz, String methodName, Object instance, Class<?>[] types, Object[] values) {

        if (TextUtils.isEmpty(methodName)
                || !isSameLen(types, values)// 参数不一致
        ) {
            return null;
        }
        if (clazz == null && instance != null) {
            clazz = instance.getClass();
        }
        if (clazz == null) {
            return null;
        }

        if (types == null || types.length < 1) {
            return invoke(instance, getMethod(clazz, methodName));
        } else {
            return invoke(instance, getMethod(clazz, methodName, types), values);
        }
    }

    public static Method getMethod(String clazzName, String methodName, Class<?>... types) {
        if (TextUtils.isEmpty(clazzName) || TextUtils.isEmpty(methodName)) {
            return null;
        }
        return getMethod(findClass(clazzName), methodName, types);
    }


    public static Object invoke(Object obj, Method method, Object... args) {
        if (method == null) {
            return null;
        }
        try {
            if (args == null || args.length < 1) {
                return method.invoke(obj);
            } else {
                return method.invoke(obj, args);
            }
        } catch (Throwable igone) {
        }
        return null;
    }

    /**
     * 获取构造函数
     *
     * @param clazzName
     * @return
     */
    public static Object newInstance(String clazzName) {
        return newInstance(clazzName, null, null);
    }

    public static Object newInstance(Class clazzName) {
        return newInstance(clazzName, null, null);
    }

    public static Object newInstance(String clazzName, Class[] types, Object[] values) {
        return newInstance(findClass(clazzName), types, values);
    }


    /**
     * 参数及类型长度是否一致
     *
     * @param types
     * @param values
     * @return true: 一致
     * false: 不一致
     */
    private static boolean isSameLen(Class[] types, Object[] values) {
        // 都为空，无参，一致
        if (types == null && values == null) {
            return true;
        } else {
            // 单个为空，不一致
            if (types == null || values == null) {
                return false;
            }
            // 判断单位长度
            return types.length == values.length;
        }
    }

    private static Constructor getConstructor(Class clazz, Class... types) {
        if (clazz == null) {
            return null;
        }
        Constructor constructor = null;
        while (clazz != Object.class) {
            try {
                if (types == null || types.length < 1) {
                    constructor = clazz.getDeclaredConstructor();
                } else {
                    constructor = clazz.getDeclaredConstructor(types);
                }
                if (constructor != null) {
                    if (!constructor.isAccessible()) {
                        constructor.setAccessible(true);
                    }
                    return constructor;
                }
            } catch (Throwable e) {
            }
            clazz = clazz.getSuperclass();
        }
        return constructor;
    }


    public static Object getFieldValue(Object o, String fieldName) {
        if (o == null) {
            return null;
        }
        return getFieldValue(o, o.getClass(), fieldName);
    }

    public static Object getStaticFieldValue(String className, String fieldName) {
        return getFieldValue(null, findClass(className), fieldName);
    }

    public static Object getStaticFieldValue(Class clazz, String fieldName) {
        return getFieldValue(null, clazz, fieldName);
    }

    public static void setStaticFieldValue(String className, String fieldName, Object value) {
        setFieldValue(null, findClass(className), fieldName, value);
    }

    public static void setStaticFieldValue(Class clazz, String fieldName, Object value) {
        if (clazz == null || TextUtils.isEmpty(fieldName)) {
            return;
        }
        setFieldValue(null, clazz, fieldName, value);
    }

    public static void setFieldValue(Object o, String fieldName, Object value) {
        if (o == null) {
            return;
        }
        setFieldValue(o, o.getClass(), fieldName, value);
    }

    private static void setFieldValue(Object o, Class<?> clazz, String fieldName, Object value) {
        try {
            Field field = getField(clazz, fieldName);
            if (field != null) {
                field.set(o, value);
            }
        } catch (Throwable igone) {
        }
    }

    public static Object getFieldValue(Object o, Class clazz, String fieldName) {
        try {
            Field field = getField(clazz, fieldName);
            if (field != null) {
                return field.get(o);
            }
        } catch (Throwable igone) {
        }
        return null;
    }


    public static Class findClass(String name) {
        Class result = null;
        if (TextUtils.isEmpty(name)) {
            return result;
        }
        try {
            result = Class.forName(name);
        } catch (Throwable igone) {
        }
        if (result == null) {
            try {
                //  ClassLoader.getSystemClassLoader().loadClass(className);
                // 1. get classloader
                // 1.1. ClassLoader.getSystemClassLoader()
                Method ldM = getMethod(Class.forName("java.lang.ClassLoader"), "getSystemClassLoader");
                if (ldM == null) {
                    // 1.2. ClazzUtils.class.getClassLoader()
                    ldM = getMethod(ClazzUtils.class, "getClassLoader");
                }
                Object o = invoke(null, ldM);
                if (o != null) {
                    // 2. ClassLoader.loadClass("");
                    Method loadClassMethod = getMethod(o.getClass(), "loadClass", new Class[]{String.class});
                    result = (Class) invoke(o, loadClassMethod, name);
                }

            } catch (Throwable igone) {
            }
        }
        return result;
    }

    /**
     * get Build's static field
     *
     * @param fieldName
     * @return
     */
    public static String getBuildStaticField(String fieldName) {
        try {
            Field fd = getField(Build.class, fieldName);
            if (fd != null) {
                return (String) fd.get(null);
            }
        } catch (Throwable igone) {
        }
        return "";
    }

    /**
     * 反射SystemProperties.get(String).获取数据是default.prop中的数据.
     * api 14-29均有
     *
     * @param key
     * @return
     */
    public static Object getDefaultProp(String key) {
        if (TextUtils.isEmpty(key)) {
            return "";
        }
        return call(findClass("android.os.SystemProperties"), "get", null, new Class[]{String.class}, new Object[]{key});
    }


    public static Object newInstance(Class clazz, Class[] types, Object[] values) {
        try {
            if (clazz == null // 类为空
                    || !isSameLen(types, values)// 参数不一致
            ) {
                return null;
            }
            // support has args and no args
            Constructor ctor = getConstructor(clazz, types);
            if (ctor != null) {
                if (values == null || values.length < 1) {
                    return ctor.newInstance();
                } else {
                    return ctor.newInstance(values);
                }

            }
        } catch (Throwable igone) {
            //
        }
        return null;
    }

    // 基础方法，不能使用其他内部反射
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... types) {
        if (clazz == null || TextUtils.isEmpty(methodName)) {
            return null;
        }
        Method method = null;
        while (clazz != Object.class) {
            try {
                method = clazz.getDeclaredMethod(methodName, types);
                if (method != null) {
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    return method;
                }
            } catch (Throwable igone) {
            }
            clazz = clazz.getSuperclass();
        }
        return method;
    }

    public static Field getFieldAccessFlags(Field field) {
        Field modifiersField = null;
        try {
            modifiersField = Field.class.getDeclaredField("accessFlags");
        } catch (Throwable e) {
        }
        if (modifiersField == null) {
            try {
                modifiersField = Field.class.getDeclaredField("modifiers");
            } catch (Throwable e) {
            }
        }
        if (modifiersField != null) {
            return modifiersField;
        }
        return field;
    }

    public static void unFinal(Field field) {
//        Field modifiersField = Field.class.getDeclaredField("accessFlags");
//        modifiersField.setAccessible(true);
//        //去final
//        modifiersField.setInt(impl, impl.getModifiers() & ~java.lang.reflect.Modifier.FINAL);

        int modifier = field.getModifiers();
        if ((modifier & Modifier.FINAL) == Modifier.FINAL) {
            Field modifiersField = null;
            try {
                modifiersField = Field.class.getDeclaredField("accessFlags");
            } catch (Throwable e) {
            }
            if (modifiersField == null) {
                try {
                    modifiersField = Field.class.getDeclaredField("modifiers");
                } catch (Throwable e) {
                }
            }
            if (modifiersField != null) {
                try {
                    modifiersField.setAccessible(true);
                    modifiersField.setInt(field, modifier & ~Modifier.FINAL);
                } catch (Throwable e) {
                }
            }
        }

    }

    public static Field getField(Class clazz, String fieldName) {
        if (clazz == null) {
            return null;
        }
        Field field = null;
        while (clazz != Object.class) {
            try {
                field = clazz.getDeclaredField(fieldName);
                if (field != null) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    return field;
                }
            } catch (Exception e) {
            }
            clazz = clazz.getSuperclass();
        }
        return field;
    }

    /**
     * 将类名字转换为Class类型
     *
     * @param classNames
     * @return
     */
    public static Class[] converStringToClass(String... classNames) {
        if (classNames != null) {
            Class[] argsClass = new Class[classNames.length];
            for (int i = 0; i < classNames.length; i++) {
                try {
                    argsClass[i] = findClass(classNames[i]);
                } catch (Throwable e) {
                }
            }
            return argsClass;
        }
        return new Class[]{};
    }


}
