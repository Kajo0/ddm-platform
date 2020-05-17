package pl.edu.pw.ddm.platform.core.util;

import java.util.Arrays;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Configuration
@Conditional(InstanceCreationProfileChecker.InstanceCreationProfileCondition.class)
class InstanceCreationProfileChecker {

    static {
        // FIXME make it more elegant
        if (true) {
            throw new IllegalStateException("More than 1 excluding profile is enabled at the same time.");
        }
    }

    static class InstanceCreationProfileCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String[] profiles = context.getEnvironment()
                    .getActiveProfiles();
            return Arrays.stream(profiles)
                    .filter(this::filterProfiles)
                    .count() > 1;
        }

        private boolean filterProfiles(String profile) {
            return ProfileConstants.INSTANCE_LOCAL_DOCKER.equals(profile) ||
                    ProfileConstants.INSTANCE_MANUAL_SETUP.equals(profile);
        }
    }

}
