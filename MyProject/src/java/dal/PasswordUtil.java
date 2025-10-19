/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;
import java.security.MessageDigest;

public class PasswordUtil {
    public static String sha256Hex(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(b.length * 2);
            for (byte x : b) sb.append(String.format("%02X", x));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verify(String plain, String hexHash) {
        if (plain == null || hexHash == null) return false;
        return sha256Hex(plain).equalsIgnoreCase(hexHash);
    }
}