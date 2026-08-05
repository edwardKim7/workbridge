'use strict';

const interfaceState = {
  page: 0,
  size: 20,
};

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('interface-search').addEventListener('submit', (event) => {
    event.preventDefault();
    interfaceState.page = 0;
    loadInterfaces();
  });

  loadInterfaces();
});

async function loadInterfaces() {
  try {
    const query = WorkBridge.buildQuery({
      status: document.getElementById('interface-status').value,
      traceId: document.getElementById('interface-trace').value.trim(),
      page: interfaceState.page,
      size: interfaceState.size,
    });
    const response = await WorkBridge.request(`/api/v1/interfaces${query}`);
    const page = response.data;

    document.getElementById('interface-count').textContent = `총 ${page.totalElements}건`;
    document.getElementById('interface-body').innerHTML = page.items.length
      ? page.items.map(renderInterfaceRow).join('')
      : '<tr><td colspan="7" class="table-empty">처리 이력이 없습니다.</td></tr>';

    document.querySelectorAll('[data-id]').forEach((row) => {
      row.addEventListener('click', () => {
        location.href = `${WorkBridge.contextPath}/web/admin/interfaces/${row.dataset.id}`;
      });
    });

    WorkBridge.renderPagination(
      document.getElementById('interface-pagination'),
      page.page,
      page.totalPages,
      (nextPage) => {
        interfaceState.page = nextPage;
        loadInterfaces();
      },
    );
  } catch (error) {
    WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
  }
}

function renderInterfaceRow(item) {
  const statusClass = item.status === 'SUCCESS'
    ? 'status-success'
    : item.status.includes('FAILED')
      ? 'status-danger'
      : 'status-warning';

  return `
    <tr class="clickable-row" data-id="${item.interfaceId}">
      <td>${item.interfaceId}</td>
      <td>
        <span class="table-primary">${item.businessType} #${item.businessId}</span>
        <small>${WorkBridge.escapeHtml(item.idempotencyKey)}</small>
      </td>
      <td><span class="status-badge ${statusClass}">${item.status}</span></td>
      <td>${item.responseStatus ?? '-'}</td>
      <td>${item.retryCount}</td>
      <td><code>${WorkBridge.escapeHtml(item.traceId)}</code></td>
      <td>${WorkBridge.formatDateTime(item.completedAt || item.createdAt)}</td>
    </tr>
  `;
}
