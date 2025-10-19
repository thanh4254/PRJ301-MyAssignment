/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.time.LocalDate; import java.time.LocalDateTime;
/**
 *
 * @author Admin
 */
public enum RequestStatus {
    NEW, APPROVED, REJECTED, CANCELLED;

    public static RequestStatus fromDbString(String s) {
        if (s == null) return NEW;
        return RequestStatus.valueOf(s.trim().toUpperCase());
    }

    public String toDbString() {
        return name();
    }

    /** Map sang int (nếu cần dùng StatusMap kiểu int) */
    public int toInt() {
        switch (this) {
            case NEW: return 0;
            case APPROVED: return 1;
            case REJECTED: return 2;
            case CANCELLED: return 3;
            default: return 0;
        }
    }

    public static RequestStatus fromInt(int v) {
        switch (v) {
            case 1: return APPROVED;
            case 2: return REJECTED;
            case 3: return CANCELLED;
            default: return NEW;
        }
    }
}
