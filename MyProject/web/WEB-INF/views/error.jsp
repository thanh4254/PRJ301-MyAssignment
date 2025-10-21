<%-- 
    Document   : error
    Created on : Oct 12, 2025, 3:45:15 PM
    Author     : Admin
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%
  String ctx = request.getContextPath();
  String message = (String) request.getAttribute("error");
  if (message == null || message.isBlank()) message = "Đã xảy ra lỗi không xác định.";
  String backUrl = (String) request.getAttribute("backUrl");
  if (backUrl == null || backUrl.isBlank()) backUrl = "javascript:history.back()";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Có lỗi xảy ra</title>
  <link rel="stylesheet" href="<%=ctx%>/css/theme.css"/>
  <style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap');
    html,body{height:100%;margin:0;font-family:Inter,system-ui,Arial,sans-serif}
    .page{max-width:900px;margin:28px auto;padding:0 16px}
    .glass{color:#0f172a;background:rgba(255,255,255,.08);border:1px solid rgba(255,255,255,.12);
      border-radius:18px;padding:24px;box-shadow:0 18px 50px rgba(0,0,0,.35), inset 0 0 0 1px rgba(255,255,255,.08);
      backdrop-filter:blur(14px)}
    .title{font-size:32px;font-weight:800;margin:6px 2px 10px;color:#b00020}
    .lead{font-size:18px;margin:6px 0 18px}
    .btns{display:flex;gap:12px;flex-wrap:wrap;margin-bottom:8px}
    .btn{appearance:none;border:1px solid rgba(15,23,42,.22);border-radius:14px;padding:10px 16px;
      background:rgba(255,255,255,.35);font-weight:800;text-decoration:none;color:#0f172a;
      box-shadow:0 6px 16px rgba(0,0,0,.18)}
    details{margin-top:10px;background:rgba(255,255,255,.45);border:1px solid rgba(15,23,42,.18);
      border-radius:12px;padding:12px}
    summary{cursor:pointer;font-weight:700}
    pre{white-space:pre-wrap;margin:8px 0 0;color:#0f172a}
  </style>
</head>
<body>
<div class="page">
  <div class="glass">
    <div class="btns">
      <a class="btn" href="<%=ctx%>/requestlistmyservlet1">Đơn của tôi</a>
      <a class="btn" href="<%=ctx%>/requestsubordinatesservlet1">Đơn cấp dưới</a>
      <a class="btn" href="<%=ctx%>/agendaservlet1">Agenda phòng</a>
      <a class="btn" href="<%=ctx%>/logoutservlet1">Đăng xuất</a>
    </div>

    <div class="title">Có lỗi xảy ra</div>
    <div class="lead"><%= message %></div>

    <!-- CHỈ GIỮ LẠI NÚT QUAY LẠI -->
    <div class="btns">
      <a class="btn" href="<%= backUrl %>">Quay lại</a>
    </div>

    <%
      String detail = (String) request.getAttribute("errorDetail");
      if (detail != null && !detail.isBlank()) {
    %>
      <details>
        <summary>Xem chi tiết kỹ thuật</summary>
        <pre><%= detail %></pre>
      </details>
    <% } %>
  </div>
</div>
</body>
</html>