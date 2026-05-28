package chemlab_system.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ActivityTemplateItem {
    private final IntegerProperty apparatusId;
    private final StringProperty apparatusName;
    private final IntegerProperty fixedQty;

    public ActivityTemplateItem(int apparatusId, String apparatusName, int fixedQty) {
        this.apparatusId = new SimpleIntegerProperty(apparatusId);
        this.apparatusName = new SimpleStringProperty(apparatusName);
        this.fixedQty = new SimpleIntegerProperty(fixedQty);
    }

    public int getApparatusId() {
        return apparatusId.get();
    }

    public IntegerProperty apparatusIdProperty() {
        return apparatusId;
    }

    public String getApparatusName() {
        return apparatusName.get();
    }

    public StringProperty apparatusNameProperty() {
        return apparatusName;
    }

    public int getFixedQty() {
        return fixedQty.get();
    }

    public IntegerProperty fixedQtyProperty() {
        return fixedQty;
    }
}
