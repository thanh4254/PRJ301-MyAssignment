<%-- 
    Document   : agenda
    Created on : Oct 12, 2025, 5:24:29 PM
    Author     : Admin
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, java.time.*, model.User" %>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>Agenda phòng ban</title>
<style>
  table { border-collapse: collapse; }
  th,td { border:1px solid #aaa; padding:6px 10px; text-align:center }
  .off { background:#ffd6d6; font-weight:bold }
</style>
</head>
<body>
  <h2>Agenda phòng ban</h2>
  <form method="get" action="${pageContext.request.contextPath}/agendaservlet1">
    Từ: <input type="date" name="from" value="${from}"/>
    Đến: <input type="date" name="to" value="${to}"/>
    <button type="submit">Xem</button>
    <a href="${pageContext.request.contextPath}/requestsubordinatesservlet1">Đơn cấp dưới</a>
    <a href="${pageContext.request.contextPath}/requestlistmyservlet1">Đơn của tôi</a>
  </form>

  <%
    List<User> users = (List<User>) request.getAttribute("users");
    List<LocalDate> days = (List<LocalDate>) request.getAttribute("days");
    Map<Integer, Set<LocalDate>> daysOff = (Map<Integer, Set<LocalDate>>) request.getAttribute("daysOff");
  %>

  <table>
    <tr>
      <th>Nhân sự</th>
      <% for(LocalDate d : days){ %>
        <th><%= d %></th>
      <% } %>
    </tr>
    <% for(User u : users){ %>
      <tr>
        <td style="text-align:left"><%= u.getFullName() %> (<%= u.getUsername() %>)</td>
        <% Set<LocalDate> off = daysOff.get(u.getId()); %>
        <% for(LocalDate d : days){ %>
          <td class="<%= (off!=null && off.contains(d)) ? "off" : "" %>">
            <%= (off!=null && off.contains(d)) ? "OFF" : "" %>
          </td>
        <% } %>
      </tr>
    <% } %>
  </table>

  <p style="color:red">${requestScope.error}</p>
</body></html>
