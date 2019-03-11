package com.ea.agentloader.loaders;

public final class DynamicInstrumentationLoadAgentMain {

    private DynamicInstrumentationLoadAgentMain() {
    }

    public static void main(final String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: " + DynamicInstrumentationLoadAgentMain.class.getSimpleName()
                    + " <pid> <agentJarAbsolutePath>");
        }
        final String pid = args[0];
        final String agentJarAbsolutePath = args[1];
        AgentLoaderRemote.newInstance(pid).loadAgent(agentJarAbsolutePath, "");
    }
}