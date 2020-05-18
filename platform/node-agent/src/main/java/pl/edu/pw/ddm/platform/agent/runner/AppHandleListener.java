package pl.edu.pw.ddm.platform.agent.runner;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.launcher.SparkAppHandle;

@Slf4j
class AppHandleListener implements SparkAppHandle.Listener {

    @Override
    public void stateChanged(SparkAppHandle handle) {
        // TODO use appId from here instead of app-runner status file
        log.info("Spark app '{}' state changed: {}.", handle.getAppId(), handle.getState());
    }

    @Override
    public void infoChanged(SparkAppHandle handle) {
        log.info("Spark app '{}' info changed: {}.", handle.getAppId(), handle.getState());
    }

}
