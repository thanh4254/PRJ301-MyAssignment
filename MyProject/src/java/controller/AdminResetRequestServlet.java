package controller;

import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import model.User;

@WebServlet(name="AdminResetRequestServlet", urlPatterns={"/admin/reset-requests"})
public class AdminResetRequestServlet extends HttpServlet {
  private final UserDAO userDAO = new UserDAO();

  private boolean isAdmin(User u){
    if (u == null || u.getRoles()==null) return false;
    return u.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getCode()));
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User me = (User) req.getSession().getAttribute("user");
    if (!isAdmin(me)) { resp.sendError(403); return; }

    try {
      List<Map<String,Object>> pending = userDAO.listPendingResetRequests();
      req.setAttribute("items", pending);
    } catch (Exception e) {
      req.setAttribute("error", e.getMessage());
    }
    req.getRequestDispatcher("/WEB-INF/views/admin_reset_list.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User me = (User) req.getSession().getAttribute("user");
    if (!isAdmin(me)) { resp.sendError(403); return; }

    String action = req.getParameter("action");
    int id = Integer.parseInt(req.getParameter("id"));
    try {
      if ("deny".equals(action)) {
        userDAO.denyReset(id, me.getId());
        req.setAttribute("flash", "Đã từ chối yêu cầu #" + id);
      } else if ("approve".equals(action)) {
        String token = userDAO.approveReset(id, me.getId(), 30); // 30 phút
        String link = req.getContextPath() + "/reset-password?token=" + token;
        req.setAttribute("flash", "ĐÃ DUYỆT #" + id + ". Gửi link cho người dùng: " + link);
      }
    } catch (Exception e) {
      req.setAttribute("error", e.getMessage());
    }
    doGet(req, resp);
  }
}
