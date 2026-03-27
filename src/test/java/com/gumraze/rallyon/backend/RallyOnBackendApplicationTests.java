package com.gumraze.rallyon.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@ActiveProfiles("test")
class RallyOnBackendApplicationTests {

	@MockitoBean
	RestClient.Builder restClientBuilder;

	@Test
	void contextLoads() {
	}

}
