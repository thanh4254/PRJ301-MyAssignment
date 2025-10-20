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

  /** Quy ước: role "Head" hoặc có feature REQ_HEAD là trưởng phòng */
  public static boolean isDepartmentHead(User u) {
    if (u == null || u.getRoles() == null) return false;
    if (hasFeatureCode(u, "REQ_HEAD")) return true;
    for (Role r : u.getRoles()) {
      if ("Head".equalsIgnoreCase(r.getName())) return true;
    }
    return false;
  }

  /** me có quyền xử lý đơn do creatorId tạo không? */
  public static boolean canProcess(User me, int creatorId,
                                   UserDAO userDAO, EmployeeDAO empDAO) throws Exception {
    if (me == null) return false;
    if (me.getId() == creatorId) return false; // không tự duyệt

    // Head: cùng phòng là được
    if (isDepartmentHead(me)) {
      User creator = userDAO.findById(creatorId);
      return creator != null && creator.getDepartmentId() == me.getDepartmentId();
    }

    // Manager thường: nằm trong toàn bộ cây cấp dưới
    return empDAO.findAllReportUserIds(me.getId()).contains(creatorId);
  }
}