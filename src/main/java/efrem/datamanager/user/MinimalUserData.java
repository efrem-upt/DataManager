package efrem.datamanager.user;

public class MinimalUserData {
    private String email;
    private String action;

    public MinimalUserData(String email, String action) {
        this.email = email;
        this.action = action;
    }

    public String getEmail() {
        return email;
    }

    public String getAction() {
        return action;
    }
}
