package com.soundspace;

import com.soundspace.config.ApplicationConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationConfigProperties.class)
@EnableCaching
public class SoundspaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoundspaceApplication.class, args);
	}

}
