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
import model.RequestStatus;

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

      // tên người tạo / người xử lý
      Set<Integer> ids = new HashSet<>();
      ids.add(r.getCreatedBy());
      if (r.getProcessedBy()!=null) ids.add(r.getProcessedBy());
      Map<Integer,String> names = userDAO.getFullNamesByIds(ids);

      // Chỉ hiển thị người duyệt khi đơn đã được xử lý (khác NEW)
      String approverName = "";
      String approverRole = "";
      if (r.getProcessedBy()!=null && r.getStatus() != RequestStatus.NEW) {
        approverName = names.getOrDefault(r.getProcessedBy(), String.valueOf(r.getProcessedBy()));
        // role của người duyệt không cần quá chuẩn xác -> lấy role đầu tiên
        User approver = userDAO.findById(r.getProcessedBy());
        if (approver != null && approver.getRoles()!=null && !approver.getRoles().isEmpty()) {
          approverRole = approver.getRoles().iterator().next().getName();
        }
      }

      // Chỉ cho duyệt khi: có quyền + là người có thẩm quyền với nhân viên tạo + đơn đang NEW + không phải đơn của chính mình
      boolean canApprove = PermissionUtil.hasFeatureCode(me, "REQ_APPROVE")
          && PermissionUtil.canProcess(me, r.getCreatedBy(), userDAO)
          && r.getStatus() == RequestStatus.NEW
          && me.getId() != r.getCreatedBy();

      req.setAttribute("item", r);
      req.setAttribute("names", names);
      req.setAttribute("approverName", approverName);
      req.setAttribute("approverRole", approverRole);
      req.setAttribute("creatorName",
          names.getOrDefault(r.getCreatedBy(), String.valueOf(r.getCreatedBy())));
      req.setAttribute("canApprove", canApprove);

      req.getRequestDispatcher("/WEB-INF/views/request_detail.jsp").forward(req, resp);
    } catch (Exception e) {
      req.setAttribute("error", e.getMessage());
      req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
    }
  }
}