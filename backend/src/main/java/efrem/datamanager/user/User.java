package efrem.datamanager.user;

import com.nimbusds.jose.util.Pair;

import javax.annotation.processing.Generated;
import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "account")
public class User {
    @Id
    @SequenceGenerator( name = "user_sequence",
    sequenceName = "user_sequence",
    allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
    generator = "user_sequence")
    private Long id;
    private String email;
    @ElementCollection
    @CollectionTable(name = "services_used",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "service_name")
    @Column(name = "taken_action")
    private Map<String, Boolean> interactions;

    public User() {
    }

    public User(Long id, String email, Map<String, Boolean> interactions) {
        this.id = id;
        this.email = email;
        this.interactions = interactions;
    }

    public User(String email, Map<String, Boolean> interactions) {
        this.email = email;
        this.interactions = interactions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, Boolean> getInteractions() {
        return interactions;
    }

    public void setInteractions(Map<String, Boolean> interactions) {
        this.interactions = interactions;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", interactions=" + interactions +
                '}';
    }
}
