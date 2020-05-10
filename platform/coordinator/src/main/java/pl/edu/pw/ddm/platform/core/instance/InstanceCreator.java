package pl.edu.pw.ddm.platform.core.instance;

interface InstanceCreator {

    String create(int workers, Integer cpuCores, Integer memoryInGb, Integer diskInGb);

    boolean destroy(String id);

    void destroyAll();

}
