package controller;

import dal.PasswordUtil;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

@WebServlet(name="ResetPasswordServlet", urlPatterns={"/reset-password"})
public class ResetPasswordServlet extends HttpServlet {
  private final UserDAO userDAO = new UserDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String token = req.getParameter("token");
    if (token == null || token.isBlank()) { resp.sendError(400); return; }

    try {
      Map<String,Object> r = userDAO.findValidResetByToken(token);
      if (r == null) {
        req.setAttribute("error", "Token không hợp lệ hoặc đã hết hạn.");
      } else {
        req.setAttribute("token", token);
        req.setAttribute("username", (String) r.get("Username"));
      }
    } catch (Exception e) {
      req.setAttribute("error", e.getMessage());
    }
    req.getRequestDispatcher("/WEB-INF/views/reset_password.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String token = req.getParameter("token");
    String p1 = req.getParameter("password");
    String p2 = req.getParameter("confirm");
    if (token==null || p1==null || p2==null || !p1.equals(p2) || p1.length()<6) {
      req.setAttribute("error", "Mật khẩu không hợp lệ hoặc không trùng khớp (tối thiểu 6 ký tự).");
      doGet(req, resp);
      return;
    }
    try {
      String hash = PasswordUtil.sha256Hex(p1);
      userDAO.resetPasswordWithToken(token, hash);
      req.getSession().setAttribute("flash", "Đổi mật khẩu thành công. Mời đăng nhập.");
      resp.sendRedirect(req.getContextPath()+"/loginservlet1");
    } catch (Exception e) {
      req.setAttribute("error", e.getMessage());
      doGet(req, resp);
    }
  }
}
