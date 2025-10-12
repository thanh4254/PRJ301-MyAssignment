/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;
import model.Feature; import model.Role; import model.User;
import java.sql.*; import java.util.*;
/**
 *
 * @author Admin
 */
public class UserDAO {

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("UserID"));
        u.setUsername(rs.getString("Username"));
        u.setPasswordHash(rs.getString("PasswordHash"));
        u.setFullName(rs.getNString("FullName"));
        u.setEmail(rs.getString("Email"));
        u.setDepartmentId(rs.getInt("DepartmentID"));
        int mgr = rs.getInt("ManagerUserID");
        u.setManagerUserId(rs.wasNull()?null:mgr);
        u.setActive(rs.getBoolean("IsActive"));
        return u;
    }

    public User findByUsername(String username) throws Exception {
        String sql = "SELECT * FROM [User] WHERE Username=? AND IsActive=1";
        try (Connection c = DBContext.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                User u = mapUser(rs);
                u.setRoles(loadRoles(u.getId(), c));
                return u;
            }
        }
    }

    private Set<Role> loadRoles(int userId, Connection c) throws Exception {
        String sql = """
            SELECT r.RoleID, r.Code, r.Name FROM UserRole ur
            JOIN Role r ON r.RoleID=ur.RoleID WHERE ur.UserID=?
        """;
        Set<Role> roles = new HashSet<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Role r = new Role(rs.getInt(1), rs.getString(2), rs.getNString(3));
                    r.setFeatures(loadFeatures(r.getId(), c));
                    roles.add(r);
                }
            }
        }
        return roles;
    }

    private Set<Feature> loadFeatures(int roleId, Connection c) throws Exception {
        String sql = """
            SELECT f.FeatureID, f.Code, f.Name, f.PathPattern FROM RoleFeature rf
            JOIN Feature f ON f.FeatureID=rf.FeatureID WHERE rf.RoleID=?
        """;
        Set<Feature> fs = new HashSet<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    fs.add(new Feature(rs.getInt(1), rs.getString(2), rs.getNString(3), rs.getString(4)));
                }
            }
        }
        return fs;
    }

    public List<Integer> findSubordinateIds(int managerId) throws Exception {
        String sql = "SELECT UserID FROM [User] WHERE ManagerUserID=? AND IsActive=1";
        List<Integer> ids = new ArrayList<>();
        try (Connection c = DBContext.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) ids.add(rs.getInt(1)); }
        }
        return ids;
    }
public java.util.List<model.User> listByDepartment(int departmentId) throws Exception {
        String sql = "SELECT * FROM [User] WHERE DepartmentID=? AND IsActive=1";
        java.util.List<model.User> list = new java.util.ArrayList<>();
        try (java.sql.Connection c = DBContext.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.User u = mapUser(rs);
                    list.add(u);
                }
            }
        }
        return list;
    }
// Lấy user theo ID (có nạp luôn roles & features)
public User findById(int id) throws Exception {
    String sql = "SELECT * FROM [User] WHERE UserID=?";
    try (Connection c = DBContext.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, id);
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return null;
            User u = mapUser(rs);            // dùng hàm mapUser đã có
            u.setRoles(loadRoles(u.getId(), c)); // nạp roles & features
            return u;
        }
    }
}



// Kiểm tra user có phải Trưởng phòng (Head) của department chỉ định không
public boolean isDepartmentHead(int userId, int departmentId) throws Exception {
    String sql = "SELECT 1 FROM Department WHERE DepartmentID=? AND ManagerUserID=?";
    try (java.sql.Connection c = DBContext.getConnection();
         java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, departmentId);
        ps.setInt(2, userId);
        try (java.sql.ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }
}

// Lấy TẤT CẢ userID trong 1 phòng (để Head xem/duyệt)
public java.util.List<Integer> findIdsInDepartment(int departmentId) throws Exception {
    String sql = "SELECT UserID FROM [User] WHERE DepartmentID=? AND IsActive=1";
    java.util.List<Integer> ids = new java.util.ArrayList<>();
    try (java.sql.Connection c = DBContext.getConnection();
         java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, departmentId);
        try (java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ids.add(rs.getInt(1));
        }
    }
    return ids;
}
public String getDepartmentName(int departmentId) throws Exception {
    String sql = "SELECT Name FROM Department WHERE DepartmentID=?";
    try (java.sql.Connection c = DBContext.getConnection();
         java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, departmentId);
        try (java.sql.ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getNString(1) : String.valueOf(departmentId);
        }
    }
}
public java.util.Map<Integer,String> getFullNamesByIds(java.util.Collection<Integer> ids) throws Exception {
    if (ids == null || ids.isEmpty()) return java.util.Collections.emptyMap();
    String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
    String sql = "SELECT UserID, FullName FROM [User] WHERE UserID IN ("+placeholders+")";

    java.util.Map<Integer,String> map = new java.util.HashMap<>();
    try (java.sql.Connection c = DBContext.getConnection();
         java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
        int i = 1;
        for (Integer id : ids) ps.setInt(i++, id);
        try (java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) map.put(rs.getInt(1), rs.getNString(2));
        }
    }
    return map;
    
}


}

