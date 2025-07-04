package lii.authservice;

import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractPostgresTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("auth_test")
                    .withUsername("auth_user")
                    .withPassword("secret");

}
