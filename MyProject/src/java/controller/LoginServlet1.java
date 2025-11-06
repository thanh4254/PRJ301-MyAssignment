package controller;

import dal.PasswordUtil;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import model.Role;
import model.User;

@WebServlet(name = "LoginServlet1", urlPatterns = {"/loginservlet1"})
public class LoginServlet1 extends HttpServlet {

  private final UserDAO userDAO = new UserDAO();

  // Giới hạn & thời gian khoá
  private static final int MAX_TRIES = 10;  // tối đa số lần nhập sai
  private static final int LOCK_MIN  = 5;   // khoá tạm (phút) khi vượt ngưỡng

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ss = req.getSession(false);
    if (ss != null) {
      User u = (User) ss.getAttribute("user");
      if (u != null) {
        resp.sendRedirect(req.getContextPath() + targetAfterLogin(u));
        return;
      }
    }
    req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    String username = trimOrEmpty(req.getParameter("username"));
    String password = trimOrEmpty(req.getParameter("password"));

    if (username.isEmpty() || password.isEmpty()) {
      req.setAttribute("error", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
      req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
      return;
    }

    try {
      User u = userDAO.findByUsername(username);

      // Không lộ thông tin tồn tại tài khoản
      if (u == null) {
        safeDelay();
        req.setAttribute("error", "Sai tên đăng nhập hoặc mật khẩu.");
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        return;
      }

      // 1) Đang bị khoá tạm -> chặn ngay
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime lockUntil = u.getLockUntil(); // có thể null
      if (lockUntil != null && now.isBefore(lockUntil)) {
        long minutes = Math.max(1, Duration.between(now, lockUntil).toMinutes());
        req.setAttribute("error",
            "Tài khoản đang bị khóa tạm thời. Vui lòng thử lại sau ~ " + minutes + " phút.");
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        return;
      }

      // 2) Kiểm tra mật khẩu
      boolean ok = PasswordUtil.matches(password, u.getPasswordHash());
      if (!ok) {
        userDAO.bumpLoginFail(u.getId(), MAX_TRIES, LOCK_MIN);
        User after = userDAO.findByUsername(username);

        if (after.getFailedLoginCount() >= MAX_TRIES && after.getLockUntil() == null) {
          userDAO.forceLock(u.getId(), LOCK_MIN);
          after = userDAO.findByUsername(username);
        }

        String msg;
        if (after.getLockUntil() != null && LocalDateTime.now().isBefore(after.getLockUntil())) {
          long minutes = Math.max(1,
              Duration.between(LocalDateTime.now(), after.getLockUntil()).toMinutes());
          msg = "Bạn đã hết số lần đăng nhập (≥ " + MAX_TRIES + "). "
              + "Tài khoản tạm khóa khoảng " + minutes + " phút.";
        } else {
          int left = Math.max(0, MAX_TRIES - after.getFailedLoginCount());
          msg = "Sai mật khẩu. Bạn còn " + left + " lần thử.";
        }

        req.setAttribute("error", msg);
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        return;
      }

      // 3) Đúng mật khẩu -> reset bộ đếm & login
      userDAO.resetLoginFail(u.getId());
      req.getSession(true).setAttribute("user", u);
      resp.sendRedirect(req.getContextPath() + targetAfterLogin(u));

    } catch (Exception ex) {
      req.setAttribute("error", "Có lỗi: " + ex.getMessage());
      req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }
  }

  /* ================= Helpers ================= */

  private static String targetAfterLogin(User u) {
    return isAdmin(u) ? "/admin/reset-requests" : "/requestlistmyservlet1";
  }

  private static boolean isAdmin(User u) {
    if (u == null || u.getRoles() == null) return false;
    for (Role r : u.getRoles()) {
      if (r != null && "ADMIN".equalsIgnoreCase(r.getCode())) return true;
    }
    return false;
  }

  private static String trimOrEmpty(String s) {
    return (s == null) ? "" : s.trim();
  }

  private static void safeDelay() {
    try { Thread.sleep(500L); } catch (InterruptedException ignored) {}
  }
}
