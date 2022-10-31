package efrem.datamanager.user;

import com.nimbusds.jose.util.Pair;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.processing.Generated;
import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "account")
public class User implements UserDetails {
    @Id
    @SequenceGenerator( name = "user_sequence",
    sequenceName = "user_sequence",
    allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
    generator = "user_sequence")
    private Long id;
    private String email;
    private String password;
    private boolean associatedGoogle = false;
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<UserRole> userRole;
    private Boolean locked = false;
    private Boolean enabled = false;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "services_used",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "service_name")
    @Column(name = "taken_action")
    private Map<String, Boolean> interactions;

    public User() {
    }

    public User(Long id, String email, String password, Set<UserRole> userRole, Map<String, Boolean> interactions) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.interactions = interactions;
    }

    public User(String email, String password, Set<UserRole> userRole, Map<String, Boolean> interactions) {
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.locked = locked;
        this.enabled = enabled;
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

    public boolean isAssociatedGoogle() {
        return associatedGoogle;
    }

    public void setAssociatedGoogle(boolean associatedGoogle) {
        this.associatedGoogle = associatedGoogle;
    }

    public Map<String, Boolean> getInteractions() {
        return interactions;
    }

    public void setInteractions(Map<String, Boolean> interactions) {
        this.interactions = interactions;
    }

    public void addInteraction(String key, Boolean done) {
        interactions.put(key, done);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<UserRole> getUserRole() {
        return userRole;
    }

    public void setUserRole(Set<UserRole> userRole) {
        this.userRole = userRole;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", interactions=" + interactions +
                '}';
    }

    public boolean hasRole(String roleName) {
        Iterator<UserRole> iterator = this.userRole.iterator();
        while (iterator.hasNext()) {
            UserRole role = iterator.next();
            if (role.name().equals(roleName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> listAuthorities = new ArrayList<SimpleGrantedAuthority>();
        List<UserRole> userRoles = userRole.stream().toList();
        for (int i = 0; i < userRoles.size(); i++) {
            listAuthorities.add(new SimpleGrantedAuthority(userRoles.get(i).name()));
        }
        return listAuthorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
