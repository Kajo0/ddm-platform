package pl.edu.pw.ddm.platform.core.algorithm;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

import lombok.Value;
import org.reflections.Reflections;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;

class ProcessorPackageEvaluator {

    TypePackageDto callForPackageName(File jar) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        var url = new URL[]{jar.toURI().toURL()};
        try (URLClassLoader loader = new URLClassLoader(url)) {
            var clazz = new Reflections(loader)
                    .getSubTypesOf(MiningMethod.class)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Local processor not found in jar: " + jar.getName()))
                    .getDeclaredConstructor()
                    .newInstance();

            return TypePackageDto.of(clazz.getClass().getPackageName(), clazz.type(), clazz.name());
        }
    }

    @Value(staticConstructor = "of")
    static class TypePackageDto {

        private String packageName;
        private String algorithmType;
        private String algorithmName;
    }

}
