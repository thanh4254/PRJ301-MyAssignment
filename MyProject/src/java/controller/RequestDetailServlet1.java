package controller;

import dal.RequestDAO;
import dal.UserDAO;
import dal.PermissionUtil;
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
  private final UserDAO    userDAO    = new UserDAO();

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User me = (User) req.getSession().getAttribute("user");
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }

    try {
      int id = Integer.parseInt(req.getParameter("id"));
      Request r = requestDAO.findById(id);
      if (r == null) throw new IllegalArgumentException("Không tìm thấy đơn.");

      Set<Integer> set = new HashSet<>();
      set.add(r.getCreatedBy());
      if (r.getProcessedBy()!=null) set.add(r.getProcessedBy());
      Map<Integer,String> names = userDAO.getFullNamesByIds(set);

      boolean canApprove = PermissionUtil.hasFeatureCode(me, "REQ_APPROVE")
          && PermissionUtil.canProcess(me, r.getCreatedBy(), userDAO);

      req.setAttribute("item", r);
      req.setAttribute("names", names);
      req.setAttribute("canApprove", canApprove);
      req.getRequestDispatcher("/WEB-INF/views/request_detail.jsp").forward(req, resp);
    } catch (Exception e) {
      req.setAttribute("error", e.getMessage());
      req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
    }
  }
}

