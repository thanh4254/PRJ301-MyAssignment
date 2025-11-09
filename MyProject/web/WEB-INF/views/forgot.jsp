<%-- WEB-INF/views/forgot.jsp --%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%
  String ctx       = request.getContextPath();
  String info      = (String) request.getAttribute("info");
  String err       = (String) request.getAttribute("error");
  String watchUser = (String) request.getAttribute("watchUser"); // username cần poll
  if (watchUser == null) watchUser = "";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>Forgot Password</title>
<link rel="stylesheet" href="<%=ctx%>/css/theme.css">
<style>
  .page{max-width:680px;margin:60px auto;padding:0 16px}
  .glass{
  background: rgba(255,255,255,.08);
  border: 1px solid rgba(255,255,255,.12);
  box-shadow: 0 18px 50px rgba(0,0,0,.35), inset 0 0 0 1px rgba(255,255,255,.06);
  border-radius: 18px;
  padding: 24px;
  color: #0f172a;

  /* NEW: làm mờ nền phía sau */
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

  h1{font-weight:800;margin:0 0 14px}
.inp{
  width: 100%;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid rgba(15,23,42,.2);
  background: rgba(255,255,255,.58); /* hơi trong suốt */
  outline: none;
  backdrop-filter: blur(2px);         /* nhẹ thôi để chữ vẫn sắc nét */
  -webkit-backdrop-filter: blur(2px);
}

  .row{display:flex;gap:10px;margin-top:12px}
  .btn{padding:10px 16px;border-radius:12px;border:0;font-weight:800;cursor:pointer}
  .btn-primary{background:#0b67ff;color:#fff}
  .muted{opacity:.9}
  .msg{margin-top:12px;font-weight:700}
  .ok{color:#0a7f3f}.err{color:#b00020}
  .navs{display:flex;gap:10px;margin-bottom:12px}
  .link{padding:10px 14px;border-radius:12px;background:rgba(255,255,255,.35);
        color:#0f172a;text-decoration:none;font-weight:800;border:1px solid rgba(15,23,42,.15)}
</style>
</head>
<body>
<div class="page">
  <div class="glass">
    <div class="navs">
      <a class="link" href="<%=ctx%>/loginservlet1">← Quay lại đăng nhập</a>
    </div>

    <h1>Yêu cầu đặt lại mật khẩu</h1>
    <p class="muted">Nhập <b>username</b>. Yêu cầu của bạn sẽ được <b>Admin</b> duyệt trước khi đặt lại mật khẩu.</p>

    <form method="post" autocomplete="off">
      <input class="inp" name="username" placeholder="username"
             value="<%= watchUser %>" required>
      <div class="row">
        <button class="btn btn-primary" type="submit">Gửi yêu cầu</button>
      </div>
    </form>

    <% if (info!=null) { %><div id="infoMsg" class="msg ok"><%=info%></div><% } %>
    <% if (err !=null) { %><div class="msg err"><%=err %></div><% } %>
  </div>
</div>

<script>
(function(){
  const ctx = "<%= ctx %>";
  const watching = "<%= watchUser %>".trim();

  // Nếu vừa gửi yêu cầu (có watchUser) thì poll trạng thái đã được duyệt chưa
  if (watching) {
    const infoEl = document.getElementById('infoMsg');
    if (infoEl) {
      infoEl.textContent = "Yêu cầu đã gửi. Đang chờ Admin duyệt... Sẽ tự chuyển trang khi được duyệt.";
    }

    const poll = async () => {
      try {
        const r = await fetch(ctx + "/forgot-status?u=" + encodeURIComponent(watching), { cache: "no-store" });
        if (r.ok) {
          const j = await r.json();
          if (j && j.ok && j.token) {
            // Tự động chuyển sang trang đổi mật khẩu
            window.location.href = ctx + "/reset-password?token=" + encodeURIComponent(j.token);
            return;
          }
        }
      } catch (e) {}
      setTimeout(poll, 4000); // 4 giây hỏi lại
    };
    setTimeout(poll, 2000);
  }
})();
</script>
</body>
</html>
