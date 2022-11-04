package efrem.datamanager.user;

import java.util.Objects;

public class ResetPasswordRequest {
    private String email;
    private String newPassword;
    private String newPasswordConfirmed;

    public ResetPasswordRequest(String email, String newPassword, String newPasswordConfirmed) {
        this.email = email;
        this.newPassword = newPassword;
        this.newPasswordConfirmed = newPasswordConfirmed;
    }

    public String getEmail() {
        return email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getNewPasswordConfirmed() {
        return newPasswordConfirmed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResetPasswordRequest that = (ResetPasswordRequest) o;
        return Objects.equals(email, that.email) && Objects.equals(newPassword, that.newPassword) && Objects.equals(newPasswordConfirmed, that.newPasswordConfirmed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, newPassword, newPasswordConfirmed);
    }

    @Override
    public String toString() {
        return "ResetPasswordRequest{" +
                "email='" + email + '\'' +
                ", newPassword='" + newPassword + '\'' +
                ", newPasswordConfirmed='" + newPasswordConfirmed + '\'' +
                '}';
    }
}
