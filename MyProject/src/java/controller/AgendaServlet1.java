package controller;

import dal.EmployeeDAO;
import dal.PermissionUtil;
import dal.RequestDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import model.Request;
import model.RequestStatus;
import model.User;

@WebServlet(name="AgendaServlet1", urlPatterns={"/agendaservlet1"})
public class AgendaServlet1 extends HttpServlet {
  private final EmployeeDAO employeeDAO = new EmployeeDAO();
  private final RequestDAO  requestDAO  = new RequestDAO();
  private final UserDAO     userDAO     = new UserDAO();

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    User me = (User) req.getSession().getAttribute("user");
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }

    // make sure đã có đủ feature trong session
    PermissionUtil.ensureFeatures(me, userDAO);

    if (!PermissionUtil.hasFeatureCode(me, "AGD")) {
      req.setAttribute("error", "Bạn không có quyền xem agenda.");
      req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
      return;
    }

    LocalDate today = LocalDate.now();
    LocalDate from = parseOrDefault(req.getParameter("from"), today.withDayOfMonth(1));
    LocalDate to   = parseOrDefault(req.getParameter("to"),   today.withDayOfMonth(today.lengthOfMonth()));
    if (from.isAfter(to)) { LocalDate t = from; from = to; to = t; }

    try {
      // 1) Tập user cần hiển thị
      List<Integer> uids;
      if (PermissionUtil.isDepartmentHead(me, userDAO)) {
        Integer depId = me.getDepartmentId();
        uids = (depId == null) ? new ArrayList<>() : employeeDAO.findDepartmentUserIds(depId);
      } else {
        uids = new ArrayList<>();
        uids.add(me.getId());
        uids.addAll(employeeDAO.findAllReportUserIds(me.getId()));
      }
      if (uids.isEmpty()) uids = List.of(-1); // tránh IN () rỗng

      // 2) Danh sách user (cột trái)
      List<User> members = fetchUsersByIds(uids);

      // 3) Các request APPROVED giao với from..to
      List<Request> reqs = requestDAO.listByCreators(uids);
      List<Request> approved = new ArrayList<>();
      for (Request r : reqs) {
        if (r.getStatus() == RequestStatus.APPROVED
            && !(r.getTo().isBefore(from) || r.getFrom().isAfter(to))) {
          approved.add(r);
        }
      }

      // 4) Map userId -> set ngày nghỉ
      Map<Integer, Set<String>> offByUser = new HashMap<>();
      for (Request r : approved) {
        LocalDate s = r.getFrom().isBefore(from) ? from : r.getFrom();
        LocalDate e = r.getTo().isAfter(to) ? to : r.getTo();
        Set<String> set = offByUser.computeIfAbsent(r.getCreatedBy(), k -> new HashSet<>());
        for (LocalDate d = s; !d.isAfter(e); d = d.plusDays(1)) set.add(d.toString());
      }

      // 5) Dải ngày hiển thị
      List<LocalDate> days = new ArrayList<>();
      for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) days.add(d);

      req.setAttribute("members", members);
      req.setAttribute("days", days);
      req.setAttribute("offByUser", offByUser);
      req.getRequestDispatcher("/WEB-INF/views/agenda.jsp").forward(req, resp);
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  private List<User> fetchUsersByIds(List<Integer> ids) throws Exception {
    List<User> out = new ArrayList<>();
    for (Integer id : ids) {
      User u = userDAO.findById(id);
      if (u != null) out.add(u);
    }
    out.sort(Comparator.comparing(
        u -> Optional.ofNullable(u.getFullName()).orElse(""), String.CASE_INSENSITIVE_ORDER));
    return out;
  }

  private LocalDate parseOrDefault(String s, LocalDate def) {
    try { return (s == null || s.isBlank()) ? def : LocalDate.parse(s); }
    catch (Exception ignored) { return def; }
  }
}
