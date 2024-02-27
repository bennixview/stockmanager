package com.bewi.stockmanager;

import com.bewi.stockmanager.position.Position;
import com.bewi.stockmanager.position.PositionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "com.bewi")
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(PositionRepository positionRepository) {
		return args -> {
			System.out.println("Start APP");
			Position p = Position.builder()
					.name("Amazon")
					.quantity(100)
					.strikeprice(1000)
					.build();

			positionRepository.save(p);
		};
	}

}
