package controller;

import dal.RequestDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import model.User;

@WebServlet(name="RequestCreateServlet1", urlPatterns={"/requestcreateservlet1"})
public class RequestCreateServlet1 extends HttpServlet {
  private final RequestDAO requestDAO = new RequestDAO();
  private final UserDAO    userDAO    = new UserDAO();

 @Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

  User me = (User) req.getSession().getAttribute("user");
  if (me == null) {
    resp.sendRedirect(req.getContextPath() + "/loginservlet1");
    return;
  }

  String roleName = "";
  if (me.getRoles() != null && !me.getRoles().isEmpty()) {
    roleName = me.getRoles().iterator().next().getName();
  }

  String depName = "";
  try {
    if (me.getDepartmentId() != null) {
      // phương thức này ở UserDAO bên dưới
      depName = new UserDAO().getDepartmentNameById(me.getDepartmentId());
      if (depName == null) depName = "";
    }
  } catch (Exception ignore) { /* để rỗng nếu có lỗi */ }

  req.setAttribute("roleName", roleName);
  req.setAttribute("depName", depName);

  req.getRequestDispatcher("/WEB-INF/views/request_create.jsp").forward(req, resp);
}

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");
    User me = (User) req.getSession().getAttribute("user");
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }

    String sFrom   = req.getParameter("from");
    String sTo     = req.getParameter("to");
    String reason  = req.getParameter("reason");

    try {
      if (reason == null || reason.trim().isEmpty())
        throw new IllegalArgumentException("Vui lòng nhập lý do.");

      LocalDate from = LocalDate.parse(sFrom);
      LocalDate to   = LocalDate.parse(sTo);
      if (to.isBefore(from))
        throw new IllegalArgumentException("Ngày kết thúc phải >= ngày bắt đầu.");

      String title = "Nghỉ phép " + from + " → " + to;

      requestDAO.create(me.getId(), from, to, title, reason.trim());

      resp.sendRedirect(req.getContextPath()+"/requestlistmyservlet1");
    } catch (Exception ex) {
      req.setAttribute("error", ex.getMessage());
      doGet(req, resp); // hiển thị lại form & lỗi
    }
  }
}