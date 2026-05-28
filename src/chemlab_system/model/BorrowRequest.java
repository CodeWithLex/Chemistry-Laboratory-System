package chemlab_system.model;

import java.sql.Timestamp;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class BorrowRequest {
    private int requestId;
    private int groupId;
    private String groupName;
    private int apparatusId;
    private String apparatusName;
    private int qty;
    private String status;
    private String labActivity;
    private Timestamp createdAt;
    private Timestamp approvedAt;
    private Timestamp updatedAt;
    private int apparatusRemaining;
    private int brokenQty;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final javafx.beans.property.IntegerProperty brokenQtyProp = new javafx.beans.property.SimpleIntegerProperty(
            0);

    public BorrowRequest() {
    }

    public BorrowRequest(int requestId, int groupId, String groupName, int apparatusId, String apparatusName, int qty,
            String status, Timestamp updatedAt) {
        this.requestId = requestId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.apparatusId = apparatusId;
        this.apparatusName = apparatusName;
        this.qty = qty;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getApparatusName() {
        return apparatusName;
    }

    public void setApparatusName(String apparatusName) {
        this.apparatusName = apparatusName;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLabActivity() {
        return labActivity;
    }

    public void setLabActivity(String labActivity) {
        this.labActivity = labActivity;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Timestamp approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public int getApparatusId() {
        return apparatusId;
    }

    public void setApparatusId(int apparatusId) {
        this.apparatusId = apparatusId;
    }

    public int getApparatusRemaining() {
        return apparatusRemaining;
    }

    public void setApparatusRemaining(int apparatusRemaining) {
        this.apparatusRemaining = apparatusRemaining;
    }

    public int getBrokenQty() {
        return brokenQtyProp.get();
    }

    public void setBrokenQty(int brokenQty) {
        this.brokenQty = brokenQty;
        this.brokenQtyProp.set(brokenQty);
    }

    public javafx.beans.property.IntegerProperty brokenQtyProperty() {
        return brokenQtyProp;
    }
}
