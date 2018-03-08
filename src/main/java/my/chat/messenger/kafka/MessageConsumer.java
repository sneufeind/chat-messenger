package my.chat.messenger.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.chat.messenger.beans.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;

import java.io.IOException;

public class MessageConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumer.class);

    @Value("${my.chat.user}")
    private String user;

    private MessageConsumerListener listener = null;

    public MessageConsumer(){}

    public void setNewMessageReceivedListener(MessageConsumerListener listener){
        this.listener = listener;
    }

    @KafkaListener(topics = "${my.chat.topic.message.consumer}")
    public void receive(final ConsumerRecord<String,String> consumerRecord){
        LOGGER.info("Receive consumer record='{}'", consumerRecord.toString());
        final Message newMessage = convertToMessage(consumerRecord.value());
        if (newMessage != null) {
            LOGGER.info("Receive message='{}'", newMessage.toString());
            if(newMessage.getReceivers() == null || newMessage.getReceivers().isEmpty() // this message is broadcasted
                    || this.user.equals(newMessage.getSender()) // this message is sent by me
                    || newMessage.getReceivers().contains(this.user)){ // this message is send to me

                if(this.listener != null) {
                    LOGGER.info("Received message is addressed to user and will be accepted!");
                    this.listener.newMessageReceived(newMessage);
                }else{
                    LOGGER.warn("There is no message listener configured yet, so the received message can not be handled!");
                }
            }else{
                LOGGER.info("Receive message is not addressed to user and will be ignored!");
            }
        }else{
            LOGGER.warn("Receive message is NULL and will be ignored!");
        }
    }

    private static Message convertToMessage(final String json){
        if(json == null){
            return null;
        }
        Message newMessage = null;
        try {
            newMessage = new ObjectMapper().readValue(json, Message.class);
        } catch (IOException e) {
            LOGGER.error("Could not deserialize received message, so it will be ignored", e);
            newMessage = null;
        }
        return newMessage;
    }
}
