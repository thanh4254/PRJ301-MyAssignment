<%-- 
    Document   : request_subordinates
    Created on : Oct 12, 2025, 3:45:07 PM
    Author     : Admin
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, model.Request" %>
<%
  String ctx = request.getContextPath();

  @SuppressWarnings("unchecked")
  List<Request> items = (List<Request>) request.getAttribute("items");
  if (items == null) items = Collections.emptyList();

  @SuppressWarnings("unchecked")
  Map<Integer,String> creatorNames   = (Map<Integer,String>) request.getAttribute("creatorNames");
  if (creatorNames == null) creatorNames = Collections.emptyMap();

  @SuppressWarnings("unchecked")
  Map<Integer,String> processorNames = (Map<Integer,String>) request.getAttribute("processorNames");
  if (processorNames == null) processorNames = Collections.emptyMap();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <title>Đơn của cấp dưới</title>
  <link rel="stylesheet" href="<%=ctx%>/css/theme.css" />
  <style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap');

    /* Khoá cuộn nền, chỉ bảng được cuộn */
    html, body { height:100%; margin:0; overflow:hidden; font-family:Inter,system-ui,Arial,sans-serif; }

    .page{ max-width:1100px; height:100%; margin:0 auto; padding:28px 16px; box-sizing:border-box; }

    .glass{
      color:#0f172a; background:rgba(255,255,255,.08);
      border:1px solid rgba(255,255,255,.12); border-radius:18px; padding:18px;
      box-shadow:0 18px 50px rgba(0,0,0,.35), inset 0 0 0 1px rgba(255,255,255,.08);
      backdrop-filter:blur(14px); display:flex; flex-direction:column; height:100%;
    }
    .title{margin:6px 2px 14px; font-size:24px; font-weight:800}

    .topnav{display:flex; gap:10px; flex-wrap:wrap; margin-bottom:14px}
    .btn-pill{
      appearance:none; display:inline-flex; align-items:center; gap:8px;
      padding:10px 16px; border-radius:14px; font-weight:800; text-decoration:none;
      color:#0f172a; background:rgba(255,255,255,.35);
      border:1px solid rgba(15,23,42,.22);
      box-shadow:0 6px 16px rgba(0,0,0,.18); backdrop-filter:blur(6px);
      cursor:pointer; transition:filter .15s, transform .02s;
    }
    .btn-pill:hover{filter:brightness(1.05)} .btn-pill:active{transform:translateY(1px)}

    .table-wrap{ flex:1 1 auto; height: calc(100vh - 180px); overflow:auto; border-radius:12px; }

    .tbl{width:100%; border-collapse:collapse; table-layout:fixed}
    .tbl th,.tbl td{padding:12px; border:1px solid rgba(15,23,42,.18); word-wrap:break-word}
    .tbl th{
      background:#0f172a; color:#fff; text-align:left; font-weight:700;
      position:sticky; top:0; z-index:2;
    }
    .tbl tr:nth-child(odd){background:rgba(255,255,255,.06)}
    .tbl tr:nth-child(even){background:rgba(255,255,255,.10)}
    .tbl td{color:#0f172a}
    .tbl a{color:#0b67ff; text-decoration:none; font-weight:700}
    .tbl a:hover{ text-decoration:underline }

    .note-input{
      width:100%; box-sizing:border-box; color:#0f172a;
      background:rgba(255,255,255,.55); border:1px solid rgba(15,23,42,.18);
      border-radius:10px; padding:10px 12px; outline:none;
    }
    .actions{display:flex; gap:8px; flex-wrap:wrap; margin-top:8px}
    .btn-approve{background:#16a34a; color:#fff}
    .btn-reject{background:#dc2626; color:#fff}
    .btn-approve,.btn-reject{ border:0; border-radius:10px; padding:10px 14px; font-weight:800; cursor:pointer; box-shadow:0 6px 12px rgba(0,0,0,.18); }

    /* Status colors */
    .status-cell{ font-weight:800; white-space:nowrap; }
    .status-new{  color:#d97706; } /* In-progress */
    .status-ok{   color:#16a34a; } /* Approved    */
    .status-bad{  color:#dc2626; } /* Rejected    */
  </style>
</head>
<body>
<div class="page">
  <div class="glass">
    <div class="title">Đơn của cấp dưới</div>

    <div class="topnav">
      <a class="btn-pill" href="<%=ctx%>/requestlistmyservlet1">← Đơn của tôi</a>
      <a class="btn-pill" href="<%=ctx%>/logoutservlet1">Đăng xuất</a>
    </div>

    <div class="table-wrap">
      <table class="tbl">
        <thead>
          <tr>
            <th style="width:22%">Title</th>
            <th style="width:12%">From</th>
            <th style="width:12%">To</th>
            <th style="width:14%">Created By</th>
            <th style="width:10%">Status</th>
            <th style="width:14%">Processed By</th>
            <th style="width:16%">Thao tác</th>
          </tr>
        </thead>
        <tbody>
        <%
          if (items.isEmpty()) {
        %>
          <tr><td colspan="7" style="text-align:center;opacity:.85">Chưa có đơn nào</td></tr>
        <%
          } else {
            for (Request r : items) {
              String creator   = creatorNames.getOrDefault(r.getCreatedBy(), String.valueOf(r.getCreatedBy()));
              String processor = (r.getProcessedBy()==null) ? "" :
                                 processorNames.getOrDefault(r.getProcessedBy(), String.valueOf(r.getProcessedBy()));

              String raw  = String.valueOf(r.getStatus());
              String norm = raw == null ? "" : raw.trim().toUpperCase(java.util.Locale.ROOT);
              String statusLabel, statusClass;
              switch (norm) {
                case "NEW":      statusLabel = "In-progress"; statusClass = "status-cell status-new"; break;
                case "APPROVED": statusLabel = "Approved";    statusClass = "status-cell status-ok";  break;
                case "REJECTED": statusLabel = "Rejected";    statusClass = "status-cell status-bad"; break;
                default:         statusLabel = raw;           statusClass = "status-cell";            break;
              }
        %>
          <tr>
            <td>
              <a href="<%= ctx %>/requestdetailservlet1?id=<%= r.getId() %>"><%= r.getTitle() %></a>
            </td>
            <td><%= r.getFrom() %></td>
            <td><%= r.getTo() %></td>
            <td><%= creator %></td>
            <td class="<%= statusClass %>"><%= statusLabel %></td>
            <td><%= processor %></td>
            <td>
              <!-- APPROVE -->
              <form method="post" action="<%=ctx%>/requestapproveservlet1">
                <input type="hidden" name="id" value="<%= r.getId() %>">
                <input class="note-input" name="note" placeholder="Ghi chú phê duyệt">
                <button type="submit" class="btn-approve">Approve</button>
              </form>

              <!-- REJECT -->
              <form method="post" action="<%=ctx%>/requestrejectservlet1" style="margin-top:8px">
                <input type="hidden" name="id" value="<%= r.getId() %>">
                <input class="note-input" name="note" placeholder="Lý do từ chối">
                <button type="submit" class="btn-reject">Reject</button>
              </form>
            </td>
          </tr>
        <%
            }
          }
        %>
        </tbody>
      </table>
    </div>

    <p style="color:#b00020;font-weight:700;margin-top:10px">
      <%= request.getAttribute("error")!=null ? request.getAttribute("error") : "" %>
    </p>
  </div>
</div>
</body>
</html>