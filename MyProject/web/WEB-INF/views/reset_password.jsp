<%-- 
    Document   : reset_password
    Created on : Nov 5, 2025, 3:49:39 PM
    Author     : Admin
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%
  String ctx = request.getContextPath();
  String token = (String) request.getAttribute("token");
  String username = (String) request.getAttribute("username");
  String err  = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>Đặt lại mật khẩu</title>
<link rel="stylesheet" href="<%=ctx%>/css/theme.css">
<style>
  .page{max-width:680px;margin:60px auto;padding:0 16px}
  .glass{
  background: rgba(255,255,255,.08);
  border: 1px solid rgba(255,255,255,.12);
  box-shadow: 0 18px 50px rgba(0,0,0,.35), inset 0 0 0 1px rgba(255,255,255,.06);
  border-radius: 18px;
  padding: 24px;
  color: #0f172a;

  /* NEW: làm mờ nền phía sau */
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

  h1{font-weight:800;margin:0 0 14px}
  .inp{
  width: 100%;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid rgba(15,23,42,.2);
  background: rgba(255,255,255,.58); /* hơi trong suốt */
  outline: none;
  backdrop-filter: blur(2px);         /* nhẹ thôi để chữ vẫn sắc nét */
  -webkit-backdrop-filter: blur(2px);
}

  .row{display:flex;gap:10px;margin-top:12px}
  .btn{padding:10px 16px;border-radius:12px;border:0;font-weight:800;cursor:pointer}
  .btn-primary{background:#0b67ff;color:#fff}
  .err{margin-top:12px;color:#b00020;font-weight:700}
  .muted{opacity:.85}
</style>
</head>
<body>
<div class="page">
  <div class="glass">
    <h1>Đặt lại mật khẩu</h1>
    <% if (err!=null) { %>
      <p class="err"><%=err%></p>
    <% } else if (token!=null) { %>
      <p class="muted">Tài khoản: <b><%=username%></b></p>
      <form method="post">
        <input type="hidden" name="token" value="<%=token%>">
        <div style="display:grid;gap:10px">
          <input class="inp" type="password" name="password" placeholder="Mật khẩu mới (≥ 6 ký tự)" required>
          <input class="inp" type="password" name="confirm"  placeholder="Nhập lại mật khẩu" required>
          <div class="row">
            <button class="btn btn-primary" type="submit">Cập nhật mật khẩu</button>
            <a class="btn" style="background:rgba(255,255,255,.35);border:1px solid rgba(15,23,42,.2);"
               href="<%=ctx%>/loginservlet1">Huỷ</a>
          </div>
        </div>
      </form>
    <% } else { %>
      <p class="err">Token không hợp lệ hoặc đã hết hạn.</p>
    <% } %>
  </div>
</div>
</body>
</html>
