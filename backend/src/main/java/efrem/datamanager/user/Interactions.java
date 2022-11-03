package efrem.datamanager.user;

class Interactions {
    private final String name;
    private final String takenAction;

    public Interactions(String name, String takenAction) {
        this.name = name;
        this.takenAction = takenAction;
    }

    public String getName() {
        return name;
    }

    public String getTakenAction() {
        return takenAction;
    }
}