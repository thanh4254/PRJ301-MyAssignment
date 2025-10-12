/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import dal.HistoryDAO;
import dal.RequestDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import model.Request;
import model.RequestStatus;
import model.User;
/**
 *
 * @author Admin
 */
@WebServlet(
    name="RequestApproveServlet1",
    urlPatterns={"/requestapproveservlet1", "/requestrejectservlet1"}
)
public class RequestApproveServlet1 extends HttpServlet {
    private final RequestDAO requestDAO = new RequestDAO();
    private final HistoryDAO historyDAO = new HistoryDAO();
    private final UserDAO userDAO = new UserDAO();

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet RequestApproveServlet1</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet RequestApproveServlet1 at " + request.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param req
     * @param resp
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
       User me = (User) req.getSession().getAttribute("user");
    if (me == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }

    String sId = req.getParameter("id");
    if (sId == null) { resp.sendError(400, "Missing id"); return; }

    try {
      int id = Integer.parseInt(sId);
      Request r = requestDAO.findById(id);
      if (r == null) { resp.sendError(404, "Request not found"); return; }

      // (tuỳ chọn) kiểm quyền: là head hoặc là quản lý trực tiếp của creator
      // nếu bạn đã có PermissionUtil.hasRightToProcess(...), gọi ở đây.

      User creator = userDAO.findById(r.getCreatedBy());
      String approverName = me.getFullName();
      String approverRole = (me.getRoles()!=null && !me.getRoles().isEmpty())
              ? me.getRoles().iterator().next().getName() : "—";

      req.setAttribute("reqObj", r);
      req.setAttribute("creatorName", creator!=null?creator.getFullName():String.valueOf(r.getCreatedBy()));
      req.setAttribute("approverName", approverName);
      req.setAttribute("approverRole", approverRole);

      req.getRequestDispatcher("/WEB-INF/views/request_approve.jsp").forward(req, resp);
    } catch (Exception e) {
      throw new ServletException(e);
    }
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param req
     * @param resp
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
      User actor = (User) req.getSession().getAttribute("user");
        if (actor==null){ resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }

        String idStr = req.getParameter("id");
        String note  = req.getParameter("note");
        int id;
        try { id = Integer.parseInt(idStr); }
        catch (Exception ex) {
            req.setAttribute("error","Thiếu hoặc sai tham số id");
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp); return;
        }

        try {
            Request r = requestDAO.findById(id);
            if (r==null) throw new IllegalArgumentException("Không tìm thấy đơn");
            if (r.getStatus()!=RequestStatus.IN_PROGRESS) throw new IllegalStateException("Trạng thái không hợp lệ");

            // Quyền xử lý:
            // 1) Quản lý trực tiếp của người tạo
            List<Integer> directSubs = userDAO.findSubordinateIds(actor.getId());
            boolean canAsDirectMgr = directSubs.contains(r.getCreatedBy());

            // 2) Trưởng phòng của phòng mà người tạo đang thuộc
            User creator = userDAO.findById(r.getCreatedBy());
            boolean canAsHead = userDAO.isDepartmentHead(actor.getId(), creator.getDepartmentId());

            if (!canAsDirectMgr && !canAsHead)
                throw new SecurityException("Bạn không có quyền xử lý đơn này");

            boolean approve = req.getServletPath().endsWith("/requestapproveservlet1");
            RequestStatus target = approve ? RequestStatus.APPROVED : RequestStatus.REJECTED;

            requestDAO.updateStatus(id, target, actor.getId(), note);
            historyDAO.add(id, actor.getId(), RequestStatus.IN_PROGRESS.name(), target.name(), note);

            resp.sendRedirect(req.getContextPath()+"/requestsubordinatesservlet1");
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
