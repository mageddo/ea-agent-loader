package com.ea.agentloader.utils;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static com.ea.agentloader.utils.ClassUtils.getClassInputStream;

public final class JarUtils {

    private JarUtils() {
    }

    /**
     * Creates a new jar that only contains the DynamicInstrumentationAgent class.
     */
    public static Path createTempJar(final Class<?> clazz, final boolean agent, final Class<?>... additionalClasses) {
        try {
            final String className = clazz.getName();
            final Path tempAgentJar = Files.createTempFile(className, ".jar");
            final Manifest manifest = new Manifest(clazz.getResourceAsStream("/META-INF/MANIFEST.MF"));
            if (agent) {
                manifest.getMainAttributes().putValue("Premain-Class", className);
                manifest.getMainAttributes().putValue("Agent-Class", className);
                manifest.getMainAttributes().putValue("Can-Redefine-Classes", String.valueOf(true));
                manifest.getMainAttributes().putValue("Can-Retransform-Classes", String.valueOf(true));
            }
            final JarOutputStream tempJarOut = new JarOutputStream(Files.newOutputStream(tempAgentJar), manifest);
            final JarEntry entry = new JarEntry(className.replace(".", "/") + ".class");
            tempJarOut.putNextEntry(entry);
            final InputStream classIn = getClassInputStream(clazz);
            IOUtils.copy(classIn, tempJarOut);
            tempJarOut.closeEntry();
            if (additionalClasses != null) {
                for (final Class<?> additionalClazz : additionalClasses) {
                    final String additionalClassName = additionalClazz.getName();
                    final JarEntry additionalEntry = new JarEntry(additionalClassName.replace(".", "/") + ".class");
                    tempJarOut.putNextEntry(additionalEntry);
                    final InputStream additionalClassIn = getClassInputStream(additionalClazz);
                    IOUtils.copy(additionalClassIn, tempJarOut);
                    tempJarOut.closeEntry();
                }
            }
            tempJarOut.close();
            return tempAgentJar;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
