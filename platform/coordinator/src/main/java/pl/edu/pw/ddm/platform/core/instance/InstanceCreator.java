package pl.edu.pw.ddm.platform.core.instance;

interface InstanceCreator {

    String create(int workers, Integer cpuCores, Integer workerMemoryInGb, Integer masterMemoryInGb, Integer diskInGb);

    boolean destroy(String id);

    void destroyAll();

}
