package pl.edu.pw.ddm.api.core.instance;

interface ProcessType {

    String STATUS = "process_status";

    interface Instance {

        String CREATE = "instance_create";
        String INIT = "instance_init";
        String DESTROY = "instance_destroy";
    }

    interface Data {

        String DATA_LOAD = "data_load";
        String DATA_SCATTER = "data_scatter";
    }

    interface Algorithm {

        String START = "algorithm_start";
        String STOP = "algorithm_stop";
    }

}
