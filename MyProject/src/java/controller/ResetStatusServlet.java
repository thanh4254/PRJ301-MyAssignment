package controller;

import dal.UserDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name="ResetStatusServlet", urlPatterns={"/reset-status"})
public class ResetStatusServlet extends HttpServlet {
  private final UserDAO userDAO = new UserDAO();

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String username = req.getParameter("username");
    if (username == null || username.isBlank()) {
      Object u = req.getSession().getAttribute("waiting_username");
      if (u != null) username = String.valueOf(u);
    }
    resp.setContentType("application/json; charset=UTF-8");
    if (username == null || username.isBlank()) { resp.getWriter().write("{\"ok\":false}"); return; }

    try {
      String token = userDAO.findLatestApprovedTokenByUsername(username.trim());
      if (token == null) resp.getWriter().write("{\"ok\":false}");
      else resp.getWriter().write("{\"ok\":true,\"token\":\""+token.replace("\"","\\\"")+"\"}");
    } catch (Exception e) {
      resp.setStatus(500);
      resp.getWriter().write("{\"ok\":false,\"error\":\""+e.getMessage().replace("\"","\\\"")+"\"}");
    }
  }
}
