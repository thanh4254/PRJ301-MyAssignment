package dal;

import model.Feature;
import model.Role;
import model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/** UserDAO – tất cả truy vấn liên quan User */
public class UserDAO {

  /* ====================== Helpers ====================== */

  /** Map các cột cơ bản của User (KHÔNG nạp role/feature) */
  private User mapUserBasic(ResultSet rs) throws SQLException {
    User u = new User();
    u.setId(rs.getInt("UserID"));
    u.setUsername(rs.getString("Username"));
    u.setPasswordHash(rs.getString("PasswordHash"));
    u.setFullName(rs.getString("FullName"));
    u.setEmail(rs.getString("Email"));
    u.setDepartmentId(rs.getInt("DepartmentID"));
    int mgr = rs.getInt("ManagerUserID");
    u.setManagerUserId(rs.wasNull() ? null : mgr);
    u.setActive(rs.getBoolean("IsActive"));

    // các cột kiểm soát đăng nhập (có thể null)
    if (hasColumn(rs, "FailedLoginCount")) {
      u.setFailedLoginCount(rs.getInt("FailedLoginCount"));
    }
    if (hasColumn(rs, "LockUntil")) {
      Timestamp ts = rs.getTimestamp("LockUntil");
      u.setLockUntil(ts == null ? null : ts.toLocalDateTime());
    }
    return u;
  }

  /** Map đầy đủ + cột kiểm soát (dùng cho findByUsername) */
  private User mapUserAll(ResultSet rs) throws SQLException {
    return mapUserBasic(rs); // hiện tại giống nhau; tách hàm để dễ mở rộng
  }

  private static boolean hasColumn(ResultSet rs, String col) {
    try {
      rs.findColumn(col);
      return true;
    } catch (SQLException ignore) {
      return false;
    }
  }

  /* ============== Load Roles & Features ============== */

  /** Lấy danh sách Role của 1 user (không kèm features) */
  public List<Role> loadRoles(int userId) throws Exception {
    String sql = """
        SELECT r.RoleID, r.Code, r.Name
        FROM UserRole ur
        JOIN Role r ON r.RoleID = ur.RoleID
        WHERE ur.UserID = ?
        ORDER BY r.RoleID
        """;
    List<Role> list = new ArrayList<>();
    try (Connection c = DBContext.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setInt(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Role r = new Role();
          r.setId(rs.getInt("RoleID"));
          r.setCode(rs.getString("Code"));
          r.setName(rs.getString("Name"));
          list.add(r);
        }
      }
    }
    return list;
  }

  /** Nạp feature cho 1 role (dùng chung connection) */
  private Set<Feature> loadFeaturesByRole(int roleId, Connection c) throws Exception {
    String sql = """
        SELECT f.FeatureID, f.Code, f.Name, f.PathPattern
        FROM RoleFeature rf
        JOIN Feature f ON f.FeatureID = rf.FeatureID
        WHERE rf.RoleID = ?
        """;
    Set<Feature> fs = new HashSet<>();
    try (PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setInt(1, roleId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Feature f = new Feature(
              rs.getInt("FeatureID"),
              rs.getString("Code"),
              rs.getString("Name"),
              rs.getString("PathPattern")
          );
          fs.add(f);
        }
      }
    }
    return fs;
  }

  /* ===================== Queries chính ===================== */

  /** Tìm theo username (dùng lúc login) – có cả FailedLoginCount/LockUntil */
  public User findByUsername(String username) throws Exception {
    String sql = """
        SELECT UserID, Username, PasswordHash, FullName, Email,
               DepartmentID, ManagerUserID, IsActive,
               FailedLoginCount, LockUntil
        FROM [User]
        WHERE Username = ?
        """;
    try (Connection c = DBContext.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        return mapUserAll(rs);
      }
    }
  }

  /** Lấy theo ID (detail/duyệt) – NẠP roles + features */
  public User findById(int id) throws Exception {
    String sql = "SELECT * FROM [User] WHERE UserID = ?";
    try (Connection c = DBContext.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        User u = mapUserBasic(rs);

        // nạp roles + features
        List<Role> roles = new ArrayList<>();
        String sqlRole = """
            SELECT r.RoleID, r.Code, r.Name
            FROM UserRole ur
            JOIN Role r ON r.RoleID = ur.RoleID
            WHERE ur.UserID = ?
            ORDER BY r.RoleID
            """;
        try (PreparedStatement pr = c.prepareStatement(sqlRole)) {
          pr.setInt(1, u.getId());
          try (ResultSet rr = pr.executeQuery()) {
            while (rr.next()) {
              Role r = new Role();
              r.setId(rr.getInt("RoleID"));
              r.setCode(rr.getString("Code"));
              r.setName(rr.getString("Name"));
              r.setFeatures(loadFeaturesByRole(r.getId(), c));
              roles.add(r);
            }
          }
        }
        u.setRoles(roles);
        return u;
      }
    }
  }

  /** Danh sách UserID là cấp dưới trực tiếp của 1 manager */
  public List<Integer> findSubordinateIds(int managerId) throws Exception {
    String sql = "SELECT UserID FROM [User] WHERE ManagerUserID = ? AND IsActive = 1";
    List<Integer> ids = new ArrayList<>();
    try (Connection c = DBContext.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setInt(1, managerId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) ids.add(rs.getInt(1));
      }
    }
    return ids;
  }

  /** Có phải trưởng phòng của department không? */
  public boolean isDepartmentHead(int userId, int departmentId) throws Exception {
    String sql = "SELECT 1 FROM Department WHERE DepartmentID = ? AND ManagerUserID = ?";
    try (Connection c = DBContext.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setInt(1, departmentId);
      ps.setInt(2, userId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  /** Tên phòng ban */
  public String getDepartmentName(int departmentId) throws Exception {
    String sql = "SELECT Name FROM Department WHERE DepartmentID = ?";
    try (Connection c = DBContext.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setInt(1, departmentId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? rs.getString(1) : String.valueOf(departmentId);
      }
    }
  }

  /** Lấy map<UserID, FullName> cho nhiều id */
  public Map<Integer, String> getFullNamesByIds(Set<Integer> uids) throws Exception {
    Map<Integer, String> map = new HashMap<>();
    if (uids == null || uids.isEmpty()) return map;

    StringBuilder ph = new StringBuilder();
    for (int i = 0; i < uids.size(); i++) {
      if (i > 0) ph.append(',');
      ph.append('?');
    }
    String sql = "SELECT UserID, FullName FROM [User] WHERE UserID IN (" + ph + ")";
    try (Connection c = DBContext.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      int i = 1;
      for (Integer id : uids) ps.setInt(i++, id);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) map.put(rs.getInt("UserID"), rs.getString("FullName"));
      }
    }
    return map;
  }

  /** Tên role đầu tiên (để hiển thị) */
  public String getFirstRoleName(User u) {
    if (u == null || u.getRoles() == null || u.getRoles().isEmpty()) return "—";
    return u.getRoles().get(0).getName();
  }

  public String getDepartmentNameById(Integer depId) throws Exception {
    if (depId == null) return null;
    String sql = "SELECT Name FROM Department WHERE DepartmentID = ?";
    try (Connection c = DBContext.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setInt(1, depId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? rs.getString(1) : null;
      }
    }
  }

  /** Lấy tất cả UserID dưới quyền 1 manager (đang active) */
  public List<Integer> getUserIdsByManager(int managerUserId) throws Exception {
    return findSubordinateIds(managerUserId);
  }

  /* ============ Kiểm soát đăng nhập (brute-force guard) ============ */

  /** Reset bộ đếm khi đăng nhập đúng */
  public void resetLoginFail(int userId) throws Exception {
    String sql = "UPDATE [User] SET FailedLoginCount = 0, LockUntil = NULL WHERE UserID = ?";
    try (Connection c = DBContext.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setInt(1, userId);
      ps.executeUpdate();
    }
  }

  /**
   * +1 lần sai; nếu đạt ngưỡng thì khóa tạm `lockMinutes` phút.
   * @param userId id người dùng
   * @param maxTries số lần cho phép (ví dụ 10)
   * @param lockMinutes số phút khóa tạm (ví dụ 5–15)
   */
 // +1 lần sai, nếu >= maxTries thì khóa tạm lockMinutes phút (theo giờ local)
public void bumpLoginFail(int userId, int maxTries, int lockMinutes) throws Exception {
  String sql = """
      UPDATE [User]
      SET FailedLoginCount = FailedLoginCount + 1,
          LockUntil = CASE
                        WHEN FailedLoginCount + 1 >= ? THEN DATEADD(MINUTE, ?, SYSDATETIME())
                        ELSE LockUntil
                      END
      WHERE UserID = ?
      """;
  try (var c = DBContext.getConnection();
       var ps = c.prepareStatement(sql)) {
    ps.setInt(1, maxTries);
    ps.setInt(2, lockMinutes);
    ps.setInt(3, userId);
    ps.executeUpdate();
  }
}
// Dự phòng: nếu vì bất kỳ lý do gì mà LockUntil chưa set, set ngay bây giờ
public void forceLock(int userId, int lockMinutes) throws Exception {
  String sql = """
      UPDATE [User]
      SET LockUntil = DATEADD(MINUTE, ?, SYSDATETIME())
      WHERE UserID = ? AND (LockUntil IS NULL OR LockUntil < SYSDATETIME())
      """;
  try (var c = DBContext.getConnection();
       var ps = c.prepareStatement(sql)) {
    ps.setInt(1, lockMinutes);
    ps.setInt(2, userId);
    ps.executeUpdate();
  }
}

/* ===================== Reset password – Admin flow ===================== */

/** User gửi yêu cầu reset (tạo 1 record PENDING) */
public void createResetRequest(int userId, String username) throws Exception {
  String sql = """
      INSERT INTO PasswordResetRequest(UserId, Username, Status)
      VALUES(?, ?, 'PENDING')
      """;
  try (var c = DBContext.getConnection();
       var ps = c.prepareStatement(sql)) {
    ps.setInt(1, userId);
    ps.setString(2, username);
    ps.executeUpdate();
  }
}

/** Danh sách yêu cầu PENDING (cho admin) mới nhất */
public java.util.List<Map<String,Object>> listPendingResetRequests() throws Exception {
  String sql = """
      SELECT r.Id, r.Username, r.UserId, r.RequestedAt
      FROM PasswordResetRequest r
      WHERE r.Status = 'PENDING'
      ORDER BY r.RequestedAt DESC
      """;
  var list = new java.util.ArrayList<Map<String,Object>>();
  try (var c = DBContext.getConnection();
       var ps = c.prepareStatement(sql);
       var rs = ps.executeQuery()) {
    while (rs.next()) {
      var m = new java.util.HashMap<String,Object>();
      m.put("Id", rs.getInt("Id"));
      m.put("UserId", rs.getInt("UserId"));
      m.put("Username", rs.getString("Username"));
      m.put("RequestedAt", rs.getTimestamp("RequestedAt"));
      list.add(m);
    }
  }
  return list;
}

/** Admin từ chối */
public void denyReset(int requestId, int adminId) throws Exception {
  String sql = """
      UPDATE PasswordResetRequest
      SET Status='DENIED', ApprovedBy=?, ApprovedAt=SYSUTCDATETIME()
      WHERE Id=? AND Status='PENDING'
      """;
  try (var c = DBContext.getConnection();
       var ps = c.prepareStatement(sql)) {
    ps.setInt(1, adminId);
    ps.setInt(2, requestId);
    ps.executeUpdate();
  }
}

/** Admin duyệt: tạo token + hạn (ví dụ 30 phút) */
public String approveReset(int requestId, int adminId, int expireMinutes) throws Exception {
  String sql = """
      UPDATE PasswordResetRequest
      SET Status='APPROVED',
          ApprovedBy=?,
          ApprovedAt=SYSUTCDATETIME(),
          Token = NEWID(),
          ExpiresAt = DATEADD(MINUTE, ?, SYSUTCDATETIME())
      WHERE Id=? AND Status='PENDING';
      SELECT CAST(Token AS VARCHAR(36)) AS Token
      FROM PasswordResetRequest WHERE Id=?;
      """;
  try (var c = DBContext.getConnection();
       var ps = c.prepareStatement(sql)) {
    ps.setInt(1, adminId);
    ps.setInt(2, expireMinutes);
    ps.setInt(3, requestId);
    ps.setInt(4, requestId);
    boolean hasResult = ps.execute();
    String token = null;
    while (hasResult) {
      try (var rs = ps.getResultSet()) {
        if (rs != null && rs.next()) token = rs.getString("Token");
      }
      hasResult = ps.getMoreResults();
    }
    return token;
  }
}

/** Tìm yêu cầu hợp lệ theo token (chưa dùng, chưa hết hạn, đã duyệt) */
public Map<String,Object> findValidResetByToken(String token) throws Exception {
  String sql = """
      SELECT r.Id, r.UserId, r.Username, r.Status, r.Token, r.ExpiresAt, r.UsedAt
      FROM PasswordResetRequest r
      WHERE r.Token = ? AND r.Status='APPROVED' AND r.UsedAt IS NULL
            AND r.ExpiresAt > SYSUTCDATETIME()
      """;
  try (var c = DBContext.getConnection();
       var ps = c.prepareStatement(sql)) {
    ps.setString(1, token);
    try (var rs = ps.executeQuery()) {
      if (!rs.next()) return null;
      var m = new java.util.HashMap<String,Object>();
      m.put("Id", rs.getInt("Id"));
      m.put("UserId", rs.getInt("UserId"));
      m.put("Username", rs.getString("Username"));
      m.put("ExpiresAt", rs.getTimestamp("ExpiresAt"));
      return m;
    }
  }
}

/** Cập nhật mật khẩu bằng hash + đánh dấu đã dùng token, reset khoá/đếm sai */
public void resetPasswordWithToken(String token, String newHash) throws Exception {
  try (var c = DBContext.getConnection()) {
    c.setAutoCommit(false);
    try {
      // Lấy userId
      Integer userId = null;
      try (var ps = c.prepareStatement("""
              SELECT UserId FROM PasswordResetRequest
              WHERE Token=? AND Status='APPROVED' AND UsedAt IS NULL AND ExpiresAt > SYSUTCDATETIME()
          """)) {
        ps.setString(1, token);
        try (var rs = ps.executeQuery()) {
          if (rs.next()) userId = rs.getInt(1);
        }
      }
      if (userId == null) throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn.");

      // Update password + mở khoá + reset đếm
      try (var ps = c.prepareStatement("""
              UPDATE [User]
              SET PasswordHash=?, FailedLoginCount=0, LockUntil=NULL
              WHERE UserID=?
          """)) {
        ps.setString(1, newHash);
        ps.setInt(2, userId);
        ps.executeUpdate();
      }

      // Đánh dấu Used
      try (var ps = c.prepareStatement("""
              UPDATE PasswordResetRequest
              SET Status='USED', UsedAt=SYSUTCDATETIME()
              WHERE Token=? AND Status='APPROVED' AND UsedAt IS NULL
          """)) {
        ps.setString(1, token);
        ps.executeUpdate();
      }

      c.commit();
    } catch (Exception ex) {
      c.rollback();
      throw ex;
    } finally {
      c.setAutoCommit(true);
    }
  }
}

}
