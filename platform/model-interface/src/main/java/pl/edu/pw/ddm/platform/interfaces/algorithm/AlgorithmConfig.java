package pl.edu.pw.ddm.platform.interfaces.algorithm;

import lombok.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface AlgorithmConfig {

    DdmPipeline pipeline();

    /**
     * If the algorithm works only with numerical data
     */
    default boolean onlyNumerical() {
        return false;
    }

    /**
     * If the algorithm works only with nominal data
     */
    default boolean onlyNominal() {
        return false;
    }

    /**
     * If the algorithm works only on single node - non-distributed implementation
     */
    default boolean onlySingleNode() {
        return false;
    }

    /**
     * List of available parameters used by the algorithm with description
     */
    default List<CustomParamDesc> availableParameters() {
        return Collections.singletonList(CustomParamDesc.of("dummy", Void.class, null, "no desc provided"));
    }

    default String printAvailableParameters() {
        List<CustomParamDesc> params = availableParameters();
        if (params == null) {
            return "no params described";
        } else {
            StringBuilder str = new StringBuilder();
            for (CustomParamDesc param : params) {
                str.append(param.key);
                str.append(" [");
                str.append(param.type.getSimpleName());
                str.append(", default=");
                str.append(param.defaultValue);
                str.append("]: ");
                str.append(param.desc);
                Arrays.stream(param.options)
                        .forEach(o -> str.append(System.lineSeparator())
                                .append("  - ")
                                .append(o));
                str.append(System.lineSeparator());
            }
            return str.toString();
        }
    }

    @Value
    class CustomParamDesc {

        public static CustomParamDesc of(String key,
                                         Class<?> type,
                                         String defaultValue,
                                         String desc,
                                         String... options) {
            return new CustomParamDesc(key, type, defaultValue, desc, options);
        }

        String key;
        Class<?> type;
        String defaultValue;
        String desc;
        String[] options;
    }

}
