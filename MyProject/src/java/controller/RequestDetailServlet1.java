/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller;
import dal.RequestDAO;
import dal.UserDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import dal.RequestDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import model.Request;
import model.User;
/**
 *
 * @author Admin
 */
@WebServlet(name="RequestDetailServlet1", urlPatterns={"/requestdetailservlet1"})
public class RequestDetailServlet1 extends HttpServlet {
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
            out.println("<title>Servlet RequestDetailServlet1</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet RequestDetailServlet1 at " + request.getContextPath () + "</h1>");
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

    String sid = req.getParameter("id");
    if (sid == null) { resp.sendError(400, "Missing id"); return; }

    try {
      int id = Integer.parseInt(sid);
      Request r = requestDAO.findById(id);
      if (r == null) { resp.sendError(404, "Request not found"); return; }

      // (tuỳ chọn) có thể chèn kiểm tra quyền xử lý ở đây

      User creator = userDAO.findById(r.getCreatedBy());
      String approverName = me.getFullName();
      String approverRole = (me.getRoles()!=null && !me.getRoles().isEmpty())
              ? me.getRoles().iterator().next().getName() : "—";

      req.setAttribute("reqObj", r);
      req.setAttribute("creatorName",
              creator!=null ? creator.getFullName() : String.valueOf(r.getCreatedBy()));
      req.setAttribute("approverName", approverName);
      req.setAttribute("approverRole", approverRole);

      req.getRequestDispatcher("/WEB-INF/views/request_detail.jsp").forward(req, resp);
    } catch (Exception e) {
      throw new ServletException(e);
    }
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
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
