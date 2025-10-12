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
public class Role {
        private int id; private String code; private String name; private Set<Feature> features;
    public Role() {}
    public Role(int id, String code, String name){this.id=id; this.code=code; this.name=name;}
    public int getId(){return id;} public void setId(int id){this.id=id;}
    public String getCode(){return code;} public void setCode(String code){this.code=code;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public Set<Feature> getFeatures(){return features;} public void setFeatures(Set<Feature> features){this.features=features;}

}
