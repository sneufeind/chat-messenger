package my.chat.messenger.beans;

public class UserOnlineStatusChangedEvent {

    private String user;
    private boolean isOnline;

    public UserOnlineStatusChangedEvent(){
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
