package dal;

import model.Request;
import model.RequestStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequestDAO {

    private Request map(ResultSet rs) throws SQLException {
        Request r = new Request();
        r.setId(rs.getInt("RequestID"));
        r.setTitle(rs.getNString("Title"));
        r.setFrom(rs.getDate("DateFrom").toLocalDate());
        r.setTo(rs.getDate("DateTo").toLocalDate());
        r.setReason(rs.getNString("Reason"));
        r.setCreatedBy(rs.getInt("CreatedBy"));
        r.setStatus(RequestStatus.valueOf(rs.getString("Status")));
        int p = rs.getInt("ProcessedBy");
        r.setProcessedBy(rs.wasNull() ? null : p);
        r.setProcessedNote(rs.getNString("ProcessedNote"));
        Timestamp ca = rs.getTimestamp("CreatedAt"), ua = rs.getTimestamp("UpdatedAt");
        r.setCreatedAt(ca == null ? null : ca.toLocalDateTime());
        r.setUpdatedAt(ua == null ? null : ua.toLocalDateTime());
        return r;
    }

    public boolean existsApprovedOverlap(int userId, LocalDate from, LocalDate to) throws Exception {
        String sql =
            "SELECT 1 FROM [Request] " +
            "WHERE CreatedBy=? AND Status='APPROVED' " +
            "  AND NOT (DateTo < ? OR DateFrom > ?)";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int insert(Request r) throws Exception {
        String sql =
            "INSERT INTO [Request](Title,DateFrom,DateTo,Reason,CreatedBy,Status,CreatedAt,UpdatedAt) " +
            "VALUES(?,?,?,?,?, ?, SYSDATETIME(), SYSDATETIME()); " +
            "SELECT SCOPE_IDENTITY();";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setNString(1, r.getTitle());
            ps.setDate(2, java.sql.Date.valueOf(r.getFrom()));
            ps.setDate(3, java.sql.Date.valueOf(r.getTo()));
            ps.setNString(4, r.getReason());
            ps.setInt(5, r.getCreatedBy());
            ps.setString(6, r.getStatus().name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Request findById(int id) throws Exception {
        String sql = "SELECT * FROM [Request] WHERE RequestID=?";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void updateStatus(int requestId, RequestStatus st, Integer processedBy, String note) throws Exception {
        String sql = "UPDATE [Request] SET Status=?,ProcessedBy=?,ProcessedNote=?,UpdatedAt=SYSDATETIME() WHERE RequestID=?";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, st.name());
            if (processedBy == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, processedBy);
            if (note == null) ps.setNull(3, Types.NVARCHAR); else ps.setNString(3, note);
            ps.setInt(4, requestId);
            ps.executeUpdate();
        }
    }

    public List<Request> listMine(int userId) throws Exception {
        String sql = "SELECT * FROM [Request] WHERE CreatedBy=? ORDER BY CreatedAt DESC";
        List<Request> list = new ArrayList<>();
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Request> listByCreators(List<Integer> creatorIds) throws Exception {
        if (creatorIds.isEmpty()) return Collections.emptyList();
        String placeholders = String.join(",", Collections.nCopies(creatorIds.size(), "?"));
        String sql = "SELECT * FROM [Request] WHERE CreatedBy IN (" + placeholders + ") ORDER BY CreatedAt DESC";
        List<Request> list = new ArrayList<>();
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < creatorIds.size(); i++) ps.setInt(i + 1, creatorIds.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // === Agenda: các đơn APPROVED giao với khoảng ngày cho danh sách user ===
    public List<Request> listApprovedByCreatorsInRange(List<Integer> creatorIds,
                                                       LocalDate from,
                                                       LocalDate to) throws Exception {
        if (creatorIds == null || creatorIds.isEmpty()) return Collections.emptyList();
        String placeholders = String.join(",", Collections.nCopies(creatorIds.size(), "?"));
        String sql =
            "SELECT * FROM [Request] " +
            "WHERE Status='APPROVED' " +
            "  AND CreatedBy IN (" + placeholders + ") " +
            "  AND NOT (DateTo < ? OR DateFrom > ?) " +
            "ORDER BY CreatedBy";
        List<Request> list = new ArrayList<>();
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int idx = 1;
            for (Integer id : creatorIds) ps.setInt(idx++, id);
            ps.setDate(idx++, java.sql.Date.valueOf(from));
            ps.setDate(idx,   java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }
}
