package pl.edu.pw.ddm.platform.agent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import pl.edu.pw.ddm.platform.agent.data.DataLoaderMock;

@SpringBootTest(classes = DataLoaderMock.class)
class AgentApplicationTest {

    @Test
    void contextLoads() {
    }

}
