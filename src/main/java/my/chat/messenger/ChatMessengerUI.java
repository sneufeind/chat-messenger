package my.chat.messenger;

import com.vaadin.annotations.*;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.SpringViewDisplay;
import com.vaadin.spring.server.SpringVaadinServlet;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import my.chat.messenger.model.ChatMessengerViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;

@Theme(ValoTheme.THEME_NAME)
@Title("Chat-Messenger")
@Push
@SpringUI
@SpringViewDisplay
@Viewport("width=device-width,initial-scale=1.0,user-scalable=no")
public class ChatMessengerUI extends UI implements ViewDisplay {

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = true, ui = ChatMessengerUI.class)
    public class ChatMessengerServlet2 extends SpringVaadinServlet {
        {
            System.out.println("\n\n\n\n\n\n\n\n\nOKAY\n\n\n\n\n\n\n\n\n"); //TODO
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatMessengerUI.class);

    @Autowired
    private ChatMessengerViewModel viewModel;
    private Panel springViewDisplay;

    @Override
    protected void init(VaadinRequest request) {
        // Title & User
        final Label titleLabel = new Label(String.format("Chat-Messenger (User: %s)", viewModel.getUser()));
        titleLabel.addStyleName(ValoTheme.LABEL_COLORED);
        titleLabel.addStyleName(ValoTheme.LABEL_BOLD);
        titleLabel.addStyleName(ValoTheme.LABEL_H1);

        // Navigable Panel
        this.springViewDisplay = new Panel();
        this.springViewDisplay.setSizeFull();

        // Root Panel
        final VerticalLayout root = new VerticalLayout();
        root.setSizeFull();
        setContent(root);
        root.addComponents(titleLabel, this.springViewDisplay);
        root.setExpandRatio(springViewDisplay, 1.0f);

        LOGGER.info("Session-Id: {}", UI.getCurrent().getSession().getSession().getId());
        LOGGER.info("Push-Id: {}", UI.getCurrent().getSession().getPushId());
        showWelcomeNotification(this.viewModel.getUser());

        this.viewModel.sendUserIsOnline();
    }

    @Override
    public void detach() {
        this.viewModel.sendUserIsOffline();
        super.detach();
    }

    private void showWelcomeNotification(final String user){
        final String caption = new StringBuilder()
                .append("Hello ").append(this.viewModel.getUser()).append(",")
                .append("\nwelcome to Chat-Messenger!")
                .toString();
        final String description = "Nice to see you again!!!\n:-)";
        Notification.show(caption, description, Notification.Type.HUMANIZED_MESSAGE);
    }

    @Override
    public void showView(View view) {
        if(view instanceof Component) {
            springViewDisplay.setContent(Component.class.cast(view));
        }else{
            LOGGER.error("Can not show view '{}' because it is not a component.", view);
        }
    }
}
