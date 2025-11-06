<%-- WEB-INF/views/request_subordinates.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, model.Request" %>
<%
  String ctx = request.getContextPath();

  @SuppressWarnings("unchecked")
  List<Request> items = (List<Request>) request.getAttribute("items");
  if (items == null) items = Collections.emptyList();

  @SuppressWarnings("unchecked")
  Map<Integer,String> creatorNames = (Map<Integer,String>) request.getAttribute("creatorNames");
  if (creatorNames == null) creatorNames = Collections.emptyMap();

  @SuppressWarnings("unchecked")
  Map<Integer,String> processorNames = (Map<Integer,String>) request.getAttribute("processorNames");
  if (processorNames == null) processorNames = Collections.emptyMap();

  String q = (String) request.getAttribute("q");
  int curPage    = (request.getAttribute("page")!=null) ? (Integer)request.getAttribute("page") : 1;
  int totalPages = (request.getAttribute("totalPages")!=null) ? (Integer)request.getAttribute("totalPages") : 1;

  String baseSub = ctx + "/requestsubordinatesservlet1";
  String qParam = "";
  try { qParam = (q!=null && !q.isBlank()) ? ("&q=" + java.net.URLEncoder.encode(q,"UTF-8")) : ""; }
  catch (Exception ignore) {}
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <title>Đơn của cấp dưới</title>
  <link rel="stylesheet" href="<%=ctx%>/css/theme.css" />
  <style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap');

    html, body { height:100%; margin:0; overflow:hidden; font-family:Inter,system-ui,Arial,sans-serif; }
    .page{ max-width:1100px; height:100%; margin:0 auto; padding:28px 16px; box-sizing:border-box; }
    .glass{
      color:#0f172a; background:rgba(255,255,255,.08); border:1px solid rgba(255,255,255,.12);
      border-radius:18px; padding:18px; box-shadow:0 18px 50px rgba(0,0,0,.35), inset 0 0 0 1px rgba(255,255,255,.08);
      backdrop-filter:blur(14px); display:flex; flex-direction:column; height:100%;
    }
    .title{margin:6px 2px 14px; font-size:24px; font-weight:800}

    .topnav{display:flex; gap:10px; flex-wrap:wrap; margin-bottom:10px}
    .btn-pill{
      appearance:none; display:inline-flex; align-items:center; gap:8px;
      padding:10px 16px; border-radius:14px; font-weight:800; text-decoration:none;
      color:#0f172a; background:rgba(255,255,255,.35); border:1px solid rgba(15,23,42,.22);
      box-shadow:0 6px 16px rgba(0,0,0,.18); cursor:pointer;
    }

    /* search bar */
    .search-bar{display:flex; gap:8px; align-items:center; margin:4px 0 14px 0}
    .search-input{
      padding:10px 12px; border-radius:12px; border:1px solid rgba(15,23,42,.22);
      background:rgba(255,255,255,.55); color:#0f172a; min-width:260px;
    }

    /* bảng + khung cuộn */
    .table-wrap{ flex:1 1 auto; overflow:auto; border-radius:12px; }
    .tbl{width:100%; border-collapse:collapse; table-layout:fixed; min-width:1020px;}
    .tbl th,.tbl td{padding:12px; border:1px solid rgba(15,23,42,.18); word-wrap:break-word; vertical-align:top}
    .tbl th{ background:#0f172a; color:#fff; text-align:left; font-weight:700; position:sticky; top:0; z-index:2; white-space:nowrap }
    .tbl tr:nth-child(odd){background:rgba(255,255,255,.06)}
    .tbl tr:nth-child(even){background:rgba(255,255,255,.10)}
    .tbl a{color:#0b67ff; text-decoration:none; font-weight:700}
    .tbl a:hover{text-decoration:underline}

    .status-cell{ font-weight:800; white-space:nowrap; }
    .status-new{ color:#d97706 !important; }
    .status-ok{  color:#16a34a !important; }
    .status-bad{ color:#dc2626 !important; }

    /* ---- Cột Thao tác: không cắt nút, tự co dãn ---- */
    .td-actions{ box-sizing:border-box; width:260px; max-width:260px; min-width:240px; overflow:visible; }
    .muted{opacity:.9; line-height:1.25; margin-bottom:4px}

    /* card thao tác: ghi chú 1 hàng, cụm nút auto-fit (2 cột nếu đủ chỗ) */
    .action-card{ display:grid; grid-template-columns:1fr 1fr; gap:8px; }
    .action-card .note-input{
      grid-column:1 / -1; width:100%; box-sizing:border-box; color:#0f172a;
      background:rgba(255,255,255,.55); border:1px solid rgba(15,23,42,.18);
      border-radius:10px; padding:10px 12px; outline:none;
    }

    .btn-row{ display:grid; grid-template-columns:repeat(auto-fit, minmax(120px, 1fr)); gap:8px; }
    .btn-approve,.btn-reject{
      border:0; border-radius:10px; padding:10px 14px; font-weight:800; cursor:pointer;
      box-shadow:0 6px 12px rgba(0,0,0,.18); width:100%; white-space:nowrap;
    }
    .btn-approve{ background:#16a34a; color:#fff }
    .btn-reject{  background:#dc2626; color:#fff }

    .pager{display:flex;gap:10px;align-items:center;justify-content:flex-end;margin-top:12px}

    /* ==== Responsive: mượt như trang admin_reset_list ==== */
    @media (max-width: 1200px){
      .tbl{min-width:980px}
      .td-actions{width:230px; max-width:230px}
    }
    @media (max-width: 860px){
      .tbl th,.tbl td{padding:10px}
      .btn-approve,.btn-reject{padding:9px 12px}
      .muted{font-size:13px}
    }
    @media (max-width: 700px){
      .action-card{grid-template-columns:1fr}  /* ghi chú + nút xếp dọc */
      .btn-row{grid-template-columns:1fr}      /* 2 nút xếp dọc, không rớt */
    }
    @media (max-width: 560px){
      .tbl th,.tbl td{padding:8px; font-size:14px}
      .btn-approve,.btn-reject{padding:8px 10px}
    }
  </style>
</head>
<body>
<div class="page">
  <div class="glass">
    <div class="title">Đơn của cấp dưới</div>

    <div class="topnav">
      <a class="btn-pill" href="<%=ctx%>/requestlistmyservlet1">← Đơn của tôi</a>
      <a class="btn-pill" href="<%=ctx%>/logoutservlet1">Đăng xuất</a>
    </div>

    <!-- Thanh tìm kiếm theo tên Created By -->
    <form class="search-bar" method="get" action="<%=baseSub%>">
      <input class="search-input" type="text" name="q" placeholder="Tìm theo tên người tạo (Created By)"
             value="<%= q==null? "" : q %>">
      <button class="btn-pill" type="submit">Search</button>
      <a class="btn-pill" href="<%=baseSub%>">Clear</a>
    </form>

    <div class="table-wrap">
      <table class="tbl">
        <thead>
          <tr>
            <th style="width:22%">Title</th>
            <th style="width:12%">From</th>
            <th style="width:12%">To</th>
            <th style="width:14%">Created By</th>
            <th style="width:12%">Status</th>
            <th style="width:14%">Processed By</th>
            <th style="width:16%">Thao tác</th>
          </tr>
        </thead>
        <tbody>
        <% if (items.isEmpty()) { %>
          <tr>
            <td colspan="7" style="text-align:center;opacity:.85">
              <%= (q!=null && !q.isBlank()) ? "Không có đơn nào khớp từ khóa \""+q+"\"" : "Chưa có đơn nào" %>
            </td>
          </tr>
        <% } else {
             for (Request r : items) {
               String creator   = creatorNames.getOrDefault(r.getCreatedBy(), String.valueOf(r.getCreatedBy()));
               String processor = (r.getProcessedBy()==null) ? "" :
                 processorNames.getOrDefault(r.getProcessedBy(), String.valueOf(r.getProcessedBy()));

               String raw  = String.valueOf(r.getStatus());
               String norm = raw==null? "" : raw.trim().toUpperCase(java.util.Locale.ROOT);
               String statusLabel, statusClass;
               switch (norm) {
                 case "NEW":      statusLabel="In-progress"; statusClass="status-cell status-new"; break;
                 case "APPROVED": statusLabel="Approved";    statusClass="status-cell status-ok";  break;
                 case "REJECTED": statusLabel="Rejected";    statusClass="status-cell status-bad"; break;
                 default:         statusLabel=raw;           statusClass="status-cell";            break;
               }
        %>
          <tr id="row-<%=r.getId()%>">
            <td><a href="<%= ctx %>/requestdetailservlet1?id=<%= r.getId() %>"><%= r.getTitle() %></a></td>
            <td><%= r.getFrom() %></td>
            <td><%= r.getTo() %></td>
            <td><%= creator %></td>
            <td id="status-<%=r.getId()%>" class="<%= statusClass %>"><%= statusLabel %></td>
            <td id="proc-<%=r.getId()%>"><%= processor %></td>
            <td id="act-<%=r.getId()%>" class="td-actions">
              <% if ("APPROVED".equals(norm)) { %>
                <div class="muted">Đã <strong>Approved</strong> — có thể đổi sang <strong>Rejected</strong>:</div>
                <div class="action-card">
                  <input class="note-input" id="note-<%=r.getId()%>" placeholder="Lý do từ chối">
                  <div class="btn-row">
                    <button class="btn-reject" data-action="reject" data-id="<%=r.getId()%>">Reject</button>
                  </div>
                </div>
              <% } else if ("REJECTED".equals(norm)) { %>
                <div class="muted">Đã <strong>Rejected</strong> — có thể đổi sang <strong>Approved</strong>:</div>
                <div class="action-card">
                  <input class="note-input" id="note-<%=r.getId()%>" placeholder="Ghi chú phê duyệt">
                  <div class="btn-row">
                    <button class="btn-approve" data-action="approve" data-id="<%=r.getId()%>">Approve</button>
                  </div>
                </div>
              <% } else { %>
                <div class="action-card">
                  <input class="note-input" id="note-<%=r.getId()%>" placeholder="Ghi chú (tuỳ chọn)">
                  <div class="btn-row">
                    <button class="btn-approve" data-action="approve" data-id="<%=r.getId()%>">Approve</button>
                    <button class="btn-reject"  data-action="reject"  data-id="<%=r.getId()%>">Reject</button>
                  </div>
                </div>
              <% } %>
            </td>
          </tr>
        <% } } %>
        </tbody>
      </table>
    </div>

    <!-- Pager -->
    <div class="pager">
      <a class="btn-pill" style="<%= (curPage<=1) ? "pointer-events:none;opacity:.45" : "" %>"
         href="<%= baseSub %>?page=<%= curPage-1 %><%= qParam %>">← Trước</a>
      <div><b>Trang <%= curPage %></b>/<%= totalPages %></div>
      <a class="btn-pill" style="<%= (curPage>=totalPages) ? "pointer-events:none;opacity:.45" : "" %>"
         href="<%= baseSub %>?page=<%= curPage+1 %><%= qParam %>">Sau →</a>
    </div>

    <p id="page-error" style="color:#b00020;font-weight:700;margin-top:10px">
      <%= request.getAttribute("error")!=null ? request.getAttribute("error") : "" %>
    </p>
  </div>
</div>

<script>
(function(){
  function postAction(id, action, note){
    const url = action === 'approve'
        ? '<%=ctx%>/requestapproveservlet1'
        : '<%=ctx%>/requestrejectservlet1';
    const form = new URLSearchParams();
    form.set('id', id);
    if (note) form.set('note', note);
    form.set('ajax', '1');

    return fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
        'X-Requested-With': 'XMLHttpRequest'
      },
      body: form.toString()
    }).then(async res => {
      const data = await res.json().catch(()=>({ok:false, message:'Invalid response'}));
      if (!res.ok || !data.ok) throw new Error(data.message || 'Request failed');
      return data; // {ok:true,id,status,processorName}
    });
  }

  function renderRowAfter(id, status, processorName){
    const st = document.getElementById('status-' + id);
    const proc = document.getElementById('proc-' + id);
    const act = document.getElementById('act-' + id);
    if (!st || !proc || !act) return;

    st.classList.remove('status-new','status-ok','status-bad');
    let label = status;
    if (status === 'APPROVED'){ st.classList.add('status-cell','status-ok');  label='Approved'; }
    else if (status === 'REJECTED'){ st.classList.add('status-cell','status-bad'); label='Rejected'; }
    else { st.classList.add('status-cell','status-new'); label='In-progress'; }
    st.textContent = label;

    proc.textContent = processorName || '';

    if (status === 'APPROVED'){
      act.innerHTML =
        '<div class="muted">Đã <strong>Approved</strong> — có thể đổi sang <strong>Rejected</strong>:</div>'
       +'<div class="action-card">'
       +  '<input class="note-input" id="note-'+id+'" placeholder="Lý do từ chối">'
       +  '<div class="btn-row"><button class="btn-reject" data-action="reject" data-id="'+id+'">Reject</button></div>'
       +'</div>';
    } else if (status === 'REJECTED'){
      act.innerHTML =
        '<div class="muted">Đã <strong>Rejected</strong> — có thể đổi sang <strong>Approved</strong>:</div>'
       +'<div class="action-card">'
       +  '<input class="note-input" id="note-'+id+'" placeholder="Ghi chú phê duyệt">'
       +  '<div class="btn-row"><button class="btn-approve" data-action="approve" data-id="'+id+'">Approve</button></div>'
       +'</div>';
    } else {
      act.innerHTML =
        '<div class="action-card">'
       +  '<input class="note-input" id="note-'+id+'" placeholder="Ghi chú (tuỳ chọn)">'
       +  '<div class="btn-row">'
       +    '<button class="btn-approve" data-action="approve" data-id="'+id+'">Approve</button>'
       +    '<button class="btn-reject"  data-action="reject"  data-id="'+id+'">Reject</button>'
       +  '</div>'
       +'</div>';
    }
  }

  document.addEventListener('click', function(e){
    const btn = e.target.closest('button[data-action]');
    if (!btn) return;
    e.preventDefault();

    const id = btn.getAttribute('data-id');
    const action = btn.getAttribute('data-action');
    const noteInput = document.getElementById('note-' + id);
    const note = noteInput ? noteInput.value : '';

    btn.disabled = true;
    postAction(id, action, note)
      .then(data => renderRowAfter(data.id, data.status, data.processorName))
      .catch(err => alert(err.message || 'Có lỗi xảy ra'))
      .finally(() => { btn.disabled = false; });
  });
})();
</script>
</body>
</html>
