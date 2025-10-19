/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBContext {
    private static final String URL =
        "jdbc:sqlserver://localhost:1433;databaseName=ASSIGNMENT;encrypt=true;trustServerCertificate=true;loginTimeout=15;";
    private static final String USER = "thanhnd";
    private static final String PASS = "123"; // đổi theo máy bạn

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Không tìm thấy SQLServer JDBC Driver", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
