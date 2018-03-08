package my.chat.messenger.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.chat.messenger.beans.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class MessageConsumerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumerTest.class);
    private static final String TOPIC = "test.chat.message.consumer";
    @ClassRule
    public static final KafkaEmbedded EMBEDDED_KAFKA = new KafkaEmbedded(1, true, TOPIC);

    @Value("${my.chat.user}")
    private String user;
    @Autowired
    private MessageConsumer consumer;
    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    private KafkaTemplate<String, String> kafkaTemplate;
    private InternalMessageConsumerListener messageConsumerListener;

    @Before
    public void setUp() throws Exception{
        // producer
        final Map<String, Object> producerProps = KafkaTestUtils.senderProps(EMBEDDED_KAFKA.getBrokersAsString());
        final ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        this.kafkaTemplate = new KafkaTemplate<>(producerFactory);
        this.kafkaTemplate.setDefaultTopic(TOPIC);

        this.messageConsumerListener = new InternalMessageConsumerListener();
        this.consumer.setNewMessageReceivedListener(this.messageConsumerListener);

        // wait until the partitions are assigned
        for ( final MessageListenerContainer container : this.kafkaListenerEndpointRegistry.getListenerContainers() ) {
            ContainerTestUtils.waitForAssignment(container, EMBEDDED_KAFKA.getPartitionsPerTopic());
        }
    }

    @After
    public void tearDown(){
        this.consumer.setNewMessageReceivedListener(null);
    }

    private static Message createMessage(String sender, String messageText, long creationTimestamp){
        final Message message = new Message();
        message.setSender(sender);
        message.setMessageText(messageText);
        message.setCreationTimestamp(creationTimestamp);
        return message;
    }

    private static void waitForMessageReceived(final InternalMessageConsumerListener listener) throws InterruptedException {
        while (listener.getMessage() == null){
            Thread.sleep(100L);
        }
    }

    @Test(timeout = 10*1000L)
    public void testReceiveMessage() throws Exception{
        // Arrange
        final Message sendMessage = createMessage(this.user, "Hello Test!", 123L);
        final String sendMessageJson = new ObjectMapper().writeValueAsString(sendMessage);
        // Act
        this.kafkaTemplate.sendDefault(sendMessageJson);
        LOGGER.info("test-sender sent message='{}'", sendMessageJson);
        waitForMessageReceived(this.messageConsumerListener);
        // Assert
        final Message receivedMessage = this.messageConsumerListener.getMessage();
        assertNotNull(receivedMessage);
        assertEquals(sendMessage.getSender(), receivedMessage.getSender());
        assertEquals(sendMessage.getCreationTimestamp(), receivedMessage.getCreationTimestamp());
        assertEquals(sendMessage.getMessageText(), receivedMessage.getMessageText());
        assertEquals(sendMessage.getReceivers(), receivedMessage.getReceivers());
    }

    private static class InternalMessageConsumerListener implements MessageConsumerListener{

        private Message message = null;

        @Override
        public void newMessageReceived(Message newMessage) {
            this.message = newMessage;
        }

        public Message getMessage() {
            return message;
        }
    }
}
