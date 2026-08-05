'use strict';

const customerState = {
    page: 0,
    size: 15,
    canWrite: document.body.dataset.canWriteCustomer === 'true',
    salesEmployees: [],
    currentUser: null
};

document.addEventListener('DOMContentLoaded', async () => {
    bindCustomerEvents();
    configureCustomerWorkspace();
    try {
        const [employeePayload, mePayload] = await Promise.all([
            WorkBridge.request('/api/v1/sales/employees/options'),
            WorkBridge.request('/api/v1/auth/me')
        ]);
        customerState.salesEmployees = employeePayload.data || [];
        customerState.currentUser = mePayload.data;
        renderSalesEmployeeOptions();
        resetCustomerCreateForm();
        await loadCustomers();
    } catch (error) {
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    }
});

function bindCustomerEvents() {
    document.getElementById('customer-search-form')?.addEventListener('submit', event => {
        event.preventDefault();
        customerState.page = 0;
        loadCustomers();
    });
    document.getElementById('customer-create-form')?.addEventListener('submit', createCustomer);
    document.getElementById('customer-create-reset')?.addEventListener('click', resetCustomerCreateForm);
    document.getElementById('create-customer-status')?.addEventListener('change', event => {
        if (event.target.value === 'INACTIVE') {
            document.getElementById('create-customer-active').checked = false;
        }
    });
}

function configureCustomerWorkspace() {
    if (customerState.canWrite) {
        return;
    }
    document.getElementById('customer-create-panel')?.classList.add('hidden');
    document.querySelector('.sales-workspace-grid')?.classList.add('read-only-workspace');
}

function renderSalesEmployeeOptions() {
    const searchSelect = document.getElementById('search-assigned-sales');
    const createSelect = document.getElementById('create-assigned-sales');
    WorkBridgeSales.populateSelect(searchSelect, customerState.salesEmployees, {
        placeholder: '전체',
        label: employeeLabel
    });
    WorkBridgeSales.populateSelect(createSelect, customerState.salesEmployees, {
        placeholder: '담당자를 선택하세요.',
        label: employeeLabel
    });

    if (isSalesOnly()) {
        createSelect.value = String(customerState.currentUser.employeeId);
        createSelect.disabled = true;
    }
}

async function loadCustomers() {
    const query = WorkBridge.buildQuery({
        customerName: document.getElementById('search-customer-name').value.trim(),
        businessRegistrationNo: document.getElementById('search-business-no').value.trim(),
        contactName: document.getElementById('search-contact-name').value.trim(),
        assignedSalesId: document.getElementById('search-assigned-sales').value,
        status: document.getElementById('search-customer-status').value,
        active: document.getElementById('search-active').value,
        page: customerState.page,
        size: customerState.size
    });
    const body = document.getElementById('customer-table-body');
    body.innerHTML = '<tr><td colspan="6" class="table-empty">고객 정보를 조회하는 중입니다.</td></tr>';
    try {
        const payload = await WorkBridge.request(`/api/v1/customers${query}`);
        renderCustomerRows(payload.data.items || []);
        document.getElementById('customer-result-count').textContent = `${Number(payload.data.totalElements || 0).toLocaleString('ko-KR')}건`;
        WorkBridge.renderPagination(
            document.getElementById('customer-pagination'),
            payload.data.page,
            payload.data.totalPages,
            page => {
                customerState.page = page;
                loadCustomers();
            }
        );
    } catch (error) {
        body.innerHTML = `<tr><td colspan="6" class="table-empty">${WorkBridge.escapeHtml(WorkBridge.errorMessage(error))}</td></tr>`;
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    }
}

function renderCustomerRows(items) {
    const body = document.getElementById('customer-table-body');
    if (!items.length) {
        body.innerHTML = '<tr><td colspan="6" class="table-empty">조회된 고객이 없습니다.</td></tr>';
        return;
    }
    body.innerHTML = items.map(item => `
        <tr class="clickable-row" data-customer-id="${item.customerId}" tabindex="0">
            <td>
                <strong class="table-primary">${WorkBridge.escapeHtml(item.customerName)}</strong>
                <small class="table-secondary">${WorkBridge.escapeHtml(item.representativeName || '대표자 미등록')} · v${item.version}</small>
            </td>
            <td><code>${WorkBridge.escapeHtml(item.formattedBusinessRegistrationNo || item.businessRegistrationNo)}</code></td>
            <td>
                <strong class="table-primary">${WorkBridge.escapeHtml(item.contactName || '-')}</strong>
                <small class="table-secondary">${WorkBridge.escapeHtml(item.contactPhone || item.contactEmail || '연락처 미등록')}</small>
            </td>
            <td>
                <strong class="table-primary">${WorkBridge.escapeHtml(item.assignedSalesName || '-')}</strong>
                <small class="table-secondary">${WorkBridge.escapeHtml(item.assignedSalesEmployeeNo || '-')}</small>
            </td>
            <td>${customerStatusBadge(item.status, item.active)}</td>
            <td>${WorkBridge.formatDateTime(item.updatedAt)}</td>
        </tr>
    `).join('');

    body.querySelectorAll('[data-customer-id]').forEach(row => {
        const move = () => {
            window.location.href = `${WorkBridge.contextPath}/web/customers/${row.dataset.customerId}`;
        };
        row.addEventListener('click', move);
        row.addEventListener('keydown', event => {
            if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                move();
            }
        });
    });
}

async function createCustomer(event) {
    event.preventDefault();
    if (!customerState.canWrite) {
        return;
    }
    const form = event.currentTarget;
    if (!form.reportValidity()) {
        return;
    }
    const message = document.getElementById('customer-create-message');
    const submitButton = form.querySelector('button[type="submit"]');
    setBusy(submitButton, true, '등록 중');
    WorkBridge.setFormMessage(message, '고객 정보를 저장하는 중입니다.');

    const requestBody = {
        businessRegistrationNo: document.getElementById('create-business-no').value.trim(),
        customerName: document.getElementById('create-customer-name').value.trim(),
        representativeName: emptyToNull('create-representative-name'),
        contactName: emptyToNull('create-contact-name'),
        contactPhone: emptyToNull('create-contact-phone'),
        contactEmail: emptyToNull('create-contact-email'),
        establishedDate: document.getElementById('create-established-date').value || null,
        assignedSalesId: numberOrNull(document.getElementById('create-assigned-sales').value),
        status: document.getElementById('create-customer-status').value,
        active: document.getElementById('create-customer-active').checked
    };

    try {
        const payload = await WorkBridge.request('/api/v1/customers', {
            method: 'POST',
            body: requestBody
        });
        WorkBridge.setFormMessage(message, payload.message, 'success');
        WorkBridge.showToast(payload.message, 'success', payload.traceId);
        window.location.href = `${WorkBridge.contextPath}/web/customers/${payload.data.customerId}`;
    } catch (error) {
        WorkBridge.setFormMessage(message, WorkBridge.errorMessage(error), 'danger');
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    } finally {
        setBusy(submitButton, false, '등록');
    }
}

function resetCustomerCreateForm() {
    const form = document.getElementById('customer-create-form');
    if (!form) {
        return;
    }
    form.reset();
    document.getElementById('create-customer-status').value = 'PROSPECT';
    document.getElementById('create-customer-active').checked = true;
    if (isSalesOnly()) {
        document.getElementById('create-assigned-sales').value = String(customerState.currentUser.employeeId);
    }
    WorkBridge.setFormMessage(document.getElementById('customer-create-message'), '');
}

function customerStatusBadge(status, active) {
    if (!active) {
        return '<span class="status-badge status-inactive">미사용</span>';
    }
    return `<span class="status-badge ${WorkBridgeSales.statusBadgeClass(status)}">${WorkBridge.escapeHtml(WorkBridgeSales.customerStatusLabel(status))}</span>`;
}

function employeeLabel(employee) {
    const department = employee.departmentName ? ` · ${employee.departmentName}` : '';
    return `${employee.employeeName} · ${employee.employeeNo}${department}`;
}

function isSalesOnly() {
    return WorkBridgeSales.hasRole(customerState.currentUser, 'SALES')
        && !WorkBridgeSales.hasRole(customerState.currentUser, 'ADMIN');
}

function emptyToNull(elementId) {
    const value = document.getElementById(elementId).value.trim();
    return value || null;
}

function numberOrNull(value) {
    return value === '' || value === null || value === undefined ? null : Number(value);
}

function setBusy(button, busy, normalLabel) {
    if (!button) {
        return;
    }
    button.disabled = busy;
    button.textContent = busy ? '처리 중…' : normalLabel;
}
