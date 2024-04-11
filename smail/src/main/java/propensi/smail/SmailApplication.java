package propensi.smail;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import jakarta.transaction.Transactional;
import propensi.smail.service.AuthService;
import propensi.smail.service.SuratMasukService;

@SpringBootApplication
public class SmailApplication {
	private static SuratMasukService emailService;

	public SmailApplication(SuratMasukService emailService) {
		this.emailService = emailService;
	}

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
