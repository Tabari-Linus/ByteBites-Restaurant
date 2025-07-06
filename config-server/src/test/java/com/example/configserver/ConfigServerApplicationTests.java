package com.example.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest

@TestPropertySource(properties = {
        "spring.profiles.active=native",
        "spring.cloud.config.server.native.search-locations=classpath:file://${user.home}/bytebites-platform/config-repo"
})
class ConfigServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
