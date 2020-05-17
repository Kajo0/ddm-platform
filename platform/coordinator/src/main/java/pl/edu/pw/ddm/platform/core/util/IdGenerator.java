package pl.edu.pw.ddm.platform.core.util;

import java.util.UUID;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

@UtilityClass
public class IdGenerator {

    // TODO improve unique generation
    public String generate(@NonNull String str) {
        int code = str.hashCode();
        return String.valueOf(Math.abs(code));
    }

    public String instanceId() {
        return UUID.randomUUID()
                .toString();
    }

    public String nodeId(String name, String withId) {
        if (name == null) {
            name = StringUtils.EMPTY;
        } else {
            name = name + "-";
        }

        if (withId == null) {
            withId = RandomStringUtils.randomAlphanumeric(64 - name.length());
        }

        return name + withId;
    }

}
