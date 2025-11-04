package controller;

import dal.RequestDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

  final int PAGE_SIZE = 5;
  int page;
  try { page = Math.max(1, Integer.parseInt(req.getParameter("page"))); }
  catch (Exception ignore) { page = 1; }
  int offset = (page - 1) * PAGE_SIZE;

  try {
    var items = requestDAO.listMyPaged(me.getId(), offset, PAGE_SIZE);
    int total = requestDAO.countMy(me.getId());
    int totalPages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);

    // tên người xử lý
    java.util.Set<Integer> pids = new java.util.HashSet<>();
    for (model.Request r : items) if (r.getProcessedBy()!=null) pids.add(r.getProcessedBy());
    java.util.Map<Integer,String> names = userDAO.getFullNamesByIds(pids);

    req.setAttribute("items", items);
    req.setAttribute("names", names);
    req.setAttribute("page", page);
    req.setAttribute("totalPages", totalPages);

    req.getRequestDispatcher("/WEB-INF/views/request_list.jsp").forward(req, resp);
  } catch (Exception e) {
    throw new ServletException(e);
  }
}
}
