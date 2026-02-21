package chemlab_system.model;

import java.sql.Timestamp;

public class BorrowRequest {
    private int requestId;
    private String groupName;
    private String apparatusName;
    private int qty;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    
    public String getApparatusName() { return apparatusName; }
    public void setApparatusName(String apparatusName) { this.apparatusName = apparatusName; }
    
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
