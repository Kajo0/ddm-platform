package pl.edu.pw.ddm.platform.strategies.covariateshift;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class SplitMapEntry {

    private int lastPut;
    private int modulo;
    private int shift;

}
