package controller;

import dal.RequestDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import model.Request;
import model.User;

@WebServlet(name="RequestSubordinatesServlet1", urlPatterns={"/requestsubordinatesservlet1"})
public class RequestSubordinatesServlet1 extends HttpServlet {

  private final RequestDAO requestDAO = new RequestDAO();
  private final UserDAO    userDAO    = new UserDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    User me = (User) req.getSession().getAttribute("user");
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }

    final int PAGE_SIZE = 5;

    // -------- Query params
    String q = req.getParameter("q");
    if (q != null) q = q.trim();
    int page = 1;
    try { page = Math.max(1, Integer.parseInt(req.getParameter("page"))); } catch (Exception ignore) {}
    int offset = (page - 1) * PAGE_SIZE;

    try {
      // 1) Lấy toàn bộ cấp dưới của user hiện tại (trực tiếp + gián tiếp)
      //   NOTE: Nếu bạn đang dùng tên hàm khác, đổi lại cho khớp DAO của bạn
      List<Integer> creators = userDAO.getUserIdsByManager(me.getId()); // trả về list UID cấp dưới
      if (creators == null) creators = Collections.emptyList();

      // 2) Nếu có từ khóa, lọc theo tên "Created By"
      List<Integer> filtered = creators;
      if (q != null && !q.isEmpty()) {
        Map<Integer,String> nameMap = userDAO.getFullNamesByIds(new HashSet<>(creators));
        String qLower = q.toLowerCase(Locale.ROOT);
        filtered = new ArrayList<>();
        for (Integer uid : creators) {
          String nm = nameMap.get(uid);
          if (nm != null && nm.toLowerCase(Locale.ROOT).contains(qLower)) {
            filtered.add(uid);
          }
        }
      }

      // 3) Đếm & lấy trang
      int total = requestDAO.countByCreators(filtered);
      int totalPages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
      if (page > totalPages) { page = totalPages; offset = (page - 1) * PAGE_SIZE; }

      List<Request> items = requestDAO.listByCreatorsPaged(filtered, offset, PAGE_SIZE);

      // 4) Map tên người tạo và người xử lý để render bảng
      //    - tên creator: nên lấy theo toàn bộ "filtered" để không thiếu
      Map<Integer,String> creatorNames = userDAO.getFullNamesByIds(new HashSet<>(filtered));

      //    - tên processor: gom các id xuất hiện trong page
      Set<Integer> pids = new HashSet<>();
      for (Request r : items) if (r.getProcessedBy() != null) pids.add(r.getProcessedBy());
      Map<Integer,String> processorNames = pids.isEmpty()
          ? Collections.emptyMap()
          : userDAO.getFullNamesByIds(pids);

      // 5) Gán attribute & forward
      req.setAttribute("items", items);
      req.setAttribute("creatorNames", creatorNames);
      req.setAttribute("processorNames", processorNames);

      req.setAttribute("q", q);                 // giữ lại ô search
      req.setAttribute("page", page);           // phân trang
      req.setAttribute("totalPages", totalPages);

      req.getRequestDispatcher("/WEB-INF/views/request_subordinates.jsp").forward(req, resp);

    } catch (Exception ex) {
      req.setAttribute("error", ex.getMessage());
      req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
    }
  }
}
