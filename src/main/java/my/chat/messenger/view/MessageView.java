package my.chat.messenger.view;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import my.chat.messenger.beans.Message;
import my.chat.messenger.model.ChatMessengerViewModel;
import my.chat.messenger.model.ModelStateChangeListener;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ViewScope
@SpringView(name = MessageView.VIEW_NAME)
public class MessageView extends HorizontalLayout implements View, ModelStateChangeListener {
    public static final String VIEW_NAME = "";

    private final ChatMessengerViewModel viewModel;
    private final UIUpdater uiUpdater;

    private TextArea receivedMessagesTA;
    private TextArea messageEditorTA;
    private Button sendMessageBtn;
    private Button clearMessageBtn;
    private CheckBoxGroup<String> channelSelectChBGrp;

    @Autowired
    public MessageView(final ChatMessengerViewModel viewModel, final UIUpdater uiUpdater){
        this.viewModel = viewModel;
        this.uiUpdater = uiUpdater;
    }

    @PostConstruct
    void init() {
        // Channel select
        this.channelSelectChBGrp = new CheckBoxGroup<>("Online users");
        this.channelSelectChBGrp.setSizeFull();
        this.channelSelectChBGrp.setWidth(150.f, Unit.PIXELS);
        this.channelSelectChBGrp.addValueChangeListener(e -> updateReceiverText(e.getValue()));

        final Layout channelLayout = new VerticalLayout();
        channelLayout.addComponent(this.channelSelectChBGrp);
        channelLayout.setSizeFull();

        // Received Message Box
        this.receivedMessagesTA = new TextArea("Received Messages");
        this.receivedMessagesTA.setSizeFull();

        // Send Message Box
        this.messageEditorTA = new TextArea("New Message");
        this.messageEditorTA.setSizeFull();
        this.messageEditorTA.setRows(4);
        this.messageEditorTA.setValueChangeMode(ValueChangeMode.EAGER);
        this.messageEditorTA.addValueChangeListener(e -> onMessageEditorTextChanged(e.getValue()));

        this.sendMessageBtn = new Button("Send", VaadinIcons.TWITTER); //alt: PAPERPLANE
        this.sendMessageBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        this.sendMessageBtn.addClickListener(e -> onSendButtonClicked());
        this.sendMessageBtn.setEnabled(false);
        this.clearMessageBtn = new Button("Clear", VaadinIcons.TRASH);
        this.clearMessageBtn.addClickListener(e -> onClickBtnClicked());
        final HorizontalLayout btnLayout = new HorizontalLayout();
        btnLayout.setMargin(false);
        btnLayout.addComponents(this.sendMessageBtn, this.clearMessageBtn);

        final VerticalLayout messagesLayout = new VerticalLayout();
        messagesLayout.setSizeFull();
        messagesLayout.addComponents(this.receivedMessagesTA, this.messageEditorTA, btnLayout);
        messagesLayout.setExpandRatio(this.receivedMessagesTA, 6.f);
        messagesLayout.setExpandRatio(this.messageEditorTA, 2.f);
        messagesLayout.setExpandRatio(btnLayout, 1.f);

        addComponents(channelLayout, messagesLayout);
        setSizeFull();
        setExpandRatio(channelLayout, 1.f);
        setExpandRatio(messagesLayout, 5.f);

        initMessageInput();
        this.viewModel.addModelStateChangeListener(this);
    }

    @PreDestroy
    private void cleanUp(){
        this.viewModel.removeModelStateChangeListener(this);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        updateReceiverText(null);
        update();
    }

    @Override
    public void onModelStateChanged() {
        update();
    }

    private void update(){
        updateOnlineUsers();
        updateReceivedMessages();
        this.uiUpdater.update(getUI());
    }

    private void updateOnlineUsers(){
        final Collection<String> onlineUsers = this.viewModel.onlineUsers().collect(Collectors.toList());
        final Collection<String> newSelectedItems = this.channelSelectChBGrp.getSelectedItems().stream()
                .filter(item -> onlineUsers.contains(item))
                .collect(Collectors.toList());
        this.channelSelectChBGrp.setItems(onlineUsers);
        this.channelSelectChBGrp.select(newSelectedItems.toArray(new String[newSelectedItems.size()]));
    }

    private void updateReceivedMessages(){
        final Stream<String> messagesStream = this.viewModel.getMessages().stream()
                .map(m -> convertReceivedMessage(m));
        final StringBuilder receivedMessagesStringBuilder = new StringBuilder();
        messagesStream.forEach(text -> {
            receivedMessagesStringBuilder.insert(0,text+"\n");
        });
        this.receivedMessagesTA.setValue(receivedMessagesStringBuilder.toString());
    }

    private void initMessageInput(){
        this.messageEditorTA.clear();
    }

    private void updateReceiverText(Collection<String> selections){
        final StringBuilder sb = new StringBuilder("New Message");
        if(selections == null || selections.isEmpty()){
            sb.append(" @All");
        }else{
            selections.stream().forEach(sel -> sb.append(" @").append(sel));
        }
        this.messageEditorTA.setCaption(sb.toString());
    }

    private void onMessageEditorTextChanged(String newValue) {
        // enables/disables send button
        this.sendMessageBtn.setEnabled(newValue != null && !newValue.trim().isEmpty());
    }

    private void onSendButtonClicked(){
        this.viewModel.sendMessage(this.messageEditorTA.getValue(), this.channelSelectChBGrp.getSelectedItems());
        initMessageInput();
    }

    private void onClickBtnClicked(){
        initMessageInput();
    }

    private String convertReceivedMessage(final Message newMessage){
        final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy - hh:mm:ss");
        final String newMessageText = new StringBuilder()
                .append(dateFormat.format(new Date(newMessage.getCreationTimestamp())))
                .append("\t'").append(newMessage.getSender()).append("':\n\t")
                .append(newMessage.getMessageText())
                .toString();
        return newMessageText;
    }
}
