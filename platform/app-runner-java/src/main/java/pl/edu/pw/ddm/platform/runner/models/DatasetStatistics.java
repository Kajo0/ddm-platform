package pl.edu.pw.ddm.platform.runner.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class DatasetStatistics implements Serializable {

    private Integer avgSampleSize;
    private Integer trainingSamplesAmount;

    public int trainingDataSize() {
        if (avgSampleSize == null || trainingSamplesAmount == null) {
            return -1;
        } else {
            return avgSampleSize * trainingSamplesAmount;
        }
    }

    @Override
    public String toString() {
        int size = trainingDataSize();
        return size != -1 ? String.valueOf(size) : "n/a";
    }

}
