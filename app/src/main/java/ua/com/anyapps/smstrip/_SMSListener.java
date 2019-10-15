package ua.com.anyapps.smstrip;

public interface _SMSListener {
    public void messageReceived(String messageText, String phoneNumber, Long currentTimeMillis);
}
