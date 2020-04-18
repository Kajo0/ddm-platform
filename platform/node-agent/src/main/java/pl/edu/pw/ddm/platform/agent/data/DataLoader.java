package pl.edu.pw.ddm.platform.agent.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.pw.ddm.platform.agent.data.dto.DataDesc;

public interface DataLoader {

    boolean save(byte[] bytes, DataType type, DataDesc dataDesc);

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    enum DataType {
        TRAIN("train"),
        TEST("test"),
        DESCRIPTION("desc");

        private String code;

        public static DataType ofType(String typeCode) {
            for (DataType dt : DataType.values()) {
                if (dt.code.equals(typeCode)) {
                    return dt;
                }
            }
            throw new IllegalArgumentException("Not found DataType with code: " + typeCode);
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    enum DescriptionKey {
        SEPARATOR("separator"),
        ID_INDEX("idIndex"),
        LABEL_INDEX("labelIndex"),
        ATTRIBUTES_AMOUNT("attributesAmount"),
        COLUMNS_TYPES("colTypes");

        private String code;
    }

}
