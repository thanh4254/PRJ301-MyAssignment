<%-- 
    Document   : request_list
    Created on : Oct 12, 2025, 3:44:55 PM
    Author     : Admin
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, model.Request, model.User, model.Role, model.Feature" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Đơn của tôi</title>
  <style>
    a { color:#1a66cc; text-decoration: underline; }
    table { border-collapse: collapse; margin-top:10px; }
    th, td { border:1px solid #aaa; padding:6px 10px; }
  </style>
</head>
<body>
  <h2>Đơn của tôi</h2>

  <a href="${pageContext.request.contextPath}/requestcreateservlet1">+ Tạo đơn</a>
  <a href="${pageContext.request.contextPath}/requestsubordinatesservlet1" style="margin-left:16px">Đơn cấp dưới</a>
  <a href="${pageContext.request.contextPath}/logoutservlet1" style="margin-left:16px">Đăng xuất</a>

  <%-- >>> Hiện nút Agenda nếu user có Feature 'AGD' (Head) --%>
  <%
    User me = (User) session.getAttribute("user");
    boolean showAgenda = false;
    if (me != null && me.getRoles() != null) {
      for (Role r : me.getRoles()) {
        if (r.getFeatures() == null) continue;
        for (Feature f : r.getFeatures()) {
          if ("AGD".equalsIgnoreCase(f.getCode())) { showAgenda = true; break; }
        }
        if (showAgenda) break;
      }
    }
  %>
  <% if (showAgenda) { %>
    <a href="${pageContext.request.contextPath}/agendaservlet1" style="margin-left:16px">Agenda phòng</a>
  <% } %>
  <%-- <<< --%>

  <%
    List<Request> items = (List<Request>) request.getAttribute("items");
    Map<Integer,String> names = (Map<Integer,String>) request.getAttribute("names"); // processedBy name map
    if (items == null) items = Collections.emptyList();
    if (names == null) names = Collections.emptyMap();
  %>

  <table>
    <tr>
      <th>Title</th>
      <th>From</th>
      <th>To</th>
      <th>Status</th>
      <th>Processed By</th>
      <th>Note</th>
    </tr>

    <% if (items.isEmpty()) { %>
      <tr><td colspan="6" style="text-align:center">Chưa có đơn nào</td></tr>
    <% } %>

    <% for (Request r : items) {
         String processedName = (r.getProcessedBy()==null) ? ""
             : names.getOrDefault(r.getProcessedBy(), String.valueOf(r.getProcessedBy()));
    %>
      <tr>
        <td><a href="#"> <%= r.getTitle() %> </a></td>
        <td><%= r.getFrom() %></td>
        <td><%= r.getTo() %></td>
        <td><%= r.getStatus() %></td>
        <td><%= processedName %></td>
        <td><%= r.getProcessedNote()==null ? "" : r.getProcessedNote() %></td>
      </tr>
    <% } %>
  </table>

  <p style="color:red">${requestScope.error}</p>
</body>
</html>
