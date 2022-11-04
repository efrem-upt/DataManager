package efrem.datamanager.user;

class Interactions {
    private final String name;
    private final boolean takenAction;
    private final boolean suggestEnabled;
    private final String currentContactEmail;

    public Interactions(String name, boolean takenAction, boolean suggestEnabled, String currentContactEmail) {
        this.name = name;
        this.takenAction = takenAction;
        this.suggestEnabled = suggestEnabled;
        this.currentContactEmail = currentContactEmail;
    }

    public String getName() {
        return name;
    }

    public boolean getTakenAction() {
        return takenAction;
    }

    public String getCurrentContactEmail() {
        return currentContactEmail;
    }

    public boolean getSuggestEnabled() {
        return suggestEnabled;
    }
}