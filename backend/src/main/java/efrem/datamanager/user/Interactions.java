package efrem.datamanager.user;

class Interactions {
    private final String name;
    private final boolean takenAction;
    private final boolean suggestEnabled;

    public Interactions(String name, boolean takenAction, boolean suggestEnabled) {
        this.name = name;
        this.takenAction = takenAction;
        this.suggestEnabled = suggestEnabled;
    }

    public String getName() {
        return name;
    }

    public boolean getTakenAction() {
        return takenAction;
    }

    public boolean getSuggestEnabled() {
        return suggestEnabled;
    }
}