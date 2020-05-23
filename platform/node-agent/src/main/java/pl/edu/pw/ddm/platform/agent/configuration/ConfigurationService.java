package pl.edu.pw.ddm.platform.agent.configuration;

import java.nio.file.AccessDeniedException;

public interface ConfigurationService {

    void setup(String masterPublicAddr) throws AccessDeniedException;

}
