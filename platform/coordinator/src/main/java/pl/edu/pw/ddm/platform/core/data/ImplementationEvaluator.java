package pl.edu.pw.ddm.platform.core.data;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

import lombok.Value;
import org.reflections.Reflections;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy;

class ImplementationEvaluator {

    NamePackageDto callForDistanceFunctionName(File jar) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        var url = new URL[]{jar.toURI().toURL()};
        try (URLClassLoader loader = new URLClassLoader(url)) {
            DistanceFunction clazz = new Reflections(loader)
                    .getSubTypesOf(DistanceFunction.class)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("distance function not found in jar: " + jar.getName()))
                    .getDeclaredConstructor()
                    .newInstance();

            return NamePackageDto.of(clazz.getClass().getPackageName(), clazz.name());
        }
    }

    NamePackageDto callForPartitionerStrategyName(File jar) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        var url = new URL[]{jar.toURI().toURL()};
        try (URLClassLoader loader = new URLClassLoader(url)) {
            PartitionerStrategy clazz = new Reflections(loader)
                    .getSubTypesOf(PartitionerStrategy.class)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("partitioner strategy not found in jar: " + jar.getName()))
                    .getDeclaredConstructor()
                    .newInstance();

            return NamePackageDto.of(clazz.getClass().getPackageName(), clazz.name());
        }
    }

    DistanceFunction callForDistanceFunction(File jar) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        var url = new URL[]{jar.toURI().toURL()};
        try (URLClassLoader loader = new URLClassLoader(url)) {
            return new Reflections(loader)
                    .getSubTypesOf(DistanceFunction.class)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("distance function not found in jar: " + jar.getName()))
                    .getConstructor()
                    .newInstance();
        }
    }

    PartitionerStrategy callForPartitionerStrategy(File jar) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        // FIXME try to load dynamically transitive classes instead of forcing classloader in jar to load classes
        var url = new URL[]{jar.toURI().toURL()};
        try (URLClassLoader loader = new URLClassLoader(url)) {
            return new Reflections(loader)
                    .getSubTypesOf(PartitionerStrategy.class)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("partitioner strategy not found in jar: " + jar.getName()))
                    .getConstructor()
                    .newInstance();
        }
    }

    @Value(staticConstructor = "of")
    static class NamePackageDto {

        private String packageName;
        private String implName;
    }

}
