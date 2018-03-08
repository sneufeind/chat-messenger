package my.chat.messenger;


import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.spring.server.SpringVaadinServlet;

import javax.servlet.annotation.WebServlet;

@WebServlet(value = "/*", asyncSupported = true)
@VaadinServletConfiguration(productionMode = true, ui = ChatMessengerUI.class)
public class ChatMessengerServlet extends SpringVaadinServlet {
}
