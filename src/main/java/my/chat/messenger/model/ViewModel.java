package my.chat.messenger.model;

public interface ViewModel {

    void addModelStateChangeListener(ModelStateChangeListener listener);

    void removeModelStateChangeListener(ModelStateChangeListener listener);
}
