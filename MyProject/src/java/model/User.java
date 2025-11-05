/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
  private Integer id;
private String username;
private String passwordHash;
private String fullName;
private String email;
private Integer departmentId;
private Integer managerUserId;
private boolean active;
// trong User.java
private java.util.List<Role> roles = new java.util.ArrayList<>();
private int failedLoginCount;
private java.time.LocalDateTime lockUntil;
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

 public java.util.List<Role> getRoles() { return roles; }
public void setRoles(java.util.List<Role> roles) { this.roles = roles; }

public int getFailedLoginCount() { return failedLoginCount; }
public void setFailedLoginCount(int n) { this.failedLoginCount = n; }

public java.time.LocalDateTime getLockUntil() { return lockUntil; }
public void setLockUntil(java.time.LocalDateTime t) { this.lockUntil = t; }
}
