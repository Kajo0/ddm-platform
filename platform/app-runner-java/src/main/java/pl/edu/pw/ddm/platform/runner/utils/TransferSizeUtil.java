package pl.edu.pw.ddm.platform.runner.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.model.BaseModel;

public class TransferSizeUtil {

    // TODO make it more exact
    public static Integer sizeOf(BaseModel model) {
        // TODO remove base class serialization size
        return size(model);
    }

    public static Integer sizeOf(Data data) {
        return size(data);
    }

    private static Integer size(Serializable data) {
        if (data == null) {
            return null;
        }

        try {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(data);
                oos.flush();
                return baos.toByteArray().length;
            }
        } catch (IOException e) {
            return null;
        }
    }

}
