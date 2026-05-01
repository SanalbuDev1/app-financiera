package com.finanzas.personales.finanzas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.r2dbc.autoconfigure.R2dbcAutoConfiguration;

@SpringBootApplication(exclude = R2dbcAutoConfiguration.class)
public class FinanzasApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanzasApplication.class, args);
	}

}
