'use strict';

const approvalState = {
  page: 0,
  size: 15,
  user: null,
};

document.addEventListener('DOMContentLoaded', async () => {
  document.getElementById('approval-search-form').addEventListener('submit', (event) => {
    event.preventDefault();
    approvalState.page = 0;
    loadApprovals();
  });
  document.getElementById('approval-create-form').addEventListener('submit', createApproval);

  try {
    const [userResponse, approverResponse, opportunityResponse] = await Promise.all([
      WorkBridge.request('/api/v1/auth/me'),
      WorkBridge.request('/api/v1/approvals/approvers'),
      WorkBridge.request('/api/v1/opportunities?size=100'),
    ]);

    approvalState.user = userResponse.data;
    configureApprovalScope();
    fillSelect(
      'approval-approver',
      approverResponse.data,
      'employeeId',
      (item) => `${item.employeeName} · ${item.departmentName}`,
    );
    fillSelect(
      'approval-business-id',
      opportunityResponse.data.items,
      'opportunityId',
      (item) => `${item.opportunityNo} · ${item.opportunityName}`,
    );

    const canCreate = approvalState.user.roles?.some((role) =>
      ['SALES', 'ADMIN'].includes(role.roleCode || role));
    document.getElementById('approval-create-panel').classList.toggle('hidden', !canCreate);

    await loadApprovals();
  } catch (error) {
    WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
  }
});

function configureApprovalScope() {
  const roleCodes = (approvalState.user.roles || []).map((role) => role.roleCode || role);
  const allOption = document.querySelector('#approval-view option[value="ALL"]');

  if (!roleCodes.some((roleCode) => ['ADMIN', 'VIEWER'].includes(roleCode))) {
    allOption.remove();
  }
  if (roleCodes.includes('APPROVER') && !roleCodes.includes('SALES')) {
    document.getElementById('approval-view').value = 'MY_APPROVALS';
  }
}

function fillSelect(elementId, items, valueKey, labelSelector) {
  const select = document.getElementById(elementId);
  const options = items
    .map((item) => `
      <option value="${item[valueKey]}">
        ${WorkBridge.escapeHtml(labelSelector(item))}
      </option>
    `)
    .join('');

  select.innerHTML = `<option value="">선택</option>${options}`;
}

async function loadApprovals() {
  try {
    const query = WorkBridge.buildQuery({
      view: document.getElementById('approval-view').value,
      status: document.getElementById('approval-status').value,
      approvalType: document.getElementById('approval-type').value,
      keyword: document.getElementById('approval-keyword').value.trim(),
      page: approvalState.page,
      size: approvalState.size,
    });
    const response = await WorkBridge.request(`/api/v1/approvals${query}`);
    const page = response.data;

    document.getElementById('approval-count').textContent =
      `총 ${page.totalElements.toLocaleString()}건`;
    document.getElementById('approval-body').innerHTML = page.items.length
      ? page.items.map(renderApprovalRow).join('')
      : '<tr><td colspan="6" class="table-empty">조회된 결재가 없습니다.</td></tr>';

    document.querySelectorAll('[data-approval-id]').forEach((row) => {
      row.addEventListener('click', () => {
        location.href = `${WorkBridge.contextPath}/web/approvals/${row.dataset.approvalId}`;
      });
    });

    WorkBridge.renderPagination(
      document.getElementById('approval-pagination'),
      page.page,
      page.totalPages,
      (nextPage) => {
        approvalState.page = nextPage;
        loadApprovals();
      },
    );
  } catch (error) {
    WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
  }
}

function renderApprovalRow(approval) {
  return `
    <tr class="clickable-row" data-approval-id="${approval.approvalId}">
      <td>
        <span class="table-primary">${WorkBridge.escapeHtml(approval.approvalNo)}</span>
        <small>${WorkBridge.escapeHtml(approval.title)}</small>
      </td>
      <td>${getApprovalTypeLabel(approval.approvalType)}</td>
      <td>${WorkBridge.escapeHtml(approval.requesterName)}</td>
      <td>${WorkBridge.escapeHtml(approval.approverName)}</td>
      <td>
        <span class="status-badge ${getApprovalStatusClass(approval.status)}">
          ${approval.status}
        </span>
      </td>
      <td>${WorkBridge.formatDateTime(approval.requestedAt || approval.createdAt)}</td>
    </tr>
  `;
}

async function createApproval(event) {
  event.preventDefault();

  const requestBody = {
    approvalType: getValue('approval-create-type'),
    businessType: 'OPPORTUNITY',
    businessId: Number(getValue('approval-business-id')),
    approverId: Number(getValue('approval-approver')),
    title: getValue('approval-title').trim(),
    amount: getNumberValue('approval-amount'),
    expenseCategory: getNullableValue('approval-expense-category'),
    visitDate: getNullableValue('approval-visit-date'),
    discountRate: getNumberValue('approval-discount-rate'),
    description: getNullableValue('approval-description'),
  };

  try {
    const response = await WorkBridge.request('/api/v1/approvals', {
      method: 'POST',
      body: requestBody,
    });

    WorkBridge.showToast(response.message);
    location.href =
      `${WorkBridge.contextPath}/web/approvals/${response.data.approval.approvalId}`;
  } catch (error) {
    WorkBridge.setFormMessage(
      document.getElementById('approval-form-message'),
      WorkBridge.errorMessage(error),
      'danger',
    );
  }
}

function getValue(elementId) {
  return document.getElementById(elementId).value;
}

function getNullableValue(elementId) {
  const value = getValue(elementId).trim();
  return value || null;
}

function getNumberValue(elementId) {
  const value = getValue(elementId);
  return value === '' ? null : Number(value);
}

function getApprovalTypeLabel(type) {
  return {
    SALES_EXPENSE: '영업비용',
    VISIT_PLAN: '방문 계획',
    DISCOUNT_EXCEPTION: '할인율 예외',
  }[type] || type;
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
