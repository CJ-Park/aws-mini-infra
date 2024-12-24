package file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Step16S3FileTApplication {

	public static void main(String[] args) {
		SpringApplication.run(Step16S3FileTApplication.class, args);
	}

}
