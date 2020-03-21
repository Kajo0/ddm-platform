package pl.edu.pw.ddm.platform.runner.utils;

import java.util.Set;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.reflections.Reflections;
import pl.edu.pw.ddm.platform.interfaces.algorithm.GlobalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.LocalProcessor;

@UtilityClass
public class AlgorithmProcessorInitializer {

    private static final String BASE_PACKAGE = "pl.edu.pw.ddm.platform";

    @SneakyThrows
    public LocalProcessor initLocalProcessor() {
        Reflections reflections = new Reflections(BASE_PACKAGE);
        Set<Class<? extends LocalProcessor>> classes = reflections.getSubTypesOf(LocalProcessor.class);
        Class<? extends LocalProcessor> clazz = classes.stream()
                .peek(System.out::println)
                .findFirst()
                .orElseThrow(() -> new ProcessorNotFoundException("local"));
        return clazz.getDeclaredConstructor()
                .newInstance();
    }

    @SneakyThrows
    public GlobalProcessor initGlobalProcessor() {
        Reflections reflections = new Reflections(BASE_PACKAGE);
        Set<Class<? extends GlobalProcessor>> classes = reflections.getSubTypesOf(GlobalProcessor.class);
        Class<? extends GlobalProcessor> clazz = classes.stream()
                .peek(System.out::println)
                .findFirst()
                .orElseThrow(() -> new ProcessorNotFoundException("global"));
        return clazz.getDeclaredConstructor()
                .newInstance();
    }

    public static class ProcessorNotFoundException extends RuntimeException {

        ProcessorNotFoundException(String msg) {
            super(msg);
        }
    }

}
