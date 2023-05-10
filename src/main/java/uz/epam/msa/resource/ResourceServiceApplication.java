package uz.epam.msa.resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@SpringBootApplication
@EnableJpaRepositories("uz.epam.msa.resource.repository")
@EntityScan("uz.epam.msa.resource.domain")
@EnableEurekaClient
@EnableResourceServer
public class ResourceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResourceServiceApplication.class, args);
	}

}
