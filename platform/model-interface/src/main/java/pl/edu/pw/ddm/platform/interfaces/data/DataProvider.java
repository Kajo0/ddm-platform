package pl.edu.pw.ddm.platform.interfaces.data;

import java.util.Collection;
import java.util.stream.Stream;

import lombok.Builder;

// TODO data lazy/partial loading
public interface DataProvider {

    DataDesc getDataDescription();

    Collection<Data> training();

    Collection<Data> test();

    Collection<Data> all();

    @lombok.Data
    @Builder
    class DataDesc {

        private String separator;
        private Integer idIndex;
        private Integer labelIndex;
        private Integer attributesAmount;
        private String[] colTypes;
        private String[] attributesColTypes;
        private Boolean onlyNumericAttributes;

        public boolean hasOnlyNumericAttributes() {
            if (onlyNumericAttributes == null) {
                onlyNumericAttributes = Stream.of(getAttributesColTypes())
                        .allMatch(AttributeType.NUMERIC::equals);
            }
            return onlyNumericAttributes;
        }

        public String[] getAttributesColTypes() {
            if (attributesColTypes != null) {
                return attributesColTypes;
            }

            attributesColTypes = new String[attributesAmount];
            for (int i = 0, j = 0; i < colTypes.length; ++i) {
                if (i != idIndex && i != labelIndex) {
                    attributesColTypes[j++] = colTypes[i];
                }
            }

            return attributesColTypes;
        }

    }

    interface AttributeType {

        String NUMERIC = "numeric";
        String NOMINAL = "nominal";
    }

}
