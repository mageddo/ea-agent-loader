package com.ea.agentloader.loaders;

import com.ea.agentloader.AgentLoader;
import com.ea.agentloader.ClassPathUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AgentLoaderRemote implements AgentLoaderInterface {

    private final Object agentLoaderObject;

    AgentLoaderRemote(Object agentLoaderObject) {
        this.agentLoaderObject = agentLoaderObject;
    }

    @Override
    public void loadAgent(String agentJar, String options) {
        try {
            final Method loadAgentMethod = agentLoaderObject.getClass().getMethod("loadAgent", String.class, String.class);
            loadAgentMethod.invoke(agentLoaderObject, agentJar, options);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static AgentLoaderInterface newInstance(String pid) {
        Class<AgentLoaderInterface> agentLoaderClass;
        try {
            Class.forName("com.sun.tools.attach.VirtualMachine");
            agentLoaderClass = (Class<AgentLoaderInterface>) Class.forName("com.ea.agentloader.AgentLoaderHotSpot");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            // tools.jar not available in the class path
            // so we load our own copy of those files
            final List<String> shaded = Arrays.asList(
                    "shaded/AttachProvider.class",
                    "shaded/VirtualMachine.class",
                    "AttachProviderPlaceHolder.class",
                    "shaded/AgentInitializationException.class",
                    "shaded/AgentLoadException.class",
                    "shaded/AttachNotSupportedException.class",
                    "shaded/AttachOperationFailedException.class",
                    "shaded/AttachPermission.class",
                    "shaded/HotSpotAttachProvider.class",
                    "shaded/HotSpotVirtualMachine.class",
                    "shaded/VirtualMachineDescriptor.class",

                    "shaded/WindowsAttachProvider.class",
                    "shaded/WindowsVirtualMachine.class",

                    "shaded/SolarisAttachProvider.class",
                    "shaded/SolarisVirtualMachine.class",

                    "shaded/LinuxAttachProvider.class",
                    "shaded/LinuxVirtualMachine.class",

                    "shaded/BsdAttachProvider.class",
                    "shaded/BsdVirtualMachine.class",

                    "shaded/HotSpotAttachProvider$HotSpotVirtualMachineDescriptor.class",

                    "shaded/BsdVirtualMachine$SocketInputStream.class",
                    "shaded/LinuxVirtualMachine$SocketInputStream.class",
                    "shaded/SolarisVirtualMachine$SocketInputStream.class",
                    "shaded/WindowsVirtualMachine$PipedInputStream.class"
            );
            final ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
            List<Class<?>> classes = new ArrayList<Class<?>>();
            for (String s : shaded) {
                // have to load those class in the system class loader
                // to prevent getting "Native Library .../libattach.so already loaded in another classloader"
                // when the vm is used more than once.
                try {
                    classes.add(ClassPathUtils.defineClass(systemLoader, AgentLoaderRemote.class.getResourceAsStream(s)));
                } catch (Exception e) {
                    throw new RuntimeException("Error defining: " + s, e);
                }
            }
            try {
                agentLoaderClass = (Class<AgentLoaderInterface>) ClassPathUtils.defineClass(
                    systemLoader,
                    AgentLoader.class.getResourceAsStream("/com/ea/agentloader/AgentLoaderHotSpot.class")
                );
            } catch (Exception e) {
                throw new RuntimeException("Error loading AgentLoader implementation", e);
            }
        }
        try {
            final Object agentLoaderObject = agentLoaderClass.getDeclaredConstructor(String.class).newInstance(pid);

            // the agent loader might be instantiated in another class loader
            // so no interface it implements is guaranteed to be visible here.
            // this reflection based implementation of this interface solves this problem.
            return new AgentLoaderRemote(agentLoaderObject);
        } catch (Exception e) {
            throw new RuntimeException("Error getting agent loader implementation to load: ", e);
        }
    }
}
