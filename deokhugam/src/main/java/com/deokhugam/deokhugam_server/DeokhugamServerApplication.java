package com.deokhugam.deokhugam_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DeokhugamServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeokhugamServerApplication.class, args);
	}

}
