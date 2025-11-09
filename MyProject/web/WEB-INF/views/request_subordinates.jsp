<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, java.net.URLEncoder, model.Request" %>
<%
  String ctx = request.getContextPath();

  // ===== DATA từ servlet =====
  @SuppressWarnings("unchecked")
  List<Request> items = (List<Request>) request.getAttribute("items");
  if (items == null) items = Collections.emptyList();

  @SuppressWarnings("unchecked")
  Map<Integer,String> creatorNames = (Map<Integer,String>) request.getAttribute("creatorNames");
  if (creatorNames == null) creatorNames = Collections.emptyMap();

  @SuppressWarnings("unchecked")
  Map<Integer,String> processorNames = (Map<Integer,String>) request.getAttribute("processorNames");
  if (processorNames == null) processorNames = Collections.emptyMap();

  String q = (String) request.getAttribute("q");
  if (q == null) q = "";

  // KHÔNG dùng tên 'page' (trùng implicit object)
  int curPage    = (request.getAttribute("page") != null) ? (Integer) request.getAttribute("page") : 1;
  int totalPages = (request.getAttribute("totalPages") != null) ? (Integer) request.getAttribute("totalPages") : 1;

  // Chỉ Head / có feature AGD mới xem agenda
  Boolean showAgendaAttr = (Boolean) request.getAttribute("showAgenda");
  boolean showAgenda = (showAgendaAttr != null) && showAgendaAttr.booleanValue();

  String err = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Đơn của cấp dưới</title>
  <link rel="stylesheet" href="<%=ctx%>/css/theme.css">
  <style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap');
    html,body{font-family:Inter,system-ui,Arial,sans-serif}
    .page{max-width:1100px;margin:28px auto;padding:0 16px}
    .glass{color:#0f172a;background:rgba(255,255,255,.08);border-radius:18px;padding:18px;
      box-shadow:0 18px 50px rgba(0,0,0,.35), inset 0 0 0 1px rgba(255,255,255,.08);
      border:1px solid rgba(255,255,255,.12); backdrop-filter:blur(14px)}
    .title{margin:6px 2px 14px;font-weight:800;font-size:28px}
    .topnav{display:flex;gap:10px;flex-wrap:wrap;margin-bottom:14px}
    .btn-pill{display:inline-block;padding:10px 14px;border-radius:12px;
      background:rgba(255,255,255,.35);border:1px solid rgba(15,23,42,.15);
      color:#0f172a;text-decoration:none;font-weight:700;transition:filter .15s,transform .02s}
    .btn-pill:hover{filter:brightness(1.05)} .btn-pill:active{transform:translateY(1px)}
    .searchbar{display:flex;gap:10px;align-items:center;margin-bottom:12px;flex-wrap:wrap}
    .inp{padding:10px 12px;border-radius:12px;border:1px solid rgba(15,23,42,.2);background:rgba(255,255,255,.6);min-width:280px}
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
    .status-new{color:#d97706!important}.status-ok{color:#16a34a!important}.status-bad{color:#dc2626!important}
    .pager{display:flex;gap:10px;align-items:center;justify-content:flex-end;margin-top:12px}
  </style>
</head>
<body>
<div class="page">
  <div class="glass">
    <div class="title">Đơn của cấp dưới</div>

    <!-- NAV -->
    <div class="topnav">
      <a class="btn-pill" href="<%=ctx%>/requestlistmyservlet1">← Đơn của tôi</a>
      <% if (showAgenda) { %>
        <a class="btn-pill" href="<%=ctx%>/agendaservlet1">Agenda phòng</a>
      <% } %>
      <a class="btn-pill" href="<%=ctx%>/logoutservlet1">Đăng xuất</a>
    </div>

    <!-- SEARCH -->
    <form method="get" class="searchbar" action="<%=ctx%>/requestsubordinatesservlet1">
      <input class="inp" type="text" name="q" placeholder="Tìm theo tên người tạo (Created By)" value="<%=q%>">
      <button class="btn-pill" type="submit">Search</button>
      <a class="btn-pill" href="<%=ctx%>/requestsubordinatesservlet1">Clear</a>
    </form>

    <!-- TABLE -->
    <table class="tbl">
      <thead>
      <tr>
        <th>Title</th>
        <th>From</th>
        <th>To</th>
        <th>Created By</th>
        <th>Status</th>
      </tr>
      </thead>
      <tbody>
      <% if (items.isEmpty()) { %>
        <tr><td colspan="5" class="muted"><%= q.isEmpty() ? "Chưa có đơn nào" : ("Không có đơn nào khớp từ khóa \""+q+"\"") %></td></tr>
      <% } else {
           for (Request r : items) {
             String creator = creatorNames.getOrDefault(r.getCreatedBy(), String.valueOf(r.getCreatedBy()));
             String statusRaw = String.valueOf(r.getStatus());
             String label, css;
             if ("NEW".equals(statusRaw)) { label="In-progress"; css="status-pill status-new"; }
             else if ("APPROVED".equals(statusRaw)) { label="Approved"; css="status-pill status-ok"; }
             else if ("REJECTED".equals(statusRaw)) { label="Rejected"; css="status-pill status-bad"; }
             else { label=statusRaw; css="status-pill"; }
      %>
        <tr>
          <td><a class="link-title" href="<%=ctx%>/requestdetailservlet1?id=<%=r.getId()%>"><%= r.getTitle() %></a></td>
          <td><%= r.getFrom() %></td>
          <td><%= r.getTo() %></td>
          <td><%= creator %></td>
          <td><span class="<%=css%>"><%=label%></span></td>
        </tr>
      <% } } %>
      </tbody>
    </table>

    <!-- PAGER -->
    <div class="pager">
      <a class="btn-pill"
         style="<%= (curPage<=1) ? "pointer-events:none;opacity:.45" : "" %>"
         href="<%=ctx%>/requestsubordinatesservlet1?page=<%=curPage-1%><%= q.isEmpty()? "" : "&q="+URLEncoder.encode(q,"UTF-8") %>">← Trước</a>
      <div><b>Trang <%=curPage%></b>/<%=totalPages%></div>
      <a class="btn-pill"
         style="<%= (curPage>=totalPages) ? "pointer-events:none;opacity:.45" : "" %>"
         href="<%=ctx%>/requestsubordinatesservlet1?page=<%=curPage+1%><%= q.isEmpty()? "" : "&q="+URLEncoder.encode(q,"UTF-8") %>">Sau →</a>
    </div>

    <% if (err != null && !err.isEmpty()) { %>
      <p class="err"><%= err %></p>
    <% } %>
  </div>
</div>
</body>
</html>
