package pl.edu.pw.ddm.platform.core.instance;

interface InstanceCreator {

    String create();

    boolean destroy(String id);

}
