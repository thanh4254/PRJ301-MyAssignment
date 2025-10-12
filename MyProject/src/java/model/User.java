/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.util.Set;
/**
 *
 * @author Admin
 */
public class User {
      private int id; private String username; private String passwordHash;
    private String fullName; private String email;
    private int departmentId; private Integer managerUserId;
    private boolean isActive; private Set<Role> roles;
    public User() {}
    public User(int id,String username,String passwordHash,String fullName,String email,
                int departmentId,Integer managerUserId,boolean isActive){
        this.id=id; this.username=username; this.passwordHash=passwordHash; this.fullName=fullName;
        this.email=email; this.departmentId=departmentId; this.managerUserId=managerUserId; this.isActive=isActive;
    }
    public int getId(){return id;} public void setId(int id){this.id=id;}
    public String getUsername(){return username;} public void setUsername(String username){this.username=username;}
    public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String passwordHash){this.passwordHash=passwordHash;}
    public String getFullName(){return fullName;} public void setFullName(String fullName){this.fullName=fullName;}
    public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
    public int getDepartmentId(){return departmentId;} public void setDepartmentId(int departmentId){this.departmentId=departmentId;}
    public Integer getManagerUserId(){return managerUserId;} public void setManagerUserId(Integer managerUserId){this.managerUserId=managerUserId;}
    public boolean isActive(){return isActive;} public void setActive(boolean active){isActive=active;}
    public Set<Role> getRoles(){return roles;} public void setRoles(Set<Role> roles){this.roles=roles;}

}
