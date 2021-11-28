package pl.edu.pw.ddm.platform.algorithms.clustering.dbirch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LModel implements LocalModel {

    private List<double[]> means;

}
