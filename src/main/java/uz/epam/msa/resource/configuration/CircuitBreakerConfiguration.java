package uz.epam.msa.resource.configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uz.epam.msa.resource.constant.Constants;

@Configuration
public class CircuitBreakerConfiguration {

    @Bean
    public CircuitBreaker getCircuitBreaker() {
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(CircuitBreakerConfig.ofDefaults());
        return circuitBreakerRegistry.circuitBreaker(Constants.CIRCUIT_BREAKER_CONFIG_NAME);
    }
}
