package efrem.datamanager.dashboard;

public class GoogleStatus {
    public String image;
    public String message;

    public GoogleStatus(String image, String message) {
        this.image = image;
        this.message = message;
    }

    public String getImage() {
        return image;
    }

    public String getMessage() {
        return message;
    }
}
