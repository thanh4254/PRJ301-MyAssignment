<%-- 
    Document   : login
    Created on : Oct 12, 2025, 3:44:27 PM
    Author     : Admin
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%
  String ctx = request.getContextPath(); // -> "/MyProject"
  String err = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <title>Đăng nhập</title>

  <style>
    html, body {
  height: 100%;
  margin: 0;
  /* KHÓA CUỘN TRANG */
  overflow: hidden;         /* chặn cả cuộn dọc & ngang */
}
body {
  position: fixed;          /* ghim body vào viewport */
  top: 0; left: 0; right: 0; bottom: 0;
  width: 100%;
  height: 100%;
}

    /* Ảnh nền toàn trang */
    .bg-fixed{
        
      position:fixed; inset:0; z-index:-1;
      width:100%; height:100%; object-fit:cover;
    }
    .bg-overlay{
      position:fixed; inset:0; z-index:-1;
      background:rgba(255,255,255,.06);
    }
    .wrap{
  height: 100vh;            /* thay vì min-height */
  padding: 0 16px;          /* giảm padding để không vượt quá viewport */
  display: flex; 
  align-items: center; 
  justify-content: center;
  box-sizing: border-box;
}
form.login-card .btn-login{
  display: block;
  width: 100%;
  box-sizing: border-box;

  border: none;
  border-radius: 8px;
  padding: 14px 16px;

  background: #ff6b7d;   /* hồng như trước */
  color: #fff;
  font-weight: 700;
  text-align: center;
  cursor: pointer;

  appearance: none;        /* tránh style mặc định của trình duyệt */
  -webkit-appearance: none;
  transition: filter .15s ease, transform .02s;
}
    .login-card{
        
      width:520px; max-width:92vw;
      background:rgba(255,255,255,.92);
      border-radius:12px; box-shadow:0 10px 30px rgba(0,0,0,.25);
      padding:28px 30px; backdrop-filter:blur(2px);
    }
    .login-title{margin:6px 0 18px; text-align:center; font-weight:700; font-size:28px}
    .form-group{margin-bottom:12px}
    .form-control{
  display:block;
  width: 100%;
  padding: 14px 12px;
  font-size: 16px;
  border: 1px solid #cad1e1;
  border-radius: 8px;
  outline: none;

  /* QUAN TRỌNG: để chiều rộng khớp nút Login */
  box-sizing: border-box;
  margin: 0;               /* tránh lùi vào do margin mặc định */
}
    .form-control:focus{box-shadow:0 0 0 3px rgba(80,141,255,.15)}
   .btn-login{
  display:block;
  width:100%;
  box-sizing: border-box;
}
form.login-card .btn-login:hover { filter: brightness(.95); }
form.login-card .btn-login:active { transform: translateY(1px); }
    .btn-login:hover{filter:brightness(.95)}
    .error{margin-top:10px; color:#b00020; text-align:center; font-weight:600}
    .note{margin-top:10px; text-align:center; font-size:12px; opacity:.85}
  </style>
</head>

<body>
  <!-- Ảnh nền: NHỚ dùng context path -->
  <img class="bg-fixed" src="<%=ctx%>/anh/back.jpg" alt="background">
  <div class="bg-overlay"></div>

  <div class="wrap">
    <form class="login-card" method="post" action="<%=ctx%>/loginservlet1">
      <div class="login-title">Đăng Nhập</div>

      <div class="form-group">
        <input class="form-control" name="username" placeholder="Username" autocomplete="username">
      </div>
      <div class="form-group">
        <input class="form-control" type="password" name="password" placeholder="Password" autocomplete="current-password">
      </div>

      <button class="btn-login" type="submit">Login</button>

      <% if (err != null) { %><div class="error"><%=err%></div><% } %>
      <div class="note">Trình duyệt của bạn cần phải mở chức năng quản lí cookie</div>
    </form>
  </div>
</body>
</html>
