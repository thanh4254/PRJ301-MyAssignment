/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;
import model.Feature;
import model.Role;
import model.User;
/**
 *
 * @author Admin
 */
public final class PermissionUtil {
    private PermissionUtil(){}

    // kiểm tra theo mã feature (ví dụ "REQ", "AGD")
    public static boolean hasFeatureCode(User u, String code) {
        if (u == null || u.getRoles() == null) return false;
        for (Role r : u.getRoles()) {
            if (r.getFeatures() == null) continue;
            for (Feature f : r.getFeatures())
                if (code.equalsIgnoreCase(f.getCode())) return true;
        }
        return false;
    }
}