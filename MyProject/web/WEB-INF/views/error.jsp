<%-- 
    Document   : error
    Created on : Oct 12, 2025, 3:45:15 PM
    Author     : Admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>Error</title></head>
<body>
  <h2 style="color:red">Có lỗi xảy ra</h2>
  <p>${requestScope.error}</p>
  <a href="javascript:history.back()">Quay lại</a>
</body></html>
