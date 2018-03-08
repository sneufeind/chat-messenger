package my.chat.messenger.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${my.chat.topic.message.producer}")
    private String messageProducerTopic;

    @Value("${my.chat.topic.onlinestatus.producer}")
    private String onlineStatusChangedTopic;

    Map<String, Object> producerProperties(){
        final Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return props;
    }

    ProducerFactory<String, String> producerFactory(){
        return new DefaultKafkaProducerFactory<>(producerProperties());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(){
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public MessageProducer messageProducer(final KafkaTemplate<String, String> kafkaTemplate){
        return new MessageProducer(this.messageProducerTopic, kafkaTemplate);
    }

    @Bean
    public UserOnlineStatusProducer userOnlineStatusProducer(final KafkaTemplate<String, String> kafkaTemplate){
        return new UserOnlineStatusProducer(this.onlineStatusChangedTopic, kafkaTemplate);
    }
}
