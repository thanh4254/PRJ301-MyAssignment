package controller;

import dal.RequestDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import model.Request;
import model.RequestStatus;
import model.User;

@WebServlet(name="RequestCreateServlet1", urlPatterns={"/requestcreateservlet1"})
public class RequestCreateServlet1 extends HttpServlet {

  private final RequestDAO requestDAO = new RequestDAO();
  private final UserDAO userDAO = new UserDAO(); // dùng để lấy tên phòng ban nếu muốn

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // nếu muốn hiển thị tên phòng ban:
    try {
      User me = (User) req.getSession().getAttribute("user");
      if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }
      String depName = userDAO.getDepartmentName(me.getDepartmentId());
      req.setAttribute("departmentName", depName);
    } catch (Exception ignore) {}
    req.getRequestDispatcher("/WEB-INF/views/request_create.jsp").forward(req, resp);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");
    User me = (User) req.getSession().getAttribute("user");
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }

    String fromStr = trim(req.getParameter("from"));
    String toStr   = trim(req.getParameter("to"));
    String reason  = trim(req.getParameter("reason"));

    // Validate đơn giản
    if (fromStr == null || toStr == null || reason == null ||
        fromStr.isEmpty() || toStr.isEmpty() || reason.isEmpty()) {
      req.setAttribute("error", "Vui lòng nhập đầy đủ Từ ngày, Tới ngày và Lý do.");
      doGet(req, resp);
      return;
    }

    LocalDate fromDate, toDate;
    try {
      fromDate = LocalDate.parse(fromStr);
      toDate   = LocalDate.parse(toStr);
    } catch (DateTimeParseException e) {
      req.setAttribute("error", "Định dạng ngày không hợp lệ.");
      doGet(req, resp);
      return;
    }
    if (toDate.isBefore(fromDate)) {
      req.setAttribute("error", "Tới ngày phải >= Từ ngày.");
      doGet(req, resp);
      return;
    }

    // Tạo tiêu đề gợi nhớ: "Nghỉ phép yyyy-MM-dd → yyyy-MM-dd"
    String title = "Nghỉ phép " + fromDate + " → " + toDate;

    try {
      // ===== Cách 1: nếu DAO có hàm create(...) =====
      // requestDAO.create(title, fromDate, toDate, reason, me.getId(), RequestStatus.NEW);

      // ===== Cách 2: set vào model rồi insert =====
      Request r = new Request();
      r.setTitle(title);
      r.setFrom(fromDate);
      r.setTo(toDate);
      r.setReason(reason);             // QUAN TRỌNG: không để null
      r.setCreatedBy(me.getId());
      r.setStatus(RequestStatus.NEW);  // trạng thái khởi tạo
      requestDAO.insert(r);

      resp.sendRedirect(req.getContextPath()+"/requestlistmyservlet1");
    } catch (Exception e) {
      e.printStackTrace();
      req.setAttribute("error", "Không thể gửi đơn: " + e.getMessage());
      doGet(req, resp);
    }
  }

  private static String trim(String s){ return s==null? null : s.trim(); }
}