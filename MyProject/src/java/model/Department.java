/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Admin
 */
public class Department {
      private int id;
    private String name;
    private Integer managerUserId;
    public Department() {}
    public Department(int id, String name, Integer managerUserId) {
        this.id=id; this.name=name; this.managerUserId=managerUserId;
    }
    public int getId(){return id;} public void setId(int id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public Integer getManagerUserId(){return managerUserId;}
    public void setManagerUserId(Integer managerUserId){this.managerUserId=managerUserId;}

}
