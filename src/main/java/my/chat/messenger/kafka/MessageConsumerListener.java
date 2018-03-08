package my.chat.messenger.kafka;

import my.chat.messenger.beans.Message;

public interface MessageConsumerListener {

    void newMessageReceived(Message newMessage);
}
