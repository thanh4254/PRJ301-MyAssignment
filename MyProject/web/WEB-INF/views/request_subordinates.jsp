<%-- 
    Document   : request_subordinates
    Created on : Oct 12, 2025, 3:45:07 PM
    Author     : Admin
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, model.Request, model.User, model.Role, model.Feature" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Đơn của cấp dưới</title>
  <style>
    body { font-family: Arial, sans-serif; }
    h2 { margin-bottom: 8px; }
    .nav a { margin-right: 14px; }
    table { border-collapse: collapse; margin-top: 10px; width: 100%; }
    th, td { border: 1px solid #aaa; padding: 6px 10px; vertical-align: top; }
    .actions form { display:inline-block; margin-right:6px; }
    .status-approved { color: #2a7a2a; font-weight: bold; }
    .status-rejected { color: #b22; font-weight: bold; }
    .status-inprogress { color: #aa7a00; font-weight: bold; } /* dùng cho NEW */
    .note-input { width: 180px; }
    a { color:#1a66cc; text-decoration: underline; }
  </style>
</head>
<body>
  <h2>Đơn của cấp dưới</h2>

  <div class="nav">
    <a href="${pageContext.request.contextPath}/requestlistmyservlet1">← Đơn của tôi</a>
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
      <a href="${pageContext.request.contextPath}/agendaservlet1">Agenda</a>
    <% } %>
    <a href="${pageContext.request.contextPath}/logoutservlet1">Đăng xuất</a>
  </div>

  <%
    List<Request> items = (List<Request>) request.getAttribute("items");
    Map<Integer,String> names = (Map<Integer,String>) request.getAttribute("names"); // id -> fullName
    if (items == null) items = Collections.emptyList();
    if (names == null) names = Collections.emptyMap();
  %>

  <table>
    <tr>
      <th>Title</th>
      <th>From</th>
      <th>To</th>
      <th>Created By</th>
      <th>Status</th>
      <th>Processed By</th>
      <th>Note</th>
      <th>Thao tác</th>
    </tr>

    <% if (items.isEmpty()) { %>
      <tr><td colspan="8" style="text-align:center">Chưa có đơn nào</td></tr>
    <% } %>

    <% for (Request r : items) {
         String createdName   = names.getOrDefault(r.getCreatedBy(), String.valueOf(r.getCreatedBy()));
         String processedName = (r.getProcessedBy()==null) ? ""
                                : names.getOrDefault(r.getProcessedBy(), String.valueOf(r.getProcessedBy()));
         String st = r.getStatus().name(); // NEW / APPROVED / REJECTED
    %>
      <tr>
        <td>
          <a href="${pageContext.request.contextPath}/requestdetailservlet1?id=<%= r.getId() %>">
            <%= r.getTitle() %>
          </a>
        </td>
        <td><%= r.getFrom() %></td>
        <td><%= r.getTo() %></td>
        <td><%= createdName %></td>
        <td class="<%= "APPROVED".equals(st) ? "status-approved" :
                       "REJECTED".equals(st) ? "status-rejected" :
                       "status-inprogress" %>">
          <%= st %>
        </td>
        <td><%= processedName %></td>
        <td><%= r.getProcessedNote()==null ? "" : r.getProcessedNote() %></td>

        <td class="actions">
          <% if ("NEW".equals(st)) { %>
            <form method="post" action="${pageContext.request.contextPath}/requestapproveservlet1">
              <input type="hidden" name="id" value="<%= r.getId() %>"/>
              <input class="note-input" name="note" placeholder="Ghi chú phê duyệt"/>
              <button type="submit">Approve</button>
            </form>
            <form method="post" action="${pageContext.request.contextPath}/requestrejectservlet1">
              <input type="hidden" name="id" value="<%= r.getId() %>"/>
              <input class="note-input" name="note" placeholder="Lý do từ chối"/>
              <button type="submit">Reject</button>
            </form>
          <% } else { %>
            (Đã xử lý)
          <% } %>
        </td>
      </tr>
    <% } %>
  </table>

  <p style="color:red">${requestScope.error}</p>
</body>
</html>