package controller;

import dal.HistoryDAO;
import dal.PermissionUtil;
import dal.RequestDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import model.Request;
import model.RequestStatus;
import model.User;

@WebServlet(
    name = "RequestApproveServlet1",
    urlPatterns = {"/requestapproveservlet1", "/requestrejectservlet1"}
)
public class RequestApproveServlet1 extends HttpServlet {

  private final RequestDAO requestDAO = new RequestDAO();
  private final HistoryDAO historyDAO = new HistoryDAO();
  private final UserDAO    userDAO    = new UserDAO();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");

    // 1) Xác thực phiên đăng nhập
    User me = (User) req.getSession().getAttribute("user");
    if (me == null) {
      resp.sendRedirect(req.getContextPath() + "/loginservlet1");
      return;
    }

    // 2) Phân biệt approve / reject từ servlet path
    final String spath = req.getServletPath(); // "/requestapproveservlet1" or "/requestrejectservlet1"
    final boolean isApprove = "/requestapproveservlet1".equalsIgnoreCase(spath);
    final RequestStatus target = isApprove ? RequestStatus.APPROVED : RequestStatus.REJECTED;

    try {
      // 3) Quyền tổng: cần feature REQ_APPROVE
      if (!PermissionUtil.hasFeatureCode(me, "REQ_APPROVE")) {
        throw new SecurityException("Bạn không có quyền duyệt đơn.");
      }

      // 4) Đọc tham số
      String idRaw = req.getParameter("id");
      if (idRaw == null || idRaw.isBlank()) {
        throw new IllegalArgumentException("Thiếu tham số id.");
      }
      int id = Integer.parseInt(idRaw.trim());
      String note = req.getParameter("note");
      if (note != null) note = note.trim();

      // 5) Lấy đơn & kiểm tra
      Request r = requestDAO.findById(id);
      if (r == null) {
        throw new IllegalArgumentException("Không tìm thấy đơn.");
      }

      // Chỉ cho xử lý khi là đơn của cấp dưới hợp lệ theo rule của bạn
      if (!PermissionUtil.canProcess(me, r.getCreatedBy(), userDAO)) {
        throw new SecurityException("Bạn không có quyền xử lý đơn này.");
      }

      // Không cho xử lý lại khi đã có trạng thái cuối
      RequestStatus oldStatus = r.getStatus();
      if (oldStatus == RequestStatus.APPROVED
          || oldStatus == RequestStatus.REJECTED
          || oldStatus == RequestStatus.CANCELLED) {
        throw new IllegalStateException("Đơn đã được xử lý trước đó.");
      }

      // 6) Cập nhật & ghi lịch sử
      requestDAO.updateStatus(id, target, me.getId(), note);
      historyDAO.add(id, me.getId(), oldStatus, target, note);

      // 7) Quay về danh sách
      resp.sendRedirect(req.getContextPath() + "/requestsubordinatesservlet1");

    } catch (NumberFormatException nfe) {
      forwardError(req, resp, "Mã đơn không hợp lệ.");
    } catch (SecurityException | IllegalArgumentException | IllegalStateException ex) {
      forwardError(req, resp, ex.getMessage());
    } catch (Exception ex) {
      ex.printStackTrace();
      forwardError(req, resp, "Có lỗi xảy ra khi xử lý: " + ex.getMessage());
    }
  }

  private void forwardError(HttpServletRequest req, HttpServletResponse resp, String msg)
      throws ServletException, IOException {
    req.setAttribute("error", msg);
    req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
  }
}