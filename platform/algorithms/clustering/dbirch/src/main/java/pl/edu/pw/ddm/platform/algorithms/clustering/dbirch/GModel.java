package pl.edu.pw.ddm.platform.algorithms.clustering.dbirch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GModel implements GlobalModel {

    private Map<Integer, double[]> classToCentroidMap;

}
