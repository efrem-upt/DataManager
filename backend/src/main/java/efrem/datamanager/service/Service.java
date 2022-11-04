package efrem.datamanager.service;

import javax.persistence.*;

@Entity
@Table(name = "service")
public class Service {
    @Id
    @SequenceGenerator( name = "service_sequence",
            sequenceName = "service_sequence",
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "service_sequence")
    private Long id;
    private String domain;
    private String contact_email;
    private boolean suggested = false;
    private String suggestionUserEmail;

    public Service() {
    }

    public Service(Long id, String domain, String contact_email, boolean suggested, String suggestionUserEmail) {
        this.id = id;
        this.domain = domain;
        this.contact_email = contact_email;
        this.suggested = suggested;
        this.suggestionUserEmail = suggestionUserEmail;
    }

    public Service(String domain, String contact_email, boolean suggested, String suggestionUserEmail) {
        this.domain = domain;
        this.contact_email = contact_email;
        this.suggested = suggested;
        this.suggestionUserEmail = suggestionUserEmail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getContact_email() {
        return contact_email;
    }

    public void setContact_email(String contact_email) {
        this.contact_email = contact_email;
    }

    public boolean isSuggested() {
        return suggested;
    }

    public void setSuggested(boolean suggested) {
        this.suggested = suggested;
    }

    public String getSuggestionUserEmail() {
        return suggestionUserEmail;
    }

    public void setSuggestionUserEmail(String suggestionUserEmail) {
        this.suggestionUserEmail = suggestionUserEmail;
    }

    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", domain='" + domain + '\'' +
                ", contact_email='" + contact_email + '\'' +
                '}';
    }
}
