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

    int dataAttributes() {
        int minus = 0;
        if (idIndex != null) {
            ++minus;
        }
        if (labelIndex != null) {
            ++minus;
        }
        return attributesAmount - minus;
    }

}
