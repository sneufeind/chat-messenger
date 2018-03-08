package my.chat.messenger.model.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import my.chat.messenger.beans.Message;
import my.chat.messenger.beans.UserOnlineStatusChangedEvent;
import my.chat.messenger.kafka.MessageConsumer;
import my.chat.messenger.kafka.MessageProducer;
import my.chat.messenger.kafka.UserOnlineStatusConsumer;
import my.chat.messenger.kafka.UserOnlineStatusProducer;
import my.chat.messenger.model.ChatMessengerViewModel;
import my.chat.messenger.model.ModelStateChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.stream.Stream;

@SpringComponent
@UIScope
public class ChatMessengerViewModelImpl implements ChatMessengerViewModel{

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatMessengerViewModelImpl.class);

    private final Collection<ModelStateChangeListener> stateChangelisteners = new ArrayList<>();
    private final Collection<String> onlineUsers = new LinkedList<>();
    private final Collection<Message> messages = new LinkedList<>();
    private final MessageProducer messageProducer;
    private final MessageConsumer messageConsumer;
    private final UserOnlineStatusProducer userOnlineStatusProducer;
    private final UserOnlineStatusConsumer userOnlineStatusConsumer;
    private final Thread loadMessagesThread;

    @Value("${my.chat.user}")
    private String user;

    @Autowired
    public ChatMessengerViewModelImpl(
            final MessageConsumer messageConsumer,
            final MessageProducer messageProducer,
            final UserOnlineStatusConsumer UserOnlineStatusConsumer,
            final UserOnlineStatusProducer userOnlineStatusProducer
    ){
        this.messageConsumer = messageConsumer;
        this.messageProducer = messageProducer;
        this.userOnlineStatusConsumer = UserOnlineStatusConsumer;
        this.userOnlineStatusProducer = userOnlineStatusProducer;

        this.loadMessagesThread = createLoadMessageWorker();
    }

    @Override
    public void addModelStateChangeListener(ModelStateChangeListener listener) {
        if( !this.stateChangelisteners.contains(listener) ){
            this.stateChangelisteners.add(listener);
        }
    }

    @Override
    public void removeModelStateChangeListener(ModelStateChangeListener listener) {
        if( this.stateChangelisteners.contains(listener) ){
            this.stateChangelisteners.remove(listener);
        }
    }

    private Thread createLoadMessageWorker(){
        return new Thread(){

            private final String[] dummyUsers = {"Peter", "Mary", "Anna", "Frank", "Bert", "Celine"};

            @Override
            public void run() {
                for (String user : this.dummyUsers) {
                    try {
                        ChatMessengerViewModelImpl.this.userOnlineStatusProducer.sendUserIsOnline(user);
                    } catch (JsonProcessingException e) {
                        LOGGER.error("Could not serialize online status!", e);
                    }
                }

                while (!isInterrupted()){
                    sendMessage(ChatMessengerViewModelImpl.this.user + "Bot", "Hello World!", null);
                    try {
                        Thread.sleep(5*1000L);
                    } catch (InterruptedException e) {
                        LOGGER.error("Thread has been interrupted!", e);
                        interrupt();
                    } catch (IllegalStateException e){
                        LOGGER.error("Concurrent Exception!", e);
                    }

                    for (final String dummyUser : dummyUsers) {
                        try {
                            final double random = Math.random();
                            if(random <= 0.25) {
                                ChatMessengerViewModelImpl.this.userOnlineStatusProducer.sendUserIsOffline(dummyUser);
                            } else if(random >= 0.5) {
                                ChatMessengerViewModelImpl.this.userOnlineStatusProducer.sendUserIsOnline(dummyUser);
                            }
                        } catch (JsonProcessingException e) {
                            LOGGER.error("Could not serialize online status!", e);
                        }
                    }
                }
            }
        };
    }

    private synchronized void addMessage(final Message newMessage){
        this.messages.add(newMessage);
        LOGGER.info("New message has been received: {}", newMessage.toString());
        update();
    }

    private synchronized void updateOnlineStatus(){
        update();
    }

    private void update(){
        this.stateChangelisteners.stream().forEach(l -> l.onModelStateChanged());
    }

    @PostConstruct
    private void startProcessing(){
        this.messageConsumer.setNewMessageReceivedListener(m -> addMessage(m));
        this.userOnlineStatusConsumer.setUserOnlineStatusChangedListener(this::updateOnlineStatus);
        this.loadMessagesThread.start();
    }

    @PreDestroy
    private void stopProcessing(){
        this.messageConsumer.setNewMessageReceivedListener(null);
        this.userOnlineStatusConsumer.setUserOnlineStatusChangedListener(null);
        this.loadMessagesThread.stop();
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public Collection<Message> getMessages() {
        return Collections.unmodifiableCollection(this.messages);
    }

    @Override
    public void sendMessage(final String messageText, final Collection<String> receivers) {
        sendMessage(getUser(), messageText, receivers);
    }

    private void sendMessage(final String sender, final String messageText, final Collection<String> receivers) {
        final Message message = new Message();
        message.setSender(sender);
        message.setCreationTimestamp(System.currentTimeMillis());
        message.setMessageText(messageText);
        message.setReceivers(receivers);
        try {
            this.messageProducer.send(message);
        } catch (JsonProcessingException e) {
            LOGGER.error("Could not serialize message!", e);
        }
    }

    @Override
    public void sendUserIsOnline() {
        try {
            this.userOnlineStatusProducer.sendUserIsOnline(this.user);
        } catch (JsonProcessingException e) {
            LOGGER.error("Could not serialize online status!", e);
        }
    }

    @Override
    public void sendUserIsOffline() {
        try {
            this.userOnlineStatusProducer.sendUserIsOffline(this.user);
        } catch (JsonProcessingException e) {
            LOGGER.error("Could not serialize online status!", e);
        }
    }

    @Override
    public Stream<String> onlineUsers() {
        return this.userOnlineStatusConsumer.onlineUsers() //
                .filter(u -> !this.user.equals(u));
    }
}
