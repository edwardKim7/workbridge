'use strict';

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('operations-refresh').addEventListener('click', loadSummary);
  document.getElementById('audit-search').addEventListener('submit', (event) => {
    event.preventDefault();
    loadAuditLogs();
  });
  document.getElementById('error-search').addEventListener('submit', (event) => {
    event.preventDefault();
    loadSystemErrors();
  });

  loadSummary();
  loadAuditLogs();
  loadSystemErrors();
});

async function loadSummary() {
  try {
    const response = await WorkBridge.request('/api/v1/operations/summary');
    const summary = response.data;

    document.getElementById('operations-metrics').innerHTML = [
      renderMetric('서버', summary.system.applicationStatus),
      renderMetric('DB', summary.system.databaseStatus),
      renderMetric('재처리 대기', summary.retryWaitingCount),
      renderMetric('최근 API 오류', summary.recentApiErrors.length),
    ].join('');

    document.getElementById('operations-interfaces').innerHTML = renderList(
      summary.recentInterfaces,
      (item) => `#${item.interfaceId} ${item.businessType} · ${item.status}`,
      (item) => `${item.traceId} · ${WorkBridge.formatDateTime(item.createdAt)}`,
    );

    document.getElementById('operations-migrations').innerHTML = renderList(
      summary.recentMigrations,
      (item) => `${item.batchNo} · ${item.status}`,
      (item) => `${item.successCount} 성공 / ${item.failureCount} 실패`,
    );
  } catch (error) {
    WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
  }
}

async function loadAuditLogs() {
  try {
    const query = WorkBridge.buildQuery({
      action: document.getElementById('audit-action').value.trim(),
      traceId: document.getElementById('audit-trace').value.trim(),
      size: 20,
    });
    const response = await WorkBridge.request(`/api/v1/operations/audit-logs${query}`);

    document.getElementById('audit-results').innerHTML = renderList(
      response.data.items,
      (item) => `${item.action} · ${item.actorName || 'SYSTEM'}`,
      (item) => `${item.targetType} #${item.targetId || '-'} · ${item.traceId}`,
    );
  } catch (error) {
    WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
  }
}

async function loadSystemErrors() {
  try {
    const query = WorkBridge.buildQuery({
      httpStatus: document.getElementById('error-status').value,
      traceId: document.getElementById('error-trace').value.trim(),
      size: 20,
    });
    const response = await WorkBridge.request(`/api/v1/operations/errors${query}`);

    document.getElementById('error-results').innerHTML = renderList(
      response.data.items,
      (item) => `${item.httpStatus} ${item.errorCode}`,
      (item) => `${item.requestMethod} ${item.requestPath} · ${item.traceId}`,
    );
  } catch (error) {
    WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
  }
}

function renderMetric(label, value) {
  return `
    <article class="metric-card">
      <span>${label}</span>
      <strong>${WorkBridge.escapeHtml(value)}</strong>
    </article>
  `;
}

function renderList(items, titleSelector, descriptionSelector) {
  if (!items.length) {
    return '<p class="table-empty">조회 결과가 없습니다.</p>';
  }

  return items
    .map((item) => `
      <div class="operation-item">
        <strong>${WorkBridge.escapeHtml(titleSelector(item))}</strong>
        <small>${WorkBridge.escapeHtml(descriptionSelector(item))}</small>
      </div>
    `)
    .join('');
}
