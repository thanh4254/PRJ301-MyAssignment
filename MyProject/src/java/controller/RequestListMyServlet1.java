package controller;

import dal.PermissionUtil;
import dal.RequestDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import model.User;
import model.Request;

@WebServlet(name="RequestListMyServlet1", urlPatterns={"/requestlistmyservlet1"})
public class RequestListMyServlet1 extends HttpServlet {

  private static final int PAGE_SIZE = 5;

  private final RequestDAO requestDAO = new RequestDAO();
  private final UserDAO    userDAO    = new UserDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    User me = (User) req.getSession().getAttribute("user");
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }

    int page;
    try { page = Math.max(1, Integer.parseInt(req.getParameter("page"))); }
    catch (Exception ignore) { page = 1; }
    int offset = (page - 1) * PAGE_SIZE;

    try {
      var items = requestDAO.listMyPaged(me.getId(), offset, PAGE_SIZE);
      int total = requestDAO.countMy(me.getId());
      int totalPages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);

      // map tên người xử lý
      Set<Integer> pids = new HashSet<>();
      for (var r : items) if (r.getProcessedBy()!=null) pids.add(r.getProcessedBy());
      Map<Integer,String> names = pids.isEmpty()
              ? Collections.emptyMap()
              : userDAO.getFullNamesByIds(pids);

      // chỉ Head (DB) hoặc có feature REQ_HEAD/AGD thì hiện nút Agenda
      boolean showAgenda = PermissionUtil.isDepartmentHead(me, userDAO)
                        || PermissionUtil.hasFeatureCode(me, "REQ_HEAD")
                        || PermissionUtil.hasFeatureCode(me, "AGD");

      req.setAttribute("showAgenda", showAgenda);
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
