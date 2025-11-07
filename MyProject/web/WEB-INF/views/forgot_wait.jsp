<%@ page contentType="text/html; charset=UTF-8" %>
<%
  String ctx = request.getContextPath();
  String u = (String) session.getAttribute("waiting_username");
  if (u == null) u = "";
%>
<!DOCTYPE html><html lang="vi"><head>
<meta charset="UTF-8"><title>Đang chờ Admin duyệt…</title>
<link rel="stylesheet" href="<%=ctx%>/css/theme.css">
<style>.page{max-width:760px;margin:28px auto;padding:0 16px}
.glass{background:rgba(255,255,255,.08);border:1px solid rgba(255,255,255,.12);
border-radius:18px;padding:18px;backdrop-filter:blur(14px);color:#0f172a}
.title{font-weight:800;font-size:26px;margin-bottom:8px}</style>
</head><body>
<div class="page"><div class="glass">
  <div class="title">Yêu cầu đặt lại mật khẩu</div>
  <p>Đang chờ admin duyệt cho tài khoản: <b><%=u%></b>. Trang sẽ tự chuyển khi được duyệt.</p>
  <p><a class="pill" href="<%=ctx%>/loginservlet1">← Quay lại đăng nhập</a></p>
</div></div>
<script>
(function(){
  const username = <%=("\""+u.replace("\"","\\\"")+"\"")%>;
  if (!username) return;
  function check(){
    fetch('<%=ctx%>/reset-status?username='+encodeURIComponent(username), {cache:'no-store'})
      .then(r=>r.json()).then(j=>{
        if (j && j.ok && j.token){
          location.href = '<%=ctx%>/reset-password?token=' + encodeURIComponent(j.token);
        }
      }).catch(()=>{});
  }
  check(); setInterval(check, 2000);
})();
</script>
</body></html>
