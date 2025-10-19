/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.io.Serializable;

public class Department implements Serializable {
    private Integer id;            // DepartmentID
    private String name;           // Name
    private Integer managerUserId; // ManagerUserID (UserID của Head)

    public Department() {}
    public Department(Integer id, String name, Integer managerUserId) {
        this.id = id;
        this.name = name;
        this.managerUserId = managerUserId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getManagerUserId() { return managerUserId; }
    public void setManagerUserId(Integer managerUserId) { this.managerUserId = managerUserId; }

    @Override public String toString() {
        return "Department{id=" + id + ", name='" + name + "', managerUserId=" + managerUserId + "}";
    }
}
