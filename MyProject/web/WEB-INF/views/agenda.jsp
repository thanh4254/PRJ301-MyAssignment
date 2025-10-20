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
  Map<Integer, Set<String>> offByUser = (Map<Integer, Set<String>>) request.getAttribute("offByUser");
  if (offByUser == null) offByUser = Collections.emptyMap();

  DateTimeFormatter keyFmt  = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // so khớp OFF
  DateTimeFormatter headFmt = DateTimeFormatter.ofPattern("dd");        // hiển thị số ngày

  String pFrom = request.getParameter("from");
  String pTo   = request.getParameter("to");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Agenda phòng ban</title>
  <link rel="stylesheet" href="<%=ctx%>/css/theme.css"/>

  <style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap');

    /* Khóa cuộn toàn trang */
    html, body { height:100%; margin:0; overflow:hidden; font-family:Inter,system-ui,Arial,sans-serif }

    .page{ max-width:1100px; height:100%; margin:28px auto; padding:0 16px; box-sizing:border-box }
    .glass{
      color:#0f172a; background:rgba(255,255,255,.08);
      border:1px solid rgba(255,255,255,.12); border-radius:18px; padding:18px;
      box-shadow:0 18px 50px rgba(0,0,0,.35), inset 0 0 0 1px rgba(255,255,255,.08);
      backdrop-filter:blur(14px);
      display:flex; flex-direction:column; height:100%; min-height:0;
    }
    .title{ margin:6px 2px 14px; font-size:26px; font-weight:800 }

    .pills{ display:flex; gap:10px; flex-wrap:wrap; margin-bottom:12px }
    .btn-pill{
      display:inline-flex; align-items:center; gap:8px;
      padding:10px 16px; border-radius:14px; font-weight:800; text-decoration:none;
      color:#0f172a; background:rgba(255,255,255,.35);
      border:1px solid rgba(15,23,42,.22); box-shadow:0 6px 16px rgba(0,0,0,.18);
      backdrop-filter:blur(6px); cursor:pointer; transition:filter .15s, transform .02s;
    }
    .btn-pill:hover{ filter:brightness(1.05) } .btn-pill:active{ transform:translateY(1px) }

    .toolbar{ display:flex; align-items:center; gap:10px; flex-wrap:wrap; margin-bottom:8px }
    .date-inp{
      padding:10px 12px; border-radius:12px; outline:none;
      border:1px solid rgba(15,23,42,.22); background:rgba(255,255,255,.55); color:#0f172a;
    }
    .btn-view{ padding:10px 16px; border-radius:12px; border:1px solid rgba(15,23,42,.22);
               background:rgba(255,255,255,.35); font-weight:800; cursor:pointer }

    .legend{ display:flex; gap:18px; align-items:center; margin:6px 0 12px }
    .box{ width:16px; height:12px; border:1px solid rgba(15,23,42,.22); display:inline-block }
    .work{ background:#bfe6bf } .off{ background:#f7b1b1 }

    /* Vùng cuộn riêng cho bảng */
    .table-wrap{ flex:1 1 auto; min-height:0; overflow:auto; -webkit-overflow-scrolling:touch; border-radius:12px }

    /* Bảng có thể rộng hơn viewport -> xuất hiện thanh kéo ngang */
    .agenda{ border-collapse:collapse; width:max-content; min-width:100%; table-layout:fixed }
    .agenda th, .agenda td{ border:1px solid rgba(15,23,42,.18); padding:10px; text-align:center; white-space:nowrap }

    /* Sticky header + cột trái */
    .agenda thead th{ background:#0f172a; color:#fff; position:sticky; top:0; z-index:2 }
    .sticky-left{ position:sticky; left:0; z-index:3 }
    .agenda td.sticky-left{ background:rgba(255,255,255,.55); color:#0f172a; font-weight:800 }

    .agenda tbody tr:nth-child(odd)  td{ background:rgba(255,255,255,.06) }
    .agenda tbody tr:nth-child(even) td{ background:rgba(255,255,255,.10) }

    .cell-work{ background:#bfe6bf !important }
    .cell-off { background:#f7b1b1 !important; font-weight:800 }

    .muted{ opacity:.85 }
  </style>
</head>
<body>
<div class="page">
  <div class="glass">
    <div class="title">Agenda phòng ban</div>

    <div class="pills">
      <a class="btn-pill" href="<%=ctx%>/requestsubordinatesservlet1">Đơn cấp dưới</a>
      <a class="btn-pill" href="<%=ctx%>/requestlistmyservlet1">Đơn của tôi</a>
      <a class="btn-pill" href="<%=ctx%>/logoutservlet1">Đăng xuất</a>
    </div>

    <form class="toolbar" method="get" action="<%=ctx%>/agendaservlet1">
      <label>Từ:</label>
      <input type="date" name="from" class="date-inp"<%= (pFrom!=null && !pFrom.isBlank()) ? (" value=\"" + pFrom + "\"") : "" %>>
      <label>Đến:</label>
      <input type="date" name="to" class="date-inp"<%= (pTo!=null && !pTo.isBlank()) ? (" value=\"" + pTo + "\"") : "" %>>
      <button class="btn-view" type="submit">Xem</button>
    </form>

    <div class="legend">
      <span class="box work"></span> Đi làm
      <span class="box off"></span> Nghỉ phép (Approved)
    </div>

    <div class="table-wrap">
      <table class="agenda">
        <thead>
          <tr>
            <th class="sticky-left">Nhân sự</th>
            <% for (LocalDate d : days) { %>
              <th><%= d.format(headFmt) %></th>
            <% } %>
          </tr>
        </thead>
        <tbody>
        <% if (members.isEmpty()) { %>
          <tr>
            <td class="sticky-left muted">Không có dữ liệu</td>
            <% int cols = Math.max(days.size(), 1);
               for (int i=0; i<cols; i++) { %>
              <td class="muted">—</td>
            <% } %>
          </tr>
        <% } else {
             for (User u : members) {
               Set<String> offs = offByUser.getOrDefault(u.getId(), Collections.emptySet());
        %>
          <tr>
            <td class="sticky-left">
              <div><strong><%= u.getFullName() %></strong></div>
              <div style="font-size:12px;opacity:.8">(<%= u.getUsername() %>)</div>
            </td>
            <% for (LocalDate d : days) {
                 boolean isOff = offs.contains(d.format(keyFmt));
            %>
              <td class="<%= isOff ? "cell-off" : "cell-work" %>"><%= isOff ? "OFF" : "" %></td>
            <% } %>
          </tr>
        <% } } %>
        </tbody>
      </table>
    </div>
  </div>
</div>
</body>
</html>