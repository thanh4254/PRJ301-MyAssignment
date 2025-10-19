<%-- 
    Document   : request_create
    Created on : Oct 12, 2025, 3:44:46 PM
    Author     : Admin
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="model.User" %>
<%
  User me = (User) session.getAttribute("user");
  // Các biến đã được servlet set sẵn:
  String roleName = (String) request.getAttribute("roleName");
  String depName  = (String) request.getAttribute("depName");
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css">
  <title>Tạo đơn xin nghỉ</title>
  <style>
    body { font-family: Arial, sans-serif; background:#f7f7f7; }
    .card {
      width: 560px; margin: 40px auto; padding: 18px 22px;
      background:#f9e1cf; border:2px solid #b99d8a; border-radius:4px;
      box-shadow: 0 1px 3px rgba(0,0,0,.08);
    }
    h2 { margin: 0 0 12px 0; }
    .row { margin: 10px 0; }
    label { display:inline-block; width: 90px; }
    input[type="date"]{ padding:6px 8px; border:1px solid #aaa; border-radius:4px; }
    textarea { width:100%; height:120px; padding:8px; border:1px solid #aaa; border-radius:4px; resize: vertical; }
    .actions { text-align:right; margin-top: 14px; }
    .btn {
      padding: 10px 22px; border:0; border-radius:6px;
      background:#3e73c5; color:#fff; font-size:18px; cursor:pointer;
    }
    .btn:hover { filter: brightness(1.1); }
    .muted { color:#444; }
    .links { width:560px; margin: 0 auto; }
    .links a { margin-right:14px; }
    .error { color:#b22; }
  </style>
</head>
<body>

<div class="links">
  <a href="${pageContext.request.contextPath}/requestlistmyservlet1">← Đơn của tôi</a>
  <a href="${pageContext.request.contextPath}/requestsubordinatesservlet1">Đơn cấp dưới</a>
  <a href="${pageContext.request.contextPath}/logoutservlet1">Đăng xuất</a>
</div>

<div class="card">
  <h2>Đơn xin nghỉ phép</h2>
  <div class="muted">
    User: <b><%= me.getUsername() %></b> ,
    Role: <b><%= roleName %></b>,
    Dep: <b><%= depName %></b>
  </div>

  <form method="post" action="${pageContext.request.contextPath}/requestcreateservlet1">
    <div class="row">
      <label>Từ ngày:</label>
      <input type="date" name="from" required />
    </div>
    <div class="row">
      <label>Tới ngày:</label>
      <input type="date" name="to" required />
    </div>
    <div class="row">
      <label style="vertical-align:top">Lý do:</label>
      <textarea name="reason" placeholder="Nhập lý do..." required></textarea>
    </div>

    <div class="actions">
      <button class="btn" type="submit">Gửi</button>
    </div>
  </form>

  <div class="error">${requestScope.error}</div>
  <div class="muted">${requestScope.message}</div>
</div>

</body>
</html>

