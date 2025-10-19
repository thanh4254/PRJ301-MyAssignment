<%-- 
    Document   : error
    Created on : Oct 12, 2025, 3:45:15 PM
    Author     : Admin
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%
  String ctx = request.getContextPath();
  String err = (String)request.getAttribute("error");
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Lỗi</title>
  <link rel="stylesheet" href="<%=ctx%>/css/theme.css">
</head>
<body>
  <div class="card" style="max-width:800px;margin:40px auto;">
    <h1 style="color:#b91c1c">Có lỗi xảy ra</h1>
    <p><%= err==null? "Đã có lỗi không xác định.": err %></p>
    <div class="actions" style="justify-content:flex-start">
      <a class="btn btn-primary" href="javascript:history.back()">Quay lại</a>
    </div>
  </div>
</body>
</html>