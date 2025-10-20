package dal;

import model.Request;
import model.RequestStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;        // java.sql.Date
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequestDAO {

    private Request mapRow(ResultSet rs) throws Exception {
        Request r = new Request();
        r.setId(rs.getInt("RequestID"));
        r.setTitle(rs.getString("Title"));
        Date df = rs.getDate("DateFrom");
        Date dt = rs.getDate("DateTo");
        r.setFrom(df == null ? null : df.toLocalDate());
        r.setTo(dt == null ? null : dt.toLocalDate());
        r.setReason(rs.getString("Reason"));
        r.setCreatedBy(rs.getInt("CreatedBy"));
        int pby = rs.getInt("ProcessedBy");
        r.setProcessedBy(rs.wasNull() ? null : pby);
        r.setProcessedNote(rs.getString("ProcessedNote"));
        r.setStatus(RequestStatus.valueOf(rs.getString("Status")));
        return r;
    }

    public Request findById(int id) throws Exception {
        String sql =
            "SELECT RequestID, Title, DateFrom, DateTo, Reason, CreatedBy, " +
            "       Status, ProcessedBy, ProcessedNote, CreatedAt, UpdatedAt " +
            "FROM Request WHERE RequestID=?";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /** Tạo mới: Reason = lý do xin nghỉ của nhân viên */
    public int create(int createdBy, LocalDate from, LocalDate to,
                      String title, String reason) throws Exception {
        String sql =
            "INSERT INTO Request(Title, DateFrom, DateTo, Reason, CreatedBy, Status, CreatedAt, UpdatedAt) " +
            "VALUES(?, ?, ?, ?, ?, 'NEW', GETDATE(), GETDATE()); " +
            "SELECT SCOPE_IDENTITY();";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ps.setString(4, reason);
            ps.setInt(5, createdBy);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Cập nhật trạng thái + phản hồi của cấp trên vào ProcessedNote */
    public void updateStatus(int id, RequestStatus status, int processedBy, String note) throws Exception {
        String sql =
            "UPDATE Request " +
            "   SET Status=?, " +
            "       ProcessedBy=?, " +
            "       ProcessedNote=?, " +
            "       UpdatedAt=GETDATE() " +
            " WHERE RequestID=?";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, processedBy);
            ps.setString(3, note);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    /** Danh sách theo nhiều người tạo */
    public List<Request> listByCreators(List<Integer> uids) throws Exception {
        if (uids == null || uids.isEmpty()) return Collections.emptyList();
        String placeholders = String.join(",", Collections.nCopies(uids.size(), "?"));
        String sql =
            "SELECT RequestID, Title, DateFrom, DateTo, Reason, CreatedBy, " +
            "       Status, ProcessedBy, ProcessedNote, CreatedAt, UpdatedAt " +
            "FROM Request " +
            "WHERE CreatedBy IN (" + placeholders + ") " +
            "ORDER BY DateFrom DESC, RequestID DESC";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            for (Integer uid : uids) ps.setInt(i++, uid);
            try (ResultSet rs = ps.executeQuery()) {
                List<Request> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    /** Danh sách theo 1 người tạo (dùng cho “Đơn của tôi”) */
    public List<Request> listByCreator(int uid) throws Exception {
        String sql =
            "SELECT RequestID, Title, DateFrom, DateTo, Reason, CreatedBy, " +
            "       Status, ProcessedBy, ProcessedNote, CreatedAt, UpdatedAt " +
            "FROM Request " +
            "WHERE CreatedBy=? " +
            "ORDER BY DateFrom DESC, RequestID DESC";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, uid);
            try (ResultSet rs = ps.executeQuery()) {
                List<Request> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    /** Phục vụ Agenda: lấy đã APPROVED và giao với khoảng ngày [from..to] */
    public List<Request> listApprovedByCreatorsInRange(List<Integer> uids,
                                                       LocalDate from, LocalDate to) throws Exception {
        if (uids == null || uids.isEmpty()) return Collections.emptyList();
        String placeholders = String.join(",", Collections.nCopies(uids.size(), "?"));
        String sql =
            "SELECT RequestID, Title, DateFrom, DateTo, Reason, CreatedBy, " +
            "       Status, ProcessedBy, ProcessedNote, CreatedAt, UpdatedAt " +
            "FROM Request " +
            "WHERE Status='APPROVED' " +
            "  AND CreatedBy IN (" + placeholders + ") " +
            "  AND NOT (DateTo < ? OR DateFrom > ?)";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            for (Integer uid : uids) ps.setInt(i++, uid);
            ps.setDate(i++, Date.valueOf(from));
            ps.setDate(i,   Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                List<Request> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    /** Alias tiện cho servlet “Đơn của tôi” */
    public List<Request> listMine(int uid) throws Exception {
        return listByCreator(uid);
    }
}