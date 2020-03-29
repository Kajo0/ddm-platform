package pl.edu.pw.ddm.platform.agent.data;

import org.springframework.boot.test.mock.mockito.MockBean;
import pl.edu.pw.ddm.platform.agent.data.dto.DataDesc;

@MockBean
public class DataLoaderMock implements DataLoader {

    @Override
    public boolean save(byte[] bytes, DataType type, DataDesc dataDesc) {
        return false;
    }

}
