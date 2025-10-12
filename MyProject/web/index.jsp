<%-- 
    Document   : index
    Created on : Oct 12, 2025, 4:24:40 PM
    Author     : Admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>

    </head>
    <body>
        <h1>Hello World!</h1>
        <% response.sendRedirect(request.getContextPath()+"/loginservlet1"); %>

        
    </body>
</html>
