package efrem.datamanager.registration.email;

public interface EmailSender {
    void send(String to, String email);
    void setSubject(String subject);
}
