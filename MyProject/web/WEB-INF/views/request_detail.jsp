<%-- 
    Document   : request_detail
    Created on : Oct 12, 2025, 5:29:30 PM
    Author     : Admin
--%>

<%-- WEB-INF/views/request_detail.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="model.Request" %>
<%
  String ctx = request.getContextPath();
  Request r = (Request) request.getAttribute("item");
  String approverName = (String) request.getAttribute("approverName");
  String approverRole = (String) request.getAttribute("approverRole");
  String creatorName  = (String) request.getAttribute("creatorName");
  String err = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Duyệt đơn xin nghỉ phép</title>
  <link rel="stylesheet" href="<%=ctx%>/css/theme.css"/>

  <style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap');
    html,body{font-family:Inter,system-ui,Arial,sans-serif}
    .page{max-width:900px;margin:28px auto;padding:0 16px}

    .glass{
      color:#0f172a;
      background:rgba(255,255,255,.08);
      border:1px solid rgba(255,255,255,.12);
      border-radius:18px;padding:18px;
      box-shadow:0 18px 50px rgba(0,0,0,.35), inset 0 0 0 1px rgba(255,255,255,.08);
      backdrop-filter:blur(14px);
    }
    .topnav{display:flex;gap:12px;margin-bottom:14px;flex-wrap:wrap}
    .btn-pill{
      display:inline-flex;align-items:center;gap:8px;padding:10px 16px;
      border-radius:14px;font-weight:800;text-decoration:none;cursor:pointer;
      color:#0f172a;background:rgba(255,255,255,.35);
      border:1px solid rgba(15,23,42,.22);
      box-shadow:0 6px 16px rgba(0,0,0,.18);backdrop-filter:blur(6px);
      transition:filter .15s,transform .02s;
    }
    .btn-pill:hover{filter:brightness(1.05)} .btn-pill:active{transform:translateY(1px)}
    .title{margin:6px 2px 14px;font-size:28px;font-weight:800;letter-spacing:.2px}

    .row{display:grid;grid-template-columns:140px 1fr;gap:10px 12px;margin:10px 0;align-items:center}
    .label{font-weight:700}

    .textbox, textarea{
      width:100%;box-sizing:border-box;color:#0f172a;
      background:rgba(255,255,255,.55);border:1px solid rgba(15,23,42,.18);
      border-radius:10px;padding:10px 12px;outline:none;
    }
    textarea{min-height:160px;resize:vertical}
    ::placeholder{color:#475569}

    .actions{display:flex;gap:12px;justify-content:flex-end;margin-top:12px;flex-wrap:wrap}
    .btn-approve,.btn-reject{
      border:1px solid rgba(15,23,42,.22);
      background:rgba(255,255,255,.35); color:#0f172a;
      border-radius:14px; padding:10px 18px; font-weight:800; cursor:pointer;
      box-shadow:0 6px 16px rgba(0,0,0,.18);
    }
    .btn-approve{ box-shadow:0 6px 16px rgba(16,185,129,.25) }
    .btn-reject { box-shadow:0 6px 16px rgba(239,68,68,.25) }

    .error{color:#b00020;font-weight:700;margin-top:10px}
    .glass, .glass * { color: inherit }
  </style>
</head>
<body>
  <div class="page">
    <div class="glass">
      <div class="topnav">
        <a class="btn-pill" href="<%=ctx%>/requestsubordinatesservlet1">← Đơn cấp dưới</a>
        <a class="btn-pill" href="<%=ctx%>/requestlistmyservlet1">Đơn của tôi</a>
        <a class="btn-pill" href="<%=ctx%>/logoutservlet1">Đăng xuất</a>
      </div>

      <div class="title">Duyệt đơn xin nghỉ phép</div>

      <div class="row"><div class="label">Duyệt bởi User:</div>
        <div><strong><%= approverName %></strong>, Role: <strong><%= approverRole %></strong></div></div>
      <div class="row"><div class="label">Tạo bởi:</div> <div><strong><%= creatorName %></strong></div></div>
      <div class="row"><div class="label">Từ ngày:</div> <div><%= r.getFrom() %></div></div>
      <div class="row"><div class="label">Tới ngày:</div><div><%= r.getTo() %></div></div>

      <!-- Một FORM – hai nút với 2 đích khác nhau -->
      <form method="post" action="<%=ctx%>/requestapproveservlet1">
        <input type="hidden" name="id" value="<%= r.getId() %>">

        <div class="row" style="grid-template-columns:140px 1fr;">
          <div class="label">Lý do:</div>
          <textarea name="note" placeholder="Nhập lý do / ghi chú..."></textarea>
        </div>

        <div class="actions">
          <!-- Gửi sang reject -->
          <button type="submit"
                  class="btn-reject"
                  formaction="<%=ctx%>/requestrejectservlet1">Reject</button>

          <!-- Mặc định action của form là approve -->
          <button type="submit" class="btn-approve">Approve</button>
        </div>

        <% if (err != null) { %>
          <div class="error"><%= err %></div>
        <% } %>
      </form>
    </div>
  </div>
</body>
</html>