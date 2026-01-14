package vk.vkPets;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;


@Testcontainers
class ContainersTest {

    // will be shared between test methods
    @Container
    private static final MySQLContainer MY_SQL_CONTAINER = new MySQLContainer("mysql:8.0.36");

    // will be started before and stopped after each test method
    @Container
    private PostgreSQLContainer postgresqlContainer = new PostgreSQLContainer("postgres:9.6.12")
            .withDatabaseName("foo")
            .withUsername("foo")
            .withPassword("secret");

    @Container
    public GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:6-alpine"))
            .withExposedPorts(6379);

    @BeforeEach
    void setup() {
        // Obtaining a mapped port
        String host = redis.getHost();
        Integer port = redis.getFirstMappedPort();

        System.out.println("Obtained redis address: " + host + ":" + port);
    }

    @Test
    void test() {
        Assertions.assertTrue(MY_SQL_CONTAINER.isRunning());
        Assertions.assertTrue(postgresqlContainer.isRunning());
    }
}
