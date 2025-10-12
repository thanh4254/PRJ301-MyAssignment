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
       resp.sendRedirect(req.getContextPath() + "/requestsubordinatesservlet1");
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
      User manager = (User) req.getSession().getAttribute("user");
        if (manager==null){ resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }

        String idStr = req.getParameter("id");
        String note  = req.getParameter("note");

        // parse an toàn
        int id;
        try {
            if (idStr == null || idStr.isBlank()) throw new NumberFormatException("null");
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            req.setAttribute("error", "Thiếu hoặc sai tham số id");
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
            return;
        }

        try {
            Request r = requestDAO.findById(id);
            if (r==null) throw new IllegalArgumentException("Không tìm thấy đơn");
            if (r.getStatus()!=RequestStatus.IN_PROGRESS) throw new IllegalStateException("Trạng thái không hợp lệ");

            // chỉ quản lý trực tiếp mới được duyệt
            List<Integer> subs = userDAO.findSubordinateIds(manager.getId());
            if (!subs.contains(r.getCreatedBy()))
                throw new SecurityException("Bạn không phải quản lý trực tiếp của nhân viên này");

            boolean approve = req.getServletPath().endsWith("/requestapproveservlet1");
            RequestStatus target = approve ? RequestStatus.APPROVED : RequestStatus.REJECTED;

            requestDAO.updateStatus(id, target, manager.getId(), note);
            historyDAO.add(id, manager.getId(), RequestStatus.IN_PROGRESS.name(), target.name(), note);

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
