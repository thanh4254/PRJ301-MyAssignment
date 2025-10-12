<%-- 
    Document   : request_subordinates
    Created on : Oct 12, 2025, 3:45:07 PM
    Author     : Admin
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, model.Request" %>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>Đơn cấp dưới</title></head>
<body>
  <h2>Đơn của cấp dưới</h2>
  <a href="${pageContext.request.contextPath}/requestlistmyservlet1">← Về danh sách của tôi</a>
  <table border="1" cellpadding="6">
    <tr><th>ID</th><th>Title</th><th>From</th><th>To</th><th>Status</th><th>Thao tác</th></tr>
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
      <td>
       <form method="post" action="${pageContext.request.contextPath}/requestapproveservlet1" style="display:inline">
  <input type="hidden" name="id" value="<%= r.getId() %>"/>
  <input name="note" placeholder="Ghi chú"/>
  <button type="submit">Approve</button>
</form>

       <form method="post" action="${pageContext.request.contextPath}/requestrejectservlet1" style="display:inline">
  <input type="hidden" name="id" value="<%= r.getId() %>"/>
  <input name="note" placeholder="Ghi chú"/>
  <button type="submit">Reject</button>
</form>
      </td>
    </tr>
    <% } %>
  </table>
  <p style="color:red">${requestScope.error}</p>
</body></html>
