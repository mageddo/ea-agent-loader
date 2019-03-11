package com.ea.agentloader.loaders;

import com.ea.agentloader.AgentLoaderHotSpot;
import com.ea.agentloader.ClassPathUtils;
import com.ea.agentloader.utils.JarUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * -Djdk.attach.allowAttachSelf https://www.bountysource.com/issues/45231289-self-attach-fails-on-jdk9
 * workaround this limitation by attaching from a new process
 */
public class AgentLoaderCurrentVM implements AgentLoaderInterface {

    private final String pid;

    public AgentLoaderCurrentVM(String pid) {
        this.pid = pid;
    }

    @Override
    public void loadAgent(String agentJar, String options) {
        final Class<?> instrumenterClass = DynamicInstrumentationLoadAgentMain.class;
        final Path loadAgentJar = JarUtils.createTempJar(instrumenterClass, false, requiredClasses());
        final String javaExecutable = getJavaExecutable();
        final List<String> command = new ArrayList<String>();
        command.add(javaExecutable);
        command.add("-classpath");
        command.add(String.valueOf(loadAgentJar)); //tools.jar not needed since java9
        command.add(instrumenterClass.getName());
        command.add(pid);
        command.add(agentJar);
        try {
            new ProcessExecutor().command(command)
                    .destroyOnExit()
                    .exitValueNormal()
                    .redirectOutput(System.out)
                    .redirectError(System.out)
                    .execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Class[] requiredClasses() {
        return new Class[]{AgentLoaderRemote.class, AgentLoaderHotSpot.class, AgentLoaderInterface.class, ClassPathUtils.class};
    }

    private String getJavaExecutable() {
        return System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    }
}
