package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name="LogoutServlet1", urlPatterns={"/logoutservlet1"})
public class LogoutServlet1 extends HttpServlet {
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    HttpSession s = req.getSession(false);
    if (s != null) s.invalidate();
    resp.sendRedirect(req.getContextPath()+"/loginservlet1");
  }
}