package pl.edu.pw.ddm.platform.core.instance;

import org.springframework.stereotype.Service;

@Service
public class InstanceFacade {

    private final InstanceConfig instanceConfig;
    private final InstanceCreator creator;

    InstanceFacade(InstanceConfig instanceConfig, InstanceCreator creator) {
        this.instanceConfig = instanceConfig;
        this.creator = creator;
    }

    public String create(Object params) {
        return creator.create();
    }

    public void destroy(String id) {
        creator.destroy(id);
    }

    public String info() {
        return instanceConfig.getInstanceMap().toString();
    }

}
