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

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User me = (User) req.getSession().getAttribute("user");
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }

    // cần có 1 trong 2: REQ_MGR hoặc là Head
    if (!PermissionUtil.hasFeatureCode(me, "REQ_MGR") && !PermissionUtil.isDepartmentHead(me)) {
      req.setAttribute("items", List.of());
      req.setAttribute("creatorNames", Map.of());
      req.setAttribute("processorNames", Map.of());
      req.setAttribute("error", "Bạn không có quyền xem đơn cấp dưới.");
      req.getRequestDispatcher("/WEB-INF/views/request_subordinates.jsp").forward(req, resp);
      return;
    }

    try {
      List<Integer> targetUids;
      if (PermissionUtil.isDepartmentHead(me)) {
        // Cara (Head): cả phòng
        targetUids = employeeDAO.findDepartmentUserIds(me.getDepartmentId());
      } else {
        // Manager: toàn bộ cây cấp dưới
        targetUids = employeeDAO.findAllReportUserIds(me.getId());
      }
      // không liệt kê chính mình
      targetUids.removeIf(id -> id == me.getId());

      List<Request> items = targetUids.isEmpty() ? List.of() : requestDAO.listByCreators(targetUids);

      // gom tên (người tạo & người xử lý)
      Set<Integer> nameIds = new HashSet<>(targetUids);
      for (Request r : items) if (r.getProcessedBy()!=null) nameIds.add(r.getProcessedBy());
      Map<Integer,String> names = nameIds.isEmpty() ? Map.of() : userDAO.getFullNamesByIds(nameIds);

      Map<Integer,String> creatorNames = new HashMap<>();
      Map<Integer,String> processorNames = new HashMap<>();
      for (Request r : items) {
        creatorNames.put(r.getCreatedBy(), names.get(r.getCreatedBy()));
        if (r.getProcessedBy()!=null) processorNames.put(r.getProcessedBy(), names.get(r.getProcessedBy()));
      }

      req.setAttribute("items", items);
      req.setAttribute("creatorNames", creatorNames);
      req.setAttribute("processorNames", processorNames);
      req.getRequestDispatcher("/WEB-INF/views/request_subordinates.jsp").forward(req, resp);
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }
}