/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller;

import dal.PermissionUtil;
import dal.RequestDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import model.Request;
import model.User;
/**
 *
 * @author Admin
 */
@WebServlet(name="AgendaServlet1", urlPatterns={"/agendaservlet1"})
public class AgendaServlet1 extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private final RequestDAO requestDAO = new RequestDAO();
    private static final DateTimeFormatter KEY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
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
            out.println("<title>Servlet AgendaServlet1</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet AgendaServlet1 at " + request.getContextPath () + "</h1>");
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
         User head = (User) req.getSession().getAttribute("user");
        if (head == null) { resp.sendRedirect(req.getContextPath()+"/loginservlet1"); return; }

        // Chặn nếu không có feature AGD
        if (!PermissionUtil.hasFeatureCode(head, "AGD")) {
            req.setAttribute("error", "Bạn không có quyền xem agenda (AGD).");
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
            return;
        }

        // Khoảng ngày: mặc định 1 tuần
        LocalDate today = LocalDate.now();
        LocalDate from = parseOrDefault(req.getParameter("from"), today);
        LocalDate to   = parseOrDefault(req.getParameter("to"),   today.plusDays(6));
        if (from.isAfter(to)) { LocalDate t = from; from = to; to = t; }

        try {
            // 1) Lấy tất cả nhân sự trong phòng
            List<User> members = userDAO.listByDepartment(head.getDepartmentId());

            // 2) Lấy các đơn APPROVED giao với [from..to] của tất cả nhân sự trong phòng
            List<Integer> creatorIds = new ArrayList<>();
            for (User u : members) creatorIds.add(u.getId());
            List<Request> approved = requestDAO.listApprovedByCreatorsInRange(creatorIds, from, to);

            // 3) Dựng map: userId -> set<yyyy-MM-dd> những ngày OFF
            Map<Integer, Set<String>> offByUser = new HashMap<>();
            for (User u : members) offByUser.put(u.getId(), new HashSet<>());

            for (Request r : approved) {
                LocalDate s = r.getFrom().isBefore(from) ? from : r.getFrom();
                LocalDate e = r.getTo().isAfter(to) ? to : r.getTo();
                Set<String> bucket = offByUser.computeIfAbsent(r.getCreatedBy(), k -> new HashSet<>());
                for (LocalDate d = s; !d.isAfter(e); d = d.plusDays(1)) {
                    bucket.add(d.format(KEY_FMT));
                }
            }

            // 4) Danh sách ngày header
            List<LocalDate> days = new ArrayList<>();
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) days.add(d);

            // 5) Bơm đúng attribute cho JSP agenda.jsp
            req.setAttribute("members", members);
            req.setAttribute("days", days);
            req.setAttribute("offByUser", offByUser);

            // (tuỳ chọn) cũng bơm from/to để form giữ giá trị
            req.setAttribute("from", from);
            req.setAttribute("to", to);

            req.getRequestDispatcher("/WEB-INF/views/agenda.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

     private LocalDate parseOrDefault(String s, LocalDate def) {
        try { return (s == null || s.isBlank()) ? def : LocalDate.parse(s); }
        catch (Exception ignore) { return def; }
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
