package controller;

import dal.HistoryDAO;
import dal.PermissionUtil;
import dal.RequestDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import model.RequestStatus;
import model.User;

@WebServlet(name = "RequestCreateServlet1", urlPatterns = {"/requestcreateservlet1"})
public class RequestCreateServlet1 extends HttpServlet {

  private final RequestDAO requestDAO = new RequestDAO();
  private final UserDAO    userDAO    = new UserDAO();
  private final HistoryDAO historyDAO = new HistoryDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    User me = (User) req.getSession().getAttribute("user");
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }

    // Role hiển thị
    String roleName = "";
    if (me.getRoles()!=null && !me.getRoles().isEmpty()) {
      roleName = me.getRoles().iterator().next().getName();
    }

    // Tên phòng ban hiển thị
    String depName = "";
    try {
      if (me.getDepartmentId()!=null) {
        String dn = userDAO.getDepartmentNameById(me.getDepartmentId());
        depName = (dn==null) ? "" : dn;
      }
    } catch (Exception ignore) { depName = ""; }

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

      // 1) Tạo đơn (trạng thái NEW, Reason = lý do người xin nghỉ)
      int newId = requestDAO.create(me.getId(), from, to, title, reason.trim());

      // 2) Nếu là “xếp tổng” của phòng mình và có quyền duyệt -> auto-approve (không lưu note)
      boolean isHead = false;
      try {
        isHead = (me.getDepartmentId()!=null)
              && userDAO.isDepartmentHead(me.getId(), me.getDepartmentId());
      } catch (Exception ignore) { isHead = false; }

      boolean canAuto = PermissionUtil.hasFeatureCode(me, "REQ_APPROVE") && isHead;
      if (canAuto) {
        // Không ghi ProcessedNote -> truyền null
        requestDAO.updateStatus(newId, RequestStatus.APPROVED, me.getId(), null);

        // Lịch sử vẫn ghi chú cho rõ (không ảnh hưởng cột Note của danh sách)
        try {
          historyDAO.add(newId, me.getId(), RequestStatus.NEW,
                         RequestStatus.APPROVED, "Auto-approved");
        } catch (Exception ignore) {}
      }

      resp.sendRedirect(req.getContextPath()+"/requestlistmyservlet1");
      return;

    } catch (Exception ex) {
      // Trả lại form + dữ liệu đã nhập
      req.setAttribute("error", ex.getMessage());

      String roleName = "";
      if (me.getRoles()!=null && !me.getRoles().isEmpty()) {
        roleName = me.getRoles().iterator().next().getName();
      }
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