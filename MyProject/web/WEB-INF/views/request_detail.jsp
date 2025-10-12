<%-- 
    Document   : request_detail
    Created on : Oct 12, 2025, 5:29:30 PM
    Author     : Admin
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="model.Request" %>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>Chi tiết đơn</title></head>
<body>
  <h2>Chi tiết đơn</h2>
  <%
    Request r = (Request) request.getAttribute("r");
  %>
  <ul>
    <li>ID: <%= r.getId() %></li>
    <li>Tiêu đề: <%= r.getTitle() %></li>
    <li>Từ ngày: <%= r.getFrom() %></li>
    <li>Đến ngày: <%= r.getTo() %></li>
    <li>Lý do: <%= r.getReason() %></li>
    <li>Trạng thái: <%= r.getStatus() %></li>
    <li>ProcessedBy: <%= r.getProcessedBy()==null?"":r.getProcessedBy() %></li>
    <li>Note: <%= r.getProcessedNote()==null?"":r.getProcessedNote() %></li>
  </ul>
  <a href="${pageContext.request.contextPath}/requestlistmyservlet1">← Quay về danh sách</a>
</body></html>
