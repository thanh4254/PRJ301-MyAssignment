/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller;
import dal.UserDAO;
import dal.RequestDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import model.User;
/**
 *
 * @author Admin
 */
@WebServlet(name="RequestListMyServlet1", urlPatterns={"/requestlistmyservlet1"})
public class RequestListMyServlet1 extends HttpServlet {
   private final RequestDAO dao = new RequestDAO();
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
            out.println("<title>Servlet RequestListMyServlet1</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet RequestListMyServlet1 at " + request.getContextPath () + "</h1>");
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

    try {
        java.util.List<model.Request> items = new dal.RequestDAO().listMine(me.getId());
        // gom các id cần tên
        java.util.Set<Integer> ids = new java.util.HashSet<>();
        ids.add(me.getId()); // createdBy (chính mình)
        for (model.Request r : items) if (r.getProcessedBy()!=null) ids.add(r.getProcessedBy());
        java.util.Map<Integer,String> names = userDAO.getFullNamesByIds(ids);

        req.setAttribute("items", items);
        req.setAttribute("names", names);
    } catch (Exception e) { throw new ServletException(e); }

    req.getRequestDispatcher("/WEB-INF/views/request_list.jsp").forward(req, resp);
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
