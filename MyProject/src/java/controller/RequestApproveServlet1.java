package controller;

import dal.EmployeeDAO;
import dal.HistoryDAO;
import dal.PermissionUtil;
import dal.RequestDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import model.Request;
import model.RequestStatus;
import model.User;

@WebServlet(
    name = "RequestApproveServlet1",
    urlPatterns = {"/requestapproveservlet1", "/requestrejectservlet1"}
)
public class RequestApproveServlet1 extends HttpServlet {

  private final RequestDAO  requestDAO = new RequestDAO();
  private final HistoryDAO  historyDAO = new HistoryDAO();
  private final UserDAO     userDAO    = new UserDAO();
  private final EmployeeDAO empDAO     = new EmployeeDAO();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    User me = (User) req.getSession().getAttribute("user");
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }

    // đảm bảo user trong session đã có đầy đủ feature
    PermissionUtil.ensureFeatures(me, userDAO);

    final String spath    = req.getServletPath();
    final boolean approve = "/requestapproveservlet1".equalsIgnoreCase(spath);
    final RequestStatus target = approve ? RequestStatus.APPROVED : RequestStatus.REJECTED;

    final boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))
                        || "1".equals(req.getParameter("ajax"));

    try {
      if (!PermissionUtil.hasFeatureCode(me, "REQ_APPROVE"))
        throw new SecurityException("Bạn không có quyền duyệt đơn.");

      int id = Integer.parseInt(req.getParameter("id").trim());
      String note = req.getParameter("note");
      if (note != null) note = note.trim();

      Request r = requestDAO.findById(id);
      if (r == null) throw new IllegalArgumentException("Không tìm thấy đơn.");

      if (!PermissionUtil.canProcess(me, r.getCreatedBy(), userDAO, empDAO))
        throw new SecurityException("Bạn không có quyền xử lý đơn này.");

      if (r.getStatus() == RequestStatus.CANCELLED)
        throw new IllegalStateException("Đơn đã bị hủy.");
      if (r.getStatus() == target)
        throw new IllegalStateException("Đơn đã ở trạng thái " + target.name().toLowerCase() + ".");

      requestDAO.updateStatus(id, target, me.getId(), note);
      historyDAO.add(id, me.getId(), r.getStatus(), target, note);

      if (isAjax) {
        resp.setContentType("application/json; charset=UTF-8");
        String pName = userDAO.getFullNamesByIds(java.util.Set.of(me.getId())).get(me.getId());
        if (pName == null) pName = me.getUsername();
        resp.getWriter().write("{\"ok\":true,\"id\":"+id+",\"status\":\""+target.name()
            +"\",\"processorName\":\""+pName.replace("\"","\\\"")+"\"}");
        return;
      }

      resp.sendRedirect(req.getContextPath()+"/requestsubordinatesservlet1");

    } catch (Exception ex) {
      if (isAjax) {
        resp.setStatus(400);
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write("{\"ok\":false,\"message\":\""+ex.getMessage().replace("\"","\\\"")+"\"}");
      } else {
        req.setAttribute("error", ex.getMessage());
        req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
      }
    }
  }
}
