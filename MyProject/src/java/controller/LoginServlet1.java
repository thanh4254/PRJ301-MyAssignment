package controller;

import dal.UserDAO;
import dal.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import model.User;

@WebServlet(name="LoginServlet1", urlPatterns={"/loginservlet1"})
public class LoginServlet1 extends HttpServlet {
  private final UserDAO userDAO = new UserDAO();

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String u = req.getParameter("username");
    String p = req.getParameter("password");
    try {
      User user = userDAO.findByUsername(u);
      if (user == null || !PasswordUtil.verify(p, user.getPasswordHash())) {
        req.setAttribute("error", "Sai tài khoản hoặc mật khẩu");
        doGet(req, resp); return;
      }
      req.getSession().setAttribute("user", user);
      resp.sendRedirect(req.getContextPath()+"/requestlistmyservlet1");
    } catch (Exception e) { throw new ServletException(e); }
  }
}
