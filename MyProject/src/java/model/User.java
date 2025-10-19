/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private Integer id;             // UserID
    private String username;        // Username
    private String passwordHash;    // PasswordHash
    private String fullName;        // FullName
    private String email;           // Email
    private Integer departmentId;   // DepartmentID
    private Integer managerUserId;  // ManagerUserID
    private boolean active;         // IsActive

    // ĐÃ ĐỔI: dùng List thay vì Set
    private List<Role> roles = new ArrayList<>();

    public User() {}

    public User(Integer id, String username, String passwordHash, String fullName,
                String email, Integer departmentId, Integer managerUserId, boolean active) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.departmentId = departmentId;
        this.managerUserId = managerUserId;
        this.active = active;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }

    public Integer getManagerUserId() { return managerUserId; }
    public void setManagerUserId(Integer managerUserId) { this.managerUserId = managerUserId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; }
}
