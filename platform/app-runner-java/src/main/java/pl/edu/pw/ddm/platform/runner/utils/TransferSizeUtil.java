package pl.edu.pw.ddm.platform.runner.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import pl.edu.pw.ddm.platform.interfaces.model.BaseModel;

public class TransferSizeUtil {

    // TODO make it more exact
    public static Integer sizeOf(BaseModel model) {
        // TODO remove base class serialization size
        try {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(model);
                oos.flush();
                return baos.toByteArray().length;
            }
        } catch (IOException e) {
            return null;
        }
    }

}
