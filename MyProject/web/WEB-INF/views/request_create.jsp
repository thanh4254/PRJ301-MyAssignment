<%-- 
    Document   : request_create
    Created on : Oct 12, 2025, 3:44:46 PM
    Author     : Admin
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="model.User,model.Role" %>
<%
  String ctx = request.getContextPath();
  User me = (User) session.getAttribute("user");
  String roleName = (me!=null && me.getRoles()!=null && !me.getRoles().isEmpty())
      ? me.getRoles().iterator().next().getName() : "";
  String deptName = (String) request.getAttribute("departmentName"); // nếu controller set
  String err = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <title>Tạo đơn</title>
  <link rel="stylesheet" href="<%=ctx%>/css/theme.css"/>

  <style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap');
    html,body{font-family:Inter,system-ui,Arial,sans-serif}

    .page{max-width:900px;margin:28px auto;padding:0 16px}
    .glass{
      color:#0f172a;background:rgba(255,255,255,.08);
      border:1px solid rgba(255,255,255,.12);border-radius:18px;padding:18px;
      box-shadow:0 18px 50px rgba(0,0,0,.35), inset 0 0 0 1px rgba(255,255,255,.08);
      backdrop-filter:blur(14px)
    }
    .title{margin:6px 2px 14px;font-size:28px;font-weight:800}

    .pills{display:flex;gap:12px;flex-wrap:wrap;margin-bottom:14px}
    .btn-pill{
      appearance:none;display:inline-flex;align-items:center;gap:8px;
      padding:10px 16px;border-radius:14px;font-weight:800;text-decoration:none;
      color:#0f172a;background:rgba(255,255,255,.35);
      border:1px solid rgba(15,23,42,.22);
      box-shadow:0 6px 16px rgba(0,0,0,.18);backdrop-filter:blur(6px);
      cursor:pointer;transition:filter .15s,transform .02s
    }
    .btn-pill:hover{filter:brightness(1.05)} .btn-pill:active{transform:translateY(1px)}

    .row{display:grid;grid-template-columns:150px 1fr;gap:10px 12px;align-items:center;margin:10px 0}
    label{font-weight:700}
    input[type="date"], textarea{
      width:100%;box-sizing:border-box;color:#0f172a;
      background:rgba(255,255,255,.55);border:1px solid rgba(15,23,42,.18);
      border-radius:12px;padding:12px
    }
    textarea{min-height:150px;resize:vertical}
    ::placeholder{color:#475569}
    .actions{display:flex;justify-content:flex-end;margin-top:12px}
    .err{color:#b00020;font-weight:700;margin-top:10px}
  </style>
</head>
<body>
<div class="page">
  <div class="glass">
    <div class="pills">
      <a class="btn-pill" href="<%=ctx%>/requestlistmyservlet1">← Đơn của tôi</a>
      <a class="btn-pill" href="<%=ctx%>/requestsubordinatesservlet1">Đơn cấp dưới</a>
      <a class="btn-pill" href="<%=ctx%>/logoutservlet1">Đăng xuất</a>
    </div>

    <div class="title">Đơn xin nghỉ phép</div>

    <div style="margin-bottom:10px">
      User: <strong><%= me!=null? me.getUsername() : "" %></strong>,
      Role: <strong><%= roleName %></strong>,
      Dep: <strong><%= (deptName!=null? deptName : (me!=null? String.valueOf(me.getDepartmentId()):"")) %></strong>
    </div>

    <form method="post" action="<%=ctx%>/requestcreateservlet1">
      <div class="row">
        <label for="from">Từ ngày:</label>
        <input id="from" type="date" name="from" required>
      </div>

      <div class="row">
        <label for="to">Tới ngày:</label>
        <input id="to" type="date" name="to" required>
      </div>

      <div class="row" style="grid-template-columns:150px 1fr">
        <label for="reason">Lý do:</label>
        <!-- QUAN TRỌNG: name="reason" để không bị NULL ở DB -->
        <textarea id="reason" name="reason" placeholder="Nhập lý do..." required></textarea>
      </div>

      <div class="actions">
        <button class="btn-pill" type="submit">Gửi</button>
      </div>

      <% if (err != null) { %>
        <div class="err"><%= err %></div>
      <% } %>
    </form>
  </div>
</div>
</body>
</html>