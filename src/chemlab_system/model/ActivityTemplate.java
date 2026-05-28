package chemlab_system.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ActivityTemplate {
    private final StringProperty templateId;
    private final StringProperty activityName;
    private final StringProperty description;
    private final IntegerProperty itemCount;

    public ActivityTemplate(String templateId, String activityName, String description, int itemCount) {
        this.templateId = new SimpleStringProperty(templateId);
        this.activityName = new SimpleStringProperty(activityName);
        this.description = new SimpleStringProperty(description);
        this.itemCount = new SimpleIntegerProperty(itemCount);
    }

    public String getTemplateId() {
        return templateId.get();
    }

    public StringProperty templateIdProperty() {
        return templateId;
    }

    public String getActivityName() {
        return activityName.get();
    }

    public StringProperty activityNameProperty() {
        return activityName;
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public int getItemCount() {
        return itemCount.get();
    }

    public IntegerProperty itemCountProperty() {
        return itemCount;
    }
}
