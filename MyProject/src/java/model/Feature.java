/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.io.Serializable;
import java.util.Objects;

public class Feature implements Serializable {
    private Integer id;        // FeatureID
    private String code;       // Code  (REQ_MY, REQ_CREATE, REQ_MGR, REQ_APPROVE, AGD)
    private String name;       // Name
    private String pathPattern;// PathPattern  (/requestlistmyservlet1, ...)

    public Feature() {}
    public Feature(Integer id, String code, String name, String pathPattern) {
        this.id = id; this.code = code; this.name = name; this.pathPattern = pathPattern;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPathPattern() { return pathPattern; }
    public void setPathPattern(String pathPattern) { this.pathPattern = pathPattern; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Feature)) return false;
        Feature feature = (Feature) o;
        return Objects.equals(id, feature.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }

    @Override public String toString() {
        return "Feature{id=" + id + ", code='" + code + "', name='" + name + "', pathPattern='" + pathPattern + "'}";
    }
}