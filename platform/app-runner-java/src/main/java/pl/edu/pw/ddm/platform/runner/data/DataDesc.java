package pl.edu.pw.ddm.platform.runner.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class DataDesc {

    private String separator;
    private Integer idIndex;
    private Integer labelIndex;
    private Integer attributesAmount;
    private String[] colTypes;
    private String[] attributesColTypes;

    String[] getAttributesColTypes() {
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
