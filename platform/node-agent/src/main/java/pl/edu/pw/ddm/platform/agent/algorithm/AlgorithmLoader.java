package pl.edu.pw.ddm.platform.agent.algorithm;

import java.io.File;

public interface AlgorithmLoader {

    boolean save(byte[] bytes, String algorithmId);

    File load(String algorithmId);

}
