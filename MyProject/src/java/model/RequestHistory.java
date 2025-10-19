/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.io.Serializable;
import java.time.LocalDateTime;

public class RequestHistory implements Serializable {
    private Integer id;           // HistoryID
    private Integer requestId;    // RequestID
    private Integer actionBy;     // ActionBy (UserID)
    private String oldStatus;     // OldStatus (VARCHAR)
    private String newStatus;     // NewStatus (VARCHAR)
    private String note;          // Note
    private LocalDateTime actionAt; // ActionAt

    public RequestHistory() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }

    public Integer getActionBy() { return actionBy; }
    public void setActionBy(Integer actionBy) { this.actionBy = actionBy; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getActionAt() { return actionAt; }
    public void setActionAt(LocalDateTime actionAt) { this.actionAt = actionAt; }

    @Override public String toString() {
        return "RequestHistory{id=" + id + ", requestId=" + requestId + ", actionBy=" + actionBy +
               ", " + oldStatus + " -> " + newStatus + ", at=" + actionAt + "}";
    }
}
