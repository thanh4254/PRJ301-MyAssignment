/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;
import java.sql.*;
/**
 *
 * @author Admin
 */
public class HistoryDAO {
    public void add(int requestId, int actorId, String oldStatus, String newStatus, String note) throws Exception {
        String sql = """
          INSERT INTO RequestHistory(RequestID,ActionBy,OldStatus,NewStatus,Note,ActionAt)
          VALUES(?,?,?,?,?,SYSDATETIME())
        """;
        try (Connection c = DBContext.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, requestId); ps.setInt(2, actorId);
            if (oldStatus==null) ps.setNull(3, Types.VARCHAR); else ps.setString(3, oldStatus);
            ps.setString(4, newStatus);
            if (note==null) ps.setNull(5, Types.NVARCHAR); else ps.setNString(5, note);
            ps.executeUpdate();
        }
    }
}
