<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, model.Request" %>
<%
  String ctx = request.getContextPath();

  @SuppressWarnings("unchecked")
  List<Request> items = (List<Request>) request.getAttribute("items");
  if (items == null) items = java.util.Collections.emptyList();

  @SuppressWarnings("unchecked")
  Map<Integer,String> names = (Map<Integer,String>) request.getAttribute("names");
  if (names == null) names = java.util.Collections.emptyMap();

  int curPage    = (request.getAttribute("page")       != null) ? (Integer) request.getAttribute("page")       : 1;
  int totalPages = (request.getAttribute("totalPages") != null) ? (Integer) request.getAttribute("totalPages") : 1;

  // servlet đã set showAgenda = PermissionUtil.isDepartmentHead(me)
  Boolean showAgendaAttr = (Boolean) request.getAttribute("showAgenda");
  boolean showAgenda = (showAgendaAttr != null && showAgendaAttr);
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Đơn của tôi</title>
  <link rel="stylesheet" href="<%=ctx%>/css/theme.css">
  <style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap');
    html,body{font-family:Inter,system-ui,Arial,sans-serif}
    .page{max-width:1100px;margin:28px auto;padding:0 16px}
    .glass{color:#0f172a;background:rgba(255,255,255,.08);border-radius:18px;padding:18px;
      box-shadow:0 18px 50px rgba(0,0,0,.35), inset 0 0 0 1px rgba(255,255,255,.08);
      backdrop-filter:blur(14px);border:1px solid rgba(255,255,255,.12)}
    .title{margin:6px 2px 14px;font-weight:800;font-size:28px}
    .topnav{display:flex;gap:10px;flex-wrap:wrap;margin-bottom:14px}
    .btn-pill{display:inline-block;padding:10px 14px;border-radius:12px;
      background:rgba(255,255,255,.35);border:1px solid rgba(15,23,42,.15);
      color:#0f172a;text-decoration:none;font-weight:700;transition:filter .15s,transform .02s}
    .btn-pill:hover{filter:brightness(1.05)} .btn-pill:active{transform:translateY(1px)}
    .tbl{width:100%;border-collapse:collapse;table-layout:fixed}
    .tbl th,.tbl td{padding:12px;border:1px solid rgba(15,23,42,.15);word-wrap:break-word}
    .tbl th{background:rgba(255,255,255,.55);color:#0f172a;text-align:left;font-weight:800;position:sticky;top:0;z-index:1}
    .tbl tr:nth-child(odd){background:rgba(255,255,255,.30)}
    .tbl tr:nth-child(even){background:rgba(255,255,255,.18)}
    .link-title{color:#0f172a;font-weight:800;text-decoration:none}
    .link-title:hover{text-decoration:underline}
    .muted{text-align:center;padding:12px;color:#0f172a;opacity:.9}
    .err{color:#b00020;font-weight:700;margin-top:10px}
    .status-pill{font-weight:800}
    .status-new{color:#d97706}.status-ok{color:#16a34a}.status-bad{color:#dc2626}
    .pager{display:flex;gap:10px;align-items:center;justify-content:flex-end;margin-top:12px}
  </style>
</head>
<body>
<div class="page">
  <div class="glass">
    <div class="title">Đơn của tôi</div>

    <div class="topnav">
      <a class="btn-pill" href="<%=ctx%>/requestcreateservlet1">+ Tạo đơn</a>
      <a class="btn-pill" href="<%=ctx%>/requestsubordinatesservlet1">Đơn cấp dưới</a>
      <% if (showAgenda) { %>
        <a class="btn-pill" href="<%=ctx%>/agendaservlet1">Agenda phòng</a>
      <% } %>
      <a class="btn-pill" href="<%=ctx%>/logoutservlet1">Đăng xuất</a>
    </div>

    <table class="tbl">
      <thead>
        <tr>
          <th>Title</th>
          <th>From</th>
          <th>To</th>
          <th>Status</th>
          <th>Processed By</th>
          <th>Note</th>
        </tr>
      </thead>
      <tbody>
      <% if (items.isEmpty()) { %>
        <tr><td colspan="6" class="muted">Chưa có đơn nào</td></tr>
      <% } else {
           for (Request r : items) {
             String processedName = (r.getProcessedBy()==null) ? "" :
               names.getOrDefault(r.getProcessedBy(), String.valueOf(r.getProcessedBy()));
             String raw = String.valueOf(r.getStatus());
             String statusLabel, statusClass="status-pill";
             if ("NEW".equals(raw)) { statusLabel="In-progress"; statusClass+=" status-new"; }
             else if ("APPROVED".equals(raw)) { statusLabel="Approved"; statusClass+=" status-ok"; }
             else if ("REJECTED".equals(raw)) { statusLabel="Rejected"; statusClass+=" status-bad"; }
             else { statusLabel = raw; }
      %>
        <tr>
          <td><a class="link-title" href="<%=ctx%>/requestdetailservlet1?id=<%=r.getId()%>"><%= r.getTitle() %></a></td>
          <td><%= r.getFrom() %></td>
          <td><%= r.getTo() %></td>
          <td><span class="<%= statusClass %>"><%= statusLabel %></span></td>
          <td><%= processedName %></td>
          <td><%= r.getProcessedNote()==null? "" : r.getProcessedNote() %></td>
        </tr>
      <% } } %>
      </tbody>
    </table>

    <!-- Phân trang -->
    <div class="pager">
      <a class="btn-pill" style="<%= (curPage<=1) ? "pointer-events:none;opacity:.45" : "" %>"
         href="<%= ctx %>/requestlistmyservlet1?page=<%= curPage-1 %>">← Trước</a>
      <div><b>Trang <%= curPage %></b>/<%= totalPages %></div>
      <a class="btn-pill" style="<%= (curPage>=totalPages) ? "pointer-events:none;opacity:.45" : "" %>"
         href="<%= ctx %>/requestlistmyservlet1?page=<%= curPage+1 %>">Sau →</a>
    </div>

    <p class="err"><%= request.getAttribute("error")!=null ? request.getAttribute("error") : "" %></p>
  </div>
</div>
</body>
</html>
