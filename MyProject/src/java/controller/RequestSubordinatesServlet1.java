package controller;

import dal.EmployeeDAO;
import dal.PermissionUtil;
import dal.RequestDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import model.Request;
import model.User;

@WebServlet(name = "RequestSubordinatesServlet1", urlPatterns = {"/requestsubordinatesservlet1"})
public class RequestSubordinatesServlet1 extends HttpServlet {

  private final EmployeeDAO employeeDAO = new EmployeeDAO();
  private final RequestDAO  requestDAO  = new RequestDAO();
  private final UserDAO     userDAO     = new UserDAO();

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
    // toàn bộ cấp dưới (đệ quy)
    java.util.List<Integer> creators = userDAO.getUserIdsByManager(me.getId());

    var items = requestDAO.listByCreatorsPaged(creators, offset, PAGE_SIZE);
    int total  = requestDAO.countByCreators(creators);
    int totalPages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);

    // tên creator/processor
    java.util.Set<Integer> cids = new java.util.HashSet<>();
    java.util.Set<Integer> pids = new java.util.HashSet<>();
    for (model.Request r : items) {
      cids.add(r.getCreatedBy());
      if (r.getProcessedBy()!=null) pids.add(r.getProcessedBy());
    }
    var creatorNames   = userDAO.getFullNamesByIds(cids);
    var processorNames = userDAO.getFullNamesByIds(pids);

    req.setAttribute("items", items);
    req.setAttribute("creatorNames", creatorNames);
    req.setAttribute("processorNames", processorNames);
    req.setAttribute("page", page);
    req.setAttribute("totalPages", totalPages);

    req.getRequestDispatcher("/WEB-INF/views/request_subordinates.jsp").forward(req, resp);
  } catch (Exception e) {
    throw new ServletException(e);
  }
}
}