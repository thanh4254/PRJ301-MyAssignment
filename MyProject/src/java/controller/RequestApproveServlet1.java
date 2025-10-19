package controller;

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

@WebServlet(name="RequestApproveServlet1",
  urlPatterns={"/requestapproveservlet1","/requestrejectservlet1"})
public class RequestApproveServlet1 extends HttpServlet {
  private final RequestDAO requestDAO = new RequestDAO();
  private final HistoryDAO historyDAO = new HistoryDAO();
  private final UserDAO    userDAO    = new UserDAO();

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User me = (User) req.getSession().getAttribute("user");
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }
    if (!PermissionUtil.hasFeatureCode(me, "REQ_APPROVE")) {
      req.setAttribute("error", "Bạn không có quyền duyệt đơn.");
      req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp); return;
    }

    RequestStatus target = req.getRequestURI().toLowerCase().endsWith("/requestapproveservlet1")
        ? RequestStatus.APPROVED : RequestStatus.REJECTED;

    try {
      int id = Integer.parseInt(req.getParameter("id"));
      String note = req.getParameter("note");

      Request r = requestDAO.findById(id);
      if (r == null) throw new IllegalArgumentException("Không tìm thấy đơn.");

      if (!PermissionUtil.canProcess(me, r.getCreatedBy(), userDAO)) {
        throw new SecurityException("Bạn không có quyền xử lý đơn này.");
      }

      requestDAO.updateStatus(id, target, me.getId(), note);
      historyDAO.add(r.getId(), me.getId(), r.getStatus(), target, note);

      resp.sendRedirect(req.getContextPath()+"/requestsubordinatesservlet1");
    } catch (Exception e) {
      req.setAttribute("error", e.getMessage());
      req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
    }
  }
}
