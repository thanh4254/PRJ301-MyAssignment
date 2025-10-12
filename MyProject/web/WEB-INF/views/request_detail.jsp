<%-- 
    Document   : request_detail
    Created on : Oct 12, 2025, 5:29:30 PM
    Author     : Admin
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="model.Request" %>
<%
  Request r = (Request) request.getAttribute("reqObj");
  String creatorName  = (String) request.getAttribute("creatorName");
  String approverName = (String) request.getAttribute("approverName");
  String approverRole = (String) request.getAttribute("approverRole");
  String cpath = request.getContextPath();
  boolean canProcess = (r != null && "IN_PROGRESS".equals(r.getStatus().name()));
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Duyệt đơn xin nghỉ</title>
  <style>
    body { font-family: Arial, sans-serif; background:#f7f7f7; }
    .links { width: 620px; margin: 20px auto 0; }
    .links a { margin-right: 14px; }
    .card {
      width: 620px; margin: 10px auto; padding: 18px 22px;
      background:#dfeefc; border:2px solid #8eb0d9; border-radius:4px;
      box-shadow: 0 1px 3px rgba(0,0,0,.08);
    }
    h2 { margin:0 0 12px 0; }
    .row { margin: 8px 0; }
    label { display:inline-block; width: 110px; vertical-align: top; }
    textarea {
      width: 100%; height: 110px; padding: 10px;
      border:1px solid #8eb0d9; border-radius:4px; background:#eef6ff;
    }
    .actions { margin-top: 14px; text-align: left; }
    .btn {
      min-width:120px; padding: 10px 18px; border:0; border-radius:8px;
      color:#fff; font-size:18px; cursor:pointer; margin-right:12px;
    }
    .btn-reject  { background:#4f6fa8; }
    .btn-approve { background:#3e73c5; }
    .btn[disabled]{ opacity:.55; cursor:not-allowed; }
  </style>
</head>
<body>

<div class="links">
  <a href="<%= cpath %>/requestsubordinatesservlet1">← Đơn cấp dưới</a>
  <a href="<%= cpath %>/requestlistmyservlet1">Đơn của tôi</a>
  <a href="<%= cpath %>/logoutservlet1">Đăng xuất</a>
</div>

<div class="card">
  <h2>Duyệt đơn xin nghỉ phép</h2>
  <div class="row"><b>Duyệt bởi</b> User: <%= approverName %> , Role: <%= approverRole %></div>
  <div class="row"><b>Tạo bởi:</b> <%= creatorName %></div>
  <div class="row"><b>Từ ngày:</b> <%= r.getFrom() %></div>
  <div class="row"><b>Tới ngày:</b> <%= r.getTo() %></div>

  <form method="post">
    <div class="row">
      <label>Lý do:</label>
      <textarea name="note" placeholder="Nhập lý do/ghi chú xử lý..."></textarea>
    </div>
    <input type="hidden" name="id" value="<%= r.getId() %>"/>

    <div class="actions">
      <button class="btn btn-reject" type="submit"
              formaction="<%= cpath %>/requestrejectservlet1"
              <%= canProcess ? "" : "disabled" %>>Reject</button>
      <button class="btn btn-approve" type="submit"
              formaction="<%= cpath %>/requestapproveservlet1"
              <%= canProcess ? "" : "disabled" %>>Approve</button>
    </div>
  </form>
</div>

</body>
</html>
