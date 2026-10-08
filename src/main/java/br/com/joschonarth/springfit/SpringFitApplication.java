package br.com.joschonarth.springfit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringFitApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringFitApplication.class, args);
	}

}
