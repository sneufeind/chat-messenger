package my.chat.messenger.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.chat.messenger.beans.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class MessageProducerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProducerTest.class);
    private static final String TOPIC = "test.chat.message.producer";
    @ClassRule
    public static final KafkaEmbedded EMBEDDED_KAFKA = new KafkaEmbedded(1, true, TOPIC);

    @Value("${kafka.consumer.group-id}")
    private String groupId;
    @Value("${my.chat.user}")
    private String user;
    @Autowired
    private MessageProducer producer;
    private KafkaMessageListenerContainer<String, String> container;
    private BlockingQueue<ConsumerRecord<String, String>> records;

    @Before
    public void setUp() throws Exception{
        // consumer
        final Map<String,Object> consumerProps = KafkaTestUtils.consumerProps(groupId, "false", EMBEDDED_KAFKA);
        final DefaultKafkaConsumerFactory<String,String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);

        // create a thread safe queue to store the received message
        this.records = new LinkedBlockingQueue<>();

        // message listener container
        final ContainerProperties containerProps = new ContainerProperties(TOPIC);
        this.container = new KafkaMessageListenerContainer<>(consumerFactory, containerProps);
        this.container.setupMessageListener(new MessageListener<String,String>() {
            @Override
            public void onMessage(ConsumerRecord<String, String> record) {
                LOGGER.info("test-listener received message='{}'", record.toString());
                records.add(record);
            }
        });
        this.container.start();

        // wait until the container has the required number of assigned partitions
        ContainerTestUtils.waitForAssignment(this.container, EMBEDDED_KAFKA.getPartitionsPerTopic());
    }

    @After
    public void tearDown(){
        this.container.stop();
    }


    private static Message createMessage(String sender, String messageText, long creationTimestamp){
        final Message message = new Message();
        message.setSender(sender);
        message.setMessageText(messageText);
        message.setCreationTimestamp(creationTimestamp);
        return message;
    }

    @Test
    public void testSendMessage() throws Exception {
        // Arrange
        final Message sendMessage = createMessage(this.user, "Hello Test!", 123L);
        // Act
        this.producer.send(sendMessage);
        final ConsumerRecord<String, String> receivedRecord = records.poll(10, TimeUnit.SECONDS);
        // Assert
        assertNotNull(receivedRecord);
        assertNotNull(receivedRecord.value());
        final Message receivedMessage = new ObjectMapper().readValue(receivedRecord.value(), Message.class);
        assertEquals(sendMessage.getSender(), receivedMessage.getSender());
        assertEquals(sendMessage.getCreationTimestamp(), receivedMessage.getCreationTimestamp());
        assertEquals(sendMessage.getMessageText(), receivedMessage.getMessageText());
        assertEquals(sendMessage.getReceivers(), receivedMessage.getReceivers());
    }
}
