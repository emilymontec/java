package com.bank.atlasbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación AtlasBank.
 * <p>
 * Inicializa el contexto de Spring Boot y levanta el servidor embebido en el
 * puerto configurado (por defecto 8080).
 */
@SpringBootApplication
public class AtlasBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(AtlasBankApplication.class, args);
	}

}
