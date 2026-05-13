package com.proyecto.GestionVeterinaria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GestionVeterinariaApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionVeterinariaApplication.class, args);
	}
}
