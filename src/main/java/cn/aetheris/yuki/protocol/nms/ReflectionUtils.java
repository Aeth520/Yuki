package cn.aetheris.yuki.protocol.nms;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtils {

    public static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    
    @SneakyThrows
    public static <T> T newClass(Class<?> clazz, Object... args) {
        return (T) clazz.getConstructor().newInstance(args);
    }

    
    @SneakyThrows
    public static Method getMethod(Class<?> clazz, String methodName, boolean accessible, Class<?>... argsTypes) {
        Method method = clazz.getDeclaredMethod(methodName, argsTypes);
        method.setAccessible(accessible);
        return method;
    }

    
    @SneakyThrows
    public static Field getField(Class<?> clazz, String fieldName, boolean accessible) {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(accessible);
        return field;
    }

    
    @SneakyThrows
    public static <T> T invokeMethod(Method method, Object object, Object... args) {
        Object invokeObject = method.invoke(object, args);
        if (invokeObject == null) {
            return null;
        }
        return (T) invokeObject;
    }

    
    @SneakyThrows
    public static <T> T getFieldValue(Field field, Object object) {
        return (T) field.get(object);
    }

    
    @SneakyThrows
    public static void setFieldValue(Field field, Object object, Object value) {
        field.set(object, value);
    }
}