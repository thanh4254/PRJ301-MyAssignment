<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, java.text.SimpleDateFormat" %>
<%
  String ctx = request.getContextPath();

  @SuppressWarnings("unchecked")
  List<Map<String,Object>> items =
      (List<Map<String,Object>>) request.getAttribute("items");
  if (items == null) items = java.util.Collections.emptyList();

  // SEARCH + PAGINATION (giống trang request_subordinates.jsp)
  String q = (String) request.getAttribute("q");
  if (q == null) q = "";
  int curPage    = (request.getAttribute("page")!=null) ? (Integer)request.getAttribute("page") : 1;
  int totalPages = (request.getAttribute("totalPages")!=null) ? (Integer)request.getAttribute("totalPages") : 1;

  String baseAdmin = ctx + "/admin/reset-requests";
  String qParam = "";
  try { qParam = (!q.isBlank()) ? ("&q=" + java.net.URLEncoder.encode(q,"UTF-8")) : ""; }
  catch (Exception ignore) {}

  final int pageSize = 5;
  int startNo = (curPage - 1) * pageSize;
  SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <title>Duyệt yêu cầu đặt lại mật khẩu</title>
  <link rel="stylesheet" href="<%=ctx%>/css/theme.css" />
  <style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap');

    /* layout đồng bộ với request_subordinates.jsp */
    html, body { height:100%; margin:0; overflow:hidden; font-family:Inter,system-ui,Arial,sans-serif; }
    .page{ max-width:1100px; height:100%; margin:0 auto; padding:28px 16px; box-sizing:border-box; }
    .glass{
      color:#0f172a; background:rgba(255,255,255,.08); border:1px solid rgba(255,255,255,.12);
      border-radius:18px; padding:18px; box-shadow:0 18px 50px rgba(0,0,0,.35), inset 0 0 0 1px rgba(255,255,255,.08);
      backdrop-filter:blur(14px); display:flex; flex-direction:column; height:100%;
    }
    .title{margin:6px 2px 14px; font-size:24px; font-weight:800}

    /* nút bạc */
    .btn-pill{
      appearance:none; display:inline-flex; align-items:center; gap:8px;
      padding:10px 16px; border-radius:14px; font-weight:800; text-decoration:none;
      color:#0f172a; background:rgba(255,255,255,.35); border:1px solid rgba(15,23,42,.22);
      box-shadow:0 6px 16px rgba(0,0,0,.18); cursor:pointer;
    }

    .topnav{display:flex; gap:10px; flex-wrap:wrap; margin-bottom:10px}

    /* thanh search giống hệt */
    .search-bar{display:flex; gap:8px; align-items:center; margin:4px 0 14px 0}
    .search-input{
      padding:10px 12px; border-radius:12px; border:1px solid rgba(15,23,42,.22);
      background:rgba(255,255,255,.55); color:#0f172a; min-width:260px; flex:1;
    }

    /* bàn */
    .table-wrap{ flex:1 1 auto; overflow:auto; border-radius:12px; }
    .tbl{width:100%; border-collapse:collapse; table-layout:fixed}
    .tbl th,.tbl td{padding:12px; border:1px solid rgba(15,23,42,.18); word-wrap:break-word}
    .tbl th{ background:#0f172a; color:#fff; text-align:left; font-weight:700; position:sticky; top:0; z-index:2; }
    .tbl tr:nth-child(odd){background:rgba(255,255,255,.06)}
    .tbl tr:nth-child(even){background:rgba(255,255,255,.10)}

    /* cụm hành động luôn hiển thị */
    .actions{display:flex; gap:8px; flex-wrap:wrap; align-items:center; justify-content:flex-start}
    .btn-approve,.btn-deny{
      border:0; border-radius:10px; padding:10px 14px; font-weight:800; cursor:pointer;
      box-shadow:0 6px 12px rgba(0,0,0,.18); min-width:102px; display:inline-flex; justify-content:center;
    }
    .btn-approve{ background:#16a34a; color:#fff }
    .btn-deny{  background:#dc2626; color:#fff }
    .btn-approve:hover,.btn-deny:hover{ filter:brightness(1.05) }

    .muted{opacity:.85; text-align:center}

    .pager{display:flex;gap:10px;align-items:center;justify-content:flex-end;margin-top:12px}

    /* responsive giống trang cấp dưới */
    @media (max-width: 860px){
      .btn-approve,.btn-deny{min-width:92px; padding:9px 12px}
    }
    @media (max-width: 640px){
      .actions{flex-direction:column; align-items:stretch}
      .btn-approve,.btn-deny{width:100%}
    }
  </style>
</head>
<body>
<div class="page">
  <div class="glass">
    <div class="title">Duyệt yêu cầu đặt lại mật khẩu</div>

    <div class="topnav">
      <a class="btn-pill" href="<%=ctx%>/requestlistmyservlet1">← Trang chính</a>
      <a class="btn-pill" href="<%=ctx%>/logoutservlet1">Đăng xuất</a>
    </div>

    <!-- Search -->
    <form class="search-bar" method="get" action="<%=baseAdmin%>">
      <input class="search-input" type="text" name="q"
             placeholder="Tìm theo tên người tạo (Username)" value="<%= q %>">
      <input type="hidden" name="page" value="1">
      <button class="btn-pill" type="submit">Search</button>
      <a class="btn-pill" href="<%=baseAdmin%>">Clear</a>
    </form>

    <div class="table-wrap">
      <table class="tbl">
        <thead>
          <tr>
            <th style="width:10%">#</th>
            <th style="width:30%">Username</th>
            <th style="width:28%">RequestedAt</th>
            <th style="width:32%">Thao tác</th>
          </tr>
        </thead>
        <tbody>
        <% if (items.isEmpty()) { %>
          <tr><td colspan="4" class="muted">
            <%= q.isBlank() ? "Không có yêu cầu PENDING" : ("Không có yêu cầu khớp \""+q+"\"") %>
          </td></tr>
        <% } else {
             int row = 0;
             for (Map<String,Object> m : items) {
               int no = startNo + (++row);
               String uname = String.valueOf(m.get("username"));
               java.sql.Timestamp ts = (java.sql.Timestamp) m.get("requestedAt");
               String tsText = (ts==null) ? "" : df.format(ts);
               int reqId = (Integer) m.get("id");
        %>
          <tr>
            <td><%= no %></td>
            <td><%= uname %></td>
            <td><%= tsText %></td>
            <td>
              <div class="actions">
                <form action="<%=baseAdmin%>" method="post">
                  <input type="hidden" name="id" value="<%=reqId%>">
                  <input type="hidden" name="action" value="approve">
                  <button class="btn-approve" type="submit">Approve</button>
                </form>
                <form action="<%=baseAdmin%>" method="post">
                  <input type="hidden" name="id" value="<%=reqId%>">
                  <input type="hidden" name="action" value="deny">
                  <button class="btn-deny" type="submit">Deny</button>
                </form>
              </div>
            </td>
          </tr>
        <% } } %>
        </tbody>
      </table>
    </div>

    <!-- Pager giữ tham số q -->
    <div class="pager">
      <a class="btn-pill" style="<%= (curPage<=1) ? "pointer-events:none;opacity:.45" : "" %>"
         href="<%= baseAdmin %>?page=<%= curPage-1 %><%= qParam %>">← Trước</a>
      <div><b>Trang <%= curPage %></b>/<%= totalPages %></div>
      <a class="btn-pill" style="<%= (curPage>=totalPages) ? "pointer-events:none;opacity:.45" : "" %>"
         href="<%= baseAdmin %>?page=<%= curPage+1 %><%= qParam %>">Sau →</a>
    </div>

    <p class="muted"><%= request.getAttribute("error")!=null ? request.getAttribute("error") : "" %></p>
  </div>
</div>
</body>
</html>
