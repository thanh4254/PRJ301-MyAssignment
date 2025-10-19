<%-- 
    Document   : login
    Created on : Oct 12, 2025, 3:44:27 PM
    Author     : Admin
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%
  String ctx = request.getContextPath();  // ví dụ: /MyProject
  String err = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <title>Login</title>

  <style>
    /* Khóa cuộn & nền */
    html,body{height:100%;margin:0;overflow:hidden;font-family:Inter,system-ui,Arial,sans-serif}
    .bg{position:fixed;inset:0;z-index:-2;object-fit:cover;width:100%;height:100%}
    .bg-dim{position:fixed;inset:0;z-index:-1;
      background: radial-gradient(ellipse at 30% 10%, rgba(0,0,0,.35), rgba(0,0,0,.55));
      backdrop-filter: blur(2px);
    }

    /* Canh giữa card */
    .wrap{height:100vh;display:flex;align-items:center;justify-content:center;padding:16px;box-sizing:border-box}

    /* Card kiểu glass */
    .glass{
      width: 420px; max-width: 92vw; color: #e9eefc;
      border-radius: 18px; padding: 22px 22px 18px;
      background: rgba(255,255,255,.08);
      box-shadow: 0 18px 50px rgba(0,0,0,.35), inset 0 0 0 1px rgba(255,255,255,.08);
      backdrop-filter: blur(14px);
      position: relative;
    }
    .close{
      position:absolute; top:10px; right:10px; width:34px; height:34px;
      border-radius: 10px; background: rgba(0,0,0,.5); border:1px solid rgba(255,255,255,.18);
      color:#fff; display:grid; place-items:center; cursor:pointer; font-weight:700;
      line-height:0; user-select:none;
    }
    .title{margin:6px 0 12px; text-align:center; font-weight:800; font-size:28px; letter-spacing:.2px}

    /* Ô nhập kiểu gạch chân */
    .field{margin:16px 2px 8px}
    .label{font-size:14px; opacity:.9; margin-bottom:6px}
    .line{
      display:flex; align-items:center; gap:10px;
      border-bottom: 2px solid rgba(233,238,252,.35);
      padding: 10px 4px 8px;
    }
    .line input{
      flex:1; background:transparent; border:0; outline:0; color:#fff; font:inherit;
    }
    .line input::placeholder{color:rgba(233,238,252,.65)}

    /* Row phụ */
    .subrow{display:flex; justify-content:space-between; align-items:center; margin:10px 2px 14px; font-size:14px}
    .subrow a{color:#c9d7ff; text-decoration:none}
    .subrow a:hover{text-decoration:underline}
    .check{display:flex; align-items:center; gap:8px; opacity:.95}

    /* Nút login tối màu */
    .btn{
      width:100%; border:0; border-radius:10px; padding:12px 14px; cursor:pointer;
      background:#0f172a; color:#fff; font-weight:700; letter-spacing:.2px;
      box-shadow: 0 6px 16px rgba(0,0,0,.35);
      transition: transform .02s, filter .15s;
    }
    .btn:hover{filter:brightness(1.05)}
    .btn:active{transform:translateY(1px)}

    .footer{margin-top:14px; text-align:center; font-size:14px; color:#d8e3ff}
    .footer a{color:#ffffff; font-weight:600; text-decoration:none}
    .footer a:hover{text-decoration:underline}

    .error{margin-top:10px; color:#ffb4b4; text-align:center; font-weight:700}
  </style>
</head>
<body>
  <!-- Nền (dùng ảnh background chung của bạn) -->
  <img class="bg" src="<%=ctx%>/anh/background.jpg" alt="">
  <div class="bg-dim"></div>

  <div class="wrap">
    <div class="glass">
      <div class="close">×</div>

      <div class="title">Login</div>

     <form method="post" action="<%=ctx%>/loginservlet1" >
  <!-- Username -->
  <div class="field">
    <div class="label">Username</div>
    <div class="line">
      <span>👤</span>
      <input name="username" placeholder="Username" autocomplete="username">
    </div>
  </div>

  <!-- Password -->
  <div class="field">
  <div class="label">Password</div>
  <div class="line">
    <span>🔒</span>
    <!-- SAI: name="Password" -->
    <!-- ĐÚNG: -->
    <input type="password" name="password" placeholder="••••••••" autocomplete="current-password">
  </div>
</div>

  <!-- Remember / Forgot (giữ nguyên, có thể xóa nếu không cần) -->
  <div class="subrow">
    <label class="check">
      <input type="checkbox" name="remember" style="accent-color:#0f172a"> Remember me
    </label>
    <a href="#">Forgot Password?</a>
  </div>

  <button class="btn" type="submit">Login</button>

  <% if (err != null) { %>
    <div class="error"><%= err %></div>
  <% } %>
</form>
    </div>
  </div>
</body>
</html>