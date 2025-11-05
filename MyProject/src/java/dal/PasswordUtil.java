package dal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordUtil {
    private PasswordUtil() {}

    /** Trả về SHA-256 dưới dạng HEX (UPPERCASE). */
    public static String sha256Hex(String plain) {
        if (plain == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Kiểm tra mật khẩu thô với hash HEX đã lưu (không phân biệt hoa/thường). */
    public static boolean verify(String plain, String hexHash) {
        if (plain == null || hexHash == null) return false;
        String computed = sha256Hex(plain);
        // so sánh constant-time
        return slowEquals(
            computed.toUpperCase().getBytes(StandardCharsets.US_ASCII),
            hexHash.toUpperCase().getBytes(StandardCharsets.US_ASCII)
        );
    }

    /** Alias cho verify – tiện đổi tên khi dùng ở chỗ khác. */
    public static boolean matches(String rawPassword, String storedHex) {
        return verify(rawPassword, storedHex);
    }

    // ================== helpers ==================
    private static String toHex(byte[] bytes) {
        char[] HEX = "0123456789ABCDEF".toCharArray();
        char[] out = new char[bytes.length * 2];
        int i = 0;
        for (byte b : bytes) {
            int v = b & 0xFF;
            out[i++] = HEX[v >>> 4];
            out[i++] = HEX[v & 0x0F];
        }
        return new String(out);
    }

    /** So sánh constant-time để hạn chế timing attack. */
    private static boolean slowEquals(byte[] a, byte[] b) {
        if (a == null || b == null) return false;
        int diff = a.length ^ b.length;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) diff |= (a[i] ^ b[i]);
        return diff == 0;
    }
}
