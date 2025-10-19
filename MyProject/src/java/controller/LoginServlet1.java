package controller;

import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import model.User;

@WebServlet(name = "LoginServlet1", urlPatterns = {"/loginservlet1"})
public class LoginServlet1 extends HttpServlet {

  private final UserDAO userDAO = new UserDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      req.setCharacterEncoding("UTF-8");

      String username = trim(req.getParameter("username"));
      String password = trim(req.getParameter("password"));

      if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
        req.setAttribute("error", "Vui lòng nhập đầy đủ tài khoản và mật khẩu.");
        doGet(req, resp);
        return;
      }

      // Tìm user (đã lọc IsActive=1)
      User u = userDAO.findByUsername(username);
      if (u == null) {
        req.setAttribute("error", "Sai tài khoản hoặc mật khẩu");
        doGet(req, resp);
        return;
      }

      // So sánh mật khẩu: ưu tiên giữ nguyên hành vi cũ (plaintext),
      // đồng thời chấp nhận DB lưu SHA-256.
      String stored = u.getPasswordHash();
      boolean ok = false;
      if (stored != null) {
        // 1) plaintext (cũ)
        if (password.equals(stored)) ok = true;

        // 2) SHA-256 hex (như 8D969EEF... cho "123456")
        if (!ok && stored.equalsIgnoreCase(sha256Hex(password))) ok = true;

        // 3) Nếu sau này bạn dùng BCrypt, bật dòng dưới và add lib BCrypt:
        // if (!ok && stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"))
        //   ok = org.mindrot.jbcrypt.BCrypt.checkpw(password, stored);
      }

      if (!ok) {
        req.setAttribute("error", "Sai tài khoản hoặc mật khẩu");
        doGet(req, resp);
        return;
      }

      // Đăng nhập thành công
      HttpSession session = req.getSession(true);
      session.setAttribute("user", u);
      resp.sendRedirect(req.getContextPath() + "/requestlistmyservlet1");

    } catch (Exception e) {
      e.printStackTrace();
      req.setAttribute("error", "Đăng nhập lỗi: " + e.getMessage());
      doGet(req, resp);
    }
  }

  private static String trim(String s) {
    return (s == null) ? null : s.trim();
  }

  // Tính SHA-256 và trả về hex lowercase
  private static String sha256Hex(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hash = md.digest(s.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(hash.length * 2);
      for (byte b : hash) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e) {
      return "";
    }
  }
}
