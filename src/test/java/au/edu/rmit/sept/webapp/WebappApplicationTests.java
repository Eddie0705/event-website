package au.edu.rmit.sept.webapp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebappApplicationTests {

	@Test
	void contextLoads() {
	}
	@Test
	void basicPassTest() {
		assertTrue(true, "Basic sanity check should pass");
	}

}
