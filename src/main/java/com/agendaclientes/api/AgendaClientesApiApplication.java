package com.agendaclientes.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgendaClientesApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgendaClientesApiApplication.class, args);
	}

}
