<%-- 
    Document   : login
    Created on : Oct 12, 2025, 3:44:27 PM
    Author     : Admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>Login</title></head>
<body>
  <h2>Đăng nhập</h2>
  <form method="post" action="${pageContext.request.contextPath}/loginservlet1">
    <label>User:</label><input name="username" required />
    <label>Pass:</label><input name="password" type="password" required />
    <button type="submit">Login</button>
  </form>
  <p style="color:red">${requestScope.error}</p>
</body></html>