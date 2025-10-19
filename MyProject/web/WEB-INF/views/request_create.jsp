<%-- 
    Document   : request_create
    Created on : Oct 12, 2025, 3:44:46 PM
    Author     : Admin
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="model.User,model.Role,dal.UserDAO" %>
<%
  String ctx = request.getContextPath();
  User me = (User) session.getAttribute("user");
  String roleName = (me!=null && me.getRoles()!=null && !me.getRoles().isEmpty())
      ? me.getRoles().iterator().next().getName() : "";

  // Lấy tên phòng ban (ưu tiên attr từ servlet, fallback gọi DAO)
  String depName = (String) request.getAttribute("depName");
  if (depName == null && me != null) {
    try { depName = new UserDAO().getDepartmentName(me.getDepartmentId()); }
    catch (Exception ignore) { depName = String.valueOf(me.getDepartmentId()); }
  }
  if (depName == null) depName = "";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Tạo đơn</title>
  <link rel="stylesheet" href="<%=ctx%>/css/theme.css"><!-- nền chung -->

  <style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap');
    html,body{font-family:Inter,system-ui,Arial,sans-serif}

    .page{max-width:900px;margin:28px auto;padding:0 16px}

    /* card kiểu glass, chữ đen */
    .glass{
      color:#0f172a;
      background:rgba(255,255,255,.08);
      border:1px solid rgba(255,255,255,.12);
      border-radius:18px;padding:18px;
      box-shadow:0 18px 50px rgba(0,0,0,.35), inset 0 0 0 1px rgba(255,255,255,.08);
      backdrop-filter:blur(14px);
    }
    .title{margin:6px 2px 14px;font-size:28px;font-weight:800}

    /* PILL BUTTON giống list */
    .pills{display:flex;gap:12px;flex-wrap:wrap;margin-bottom:14px}
    .btn-pill{
      appearance:none; display:inline-flex; align-items:center; gap:8px;
      padding:10px 16px; border-radius:14px; font-weight:800; text-decoration:none;
      color:#0f172a; background:rgba(255,255,255,.35);
      border:1px solid rgba(15,23,42,.22);
      box-shadow:0 6px 16px rgba(0,0,0,.18);
      backdrop-filter: blur(6px);
      cursor:pointer; transition:filter .15s, transform .02s;
    }
    .btn-pill:hover{filter:brightness(1.05)}
    .btn-pill:active{transform:translateY(1px)}

    /* input/textarea */
    .row{display:grid;grid-template-columns:150px 1fr;gap:10px 12px;align-items:center;margin:10px 0}
    label{font-weight:700}
    input[type="date"],input[type="text"],textarea,select{
      width:100%; box-sizing:border-box; color:#0f172a;
      background:rgba(255,255,255,.55); border:1px solid rgba(15,23,42,.18);
      border-radius:12px; padding:12px;
    }
    input::placeholder,textarea::placeholder{color:#475569}
    textarea{min-height:150px;resize:vertical}

    .actions{display:flex;justify-content:flex-end;margin-top:12px}
  </style>
</head>
<body>
  <div class="page">
    <div class="glass">

      <!-- các nút điều hướng dạng pill -->
      <div class="pills">
        <a class="btn-pill" href="<%=ctx%>/requestlistmyservlet1">← Đơn của tôi</a>
        <a class="btn-pill" href="<%=ctx%>/requestsubordinatesservlet1">Đơn cấp dưới</a>
        <a class="btn-pill" href="<%=ctx%>/logoutservlet1">Đăng xuất</a>
      </div>

      <div class="title">Đơn xin nghỉ phép</div>

      <div style="margin-bottom:10px">
        User: <strong><%= (me!=null? me.getUsername() : "") %></strong>,
        Role: <strong><%= roleName %></strong>,
        Dep:  <strong><%= depName %></strong>
      </div>

      <form method="post" action="<%=ctx%>/requestcreateservlet1">
        <div class="row">
          <label for="from">Từ ngày:</label>
          <input id="from" type="date" name="from">
        </div>

        <div class="row">
          <label for="to">Tới ngày:</label>
          <input id="to" type="date" name="to">
        </div>

        <div class="row" style="grid-template-columns:150px 1fr">
          <label for="reason">Lý do:</label>
          <textarea id="reason" name="note" placeholder="Nhập lý do..."></textarea>
        </div>

        <!-- nút Gửi cũng là pill -->
        <div class="actions">
          <button type="submit" class="btn-pill">Gửi</button>
        </div>

        <p style="color:#b00020;font-weight:700;margin-top:10px">
          <%= request.getAttribute("error")!=null ? request.getAttribute("error") : "" %>
        </p>
      </form>

    </div>
  </div>
</body>
</html>