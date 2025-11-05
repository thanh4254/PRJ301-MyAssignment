<%-- 
    Document   : admin_reset_list
    Created on : Nov 5, 2025, 3:49:10 PM
    Author     : Admin
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%
  String ctx = request.getContextPath();
  @SuppressWarnings("unchecked")
  List<Map<String,Object>> items = (List<Map<String,Object>>) request.getAttribute("items");
  if (items==null) items = java.util.Collections.emptyList();
  String flash = (String) request.getAttribute("flash");
  String err   = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>Admin – Duyệt reset mật khẩu</title>
<link rel="stylesheet" href="<%=ctx%>/css/theme.css">
<style>
  .page{max-width:1000px;margin:28px auto;padding:0 16px}
  .glass{background:rgba(255,255,255,.08);border:1px solid rgba(255,255,255,.12);
         border-radius:18px;padding:18px;color:#0f172a;box-shadow:0 18px 50px rgba(0,0,0,.35)}
  .title{font-size:26px;font-weight:800;margin:6px 2px 14px}
  .topnav{display:flex;gap:10px;margin-bottom:12px}
  .btn{padding:8px 12px;border-radius:12px;border:0;font-weight:800;cursor:pointer}
  .btn-approve{background:#16a34a;color:#fff}
  .btn-deny{background:#dc2626;color:#fff}
  .link{padding:8px 12px;border-radius:12px;background:rgba(255,255,255,.35);text-decoration:none;color:#0f172a;font-weight:800;border:1px solid rgba(15,23,42,.15)}
  table{width:100%;border-collapse:collapse}
  th,td{padding:12px;border:1px solid rgba(15,23,42,.18)}
  th{background:#0f172a;color:#fff;text-align:left}
  .flash{font-weight:700;color:#0a7f3f;margin-bottom:10px}
  .err{font-weight:700;color:#b00020;margin-bottom:10px}
</style>
</head>
<body>
<div class="page">
  <div class="glass">
    <div class="title">Duyệt yêu cầu đặt lại mật khẩu</div>
    <div class="topnav">
      <a class="link" href="<%=ctx%>/requestlistmyservlet1">← Trang chính</a>
    </div>
    <% if (flash!=null) { %><div class="flash"><%=flash%></div><% } %>
    <% if (err  !=null) { %><div class="err"><%=err%></div><% } %>
    <table>
      <thead><tr>
        <th>#</th><th>Username</th><th>RequestedAt</th><th>Thao tác</th>
      </tr></thead>
      <tbody>
      <% if (items.isEmpty()) { %>
        <tr><td colspan="4">Không có yêu cầu nào.</td></tr>
      <% } else { for (Map<String,Object> m : items) { %>
        <tr>
          <td><%= m.get("Id") %></td>
          <td><%= m.get("Username") %></td>
          <td><%= m.get("RequestedAt") %></td>
          <td>
            <form method="post" style="display:inline">
              <input type="hidden" name="id" value="<%=m.get("Id")%>">
              <button name="action" value="approve" class="btn btn-approve">Approve</button>
            </form>
            <form method="post" style="display:inline">
              <input type="hidden" name="id" value="<%=m.get("Id")%>">
              <button name="action" value="deny" class="btn btn-deny">Deny</button>
            </form>
          </td>
        </tr>
      <% } } %>
      </tbody>
    </table>
  </div>
</div>
</body>
</html>

