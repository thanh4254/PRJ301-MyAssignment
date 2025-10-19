package controller;

import dal.RequestDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import model.Request;
import model.User;

@WebServlet(name="RequestListMyServlet1", urlPatterns={"/requestlistmyservlet1"})
public class RequestListMyServlet1 extends HttpServlet {
  private final RequestDAO requestDAO = new RequestDAO();
  private final UserDAO    userDAO    = new UserDAO();

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User me = (User) req.getSession().getAttribute("user");
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }
    try {
      var items = requestDAO.listMine(me.getId());
      Set<Integer> ids = new HashSet<>();
      for (Request r : items) if (r.getProcessedBy()!=null) ids.add(r.getProcessedBy());
      Map<Integer,String> names = userDAO.getFullNamesByIds(ids);

      req.setAttribute("items", items);
      req.setAttribute("names", names);
      req.getRequestDispatcher("/WEB-INF/views/request_list.jsp").forward(req, resp);
    } catch (Exception e) { throw new ServletException(e); }
  }
}