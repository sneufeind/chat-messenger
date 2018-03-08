package my.chat.messenger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class ChatMessengerApp {

    public static void main(String[] args) {
        SpringApplication.run(ChatMessengerApp.class, args);
    }

}
