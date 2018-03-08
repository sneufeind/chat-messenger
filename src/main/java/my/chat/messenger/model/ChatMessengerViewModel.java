package my.chat.messenger.model;

import my.chat.messenger.beans.Message;

import java.util.Collection;
import java.util.stream.Stream;

public interface ChatMessengerViewModel extends ViewModel{

    String getUser();

    Collection<Message> getMessages();

    Stream<String> onlineUsers();

    void sendMessage(String messageText, Collection<String> receivers);

    void sendUserIsOnline();

    void sendUserIsOffline();
}
