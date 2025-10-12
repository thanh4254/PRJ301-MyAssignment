<%-- 
    Document   : agenda
    Created on : Oct 12, 2025, 5:24:29 PM
    Author     : Admin
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, java.time.*, java.time.format.DateTimeFormatter, model.User" %>
<%
  String cpath = request.getContextPath();

  @SuppressWarnings("unchecked")
  List<User> members = (List<User>) request.getAttribute("members");
  if (members == null) members = Collections.emptyList();

  @SuppressWarnings("unchecked")
  List<LocalDate> days = (List<LocalDate>) request.getAttribute("days");
  if (days == null) days = Collections.emptyList();

  @SuppressWarnings("unchecked")
  Map<Integer, Set<String>> offByUser = (Map<Integer, Set<String>>) request.getAttribute("offByUser");
  if (offByUser == null) offByUser = Collections.emptyMap();

  DateTimeFormatter d1 = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // for contains key
  DateTimeFormatter headFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // header (bạn đổi tuỳ thích)
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Agenda phòng ban</title>
  <style>
    body { font-family: Arial, sans-serif; }
    h2 { margin: 18px 0 10px; }

    .toolbar { margin-bottom: 10px; }
    .toolbar input[type="date"]{ padding:6px 8px; }
    .toolbar button { padding:6px 10px; }

    table.agenda { border-collapse: collapse; width: 100%; table-layout: fixed; }
    .agenda th, .agenda td { border:1px solid #bbb; padding:6px 8px; text-align:center; }
    .agenda th:first-child, .agenda td:first-child { text-align:left; width: 220px; }

    .cell-work { background:#bfe6bf; }     /* xanh làm việc */
    .cell-off  { background:#f7b1b1; font-weight:600; }  /* đỏ nghỉ phép */
    .legend { margin: 8px 0 14px; font-size:14px; }
    .legend .box { display:inline-block; width:16px; height:12px; border:1px solid #888; vertical-align:middle; margin-right:6px; }
    .legend .work { background:#bfe6bf; }
    .legend .off { background:#f7b1b1; }
    .nav a { margin-right:14px; }
  </style>
</head>
<body>

<h2>Agenda phòng ban</h2>

<div class="toolbar">
  <form method="get" action="<%= cpath %>/agendaservlet1" style="display:inline-block">
    Từ: <input type="date" name="from" value="${param.from}">
    Đến: <input type="date" name="to" value="${param.to}">
    <button type="submit">Xem</button>
  </form>
  <span class="nav" style="margin-left:16px">
    <a href="<%= cpath %>/requestsubordinatesservlet1">Đơn cấp dưới</a>
    <a href="<%= cpath %>/requestlistmyservlet1">Đơn của tôi</a>
    <a href="<%= cpath %>/logoutservlet1">Đăng xuất</a>
  </span>
</div>

<div class="legend">
  <span class="box work"></span> Đi làm
  &nbsp;&nbsp;
  <span class="box off"></span> Nghỉ phép (Approved)
</div>

<table class="agenda">
  <tr>
    <th>Nhân sự</th>
    <% for (LocalDate d : days) { %>
      <th><%= d.format(headFmt) %></th>
    <% } %>
  </tr>

  <% for (User u : members) {
       Set<String> offs = offByUser.getOrDefault(u.getId(), Collections.emptySet());
  %>
    <tr>
      <td>
        <div><b><%= u.getFullName() %></b></div>
        <div style="font-size:12px;color:#555">(<%= u.getUsername() %>)</div>
      </td>
      <% for (LocalDate d : days) {
           String key = d.format(d1);
           boolean isOff = offs.contains(key);
      %>
        <td class="<%= isOff ? "cell-off" : "cell-work" %>">
          <%= isOff ? "OFF" : "" %>
        </td>
      <% } %>
    </tr>
  <% } %>
</table>

</body>
</html>
