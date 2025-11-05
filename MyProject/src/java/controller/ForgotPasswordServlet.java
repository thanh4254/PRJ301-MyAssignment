package controller;

import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import model.User;

@WebServlet(name="ForgotPasswordServlet", urlPatterns={"/forgot"})
public class ForgotPasswordServlet extends HttpServlet {
  private final UserDAO userDAO = new UserDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.getRequestDispatcher("/WEB-INF/views/forgot.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String username = req.getParameter("username")==null? "" : req.getParameter("username").trim();
    try {
      User u = (username.isEmpty()) ? null : userDAO.findByUsername(username);
      if (u != null && u.isActive()) {
        userDAO.createResetRequest(u.getId(), u.getUsername());
      }
      // luôn trả lời trung lập để tránh lộ thông tin
      req.setAttribute("info", "Nếu tài khoản tồn tại, yêu cầu đã được gửi tới Admin để duyệt.");
    } catch (Exception e) {
      req.setAttribute("error", "Không thể gửi yêu cầu lúc này.");
    }
    req.getRequestDispatcher("/WEB-INF/views/forgot.jsp").forward(req, resp);
  }
}
