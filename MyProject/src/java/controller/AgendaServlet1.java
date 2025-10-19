package controller;

import dal.PermissionUtil;
import dal.RequestDAO;
import dal.EmployeeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import model.Request;
import model.User;
import model.EmployeeView;

@WebServlet(name="AgendaServlet1", urlPatterns={"/agendaservlet1"})
public class AgendaServlet1 extends HttpServlet {
  private final EmployeeDAO employeeDAO = new EmployeeDAO();
  private final RequestDAO  requestDAO  = new RequestDAO();

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User head = (User) req.getSession().getAttribute("user");
    if (head == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }
    if (!PermissionUtil.hasFeatureCode(head, "AGD")) {
      req.setAttribute("error", "Bạn không có quyền xem agenda.");
      req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp); return;
    }

    LocalDate today = LocalDate.now();
    LocalDate from = parseOrDefault(req.getParameter("from"), today.withDayOfMonth(1));
    LocalDate to   = parseOrDefault(req.getParameter("to"),   today.withDayOfMonth(today.lengthOfMonth()));
    if (from.isAfter(to)) { LocalDate t = from; from = to; to = t; }

    try {
      // nhân sự phòng của trưởng phòng (VIEW Employee)
      List<EmployeeView> emps = employeeDAO.listByDepartment(head.getDepartmentId());
      List<Integer> uids = new ArrayList<>();
      for (EmployeeView e : emps) if (e.getUserId()!=null) uids.add(e.getUserId());

      // các đơn đã APPROVED giao với khoảng
      List<Request> approved = requestDAO.listApprovedByCreatorsInRange(uids, from, to);
      Map<Integer, Set<LocalDate>> off = new HashMap<>();
      for (Request r : approved) {
        LocalDate s = r.getFrom().isBefore(from)? from : r.getFrom();
        LocalDate e = r.getTo().isAfter(to)? to : r.getTo();
        Set<LocalDate> set = off.computeIfAbsent(r.getCreatedBy(), k -> new HashSet<>());
        for (LocalDate d = s; !d.isAfter(e); d = d.plusDays(1)) set.add(d);
      }

      List<LocalDate> days = new ArrayList<>();
      for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) days.add(d);

      req.setAttribute("emps", emps);
      req.setAttribute("days", days);
      req.setAttribute("off", off);
      req.setAttribute("from", from);
      req.setAttribute("to", to);

      req.getRequestDispatcher("/WEB-INF/views/agenda.jsp").forward(req, resp);
    } catch (Exception e) { throw new ServletException(e); }
  }

  private LocalDate parseOrDefault(String s, LocalDate def) {
    try { return (s==null||s.isBlank())? def : LocalDate.parse(s); }
    catch(Exception ex){ return def; }
  }
}