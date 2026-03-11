package com.gumraze.rallyon.backend;

import com.gumraze.rallyon.backend.region.service.RegionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@ActiveProfiles("test")
class RallyOnBackendApplicationTests {

	@Mock
	RegionService regionService;

	@MockitoBean
	RestClient.Builder restClientBuilder;

	@Test
	void contextLoads() {
	}

}
