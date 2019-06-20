package valkyrie.server.ui.listview.employee;

import javafx.beans.property.*;

public class EmployeeStatus {
    private StringProperty name = new SimpleStringProperty();
    private IntegerProperty currentJobNumber = new SimpleIntegerProperty();
    private BooleanProperty clockedIn = new SimpleBooleanProperty();

    public EmployeeStatus(String name) {
        this.name.set(name);
        this.currentJobNumber.set(-1);
        this.clockedIn.set(false);
    }

    public EmployeeStatus(String name, boolean isClockedIn) {
        this.name.set(name);
        this.clockedIn.set(isClockedIn);
    }

    public EmployeeStatus(String name, boolean isClockedIn, int currentJobNumber) {
        this.name.set(name);
        this.currentJobNumber.set(currentJobNumber);
        this.clockedIn.set(isClockedIn);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public int getCurrentJobNumber() {
        return currentJobNumber.get();
    }

    public IntegerProperty currentJobNumberProperty() {
        return currentJobNumber;
    }

    public void setCurrentJobNumber(int currentJobNumber) {
        this.currentJobNumber.set(currentJobNumber);
    }

    public boolean isClockedIn() {
        return clockedIn.get();
    }

    public BooleanProperty clockedInProperty() {
        return clockedIn;
    }

    public void setClockedIn(boolean clockedIn) {
        this.clockedIn.set(clockedIn);
    }
}
