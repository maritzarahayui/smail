package propensi.smail;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import jakarta.transaction.Transactional;
import propensi.smail.service.AuthService;

@SpringBootApplication
public class SmailApplication {
	public static void main(String[] args) {
		SpringApplication.run(SmailApplication.class, args);
	}

	@Bean
	@Transactional
	CommandLineRunner run(AuthService authService) {
		return args -> {
			authService.importDataPengguna();
		};
		

	}

}
