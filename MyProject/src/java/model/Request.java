/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Request implements Serializable {
    private Integer id;              // RequestID
    private String title;            // Title
    private LocalDate from;          // DateFrom
    private LocalDate to;            // DateTo
    private String reason;           // Reason
    private Integer createdBy;       // CreatedBy (UserID)
    private RequestStatus status;    // Status (VARCHAR trong DB)
    private Integer processedBy;     // ProcessedBy (UserID)
    private String processedNote;    // ProcessedNote
    private LocalDateTime createdAt; // CreatedAt
    private LocalDateTime updatedAt; // UpdatedAt

    public Request() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getFrom() { return from; }
    public void setFrom(LocalDate from) { this.from = from; }

    public LocalDate getTo() { return to; }
    public void setTo(LocalDate to) { this.to = to; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public Integer getProcessedBy() { return processedBy; }
    public void setProcessedBy(Integer processedBy) { this.processedBy = processedBy; }

    public String getProcessedNote() { return processedNote; }
    public void setProcessedNote(String processedNote) { this.processedNote = processedNote; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override public String toString() {
        return "Request{id=" + id + ", title='" + title + "', from=" + from + ", to=" + to +
               ", status=" + status + ", createdBy=" + createdBy + "}";
    }
}

