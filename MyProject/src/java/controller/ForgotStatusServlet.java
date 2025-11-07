// controller/ForgotStatusServlet.java
package controller;

import dal.UserDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name="ForgotStatusServlet", urlPatterns={"/forgot-status"})
public class ForgotStatusServlet extends HttpServlet {
  private final UserDAO userDAO = new UserDAO();

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String u = req.getParameter("u");
    resp.setContentType("application/json; charset=UTF-8");
    if (u == null || u.isBlank()) { resp.getWriter().write("{\"ok\":false}"); return; }
    try {
      String token = userDAO.findLatestApprovedTokenByUsername(u.trim());
      if (token != null) {
        resp.getWriter().write("{\"ok\":true,\"token\":\""+token+"\"}");
      } else {
        resp.getWriter().write("{\"ok\":false}");
      }
    } catch (Exception e) {
      resp.setStatus(500);
      resp.getWriter().write("{\"ok\":false}");
    }
  }
}
