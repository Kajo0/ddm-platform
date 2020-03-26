package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.impl;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distributed.DistributedCentroidData;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point.ObjectPoint;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LModel implements LocalModel {

    private List<DistributedCentroidData> centroids;
    private List<ObjectPoint> additionalPointsNearCentroids;

}
