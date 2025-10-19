/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;
import model.Feature;
import model.Role;
import model.User;
import java.util.Set;

public class PermissionUtil {

    public static boolean hasFeatureCode(User u, String code) {
        if (u == null || u.getRoles() == null) return false;
        for (Role r : u.getRoles()) {
            Set<Feature> fs = r.getFeatures();
            if (fs == null) continue;
            for (Feature f : fs) {
                if (code.equalsIgnoreCase(f.getCode())) return true;
            }
        }
        return false;
    }

    /** me có quyền xử lý đơn do creatorId tạo không? */
    public static boolean canProcess(User me, int creatorId, UserDAO userDAO) throws Exception {
        if (me == null) return false;
        if (me.getId() == creatorId) return false; // không tự duyệt đơn của mình

        // 1) Quản lý trực tiếp của creator?
        var directSubs = userDAO.findSubordinateIds(me.getId());
        if (directSubs.contains(creatorId)) return true;

        // 2) Head phòng của creator?
        var creator = userDAO.findById(creatorId);
        return creator != null && userDAO.isDepartmentHead(me.getId(), creator.getDepartmentId());
    }
}