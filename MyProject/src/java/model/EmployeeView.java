/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Admin
 */
import java.io.Serializable;

public class EmployeeView implements Serializable {
    private Integer id;            // eid (VIEW Employee)
    private String name;           // ename
    private Integer departmentId;  // did
    private Integer supervisorId;  // supervisorid (eid của sếp)

    // Optional: user liên kết (Enrollment) nếu cần map sang User
    private Integer userId;        // uid (Enrollment)

    public EmployeeView() {}

    public EmployeeView(Integer id, String name, Integer departmentId, Integer supervisorId, Integer userId) {
        this.id = id;
        this.name = name;
        this.departmentId = departmentId;
        this.supervisorId = supervisorId;
        this.userId = userId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }

    public Integer getSupervisorId() { return supervisorId; }
    public void setSupervisorId(Integer supervisorId) { this.supervisorId = supervisorId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
}