'use strict';

const interfaceId = document.body.dataset.interfaceId;

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('interface-retry').addEventListener('click', retryInterface);
  loadInterfaceDetail();
});

async function loadInterfaceDetail() {
  try {
    const response = await WorkBridge.request(`/api/v1/interfaces/${interfaceId}`);
    renderInterfaceDetail(response.data);
  } catch (error) {
    WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
  }
}

function renderInterfaceDetail(detail) {
  const summary = detail.summary;
  const statusBadge = document.getElementById('interface-detail-status');

  statusBadge.textContent = summary.status;
  statusBadge.className = `status-badge ${getStatusClass(summary.status)}`;

  document.getElementById('interface-detail-list').innerHTML = [
    renderPair('Idempotency Key', summary.idempotencyKey),
    renderPair('Trace ID', summary.traceId),
    renderPair('업무 버전', summary.businessVersion),
    renderPair('HTTP 상태', summary.responseStatus),
    renderPair('오류 코드', summary.errorCode),
    renderPair('오류 메시지', summary.errorMessage),
  ].join('');

  document.getElementById('interface-request').textContent = detail.requestBody || '-';
  document.getElementById('interface-response').textContent = detail.responseBody || '-';
  document
    .getElementById('interface-retry-controls')
    .classList.toggle('hidden', !summary.status.includes('FAILED'));

  document.getElementById('interface-attempts').innerHTML = detail.attempts.length
    ? detail.attempts.map(renderAttempt).join('')
    : '<p class="table-empty">시도 이력이 없습니다.</p>';
}

function renderAttempt(attempt) {
  return `
    <div class="activity-item">
      <div class="activity-marker">${attempt.attemptNo}</div>
      <div class="activity-item-content">
        <strong>${attempt.status} · HTTP ${attempt.responseStatus ?? '-'}</strong>
        <p>${WorkBridge.escapeHtml(attempt.errorMessage || '정상 처리')}</p>
        <small>
          ${WorkBridge.formatDateTime(attempt.completedAt)} ·
          ${WorkBridge.escapeHtml(attempt.traceId)}
        </small>
      </div>
    </div>
  `;
}

async function retryInterface() {
  const retryButton = document.getElementById('interface-retry');
  retryButton.disabled = true;

  try {
    const response = await WorkBridge.request(`/api/v1/interfaces/${interfaceId}/retry`, {
      method: 'POST',
      body: {
        simulationMode: document.getElementById('interface-mode').value,
      },
    });

    WorkBridge.showToast(response.message);
    await loadInterfaceDetail();
  } catch (error) {
    WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
  } finally {
    retryButton.disabled = false;
  }
}

function renderPair(label, value) {
  return `<div><dt>${label}</dt><dd>${WorkBridge.escapeHtml(value ?? '-')}</dd></div>`;
}

function getStatusClass(status) {
  if (status === 'SUCCESS') {
    return 'status-success';
  }
  if (status.includes('FAILED')) {
    return 'status-danger';
  }
  return 'status-warning';
}
