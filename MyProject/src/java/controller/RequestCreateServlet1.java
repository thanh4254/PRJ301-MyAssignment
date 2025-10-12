/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller;

import dal.HistoryDAO;
import dal.RequestDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import model.Request;
import model.RequestStatus;
import model.User;

/**
 *
 * @author Admin
 */
@WebServlet(name="RequestCreateServlet1", urlPatterns={"/requestcreateservlet1"})
public class RequestCreateServlet1 extends HttpServlet {
   private final RequestDAO requestDAO = new RequestDAO();
    private final HistoryDAO historyDAO = new HistoryDAO();
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
            out.println("<title>Servlet RequestCreateServlet1</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet RequestCreateServlet1 at " + request.getContextPath () + "</h1>");
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
        if (req.getSession().getAttribute("user")==null) {
            resp.sendRedirect(req.getContextPath()+"/loginservlet1");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/request_create.jsp").forward(req, resp);

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
        User u = (User) req.getSession().getAttribute("user");
        if (u==null){ resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }

        String title = req.getParameter("title");
        String reason = req.getParameter("reason");
        LocalDate from = LocalDate.parse(req.getParameter("from"));
        LocalDate to   = LocalDate.parse(req.getParameter("to"));

        try {
            if (title==null||title.isBlank()) throw new IllegalArgumentException("Thiếu tiêu đề");
            if (reason==null||reason.isBlank()) throw new IllegalArgumentException("Thiếu lý do");
            if (from==null||to==null||from.isAfter(to)) throw new IllegalArgumentException("Khoảng ngày không hợp lệ");
            if (requestDAO.existsApprovedOverlap(u.getId(), from, to))
                throw new IllegalArgumentException("Trùng với kỳ nghỉ đã được duyệt trước đó");

            Request r = new Request();
            r.setTitle(title.trim());
            r.setReason(reason.trim());
            r.setFrom(from);
            r.setTo(to);
            r.setCreatedBy(u.getId());
            r.setStatus(RequestStatus.IN_PROGRESS);

            int id = requestDAO.insert(r);
            historyDAO.add(id, u.getId(), null, RequestStatus.IN_PROGRESS.name(), "Create");
            resp.sendRedirect(req.getContextPath()+"/requestlistmyservlet1");
        } catch (Exception ex) {
            req.setAttribute("error", ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/request_create.jsp").forward(req, resp);
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
