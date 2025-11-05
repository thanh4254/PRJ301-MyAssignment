<%-- 
    Document   : forgot
    Created on : Nov 5, 2025, 3:48:49 PM
    Author     : Admin
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%
  String ctx = request.getContextPath();
  String info = (String) request.getAttribute("info");
  String err  = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>Forgot Password</title>
<link rel="stylesheet" href="<%=ctx%>/css/theme.css">
<style>
  .page{max-width:680px;margin:60px auto;padding:0 16px}
  .glass{background:rgba(255,255,255,.08);border:1px solid rgba(255,255,255,.12);
         box-shadow:0 18px 50px rgba(0,0,0,.35), inset 0 0 0 1px rgba(255,255,255,.06);
         border-radius:18px;padding:24px;color:#0f172a}
  h1{font-weight:800;margin:0 0 14px}
  .inp{width:100%;padding:12px 14px;border-radius:12px;border:1px solid rgba(15,23,42,.2);
       background:rgba(255,255,255,.6);outline:none}
  .row{display:flex;gap:10px;margin-top:12px}
  .btn{padding:10px 16px;border-radius:12px;border:0;font-weight:800;cursor:pointer}
  .btn-primary{background:#0b67ff;color:#fff}
  .muted{opacity:.9}
  .msg{margin-top:12px;font-weight:700}
  .ok{color:#0a7f3f}.err{color:#b00020}
  .navs{display:flex;gap:10px;margin-bottom:12px}
  .link{padding:10px 14px;border-radius:12px;background:rgba(255,255,255,.35);
        color:#0f172a;text-decoration:none;font-weight:800;border:1px solid rgba(15,23,42,.15)}
</style>
</head>
<body>
<div class="page">
  <div class="glass">
    <div class="navs">
      <a class="link" href="<%=ctx%>/loginservlet1">← Quay lại đăng nhập</a>
    </div>
    <h1>Yêu cầu đặt lại mật khẩu</h1>
    <p class="muted">Nhập <b>username</b>. Yêu cầu của bạn sẽ được <b>Admin</b> duyệt trước khi đặt lại mật khẩu.</p>
    <form method="post">
      <input class="inp" name="username" placeholder="username" required>
      <div class="row">
        <button class="btn btn-primary" type="submit">Gửi yêu cầu</button>
      </div>
    </form>
    <% if (info!=null) { %><div class="msg ok"><%=info%></div><% } %>
    <% if (err !=null) { %><div class="msg err"><%=err %></div><% } %>
  </div>
</div>
</body>
</html>

