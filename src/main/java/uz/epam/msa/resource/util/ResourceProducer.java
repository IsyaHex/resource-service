package uz.epam.msa.resource.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import uz.epam.msa.resource.constant.Constants;

@Component
@Slf4j
public class ResourceProducer {
    private final NewTopic topic;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public ResourceProducer(NewTopic topic, KafkaTemplate<String, String> kafkaTemplate) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(Integer resourceId) {
        log.info(String.format(Constants.RECEIVED_RESOURCE_ID, resourceId));
        Message<Integer> message = MessageBuilder
                .withPayload(resourceId)
                .setHeader(KafkaHeaders.TOPIC, topic.name())
                .build();
        kafkaTemplate.send(message);
    }
}
