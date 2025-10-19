/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;
import model.RequestStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import model.RequestStatus;
import java.sql.*;

public class HistoryDAO {
    public void add(int requestId, int actionBy, RequestStatus oldSt, RequestStatus newSt, String note) throws Exception {
        String sql = """
          INSERT INTO RequestHistory(RequestID,ActionBy,OldStatus,NewStatus,Note,ActionAt)
          VALUES(?,?,?,?,?,SYSDATETIME())
        """;
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setInt(2, actionBy);
            ps.setString(3, oldSt == null ? null : oldSt.toDbString());
            ps.setString(4, newSt.toDbString());
            if (note == null || note.isBlank()) ps.setNull(5, Types.NVARCHAR); else ps.setNString(5, note);
            ps.executeUpdate();
        }
    }
}
