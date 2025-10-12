/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;
import java.sql.Connection; import java.sql.DriverManager;
/**
 *
 * @author Admin
 */
public class DBContext {
     private static final String URL  = "jdbc:sqlserver://localhost:1433;databaseName=ASSIGNMENT;encrypt=true;trustServerCertificate=true; ";
    private static final String USER = "thanhnd";
    private static final String PASS = "123"; // đổi theo máy bạn
    public static Connection getConnection() throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
