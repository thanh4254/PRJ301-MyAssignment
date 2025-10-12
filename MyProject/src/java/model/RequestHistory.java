/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.time.LocalDateTime;
/**
 *
 * @author Admin
 */
public class RequestHistory {
     private int id; private int requestId; private int actionBy;
    private String oldStatus; private String newStatus; private String note; private LocalDateTime actionAt;
    public RequestHistory(){}
    public RequestHistory(int id,int requestId,int actionBy,String oldStatus,String newStatus,String note,LocalDateTime actionAt){
        this.id=id; this.requestId=requestId; this.actionBy=actionBy; this.oldStatus=oldStatus; this.newStatus=newStatus;
        this.note=note; this.actionAt=actionAt;
    }
    public int getId(){return id;} public void setId(int id){this.id=id;}
    public int getRequestId(){return requestId;} public void setRequestId(int requestId){this.requestId=requestId;}
    public int getActionBy(){return actionBy;} public void setActionBy(int actionBy){this.actionBy=actionBy;}
    public String getOldStatus(){return oldStatus;} public void setOldStatus(String oldStatus){this.oldStatus=oldStatus;}
    public String getNewStatus(){return newStatus;} public void setNewStatus(String newStatus){this.newStatus=newStatus;}
    public String getNote(){return note;} public void setNote(String note){this.note=note;}
    public LocalDateTime getActionAt(){return actionAt;} public void setActionAt(LocalDateTime actionAt){this.actionAt=actionAt;}

}
