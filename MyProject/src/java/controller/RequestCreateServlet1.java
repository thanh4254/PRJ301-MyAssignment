package controller;

import dal.RequestDAO;
import dal.UserDAO;
import dal.PermissionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import model.Request;
import model.RequestStatus;
import model.User;

@WebServlet(name="RequestCreateServlet1", urlPatterns={"/requestcreateservlet1"})
public class RequestCreateServlet1 extends HttpServlet {
  private final RequestDAO requestDAO = new RequestDAO();
  private final UserDAO    userDAO    = new UserDAO();

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User u = (User) req.getSession().getAttribute("user");
    if (u == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }
    if (!PermissionUtil.hasFeatureCode(u, "REQ_CREATE")) {
      req.setAttribute("error", "Bạn không có quyền tạo đơn.");
      req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
      return;
    }
    try {
      String roleName = (u.getRoles()!=null && !u.getRoles().isEmpty())
          ? u.getRoles().iterator().next().getName() : "—";
      String depName  = userDAO.getDepartmentName(u.getDepartmentId());
      req.setAttribute("roleName", roleName);
      req.setAttribute("depName",  depName);
    } catch (Exception ignore) {}
    req.getRequestDispatcher("/WEB-INF/views/request_create.jsp").forward(req, resp);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User u = (User) req.getSession().getAttribute("user");
    if (u == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }
    if (!PermissionUtil.hasFeatureCode(u, "REQ_CREATE")) {
      req.setAttribute("error", "Bạn không có quyền tạo đơn.");
      req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
      return;
    }

    try {
      LocalDate from   = LocalDate.parse(req.getParameter("from"));
      LocalDate to     = LocalDate.parse(req.getParameter("to"));
      String reason    = req.getParameter("reason");
      if (from.isAfter(to)) throw new IllegalArgumentException("Khoảng ngày không hợp lệ.");

      // không cho trùng với kỳ đã APPROVED
      if (requestDAO.existsApprovedOverlap(u.getId(), from, to)) {
        req.setAttribute("error", "Trùng khoảng với kỳ nghỉ đã được duyệt trước đó.");
        doGet(req, resp); return;
      }

      Request r = new Request();
      r.setTitle("Nghỉ phép " + from + " → " + to);
      r.setFrom(from);
      r.setTo(to);
      r.setReason(reason);
      r.setCreatedBy(u.getId());

      // DB/enum dùng NEW/APPROVED/REJECTED ⇒ set NEW khi tạo
      r.setStatus(RequestStatus.fromDbString("NEW"));

      requestDAO.insert(r);
      resp.sendRedirect(req.getContextPath()+"/requestlistmyservlet1");
    } catch (Exception e) {
      req.setAttribute("error", e.getMessage());
      doGet(req, resp);
    }
  }
}