<%-- 
    Document   : request_detail
    Created on : Oct 12, 2025, 5:29:30 PM
    Author     : Admin
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Request, model.User, model.Role, java.util.*" %>
<%
    Request r = (Request) request.getAttribute("item");
    Map<Integer,String> names = (Map<Integer,String>) request.getAttribute("names");
    boolean canApprove = Boolean.TRUE.equals(request.getAttribute("canApprove"));

    User me = (User) session.getAttribute("user");
    String approverName = (me != null ? me.getFullName() : "");
    String approverRole = "—";
    if (me != null && me.getRoles() != null && !me.getRoles().isEmpty()) {
        Role first = me.getRoles().iterator().next();
        approverRole = first.getName();
    }
    String creatorName = (names != null)
            ? names.getOrDefault(r.getCreatedBy(), String.valueOf(r.getCreatedBy()))
            : String.valueOf(r.getCreatedBy());

    String st = r.getStatus().name();                         // VD: NEW / IN_PROGRESS / APPROVED / REJECTED
    boolean pending = !( "APPROVED".equals(st) || "REJECTED".equals(st) );
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css">
<meta charset="UTF-8">
<title>Chi tiết đơn nghỉ phép</title>
<style>
  :root {
    --blue-50:#eaf2ff; --blue-100:#d7e6ff; --blue-500:#2f6edb; --blue-600:#2358b1;
    --text:#10223d; --muted:#5a6d86; --danger:#c0392b;
  }
  /* Font & layout */
  body { margin:24px; color:var(--text);
         font-family: system-ui,-apple-system,Segoe UI,Roboto,Arial,Helvetica,sans-serif;
         line-height:1.5; font-size:16px; }
  /* Nav top */
  .topnav { display:flex; gap:18px; margin-bottom:18px; }
  .topnav a { color:var(--blue-600); font-weight:600; text-decoration:none; }
  .topnav a:hover { text-decoration:underline; }

  /* Card */
  .card { width:620px; background:var(--blue-50); border:1px solid var(--blue-100);
          border-radius:8px; padding:18px 20px; }
  .title { font-size:24px; font-weight:800; margin:4px 0 14px; }
  .row { margin:10px 0; }
  .label { color:var(--muted); margin-right:6px; }
  .mono { font-variant-numeric: tabular-nums; }
  textarea { width:100%; height:140px; padding:10px; resize:vertical;
             border:1px solid #b8cbf8; border-radius:6px; font-size:15px; background:#fff; }

.actions {
  display: flex;
  gap: 14px;
  margin-top: 16px;
  justify-content: flex-end;   /* đẩy nút sang phải */
  align-items: center;
}
  .btn { border:none; padding:11px 26px; border-radius:10px;
         font-weight:800; font-size:16px; cursor:pointer; transition:.15s; }
  .btn-approve { background:var(--blue-500); color:#fff; }
  .btn-approve:hover { background:var(--blue-600); transform:translateY(-1px); }
  .btn-reject { background:#7aa2ff; color:#fff; }
  .btn-reject:hover { background:#5b8fff; transform:translateY(-1px); }

  .hint { color:var(--muted); margin-top:10px; }
  .status { font-weight:800; }
  .status.approved { color:#237a36; }
  .status.rejected { color:var(--danger); }
  .status.progress { color:#aa7a00; }
</style>
</head>
<body>

  <!-- NAVIGATION ON TOP -->
  <div class="topnav">
    <a href="${pageContext.request.contextPath}/requestsubordinatesservlet1">Đơn cấp dưới</a>
    <a href="${pageContext.request.contextPath}/requestlistmyservlet1">Đơn của tôi</a>
    <a href="${pageContext.request.contextPath}/logoutservlet1">Đăng xuất</a>
  </div>

  <!-- APPROVAL CARD -->
  <div class="card">
    <div class="title">Duyệt đơn xin nghỉ phép</div>

    <div class="row">
      <span class="label">Duyệt bởi User:</span>
      <strong><%= approverName %></strong>
      <span class="label">, Role:</span>
      <strong><%= approverRole %></strong>
    </div>

    <div class="row"><span class="label">Tạo bởi:</span> <strong><%= creatorName %></strong></div>
    <div class="row"><span class="label">Từ ngày:</span> <span class="mono"><%= r.getFrom() %></span></div>
    <div class="row"><span class="label">Tới ngày:</span> <span class="mono"><%= r.getTo() %></span></div>

    <div class="row"><span class="label">Lý do:</span></div>

    <!-- Nếu được quyền & đơn chưa xử lý -> hiển thị 2 nút -->
    <% if (canApprove && pending) { %>
      <textarea id="noteBox" placeholder="Nhập lý do / ghi chú…"></textarea>

      <div class="actions">
        <!-- REJECT -->
        <form id="rejectForm" method="post" action="${pageContext.request.contextPath}/requestrejectservlet1" style="margin:0">
          <input type="hidden" name="id" value="<%= r.getId() %>"/>
          <input type="hidden" name="note" id="rejectNote"/>
          <button class="btn btn-reject" type="submit">Reject</button>
        </form>

        <!-- APPROVE -->
        <form id="approveForm" method="post" action="${pageContext.request.contextPath}/requestapproveservlet1" style="margin:0">
          <input type="hidden" name="id" value="<%= r.getId() %>"/>
          <input type="hidden" name="note" id="approveNote"/>
          <button class="btn btn-approve" type="submit">Approve</button>
        </form>
      </div>

      <script>
        // Đưa nội dung textarea vào cả 2 form trước khi gửi
        const noteBox = document.getElementById('noteBox');
        document.getElementById('rejectForm').addEventListener('submit', e=>{
          document.getElementById('rejectNote').value = noteBox.value;
        });
        document.getElementById('approveForm').addEventListener('submit', e=>{
          document.getElementById('approveNote').value = noteBox.value;
        });
      </script>

    <% } else { %>
      <!-- Đã xử lý hoặc không có quyền: chỉ hiển thị ghi chú & trạng thái -->
      <textarea readonly><%= r.getProcessedNote()==null ? "" : r.getProcessedNote() %></textarea>
      <div class="hint">
        Trạng thái:
        <span class="status <%= st.equals("APPROVED")?"approved":(st.equals("REJECTED")?"rejected":"progress") %>">
          <%= st %>
        </span>
        <% if (r.getProcessedBy()!=null && names!=null) { %>
          — bởi <%= names.getOrDefault(r.getProcessedBy(), String.valueOf(r.getProcessedBy())) %>
        <% } %>
      </div>
    <% } %>
  </div>

  <p style="color:#c00; margin-top:14px;"><%= (request.getAttribute("error")!=null? request.getAttribute("error"):"") %></p>
</body>
</html>