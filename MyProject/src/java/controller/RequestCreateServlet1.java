/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller;

import dal.HistoryDAO;
import dal.RequestDAO;
import dal.UserDAO;
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
         User u = (User) req.getSession().getAttribute("user");
        if (u == null) { resp.sendRedirect(req.getContextPath() + "/loginservlet1"); return; }

        try {
            // Lấy tên role đầu tiên từ Set<Role>
            String roleName = "—";
            if (u.getRoles() != null && !u.getRoles().isEmpty()) {
                roleName = u.getRoles().iterator().next().getName();  // dùng iterator vì Set không có get(0)
            }
            String depName = userDAO.getDepartmentName(u.getDepartmentId());

            req.setAttribute("roleName", roleName);
            req.setAttribute("depName",  depName);
        } catch (Exception e) {
            throw new ServletException(e);
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
        if (u == null) { resp.sendRedirect(req.getContextPath() + "/loginservlet1"); return; }

        String sFrom  = req.getParameter("from");
        String sTo    = req.getParameter("to");
        String reason = req.getParameter("reason");

        try {
            LocalDate from = LocalDate.parse(sFrom);
            LocalDate to   = LocalDate.parse(sTo);
            if (from.isAfter(to)) throw new IllegalArgumentException("Khoảng ngày không hợp lệ");

            // Không cho trùng với kỳ đã APPROVED
            if (requestDAO.existsApprovedOverlap(u.getId(), from, to)) {
                throw new IllegalStateException("Trùng với kỳ nghỉ đã được duyệt trước đó");
            }

            Request r = new Request();
            r.setTitle("Nghỉ phép " + from + " → " + to);
            r.setFrom(from);
            r.setTo(to);
            r.setReason(reason);
            r.setCreatedBy(u.getId());
            r.setStatus(RequestStatus.IN_PROGRESS);

            requestDAO.insert(r);
            resp.sendRedirect(req.getContextPath() + "/requestlistmyservlet1");
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            doGet(req, resp); // quay lại form, vẫn có thông tin User/Role/Dep
        }
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    
 @Override
    public String getServletInfo() {         // CHỈ MỘT phương thức này thôi
        return "Create leave request";
    }
}
