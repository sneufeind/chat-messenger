package my.chat.messenger.view;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;

import javax.annotation.PostConstruct;

@UIScope
@SpringComponent
public class UIUpdater {

    private final ProgressBar loadingIndicator = new ProgressBar();

    public UIUpdater(){}

    @PostConstruct
    public void init(){
        this.loadingIndicator.setIndeterminate(true);
        this.loadingIndicator.setVisible(false);
    }

    public void update(UI ui){
        this.loadingIndicator.setVisible(true);
        ui.access(() -> {
            this.loadingIndicator.setVisible(false);
        });
    }
}
