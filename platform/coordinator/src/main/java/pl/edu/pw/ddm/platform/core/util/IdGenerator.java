package pl.edu.pw.ddm.platform.core.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IdGenerator {

    // TODO improve unique generation
    public String generate(@NonNull String str) {
        int code = str.hashCode();
        return String.valueOf(Math.abs(code));
    }

}
