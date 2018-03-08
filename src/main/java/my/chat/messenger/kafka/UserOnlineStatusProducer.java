package my.chat.messenger.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.chat.messenger.beans.UserOnlineStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class UserOnlineStatusProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserOnlineStatusProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public UserOnlineStatusProducer(final String topic, final KafkaTemplate<String, String> kafkaTemplate){
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserIsOnline(final String user) throws JsonProcessingException {
        final UserOnlineStatusChangedEvent statusChangedEvent = new UserOnlineStatusChangedEvent();
        statusChangedEvent.setUser(user);
        statusChangedEvent.setOnline(true);
        LOGGER.info("Sending user='{}' is online to topic='{}'", user, this.topic);
        final String json = new ObjectMapper().writeValueAsString(statusChangedEvent);
        this.kafkaTemplate.send(this.topic, json);
    }

    public void sendUserIsOffline(final String user) throws JsonProcessingException {
        final UserOnlineStatusChangedEvent statusChangedEvent = new UserOnlineStatusChangedEvent();
        statusChangedEvent.setUser(user);
        statusChangedEvent.setOnline(false);
        LOGGER.info("Sending user='{}' is offline to topic='{}'", user, this.topic);
        final String json = new ObjectMapper().writeValueAsString(statusChangedEvent);
        this.kafkaTemplate.send(this.topic, json);
    }
}
