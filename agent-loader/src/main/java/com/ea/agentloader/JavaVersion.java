package com.ea.agentloader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaVersion {

    public static final JavaVersion JAVA_9 = new JavaVersion("1.9");
    public static final JavaVersion JAVA_8 = new JavaVersion("1.8");
    public static final JavaVersion JAVA_7 = new JavaVersion("1.7");
    public static final JavaVersion JAVA_6 = new JavaVersion("1.6");

    private Pattern JVM_VERSION_REGEX = Pattern.compile("(\\d+)[-.](\\d*)");

    private final int major;
    private final int minor;
    private final double version;

    public JavaVersion(String version) {
        final Matcher matcher = JVM_VERSION_REGEX.matcher(version);
        if(!matcher.find()){
            throw new IllegalArgumentException("Could not find java version on given string " + version);
        }
        this.major = Integer.valueOf(matcher.group(1));
        final String minorVersion = matcher.group(2);
        this.minor = Integer.valueOf(minorVersion.trim().length() == 0 ? "0" : minorVersion);
        this.version = Double.valueOf(String.format("%s.%s", major, minor));
    }

    public boolean gte(JavaVersion version) {
        return Double.compare(this.version, version.version) >= 0;
    }

    public static JavaVersion current() {
        return new JavaVersion(System.getProperty("java.version"));
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

}