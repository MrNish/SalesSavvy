package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ServletComponentScan
public class SalesSavvyAppApplication {

	public static void main(String[] args) {
		// if not work then don't initialize the varialbe of context;
		ConfigurableApplicationContext context = SpringApplication.run(SalesSavvyAppApplication.class, args);

	}

}
