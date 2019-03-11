package com.ea.agentloader.utils;

import java.io.InputStream;

public final class ClassUtils {
    private ClassUtils() {
    }
    public static InputStream getClassInputStream(final Class<?> clazz) {
        final String name = "/" + clazz.getName().replace(".", "/") + ".class";
        final InputStream classIn = clazz.getResourceAsStream(name);
        if (classIn == null) {
            throw new NullPointerException("resource input stream should not be null: " + name);
        }
        return classIn;
    }
}
