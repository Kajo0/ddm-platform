package pl.edu.pw.ddm.api.core.instance;

interface QualityMetric {

    interface Clustering {

        String RAND = "rand";
    }

    interface Classification {

        String ACCURACY = "accuracy";
        String F_MEASURE = "fmeasure";
        String AUC = "auc";
    }

}
