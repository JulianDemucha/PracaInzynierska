package com.soundspace;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SoundspaceApplicationTests {

	// hubert nie pushuj jak ten test nie przejdzie pozdrawiam
	@Test
	void contextLoads() {
	}

}
