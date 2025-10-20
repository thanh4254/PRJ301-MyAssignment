<%-- 
    Document   : request_detail
    Created on : Oct 12, 2025, 5:29:30 PM
    Author     : Admin
--%>

<%-- WEB-INF/views/request_detail.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="model.Request,model.RequestStatus" %>
<%
  String ctx = request.getContextPath();
  Request r = (Request) request.getAttribute("item");

  String approverName = (String) request.getAttribute("approverName"); // rỗng nếu NEW
  String approverRole = (String) request.getAttribute("approverRole"); // rỗng nếu NEW
  String creatorName  = (String) request.getAttribute("creatorName");

  Boolean canApproveObj = (Boolean) request.getAttribute("canApprove");
  boolean canApprove = (canApproveObj != null && canApproveObj);

  // status mapping
  String raw = String.valueOf(r.getStatus());
  String statusLabel, statusClass;
  switch (raw) {
    case "NEW":      statusLabel = "In-progress"; statusClass = "st-new"; break;
    case "APPROVED": statusLabel = "Approved";    statusClass = "st-ok";  break;
    case "REJECTED": statusLabel = "Rejected";    statusClass = "st-bad"; break;
    default:         statusLabel = raw;           statusClass = "";
  }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Duyệt đơn xin nghỉ phép</title>
  <link rel="stylesheet" href="<%=ctx%>/css/theme.css"/>
  <style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;700;800&display=swap');
    html,body{font-family:Inter,system-ui,Arial,sans-serif}

    .page{max-width:900px;margin:28px auto;padding:0 16px}
    .glass{
      color:#0f172a;background:rgba(255,255,255,.08);
      border:1px solid rgba(255,255,255,.12);
      border-radius:18px;padding:18px;
      box-shadow:0 18px 50px rgba(0,0,0,.35), inset 0 0 0 1px rgba(255,255,255,.08);
      backdrop-filter:blur(14px)
    }

    .pills{display:flex;gap:12px;margin-bottom:14px;flex-wrap:wrap}
    .pill{
      display:inline-flex;align-items:center;gap:8px;
      padding:10px 16px;border-radius:14px;font-weight:800;text-decoration:none;
      color:#0f172a;background:rgba(255,255,255,.35);
      border:1px solid rgba(15,23,42,.22);box-shadow:0 6px 16px rgba(0,0,0,.18)
    }

    .title{margin:6px 2px 14px;font-size:28px;font-weight:800}
    .row{display:grid;grid-template-columns:140px 1fr;gap:10px 12px;margin:10px 0;align-items:center}
    .label{font-weight:700}

    textarea{
      width:100%;min-height:160px;resize:vertical;
      color:#0f172a;background:rgba(255,255,255,.55);
      border:1px solid rgba(15,23,42,.18);border-radius:10px;padding:10px 12px
    }
    textarea[readonly]{opacity:.7;cursor:not-allowed}

    .actions{display:flex;gap:12px;justify-content:flex-end;margin-top:12px}
    .btn{appearance:none;border:0;border-radius:12px;padding:12px 20px;font-weight:800;cursor:pointer}
    .btn-approve{background:#16a34a;color:#fff}
    .btn-reject{background:#dc2626;color:#fff}

    .muted{opacity:.85}
    .status-pill{font-weight:800}
    .st-new{color:#d97706}
    .st-ok{color:#16a34a}
    .st-bad{color:#dc2626}
  </style>
</head>
<body>
<div class="page">
  <div class="glass">
    <div class="pills">
      <a class="pill" href="<%=ctx%>/requestsubordinatesservlet1">← Đơn cấp dưới</a>
      <a class="pill" href="<%=ctx%>/requestlistmyservlet1">Đơn của tôi</a>
      <a class="pill" href="<%=ctx%>/logoutservlet1">Đăng xuất</a>
    </div>

    <div class="title">Duyệt đơn xin nghỉ phép</div>

    <%-- Chỉ hiển thị người duyệt khi đơn đã được xử lý --%>
    <% if (approverName != null && !approverName.isEmpty()) { %>
      <div class="row">
        <div class="label">Duyệt bởi User:</div>
        <div>
          <strong><%= approverName %></strong>
          <% if (approverRole != null && !approverRole.isEmpty()) { %>
            , Role: <strong><%= approverRole %></strong>
          <% } %>
        </div>
      </div>
    <% } %>

    <div class="row"><div class="label">Tạo bởi:</div><div><strong><%= creatorName %></strong></div></div>
    <div class="row"><div class="label">Từ ngày:</div><div><%= r.getFrom() %></div></div>
    <div class="row"><div class="label">Tới ngày:</div><div><%= r.getTo() %></div></div>

    <form method="post">
      <input type="hidden" name="id" value="<%= r.getId() %>">

      <div class="row" style="grid-template-columns:140px 1fr;">
        <div class="label">Lý do:</div>
        <textarea name="note" placeholder="Nhập lý do / ghi chú..." <%= canApprove ? "" : "readonly" %>></textarea>
      </div>

      <% if (canApprove) { %>
        <div class="actions">
          <button class="btn btn-reject"
                  type="submit"
                  formaction="<%=ctx%>/requestrejectservlet1">Reject</button>
          <button class="btn btn-approve"
                  type="submit"
                  formaction="<%=ctx%>/requestapproveservlet1">Approve</button>
        </div>
      <% } else { %>
        <div class="row">
          <div class="label">Trạng thái:</div>
          <div class="status-pill <%= statusClass %>"><%= statusLabel %></div>
        </div>
      <% } %>

      <p class="muted">
        <%= request.getAttribute("error")!=null ? request.getAttribute("error") : "" %>
      </p>
    </form>
  </div>
</div>
</body>
</html>