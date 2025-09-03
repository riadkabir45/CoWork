package com.aritra.d.riad.CoWork;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoWorkApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoWorkApplication.class, args);
	}

}
