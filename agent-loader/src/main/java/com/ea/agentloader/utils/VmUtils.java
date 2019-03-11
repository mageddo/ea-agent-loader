package com.ea.agentloader.utils;

import java.lang.management.ManagementFactory;

public final class VmUtils {
    private VmUtils() {
    }

    /**
     * Gets the current jvm pid.
     *
     * @return the pid as String
     */
    public static String getPid() {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        return nameOfRunningVM.substring(0, p);
    }
}
