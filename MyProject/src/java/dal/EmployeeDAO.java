package dal;

import model.EmployeeView;

import java.sql.*;
import java.util.*;

public class EmployeeDAO {

    private EmployeeView mapEmp(ResultSet rs) throws SQLException {
        EmployeeView e = new EmployeeView();
        e.setId(rs.getInt("eid"));
        e.setName(rs.getString("ename"));
        e.setDepartmentId(rs.getInt("did"));
        int sup = rs.getInt("supervisorid");
        e.setSupervisorId(rs.wasNull() ? null : sup);
        try {
            int uid = rs.getInt("uid");
            if (!rs.wasNull()) e.setUserId(uid);
        } catch (SQLException ignore) {}
        return e;
    }

    /** Lấy eid theo uid (VIEW Enrollment) */
    public Integer findEidByUserId(int uid) throws Exception {
        String sql = "SELECT TOP 1 eid FROM Enrollment WHERE uid=? AND active=1";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, uid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("eid") : null;
            }
        }
    }

    /** Lấy EmployeeView theo uid */
    public EmployeeView findByUserId(int uid) throws Exception {
        String sql = """
            SELECT e.eid, e.ename, e.did, e.supervisorid, en.uid
            FROM Employee e
            JOIN Enrollment en ON en.eid = e.eid AND en.active=1
            WHERE en.uid=?
        """;
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, uid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapEmp(rs) : null;
            }
        }
    }

    /** Liệt kê nhân sự theo phòng */
    public List<EmployeeView> listByDepartment(int departmentId) throws Exception {
        String sql = "SELECT eid, ename, did, supervisorid FROM Employee WHERE did=? ORDER BY ename";
        List<EmployeeView> list = new ArrayList<>();
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapEmp(rs));
            }
        }
        return list;
    }

    /** Lấy UID cấp dưới trực tiếp của 1 manager UID (qua supervisorid) */
    public List<Integer> findSubordinateUserIds(int managerUid) throws Exception {
    Integer managerEid = findEidByUserId(managerUid);
    if (managerEid == null) return List.of();

    String sql = "SELECT eid FROM Employee WHERE supervisorid=?";
    List<Integer> eids = new ArrayList<>();
    try (Connection c = DBContext.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, managerEid);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) eids.add(rs.getInt(1));
        }
    }
    if (eids.isEmpty()) return List.of();

    String placeholders = String.join(",", Collections.nCopies(eids.size(), "?"));
    String sql2 = "SELECT uid FROM Enrollment WHERE eid IN (" + placeholders + ") AND active=1";
    List<Integer> uids = new ArrayList<>();
    try (Connection c = DBContext.getConnection();
         PreparedStatement ps = c.prepareStatement(sql2)) {
        int i = 0;
        for (Integer eid : eids) ps.setInt(++i, eid);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) uids.add(rs.getInt(1));
        }
    }
    return uids;
}
}
