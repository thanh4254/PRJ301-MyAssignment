package controller;

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

  private static final int PAGE_SIZE = 5;
  private final RequestDAO requestDAO = new RequestDAO();
  private final UserDAO    userDAO    = new UserDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    User me = (User) req.getSession().getAttribute("user");
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }

    // đảm bảo có features trước khi check quyền
    PermissionUtil.ensureFeatures(me, userDAO);

    String q = req.getParameter("q");
    if (q != null) q = q.trim();

    int page = 1;
    try { page = Math.max(1, Integer.parseInt(req.getParameter("page"))); }
    catch (Exception ignore) {}
    int offset = (page - 1) * PAGE_SIZE;

    try {
      boolean isHead = PermissionUtil.isDepartmentHead(me, userDAO)
                    || PermissionUtil.hasFeatureCode(me, "REQ_HEAD");

      List<Integer> creators;
      if (isHead) {
        Integer depId = me.getDepartmentId();
        creators = (depId == null) ? Collections.emptyList()
                                   : userDAO.findUserIdsByDepartment(depId);
      } else {
        // LẤY CẢ CÂY cấp dưới
        creators = userDAO.getUserIdsByManager(me.getId());
      }
      if (creators == null) creators = Collections.emptyList();

      // filter theo tên creator
      List<Integer> filtered = creators;
      if (q != null && !q.isEmpty() && !creators.isEmpty()) {
        Map<Integer,String> all = userDAO.getFullNamesByIds(new HashSet<>(creators));
        String needle = q.toLowerCase(Locale.ROOT);
        filtered = new ArrayList<>();
        for (Integer uid : creators) {
          String nm = all.get(uid);
          if (nm != null && nm.toLowerCase(Locale.ROOT).contains(needle)) filtered.add(uid);
        }
      }

      int total = filtered.isEmpty() ? 0 : requestDAO.countByCreators(filtered);
      int totalPages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
      if (page > totalPages) { page = totalPages; offset = (page - 1) * PAGE_SIZE; }

      List<Request> items = (total == 0)
              ? Collections.emptyList()
              : requestDAO.listByCreatorsPaged(filtered, offset, PAGE_SIZE);

      Map<Integer,String> creatorNames = filtered.isEmpty()
              ? Collections.emptyMap()
              : userDAO.getFullNamesByIds(new HashSet<>(filtered));

      Set<Integer> pids = new HashSet<>();
      for (Request r : items) if (r.getProcessedBy()!=null) pids.add(r.getProcessedBy());
      Map<Integer,String> processorNames = pids.isEmpty()
              ? Collections.emptyMap()
              : userDAO.getFullNamesByIds(pids);

      boolean showAgenda = isHead || PermissionUtil.hasFeatureCode(me, "AGD");

      req.setAttribute("items", items);
      req.setAttribute("creatorNames", creatorNames);
      req.setAttribute("processorNames", processorNames);
      req.setAttribute("q", q==null? "" : q);
      req.setAttribute("page", page);
      req.setAttribute("totalPages", totalPages);
      req.setAttribute("showAgenda", showAgenda);
      req.getRequestDispatcher("/WEB-INF/views/request_subordinates.jsp").forward(req, resp);
    } catch (Exception ex) {
      req.setAttribute("error", ex.getMessage());
      req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
    }
  }
}
