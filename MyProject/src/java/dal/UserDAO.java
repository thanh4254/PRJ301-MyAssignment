/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;
import model.Feature;
import model.Role;
import model.User;

import java.sql.*;
import java.util.*;

public class UserDAO {

    // -------------------- mapping 1 row -> User --------------------
    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("UserID"));
        u.setUsername(rs.getString("Username"));
        u.setPasswordHash(rs.getString("PasswordHash"));
        u.setFullName(rs.getNString("FullName"));
        u.setEmail(rs.getString("Email"));
        u.setDepartmentId(rs.getInt("DepartmentID"));
        int mgr = rs.getInt("ManagerUserID");
        u.setManagerUserId(rs.wasNull() ? null : mgr);
        u.setActive(rs.getBoolean("IsActive"));
        return u;
    }

    // -------------------- Roles & Features --------------------
    private Set<Role> loadRoles(int userId, Connection c) throws Exception {
        String sql = """
            SELECT r.RoleID, r.Code, r.Name
            FROM UserRole ur
            JOIN Role r ON r.RoleID = ur.RoleID
            WHERE ur.UserID = ?
        """;
        Set<Role> roles = new HashSet<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Role r = new Role(
                            rs.getInt("RoleID"),
                            rs.getString("Code"),
                            rs.getNString("Name")
                    );
                    r.setFeatures(loadFeatures(r.getId(), c));
                    roles.add(r);
                }
            }
        }
        return roles;
    }

    private Set<Feature> loadFeatures(int roleId, Connection c) throws Exception {
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
                    fs.add(new Feature(
                            rs.getInt("FeatureID"),
                            rs.getString("Code"),
                            rs.getNString("Name"),
                            rs.getString("PathPattern")
                    ));
                }
            }
        }
        return fs;
    }

    // -------------------- Queries chính --------------------
    // Login
  public User findByUsername(String username) throws Exception {
    String sql = "SELECT * FROM [User] WHERE Username=? AND IsActive=1";
    try (Connection c = DBContext.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setString(1, username);
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return null;
            User u = mapUser(rs);
           u.setRoles(new ArrayList<>(loadRoles(u.getId(), c))); // trả về Set<Role>, đừng cast List
            return u;
        }
    }
}

    // Lấy theo ID (phục vụ detail/duyệt) – cũng nạp quyền
    public User findById(int id) throws Exception {
    String sql = "SELECT * FROM [User] WHERE UserID=?";
    try (Connection c = DBContext.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, id);
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return null;
            User u = mapUser(rs);
            // CHỖ SỬA:
            u.setRoles(new ArrayList<>(loadRoles(u.getId(), c)));
            return u;
        }
    }
}
    // Danh sách UserID là cấp dưới trực tiếp của 1 manager
    public List<Integer> findSubordinateIds(int managerId) throws Exception {
        String sql = "SELECT UserID FROM [User] WHERE ManagerUserID=? AND IsActive=1";
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

    // Có phải trưởng phòng của department không?
    public boolean isDepartmentHead(int userId, int departmentId) throws Exception {
        String sql = "SELECT 1 FROM Department WHERE DepartmentID=? AND ManagerUserID=?";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Tên phòng ban
    public String getDepartmentName(int departmentId) throws Exception {
        String sql = "SELECT Name FROM Department WHERE DepartmentID=?";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getNString(1) : String.valueOf(departmentId);
            }
        }
    }

    // Map id -> fullName (hiển thị CreatedBy / ProcessedBy)
    public Map<Integer,String> getFullNamesByIds(Collection<Integer> ids) throws Exception {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT UserID, FullName FROM [User] WHERE UserID IN (" + placeholders + ")";
        Map<Integer,String> map = new HashMap<>();
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            for (Integer id : ids) ps.setInt(i++, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getInt(1), rs.getNString(2));
            }
        }
        return map;
    }

    // Tên role đầu tiên để hiển thị form
    public String getFirstRoleName(User u) {
        if (u == null || u.getRoles() == null || u.getRoles().isEmpty()) return "—";
        return u.getRoles().iterator().next().getName();
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
}
