/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;
import java.security.MessageDigest;
/**
 *
 * @author Admin
 */
public final class PasswordUtil {
    private PasswordUtil(){}
    public static String toSha256Hex(String raw){
        if(raw==null) return null;
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(raw.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(out.length*2);
            for(byte b: out) sb.append(String.format("%02x", b));
            return sb.toString();
        }catch(Exception e){ throw new RuntimeException(e); }
    }
    public static boolean matches(String raw, String storedHex){
        if(storedHex==null) return false;
        return storedHex.equalsIgnoreCase(toSha256Hex(raw));
    }
}
