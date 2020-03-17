package pl.edu.pw.ddm.platform.core.instance;

interface InstanceCreator {

    String create(int workers);

    boolean destroy(String id);

}
