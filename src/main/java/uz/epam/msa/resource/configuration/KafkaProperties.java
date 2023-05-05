package uz.epam.msa.resource.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties
public class KafkaProperties {
    @Value("${app.kafka.bootstrap.server}")
    private String bootstrapServer;

    @Value("${app.kafka.group.id}")
    private String groupId;
}