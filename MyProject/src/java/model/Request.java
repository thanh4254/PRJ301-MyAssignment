/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.time.LocalDate; import java.time.LocalDateTime;
/**
 *
 * @author Admin
 */
public class Request {
      private int id; private String title; private LocalDate from; private LocalDate to; private String reason;
    private int createdBy; private RequestStatus status; private Integer processedBy; private String processedNote;
    private LocalDateTime createdAt; private LocalDateTime updatedAt;
    public Request(){}
    public Request(int id,String title,LocalDate from,LocalDate to,String reason,int createdBy,
                   RequestStatus status,Integer processedBy,String processedNote,
                   LocalDateTime createdAt,LocalDateTime updatedAt){
        this.id=id; this.title=title; this.from=from; this.to=to; this.reason=reason;
        this.createdBy=createdBy; this.status=status; this.processedBy=processedBy; this.processedNote=processedNote;
        this.createdAt=createdAt; this.updatedAt=updatedAt;
    }
    public int getId(){return id;} public void setId(int id){this.id=id;}
    public String getTitle(){return title;} public void setTitle(String title){this.title=title;}
    public LocalDate getFrom(){return from;} public void setFrom(LocalDate from){this.from=from;}
    public LocalDate getTo(){return to;} public void setTo(LocalDate to){this.to=to;}
    public String getReason(){return reason;} public void setReason(String reason){this.reason=reason;}
    public int getCreatedBy(){return createdBy;} public void setCreatedBy(int createdBy){this.createdBy=createdBy;}
    public RequestStatus getStatus(){return status;} public void setStatus(RequestStatus status){this.status=status;}
    public Integer getProcessedBy(){return processedBy;} public void setProcessedBy(Integer processedBy){this.processedBy=processedBy;}
    public String getProcessedNote(){return processedNote;} public void setProcessedNote(String processedNote){this.processedNote=processedNote;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime updatedAt){this.updatedAt=updatedAt;}

}
