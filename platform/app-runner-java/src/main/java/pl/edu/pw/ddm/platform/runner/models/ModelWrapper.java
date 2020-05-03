package pl.edu.pw.ddm.platform.runner.models;

import static java.util.Optional.ofNullable;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ModelWrapper implements Serializable {

    private final LocalModel localModel;
    private final GlobalModel globalModel;

    @Getter
    private final Integer id;

    @Getter
    private final String address;

    @Getter
    private TimeStatistics timeStatistics = new TimeStatistics();

    public static ModelWrapper local(LocalModel model, String address, Integer id) {
        return new ModelWrapper(model, null, id, address);
    }

    public static ModelWrapper global(GlobalModel model, String address) {
        return new ModelWrapper(null, model, null, address);
    }

    public LocalModel getLocalModel() {
        return ofNullable(localModel).orElseThrow(() -> new IllegalArgumentException("No local model"));
    }

    public GlobalModel getGlobalModel() {
        return ofNullable(globalModel).orElseThrow(() -> new IllegalArgumentException("No global model"));
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("[");
        str.append(address);
        str.append("]");
        str.append("[");
        str.append(id);
        str.append("]: ");
        ofNullable(localModel).ifPresent(str::append);
        ofNullable(globalModel).ifPresent(str::append);

        return str.toString();
    }

}
