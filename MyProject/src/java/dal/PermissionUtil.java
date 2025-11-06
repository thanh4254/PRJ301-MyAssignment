package dal;

import model.Feature;
import model.Role;
import model.User;

import java.util.Set;

/** Tiện ích kiểm quyền/phân cấp duyệt đơn. */
public class PermissionUtil {

  /** Nạp đầy đủ roles + features vào đối tượng user đang nằm trong session nếu chưa có. */
  public static void ensureFeatures(User me, UserDAO userDAO) {
    if (me == null) return;
    try {
      boolean needReload = (me.getRoles() == null || me.getRoles().isEmpty());
      if (!needReload) {
        // roles có nhưng features trống -> cũng reload
        for (Role r : me.getRoles()) {
          Set<Feature> fs = r.getFeatures();
          if (fs == null || fs.isEmpty()) { needReload = true; break; }
        }
      }
      if (needReload) {
        User full = userDAO.findById(me.getId());      // đã nạp đủ roles + features
        if (full != null) {
          me.setRoles(full.getRoles());
          me.setDepartmentId(full.getDepartmentId());
          me.setManagerUserId(full.getManagerUserId());
        }
      }
    } catch (Exception ignore) { /* giữ an toàn, không làm app crash */ }
  }

  /** Có sở hữu feature code này không? */
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

  /** Quy ước: là Head nếu có feature REQ_HEAD hoặc role tên "Head". */
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
    if (isDepartmentHead(me)) {
      User creator = userDAO.findById(creatorId);
      return creator != null && creator.getDepartmentId() == me.getDepartmentId();
    }

    // Quản lý thông thường: duyệt bất kỳ ai trong cây cấp dưới
    return empDAO.findAllReportUserIds(me.getId()).contains(creatorId);
  }
}
