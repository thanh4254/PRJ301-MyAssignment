package dal;

import model.Feature;
import model.Role;
import model.User;

import java.util.Set;

/** Tiện ích kiểm quyền/phân cấp duyệt đơn. */
public class PermissionUtil {

  /** Nạp đầy đủ roles + features vào user nếu còn thiếu (dùng chung mọi nơi). */
  public static void ensureFeatures(User me, UserDAO userDAO) {
    if (me == null) return;
    try {
      boolean needReload = (me.getRoles() == null || me.getRoles().isEmpty());
      if (!needReload) {
        for (Role r : me.getRoles()) {
          Set<Feature> fs = r.getFeatures();
          if (fs == null || fs.isEmpty()) { needReload = true; break; }
        }
      }
      if (needReload) {
        User full = userDAO.findById(me.getId()); // đã nạp đủ roles + features
        if (full != null) {
          me.setRoles(full.getRoles());
          me.setDepartmentId(full.getDepartmentId());
          me.setManagerUserId(full.getManagerUserId());
        }
      }
    } catch (Exception ignore) {}
  }

  /** Có sở hữu feature code này không? (đã gọi ensureFeatures trước khi cần chắc ăn) */
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

  /**
   * Là Trưởng phòng? (chuẩn) – cần UserDAO để kiểm tra DB:
   *  - Có feature REQ_HEAD  **hoặc**
   *  - Là manager thực sự của Department trong bảng Department.
   */
  public static boolean isDepartmentHead(User u, UserDAO userDAO) {
    if (u == null) return false;
    if (hasFeatureCode(u, "REQ_HEAD")) return true;
    try {
      Integer depId = u.getDepartmentId();
      return (depId != null) && userDAO.isDepartmentHead(u.getId(), depId);
    } catch (Exception e) {
      return false;
    }
  }

  /** Overload dự phòng (giữ tương thích cũ) – chỉ kiểm tra role name “Head” hoặc feature REQ_HEAD. */
  public static boolean isDepartmentHead(User u) {
    if (u == null) return false;
    if (hasFeatureCode(u, "REQ_HEAD")) return true;
    if (u.getRoles() != null) {
      for (Role r : u.getRoles()) {
        if ("Head".equalsIgnoreCase(r.getName())) return true;
      }
    }
    return false;
  }

  /** me có quyền xử lý đơn do creatorId tạo không? */
  public static boolean canProcess(User me, int creatorId,
                                   UserDAO userDAO, EmployeeDAO empDAO) throws Exception {
    if (me == null) return false;
    if (me.getId() == creatorId) return false; // không tự duyệt

    // Trưởng phòng: được duyệt người cùng phòng
    if (isDepartmentHead(me, userDAO)) {
      User creator = userDAO.findById(creatorId);
      return creator != null && creator.getDepartmentId() != null
          && creator.getDepartmentId().equals(me.getDepartmentId());
    }

    // Quản lý thông thường: duyệt bất kỳ ai trong cây cấp dưới
    return empDAO.findAllReportUserIds(me.getId()).contains(creatorId);
  }
}
