<%-- 
    Document   : request_list
    Created on : Oct 12, 2025, 3:44:55 PM
    Author     : Admin
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, model.Request" %>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>Đơn của tôi</title></head>
<body>
  <h2>Đơn của tôi</h2>
 <a href="${pageContext.request.contextPath}/requestcreateservlet1">+ Tạo đơn</a>
 <a href="${pageContext.request.contextPath}/requestsubordinatesservlet1" style="margin-left:16px">Đơn cấp dưới</a>
  <table border="1" cellpadding="6">
    <tr><th>ID</th><th>Title</th><th>From</th><th>To</th><th>Status</th><th>ProcessedBy</th><th>Note</th></tr>
    <%
      List<Request> items = (List<Request>) request.getAttribute("items");
      if (items != null)
      for (Request r : items) {
    %>
    <tr>
      <td><%= r.getId() %></td>
      <td><%= r.getTitle() %></td>
      <td><%= r.getFrom() %></td>
      <td><%= r.getTo() %></td>
      <td><%= r.getStatus() %></td>
      <td><%= r.getProcessedBy()==null?"":r.getProcessedBy() %></td>
      <td><%= r.getProcessedNote()==null?"":r.getProcessedNote() %></td>
    </tr>
    <% } %>
  </table>
  <p style="color:red">${requestScope.error}</p>
</body></html>
