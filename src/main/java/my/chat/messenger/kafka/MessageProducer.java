package my.chat.messenger.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.chat.messenger.beans.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class MessageProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public MessageProducer(final String topic, final KafkaTemplate<String, String> kafkaTemplate){
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(final Message message) throws JsonProcessingException {
        LOGGER.info("Sending message='{}' to topic='{}'", message.toString(), this.topic);
        final String json = new ObjectMapper().writeValueAsString(message);
        this.kafkaTemplate.send(this.topic, json);
    }

}
