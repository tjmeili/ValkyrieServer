package valkyrie.server.ui.listview.employee;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import server.data.Day;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ObservableDay extends Day {
    private SimpleDateFormat dateFormatter, timeFormatter;
    private StringProperty stringPropertyStartTime, stringPropertyEndTime, stringPropertyDate;
    private BooleanProperty booleanPropertyIsActive;

    public ObservableDay(Day day) {
        super(day.getDate());
        dateFormatter = new SimpleDateFormat("EEE, M/d/yy");
        timeFormatter = new SimpleDateFormat("h:mm a");
        stringPropertyDate = new SimpleStringProperty(dateFormatter.format(day.getDate()));
        stringPropertyStartTime = new SimpleStringProperty(timeFormatter.format(day.getStartTime()));
        stringPropertyEndTime = new SimpleStringProperty(timeFormatter.format(day.getEndTime()));
        booleanPropertyIsActive = new SimpleBooleanProperty(day.isActive());
    }

    public void setDay(Day day){
        stringPropertyDate.setValue(dateFormatter.format(day.getDate()));
        stringPropertyStartTime.setValue(timeFormatter.format(day.getStartTime()));
        stringPropertyEndTime.setValue(timeFormatter.format(day.getEndTime()));
        booleanPropertyIsActive.setValue(day.isActive());
    }

    public String getStringPropertyStartTime() {
        return stringPropertyStartTime.get();
    }

    public StringProperty stringPropertyStartTimeProperty() {
        return stringPropertyStartTime;
    }

    public void setStringPropertyStartTime(String stringPropertyStartTime) {
        this.stringPropertyStartTime.set(stringPropertyStartTime);
    }

    public String getStringPropertyEndTime() {
        return stringPropertyEndTime.get();
    }

    public StringProperty stringPropertyEndTimeProperty() {
        return stringPropertyEndTime;
    }

    public void setStringPropertyEndTime(String stringPropertyEndTime) {
        this.stringPropertyEndTime.set(stringPropertyEndTime);
    }

    public String getStringPropertyDate() {
        return stringPropertyDate.get();
    }

    public StringProperty stringPropertyDateProperty() {
        return stringPropertyDate;
    }

    public void setStringPropertyDate(String stringPropertyDate) {
        this.stringPropertyDate.set(stringPropertyDate);
    }

    public boolean isBooleanPropertyIsActive() {
        return booleanPropertyIsActive.get();
    }

    public BooleanProperty booleanPropertyIsActiveProperty() {
        return booleanPropertyIsActive;
    }

    public void setBooleanPropertyIsActive(boolean booleanPropertyIsActive) {
        this.booleanPropertyIsActive.set(booleanPropertyIsActive);
    }
}

