package pl.edu.pw.ddm.platform.agent.results;

import java.io.File;

public interface ResultsLoader {

    File load(String executionId);

    String loadJsonStats(String executionId);

}
