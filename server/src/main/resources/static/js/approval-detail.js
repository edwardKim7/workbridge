'use strict';

const approvalId = document.body.dataset.approvalId;
let approvalDetail = null;

document.addEventListener('DOMContentLoaded', loadApprovalDetail);

async function loadApprovalDetail() {
  try {
    const response = await WorkBridge.request(`/api/v1/approvals/${approvalId}`);
    approvalDetail = response.data;
    renderApprovalDetail();
  } catch (error) {
    WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
  }
}

function renderApprovalDetail() {
  const approval = approvalDetail.approval;
  const payload = approval.payload || {};

  document.getElementById('approval-detail-title').textContent = approval.title;
  document.getElementById('approval-detail-subtitle').textContent =
    `${approval.approvalNo} · ${approval.businessName || approval.businessId}`;

  const statusBadge = document.getElementById('approval-detail-status');
  statusBadge.textContent = approval.status;
  statusBadge.className = `status-badge ${getApprovalStatusClass(approval.status)}`;

  document.getElementById('approval-detail-list').innerHTML = [
    renderPair('유형', approval.approvalType),
    renderPair('요청자', approval.requesterName),
    renderPair('승인자', approval.approverName),
    renderPair('금액', payload.amount == null ? '-' : WorkBridge.formatCurrency(payload.amount)),
    renderPair('비용 분류', payload.expenseCategory || '-'),
    renderPair('방문일', payload.visitDate || '-'),
    renderPair('할인율', payload.discountRate == null ? '-' : `${payload.discountRate}%`),
    renderPair('설명', payload.description || '-'),
    renderPair('요청일', WorkBridge.formatDateTime(approval.requestedAt)),
  ].join('');

  renderAvailableActions();
  renderApprovalHistory();
}

function renderAvailableActions() {
  const actions = document.getElementById('approval-actions');
  actions.innerHTML = '';

  if (approvalDetail.canRequest) {
    addActionButton(actions, '결재 요청', 'request', 'button-primary');
  }
  if (approvalDetail.canApprove) {
    addActionButton(actions, '승인', 'approve', 'button-primary');
  }
  if (approvalDetail.canReject) {
    addActionButton(actions, '반려', 'reject', 'button-danger');
  }
  if (approvalDetail.canCancel) {
    addActionButton(actions, '초안 취소', 'cancel', 'button-secondary-light');
  }

  const canEnterOpinion = approvalDetail.canApprove
    || approvalDetail.canReject
    || approvalDetail.canRequest
    || approvalDetail.canCancel;
  document
    .getElementById('approval-opinion-wrap')
    .classList.toggle('hidden', !canEnterOpinion);
}

function renderApprovalHistory() {
  document.getElementById('approval-history').innerHTML = approvalDetail.histories.length
    ? approvalDetail.histories.map(renderHistoryItem).join('')
    : '<p class="table-empty">처리 이력이 없습니다.</p>';
}

function renderHistoryItem(history) {
  return `
    <div class="activity-item">
      <div class="activity-marker">${history.action.substring(0, 1)}</div>
      <div class="activity-item-content">
        <div class="activity-item-heading">
          <strong>
            ${history.action} · ${WorkBridge.escapeHtml(history.actorName)}
          </strong>
          <span>${WorkBridge.formatDateTime(history.actedAt)}</span>
        </div>
        <p>
          ${WorkBridge.escapeHtml(history.fromStatus || '생성')} →
          ${WorkBridge.escapeHtml(history.toStatus)}
        </p>
        <small>
          ${WorkBridge.escapeHtml(history.opinion || '의견 없음')} ·
          Trace ${WorkBridge.escapeHtml(history.traceId)}
        </small>
      </div>
    </div>
  `;
}

function renderPair(label, value) {
  return `<div><dt>${label}</dt><dd>${WorkBridge.escapeHtml(value ?? '-')}</dd></div>`;
}

function addActionButton(parent, label, action, className) {
  const button = document.createElement('button');
  button.className = `button ${className}`;
  button.textContent = label;
  button.addEventListener('click', () => processApproval(action, button));
  parent.appendChild(button);
}

async function processApproval(action, button) {
  button.disabled = true;

  try {
    const opinion = document.getElementById('approval-opinion').value.trim() || null;
    const response = await WorkBridge.request(`/api/v1/approvals/${approvalId}/${action}`, {
      method: 'POST',
      body: {
        version: approvalDetail.approval.version,
        opinion,
      },
    });

    WorkBridge.showToast(response.message);
    await loadApprovalDetail();
  } catch (error) {
    WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
  } finally {
    button.disabled = false;
  }
}

function getApprovalStatusClass(status) {
  return {
    APPROVED: 'status-success',
    REJECTED: 'status-danger',
    REQUESTED: 'status-warning',
    DRAFT: 'status-neutral',
    CANCELED: 'status-neutral',
  }[status] || '';
}
