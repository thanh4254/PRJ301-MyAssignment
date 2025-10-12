<%-- 
    Document   : request_create
    Created on : Oct 12, 2025, 3:44:46 PM
    Author     : Admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>Create Request</title></head>
<body>
  <h2>Tạo đơn xin nghỉ</h2>
  <form method="post" action="${pageContext.request.contextPath}/requestcreateservlet1">
    <div>Tiêu đề: <input name="title" required /></div>
    <div>Từ ngày: <input name="from" type="date" required /></div>
    <div>Đến ngày: <input name="to" type="date" required /></div>
    <div>Lý do: <textarea name="reason" required></textarea></div>
    <button type="submit">Lưu</button>
   <a href="${pageContext.request.contextPath}/requestlistmyservlet1">Danh sách</a>
  </form>
  <p style="color:red">${requestScope.error}</p>
</body></html>
