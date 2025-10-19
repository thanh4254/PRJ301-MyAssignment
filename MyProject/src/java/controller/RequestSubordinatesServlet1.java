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

@WebServlet(name="RequestSubordinatesServlet1", urlPatterns={"/requestsubordinatesservlet1"})
public class RequestSubordinatesServlet1 extends HttpServlet {
  private final EmployeeDAO employeeDAO = new EmployeeDAO();
  private final RequestDAO  requestDAO  = new RequestDAO();
  private final UserDAO     userDAO     = new UserDAO();

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User me = (User) req.getSession().getAttribute("user");
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }
    if (!PermissionUtil.hasFeatureCode(me, "REQ_MGR")) {
      req.setAttribute("error", "Bạn không có quyền xem đơn cấp dưới.");
      req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp); return;
    }
    try {
      // uids cấp dưới trực tiếp theo VIEW Employee/Enrollment
      List<Integer> subUids = employeeDAO.findSubordinateUserIds(me.getId());
      var items = requestDAO.listByCreators(subUids);

      Set<Integer> ids = new HashSet<>(subUids);
      for (Request r : items) if (r.getProcessedBy()!=null) ids.add(r.getProcessedBy());
      Map<Integer,String> names = userDAO.getFullNamesByIds(ids);

      req.setAttribute("items", items);
      req.setAttribute("names", names);
      req.getRequestDispatcher("/WEB-INF/views/request_subordinates.jsp").forward(req, resp);
    } catch (Exception e) { throw new ServletException(e); }
  }
}
