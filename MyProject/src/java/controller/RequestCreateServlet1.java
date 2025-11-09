package controller;

import dal.HistoryDAO;
import dal.PermissionUtil;
import dal.RequestDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import model.RequestStatus;
import model.User;

@WebServlet(name="RequestCreateServlet1", urlPatterns={"/requestcreateservlet1"})
public class RequestCreateServlet1 extends HttpServlet {

  private final RequestDAO requestDAO = new RequestDAO();
  private final UserDAO    userDAO    = new UserDAO();
  private final HistoryDAO historyDAO = new HistoryDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User me = (User) req.getSession().getAttribute("user");
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }
    PermissionUtil.ensureFeatures(me, userDAO);

    String roleName = (me.getRoles()==null || me.getRoles().isEmpty())
                      ? "" : me.getRoles().iterator().next().getName();
    String depName  = "";
    try {
      if (me.getDepartmentId()!=null) {
        String dn = userDAO.getDepartmentNameById(me.getDepartmentId());
        depName = (dn==null) ? "" : dn;
      }
    } catch (Exception ignore) {}

    req.setAttribute("roleName", roleName);
    req.setAttribute("depName",  depName);
    req.getRequestDispatcher("/WEB-INF/views/request_create.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    User me = (User) req.getSession().getAttribute("user");
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }
    PermissionUtil.ensureFeatures(me, userDAO);

    String sFrom  = req.getParameter("from");
    String sTo    = req.getParameter("to");
    String reason = req.getParameter("reason");

    try {
      if (reason == null || reason.trim().isEmpty())
        throw new IllegalArgumentException("Vui lòng nhập lý do.");

      LocalDate from = LocalDate.parse(sFrom);
      LocalDate to   = LocalDate.parse(sTo);
      if (to.isBefore(from))
        throw new IllegalArgumentException("Tới ngày phải ≥ Từ ngày.");

      String title = "Nghỉ phép " + from + " → " + to;

      // tạo NEW
      int newId = requestDAO.create(me.getId(), from, to, title, reason.trim());

      // Head + có quyền approve -> auto-approve (không ghi ProcessedNote)
      boolean canAuto = PermissionUtil.isDepartmentHead(me, userDAO)
                     && PermissionUtil.hasFeatureCode(me, "REQ_APPROVE");
      if (canAuto) {
        requestDAO.updateStatus(newId, RequestStatus.APPROVED, me.getId(), null);
        try {
          historyDAO.add(newId, me.getId(), RequestStatus.NEW, RequestStatus.APPROVED, "Auto-approved");
        } catch (Exception ignore) {}
      }

      resp.sendRedirect(req.getContextPath()+"/requestlistmyservlet1");
    } catch (Exception ex) {
      req.setAttribute("error", ex.getMessage());

      String roleName = (me.getRoles()==null || me.getRoles().isEmpty())
                        ? "" : me.getRoles().iterator().next().getName();
      req.setAttribute("roleName", roleName);
      try {
        String depName = (me.getDepartmentId()!=null)
            ? userDAO.getDepartmentNameById(me.getDepartmentId()) : "";
        req.setAttribute("depName", depName==null? "" : depName);
      } catch (Exception ignore) { req.setAttribute("depName",""); }

      req.setAttribute("from", sFrom);
      req.setAttribute("to", sTo);
      req.setAttribute("reason", reason);
      req.getRequestDispatcher("/WEB-INF/views/request_create.jsp").forward(req, resp);
    }
  }
}
