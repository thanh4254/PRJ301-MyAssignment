package dal;

import model.Feature;
import model.Role;
import model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
    if (hasColumn(rs, "PasswordHash"))   u.setPasswordHash(rs.getString("PasswordHash"));

    if (hasColumn(rs, "FullName"))       u.setFullName(rs.getString("FullName"));
    if (hasColumn(rs, "Email"))          u.setEmail(rs.getString("Email"));
    if (hasColumn(rs, "DepartmentID"))   u.setDepartmentId(rs.getInt("DepartmentID"));

    if (hasColumn(rs, "ManagerUserID")) {
        int mgr = rs.getInt("ManagerUserID");
        u.setManagerUserId(rs.wasNull() ? null : mgr);
    }
    if (hasColumn(rs, "IsActive"))       u.setActive(rs.getBoolean("IsActive"));

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
  private List<Role> loadRoles(Connection cn, int userId) throws Exception {
    String sql = """
      SELECT r.RoleID, r.Code, r.Name
      FROM dbo.UserRole ur
      JOIN dbo.[Role] r ON r.RoleID=ur.RoleID
      WHERE ur.UserID=?
    """;
    List<Role> out = new ArrayList<>();
    try (var ps = cn.prepareStatement(sql)) {
        ps.setInt(1, userId);
        try (var rs = ps.executeQuery()) {
            while (rs.next()) {
                Role r = new Role();
                r.setId(rs.getInt("RoleID"));
                r.setCode(rs.getString("Code"));
                r.setName(rs.getString("Name"));
                out.add(r);
            }
        }
    }
    return out;
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
    String sqlUser = "SELECT UserID, Username, PasswordHash, IsActive, FailedLoginCount, LockUntil FROM dbo.[User] WHERE Username=?";
    try (var cn = DBContext.getConnection();
         var ps = cn.prepareStatement(sqlUser)) {
        ps.setString(1, username);
        try (var rs = ps.executeQuery()) {
            if (!rs.next()) return null;
            User u = mapUser(rs);
            // nạp roles
            u.setRoles(loadRoles(cn, u.getId()));
            return u;
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
    String checkSql = """
        SELECT 1 FROM dbo.PasswordResetRequest 
        WHERE UserId=? AND Status='PENDING'
    """;
    String insertSql = """
        INSERT dbo.PasswordResetRequest(UserId, Username) VALUES(?,?)
    """;
    try (var cn = DBContext.getConnection()) {
        try (var ps = cn.prepareStatement(checkSql)) {
            ps.setInt(1, userId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return; // đã có pending -> không tạo mới
            }
        }
        try (var ps = cn.prepareStatement(insertSql)) {
            ps.setInt(1, userId);
            ps.setString(2, username);
            ps.executeUpdate();
        }
    }
}
// Đếm tổng số request đang PENDING
// Đếm tổng số request PENDING theo filter username (không phân biệt hoa thường)
public int countPendingResetRequests(String q) throws Exception {
    String sql = """
        SELECT COUNT(*)
        FROM dbo.PasswordResetRequest
        WHERE Status='PENDING'
          AND ( ? = '' OR LOWER(Username) LIKE LOWER(?) )
    """;
    try (var cn = DBContext.getConnection();
         var ps = cn.prepareStatement(sql)) {
        String like = (q == null ? "" : q.trim());
        ps.setString(1, like);
        ps.setString(2, like.isEmpty() ? "" : "%" + like + "%");
        try (var rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
// Lấy danh sách PENDING có phân trang + filter username
public List<Map<String,Object>> listPendingResetRequests(int page, int size, String q) throws Exception {
    if (page < 1) page = 1;
    if (size < 1) size = 5;
    int offset = (page - 1) * size;

    String sql = """
        SELECT r.Id, r.UserId, r.Username, r.RequestedAt
        FROM dbo.PasswordResetRequest r
        WHERE r.Status='PENDING'
          AND ( ? = '' OR LOWER(r.Username) LIKE LOWER(?) )
        ORDER BY r.RequestedAt DESC
        OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
    """;
    List<Map<String,Object>> out = new ArrayList<>();
    try (var cn = DBContext.getConnection();
         var ps = cn.prepareStatement(sql)) {
        String like = (q == null ? "" : q.trim());
        ps.setString(1, like);
        ps.setString(2, like.isEmpty() ? "" : "%" + like + "%");
        ps.setInt(3, offset);
        ps.setInt(4, size);
        try (var rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String,Object> m = new HashMap<>();
                m.put("id", rs.getInt("Id"));
                m.put("userId", rs.getInt("UserId"));
                m.put("username", rs.getString("Username"));
                m.put("requestedAt", rs.getTimestamp("RequestedAt"));
                out.add(m);
            }
        }
    }
    return out;
}

/** Admin từ chối */
public void denyReset(int reqId, int adminId) throws Exception {
    String sql = """
        UPDATE dbo.PasswordResetRequest
        SET Status='DENIED', ApprovedBy=?, ApprovedAt=SYSUTCDATETIME()
        WHERE Id=? AND Status='PENDING'
    """;
    try (var cn = DBContext.getConnection(); var ps = cn.prepareStatement(sql)) {
        ps.setInt(1, adminId);
        ps.setInt(2, reqId);
        ps.executeUpdate();
    }
}

/** Admin duyệt: tạo token + hạn (ví dụ 30 phút) */
public String approveReset(int reqId, int adminId, int minutes) throws Exception {
    String sql = """
        UPDATE dbo.PasswordResetRequest
        SET Status='APPROVED', ApprovedBy=?, ApprovedAt=SYSUTCDATETIME(),
            Token=NEWID(), ExpiresAt=DATEADD(MINUTE, ?, SYSUTCDATETIME())
        WHERE Id=? AND Status='PENDING';
        SELECT CAST(Token AS VARCHAR(36)) AS Tok FROM dbo.PasswordResetRequest WHERE Id=?;
    """;
    try (var cn = DBContext.getConnection(); var ps = cn.prepareStatement(sql)) {
        ps.setInt(1, adminId);
        ps.setInt(2, minutes);
        ps.setInt(3, reqId);
        ps.setInt(4, reqId);
        try (var rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString("Tok");
        }
    }
    throw new IllegalStateException("Không duyệt được (request không ở trạng thái PENDING)");
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
/** Giữ tương thích cũ: mapUser(...) = mapUserAll(...) */
private User mapUser(ResultSet rs) throws SQLException {
    return mapUserAll(rs);
}
// Trả về token APPROVED mới nhất còn hạn cho username; null nếu chưa có
// UserDAO.java
public String findLatestApprovedTokenByUsername(String username) throws Exception {
  String sql = """
      SELECT TOP 1 CAST(Token AS VARCHAR(36)) AS Tok
      FROM dbo.PasswordResetRequest
      WHERE Username = ? AND Status = 'APPROVED'
            AND UsedAt IS NULL AND ExpiresAt > SYSUTCDATETIME()
      ORDER BY ApprovedAt DESC
      """;
  try (var cn = DBContext.getConnection();
       var ps = cn.prepareStatement(sql)) {
    ps.setString(1, username);
    try (var rs = ps.executeQuery()) {
      return rs.next() ? rs.getString("Tok") : null;
    }
  }
}



}
