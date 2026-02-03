package com.gumraze.drive.drive_backend;

import com.gumraze.drive.drive_backend.region.service.RegionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@ActiveProfiles("test")
class DriveBackendApplicationTests {

	@Mock
	RegionService regionService;

	@MockitoBean
	RestClient.Builder restClientBuilder;

	@Test
	void contextLoads() {
	}

}
