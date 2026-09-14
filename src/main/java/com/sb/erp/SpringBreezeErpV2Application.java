package com.sb.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringBreezeErpV2Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringBreezeErpV2Application.class, args);
	}

}