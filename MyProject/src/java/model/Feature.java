/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Admin
 */
public class Feature {
       private int id; private String code; private String name; private String pathPattern;
    public Feature() {}
    public Feature(int id, String code, String name, String pathPattern) {
        this.id=id; this.code=code; this.name=name; this.pathPattern=pathPattern;
    }
    public int getId(){return id;} public void setId(int id){this.id=id;}
    public String getCode(){return code;} public void setCode(String code){this.code=code;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public String getPathPattern(){return pathPattern;}
    public void setPathPattern(String pathPattern){this.pathPattern=pathPattern;}

}
