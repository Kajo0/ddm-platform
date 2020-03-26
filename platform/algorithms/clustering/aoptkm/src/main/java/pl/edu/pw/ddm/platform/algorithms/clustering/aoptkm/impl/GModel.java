package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.impl;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point.ObjectPoint;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GModel implements GlobalModel {

    private List<ObjectPoint> centroids;

}
