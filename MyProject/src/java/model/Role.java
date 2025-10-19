/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Role implements Serializable {
    private Integer id;     // RoleID
    private String code;    // Code  (DIV_LEADER | TEAM_LEAD | EMP)
    private String name;    // Name

    private Set<Feature> features = new HashSet<>(); // nạp kèm khi cần

    public Role() {}
    public Role(Integer id, String code, String name) {
        this.id = id; this.code = code; this.name = name;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Set<Feature> getFeatures() { return features; }
    public void setFeatures(Set<Feature> features) { this.features = features; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }

    @Override public String toString() {
        return "Role{id=" + id + ", code='" + code + "', name='" + name + "'}";
    }
}

