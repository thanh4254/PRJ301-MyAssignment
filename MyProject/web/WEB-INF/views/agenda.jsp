<%-- 
    Document   : agenda
    Created on : Oct 12, 2025, 5:24:29 PM
    Author     : Admin
--%>

<%-- WEB-INF/views/agenda.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, java.time.*, java.time.format.DateTimeFormatter, model.User" %>
<%
  String ctx = request.getContextPath();

  @SuppressWarnings("unchecked")
  List<User> members = (List<User>) request.getAttribute("members");
  if (members == null) members = Collections.emptyList();

  @SuppressWarnings("unchecked")
  List<LocalDate> days = (List<LocalDate>) request.getAttribute("days");
  if (days == null) days = Collections.emptyList();

  @SuppressWarnings("unchecked")
  Map<Integer, Set<String>> offByUser =
      (Map<Integer, Set<String>>) request.getAttribute("offByUser");
  if (offByUser == null) offByUser = Collections.emptyMap();

  DateTimeFormatter keyFmt   = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  DateTimeFormatter headFmt  = DateTimeFormatter.ofPattern("yyyy-MM-dd");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Agenda phòng ban</title>

  <!-- nền dùng chung (background.jpg) -->
  <link rel="stylesheet" href="<%=ctx%>/css/theme.css"/>

  <style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap');
    html,body{font-family:Inter,system-ui,Arial,sans-serif}

    .page{max-width:1100px;margin:28px auto;padding:0 16px}

    /* Khung glass (giống list/create) – chữ đen */
    .glass{
      color:#0f172a;
      background:rgba(255,255,255,.08);
      border:1px solid rgba(255,255,255,.12);
      border-radius:18px; padding:18px;
      box-shadow:0 18px 50px rgba(0,0,0,.35), inset 0 0 0 1px rgba(255,255,255,.08);
      backdrop-filter:blur(14px);
    }
    .title{margin:6px 2px 14px; font-size:24px; font-weight:800}

    /* Nút pill như các trang khác */
    .topnav{display:flex; gap:10px; flex-wrap:wrap; margin-bottom:14px}
    .btn-pill{
      appearance:none; display:inline-flex; align-items:center; gap:8px;
      padding:10px 16px; border-radius:14px; font-weight:800; text-decoration:none; cursor:pointer;
      color:#0f172a; background:rgba(255,255,255,.35);
      border:1px solid rgba(15,23,42,.22);
      box-shadow:0 6px 16px rgba(0,0,0,.18); backdrop-filter:blur(6px);
      transition:filter .15s, transform .02s;
    }
    .btn-pill:hover{filter:brightness(1.05)} .btn-pill:active{transform:translateY(1px)}

    /* Bộ lọc ngày – cùng tông sáng chữ đen */
    .filters{display:flex; gap:10px; align-items:center; flex-wrap:wrap; margin-bottom:12px}
    .inbox, .btn{
      color:#0f172a; background:rgba(255,255,255,.55);
      border:1px solid rgba(15,23,42,.18); border-radius:10px;
      padding:10px 12px; outline:none; font-weight:600;
    }
    .btn{ cursor:pointer; }
    .btn:hover{ filter:brightness(1.05) }

    /* Bảng agenda */
    .table-wrap{overflow:auto; border-radius:12px}
    .agenda{border-collapse:collapse; width:100%; table-layout:fixed; min-width:900px}
    .agenda th, .agenda td{
      padding:10px 8px; border:1px solid rgba(15,23,42,.18); text-align:center; word-wrap:break-word
    }
    .agenda th:first-child, .agenda td:first-child{ text-align:left; width:220px }
    .agenda thead th{
      background:#0f172a; color:#fff; position:sticky; top:0; z-index:1; text-align:center;
    }
    .agenda tbody tr:nth-child(odd){ background:rgba(255,255,255,.06) }
    .agenda tbody tr:nth-child(even){ background:rgba(255,255,255,.10) }
    .agenda td{ color:#0f172a }

    /* màu cho cell */
    .cell-work{ background:rgba(96,196,120,.28) }  /* xanh nhạt đi làm */
    .cell-off { background:rgba(239,83,80,.28); font-weight:700 } /* đỏ nhạt nghỉ */

    /* Chú thích (legend) */
    .legend{ display:flex; gap:18px; align-items:center; margin:8px 0 14px; font-size:14px }
    .legend .box{ width:18px; height:12px; border:1px solid rgba(15,23,42,.25); border-radius:3px; display:inline-block; vertical-align:middle; margin-right:6px }
    .legend .work{ background:rgba(96,196,120,.28) }
    .legend .off { background:rgba(239,83,80,.28) }

    /* đảm bảo kế thừa màu chữ đen */
    .glass, .glass * { color: inherit }
  </style>
</head>
<body>
<div class="page">
  <div class="glass">

    <div class="title">Agenda phòng ban</div>

    <div class="topnav">
      <a class="btn-pill" href="<%=ctx%>/requestsubordinatesservlet1">Đơn cấp dưới</a>
      <a class="btn-pill" href="<%=ctx%>/requestlistmyservlet1">Đơn của tôi</a>
      <a class="btn-pill" href="<%=ctx%>/logoutservlet1">Đăng xuất</a>
    </div>

    <form class="filters" method="get" action="<%=ctx%>/agendaservlet1">
      <label>Từ:</label>
      <input class="inbox" type="date" name="from" value="${param.from}">
      <label>Đến:</label>
      <input class="inbox" type="date" name="to" value="${param.to}">
      <button class="btn" type="submit">Xem</button>
    </form>

    <div class="legend">
      <span><span class="box work"></span>Đi làm</span>
      <span><span class="box off"></span>Nghỉ phép (Approved)</span>
    </div>

    <div class="table-wrap">
      <table class="agenda">
        <thead>
          <tr>
            <th>Nhân sự</th>
            <% for (LocalDate d : days) { %>
              <th><%= d.format(headFmt) %></th>
            <% } %>
          </tr>
        </thead>
        <tbody>
          <% for (User u : members) {
               Set<String> offs = offByUser.getOrDefault(u.getId(), Collections.emptySet());
          %>
            <tr>
              <td>
                <div><b><%= u.getFullName() %></b></div>
                <div style="font-size:12px;opacity:.75">(<%= u.getUsername() %>)</div>
              </td>
              <% for (LocalDate d : days) {
                   String key = d.format(keyFmt);
                   boolean isOff = offs.contains(key);
              %>
                <td class="<%= isOff ? "cell-off" : "cell-work" %>">
                  <%= isOff ? "OFF" : "" %>
                </td>
              <% } %>
            </tr>
          <% } %>
        </tbody>
      </table>
    </div>

  </div>
</div>
</body>
</html>