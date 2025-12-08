package com.soundspace;

import com.soundspace.config.ApplicationConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationConfigProperties.class)
public class SoundspaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoundspaceApplication.class, args);
	}

}
