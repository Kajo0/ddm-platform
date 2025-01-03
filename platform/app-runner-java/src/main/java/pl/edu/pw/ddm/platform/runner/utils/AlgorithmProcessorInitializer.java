package pl.edu.pw.ddm.platform.runner.utils;

import java.util.Set;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.reflections.Reflections;
import pl.edu.pw.ddm.platform.interfaces.algorithm.GlobalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

@UtilityClass
public class AlgorithmProcessorInitializer {

    @SneakyThrows
    public <T> T initProcessor(@NonNull String packageName, Class<? extends T> processor, Class<T> clazz) {
        return refs(packageName, clazz)
                .stream()
                .filter(processor::equals)
                .findFirst()
                .orElseThrow(() -> new ProcessorNotFoundException("processor [" + packageName + "] " + processor))
                .getConstructor()
                .newInstance();
    }

    /**
     * @deprecated Use initProcessor
     */
    @Deprecated
    @SneakyThrows
    public LocalProcessor initLocalProcessor(@NonNull String packageName) {
        return refs(packageName, LocalProcessor.class)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ProcessorNotFoundException("local"))
                .getDeclaredConstructor()
                .newInstance();
    }

    /**
     * @deprecated Use initProcessor
     */
    @Deprecated
    @SneakyThrows
    public GlobalProcessor initGlobalProcessor(@NonNull String packageName) {
        return refs(packageName, GlobalProcessor.class)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ProcessorNotFoundException("global"))
                .getDeclaredConstructor()
                .newInstance();
    }

    @SneakyThrows
    public DistanceFunction initDistanceFunction(@NonNull String packageName, @NonNull String name) {
        Set<Class<? extends DistanceFunction>> list = refs(packageName, DistanceFunction.class);
        for (Class<? extends DistanceFunction> clazz : list) {
            DistanceFunction instance = clazz.getDeclaredConstructor()
                    .newInstance();
            if (name.equals(instance.name())) {
                return instance;
            }
        }
        throw new ProcessorNotFoundException("distance function with name: " + name);
    }

    private <T> Set<Class<? extends T>> refs(@NonNull String packageName, Class<T> clazz) {
        return new Reflections(packageName)
                .getSubTypesOf(clazz);
    }

    public static class ProcessorNotFoundException extends RuntimeException {

        ProcessorNotFoundException(String msg) {
            super(msg);
        }
    }

}
