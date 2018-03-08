package my.chat.messenger.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.chat.messenger.beans.UserOnlineStatusChangedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class UserOnlineStatusConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserOnlineStatusConsumer.class);

    private final Map<String, Boolean> userOnlineStatusMap = new HashMap<>();
    private UserOnlineStatusChangedListener listener = null;

    public UserOnlineStatusConsumer(){
    }

    public void setUserOnlineStatusChangedListener(UserOnlineStatusChangedListener listener){
        this.listener = listener;
    }

    @KafkaListener(topics = "${my.chat.topic.onlinestatus.consumer}")
    public void receive(final ConsumerRecord<String,String> consumerRecord){
        LOGGER.info("Receive consumer record='{}'", consumerRecord.toString());
        final UserOnlineStatusChangedEvent event = convertToUserOnlineStatusChangedEvent(consumerRecord.value());
        if(event != null){
            if(this.listener != null) {
                LOGGER.info("Received message is addressed to user and will be accepted!");
                LOGGER.info("Online status of user '{}' changed to {}.", event.getUser(), event.isOnline());
                this.userOnlineStatusMap.put(event.getUser(), event.isOnline());
                this.listener.onlineStatusChanged();
            }else{
                LOGGER.warn("There is no message listener configured yet, so the received message can not be handled!");
            }
        }else{
            LOGGER.warn("Receive event is NULL and will be ignored!");
        }
    }

    private static UserOnlineStatusChangedEvent convertToUserOnlineStatusChangedEvent(final String json){
        if(json == null){
            return null;
        }
        UserOnlineStatusChangedEvent event = null;
        try {
            event = new ObjectMapper().readValue(json, UserOnlineStatusChangedEvent.class);
        } catch (IOException e) {
            LOGGER.error("Could not deserialize received UserOnlineStatusChangedEvent, so it will be ignored", e);
            event = null;
        }
        return event;
    }

    public Stream<String> registeredUsers(){
        return this.userOnlineStatusMap.keySet().stream();
    }

    public Stream<String> onlineUsers() {
        return registeredUsers() //
                .filter(u -> this.userOnlineStatusMap.get(u).booleanValue());
    }
}
