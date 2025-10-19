package controller;

import dal.PermissionUtil;
import dal.RequestDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import model.Request;
import model.User;

@WebServlet(name="RequestDetailServlet1", urlPatterns={"/requestdetailservlet1"})
public class RequestDetailServlet1 extends HttpServlet {
  private final RequestDAO requestDAO = new RequestDAO();
  private final UserDAO userDAO = new UserDAO();

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User me = (User) req.getSession().getAttribute("user");
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }

    try {
      int id = Integer.parseInt(req.getParameter("id"));
      Request r = requestDAO.findById(id);
      if (r == null) throw new IllegalArgumentException("Không tìm thấy đơn.");

      // Tên người tạo / người xử lý (nếu có)
      Set<Integer> ids = new HashSet<>();
      ids.add(r.getCreatedBy());
      if (r.getProcessedBy()!=null) ids.add(r.getProcessedBy());
      Map<Integer,String> names = userDAO.getFullNamesByIds(ids);

      // thông tin người duyệt (chính là người đang đăng nhập)
      String approverName = me.getFullName();
      String approverRole = "—";
      if (me.getRoles()!=null && !me.getRoles().isEmpty())
        approverRole = me.getRoles().iterator().next().getName();

      // kiểm tra có được phép duyệt không
      boolean canApprove = false;
      if (PermissionUtil.hasFeatureCode(me, "REQ_APPROVE")) {
        // được duyệt nếu là quản lý trực tiếp hoặc là Head của phòng người tạo
        if (PermissionUtil.canProcess(me, r.getCreatedBy(), userDAO)) {
          canApprove = true;
        }
      }

      req.setAttribute("item", r);
      req.setAttribute("names", names);
      req.setAttribute("approverName", approverName);
      req.setAttribute("approverRole", approverRole);
      req.setAttribute("creatorName", names.getOrDefault(r.getCreatedBy(), String.valueOf(r.getCreatedBy())));
      req.setAttribute("canApprove", canApprove);

      req.getRequestDispatcher("/WEB-INF/views/request_detail.jsp").forward(req, resp);
    } catch (Exception e) {
      req.setAttribute("error", e.getMessage());
      req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
    }
  }
}

