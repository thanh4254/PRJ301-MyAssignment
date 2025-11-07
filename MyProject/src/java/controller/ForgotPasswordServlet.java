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
    // Cho phép mở trang kèm param u để hiển thị “đang chờ duyệt”
    String u = req.getParameter("u");
    if (u != null && !u.trim().isEmpty()) {
      req.setAttribute("watchUser", u.trim());
    }
    req.getRequestDispatcher("/WEB-INF/views/forgot.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String username = req.getParameter("username") == null ? "" : req.getParameter("username").trim();
    try {
      if (!username.isEmpty()) {
        User u = userDAO.findByUsername(username);
        if (u != null && u.isActive()) {
          // Tạo yêu cầu nếu chưa có pending
          userDAO.createResetRequest(u.getId(), u.getUsername());
        }
      }
      // Luôn trả lời trung lập + kích hoạt cơ chế polling phía client
      req.setAttribute("info", "Nếu tài khoản tồn tại, yêu cầu đã được gửi tới Admin để duyệt.");
      req.setAttribute("watchUser", username); // để JS biết cần theo dõi username nào
    } catch (Exception e) {
      req.setAttribute("error", "Không thể gửi yêu cầu lúc này.");
    }
    req.getRequestDispatcher("/WEB-INF/views/forgot.jsp").forward(req, resp);
  }
}
