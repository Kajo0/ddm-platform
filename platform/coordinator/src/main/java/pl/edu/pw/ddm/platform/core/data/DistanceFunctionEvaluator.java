package pl.edu.pw.ddm.platform.core.data;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

import lombok.Value;
import org.reflections.Reflections;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

class DistanceFunctionEvaluator {

    NamePackageDto callForFunctionName(File jar) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
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

    @Value(staticConstructor = "of")
    static class NamePackageDto {

        private String packageName;
        private String functionName;
    }

}
