'use strict';

const activityState = {
    page: 0,
    size: 15,
    canWrite: document.body.dataset.canWriteActivity === 'true',
    customers: [],
    salesEmployees: [],
    currentUser: null,
    writableCustomers: [],
    opportunityFilter: null
};

document.addEventListener('DOMContentLoaded', async () => {
    bindActivityEvents();
    configureActivityWorkspace();
    try {
        const [customerPayload, employeePayload, mePayload] = await Promise.all([
            WorkBridge.request('/api/v1/customers/options'),
            WorkBridge.request('/api/v1/sales/employees/options'),
            WorkBridge.request('/api/v1/auth/me')
        ]);
        activityState.customers = customerPayload.data || [];
        activityState.salesEmployees = employeePayload.data || [];
        activityState.currentUser = mePayload.data;
        renderActivityReferenceOptions();
        resetActivityForm();
        configureActivityCreateAvailability();
        await applyActivityQueryParameters();
        await loadActivities();
    } catch (error) {
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    }
});

function bindActivityEvents() {
    document.getElementById('activity-search-form')?.addEventListener('submit', event => {
        event.preventDefault();
        activityState.page = 0;
        activityState.opportunityFilter = null;
        loadActivities();
    });
    document.getElementById('activity-form')?.addEventListener('submit', createActivity);
    document.getElementById('activity-form-reset')?.addEventListener('click', resetActivityForm);
    document.getElementById('new-activity-button')?.addEventListener('click', () => {
        document.getElementById('activity-customer')?.focus();
        document.getElementById('activity-editor-panel')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
    document.getElementById('activity-customer')?.addEventListener('change', event => {
        loadOpportunityOptions(event.target.value, null);
    });
}

function configureActivityWorkspace() {
    if (activityState.canWrite) {
        return;
    }
    document.getElementById('activity-editor-panel')?.classList.add('hidden');
    document.querySelector('.activity-workspace-grid')?.classList.add('read-only-workspace');
}

function renderActivityReferenceOptions() {
    const customerLabel = customer => `${customer.customerName} · ${customer.formattedBusinessRegistrationNo || customer.businessRegistrationNo}`;
    WorkBridgeSales.populateSelect(document.getElementById('activity-customer-filter'), activityState.customers, {
        placeholder: '전체 고객',
        valueKey: 'customerId',
        label: customerLabel
    });
    activityState.writableCustomers = isSalesOnly()
        ? activityState.customers.filter(customer => String(customer.assignedSalesId) === String(activityState.currentUser.employeeId))
        : activityState.customers;
    WorkBridgeSales.populateSelect(document.getElementById('activity-customer'), activityState.writableCustomers, {
        placeholder: activityState.writableCustomers.length ? '고객을 선택하세요.' : '등록 가능한 고객이 없습니다.',
        valueKey: 'customerId',
        label: customerLabel
    });
    WorkBridgeSales.populateSelect(document.getElementById('activity-employee-filter'), activityState.salesEmployees, {
        placeholder: '전체 담당자',
        label: employeeLabel
    });
    WorkBridgeSales.populateSelect(document.getElementById('activity-employee'), activityState.salesEmployees, {
        placeholder: '담당자를 선택하세요.',
        label: employeeLabel
    });

    if (isSalesOnly()) {
        const employeeSelect = document.getElementById('activity-employee');
        employeeSelect.value = String(activityState.currentUser.employeeId);
        employeeSelect.disabled = true;
    }
}

function configureActivityCreateAvailability() {
    if (!activityState.canWrite || activityState.writableCustomers.length) {
        return;
    }
    WorkBridgeSales.setFormEnabled(document.getElementById('activity-form'), false);
    WorkBridge.setFormMessage(
        document.getElementById('activity-form-message'),
        '현재 계정에 배정된 활성 고객이 없어 영업활동을 등록할 수 없습니다.'
    );
}

async function loadActivities() {
    const fromDate = document.getElementById('activity-from-date').value;
    const toDate = document.getElementById('activity-to-date').value;
    if (fromDate && toDate && fromDate > toDate) {
        WorkBridge.showToast('시작일은 종료일보다 늦을 수 없습니다.', 'danger');
        return;
    }

    const query = WorkBridge.buildQuery({
        keyword: document.getElementById('activity-keyword').value.trim(),
        customerId: document.getElementById('activity-customer-filter').value,
        opportunityId: activityState.opportunityFilter,
        employeeId: document.getElementById('activity-employee-filter').value,
        activityType: document.getElementById('activity-type-filter').value,
        from: fromDate ? `${fromDate}T00:00:00` : '',
        to: toDate ? `${toDate}T23:59:59` : '',
        page: activityState.page,
        size: activityState.size
    });
    const body = document.getElementById('activity-table-body');
    body.innerHTML = '<tr><td colspan="6" class="table-empty">영업활동을 조회하는 중입니다.</td></tr>';

    try {
        const payload = await WorkBridge.request(`/api/v1/activities${query}`);
        renderActivityRows(payload.data.items || []);
        document.getElementById('activity-result-count').textContent = `${Number(payload.data.totalElements || 0).toLocaleString('ko-KR')}건`;
        WorkBridge.renderPagination(
            document.getElementById('activity-pagination'),
            payload.data.page,
            payload.data.totalPages,
            page => {
                activityState.page = page;
                loadActivities();
            }
        );
    } catch (error) {
        body.innerHTML = `<tr><td colspan="6" class="table-empty">${WorkBridge.escapeHtml(WorkBridge.errorMessage(error))}</td></tr>`;
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    }
}

function renderActivityRows(items) {
    const body = document.getElementById('activity-table-body');
    if (!items.length) {
        body.innerHTML = '<tr><td colspan="6" class="table-empty">조회된 영업활동이 없습니다.</td></tr>';
        return;
    }
    body.innerHTML = items.map(item => `
        <tr>
            <td>
                <strong class="table-primary">${WorkBridge.formatDateTime(item.activityAt)}</strong>
                <small class="table-secondary">${WorkBridge.escapeHtml(WorkBridgeSales.activityTypeLabel(item.activityType))}</small>
            </td>
            <td><a class="text-link table-link" href="${WorkBridge.contextPath}/web/customers/${item.customerId}">${WorkBridge.escapeHtml(item.customerName)}</a></td>
            <td>
                <strong class="table-primary">${WorkBridge.escapeHtml(item.subject)}</strong>
                <small class="table-secondary text-clamp">${WorkBridge.escapeHtml(item.content || '상세 내용 없음')}</small>
            </td>
            <td>${item.opportunityId
                ? `<a class="text-link table-link" href="${WorkBridge.contextPath}/web/opportunities/${item.opportunityId}">${WorkBridge.escapeHtml(item.opportunityNo)} · ${WorkBridge.escapeHtml(item.opportunityName)}</a>`
                : '-'}</td>
            <td>
                <strong class="table-primary">${WorkBridge.escapeHtml(item.employeeName || '-')}</strong>
                <small class="table-secondary">${WorkBridge.escapeHtml(item.employeeNo || '-')}</small>
            </td>
            <td>${WorkBridge.escapeHtml(item.location || '-')}</td>
        </tr>
    `).join('');
}

async function createActivity(event) {
    event.preventDefault();
    if (!activityState.canWrite) {
        return;
    }
    const form = event.currentTarget;
    if (!form.reportValidity()) {
        return;
    }
    const message = document.getElementById('activity-form-message');
    const submitButton = document.getElementById('activity-save-button');
    setBusy(submitButton, true, '활동 등록');
    WorkBridge.setFormMessage(message, '영업활동을 저장하는 중입니다.');

    const requestBody = {
        customerId: numberOrNull(document.getElementById('activity-customer').value),
        opportunityId: numberOrNull(document.getElementById('activity-opportunity').value),
        employeeId: numberOrNull(document.getElementById('activity-employee').value),
        activityType: document.getElementById('activity-type').value,
        subject: document.getElementById('activity-subject').value.trim(),
        activityAt: document.getElementById('activity-at').value || null,
        location: emptyToNull('activity-location'),
        content: emptyToNull('activity-content')
    };

    try {
        const payload = await WorkBridge.request('/api/v1/activities', {
            method: 'POST',
            body: requestBody
        });
        WorkBridge.setFormMessage(message, payload.message, 'success');
        WorkBridge.showToast(payload.message, 'success', payload.traceId);
        const customerId = document.getElementById('activity-customer').value;
        resetActivityForm();
        if (customerId) {
            document.getElementById('activity-customer-filter').value = customerId;
        }
        activityState.page = 0;
        await loadActivities();
    } catch (error) {
        WorkBridge.setFormMessage(message, WorkBridge.errorMessage(error), 'danger');
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    } finally {
        setBusy(submitButton, false, '활동 등록');
    }
}

function resetActivityForm() {
    const form = document.getElementById('activity-form');
    if (!form) {
        return;
    }
    form.reset();
    document.getElementById('activity-type').value = 'VISIT';
    document.getElementById('activity-at').value = WorkBridge.toLocalDateTimeInput();
    const opportunitySelect = document.getElementById('activity-opportunity');
    opportunitySelect.innerHTML = '<option value="">고객을 먼저 선택하세요.</option>';
    opportunitySelect.disabled = true;
    if (isSalesOnly()) {
        document.getElementById('activity-employee').value = String(activityState.currentUser.employeeId);
    }
    WorkBridge.setFormMessage(document.getElementById('activity-form-message'), '');
}

async function applyActivityQueryParameters() {
    const query = new URLSearchParams(window.location.search);
    const customerId = query.get('customerId');
    const opportunityId = query.get('opportunityId');
    if (customerId && activityState.customers.some(item => String(item.customerId) === customerId)) {
        document.getElementById('activity-customer-filter').value = customerId;
        activityState.opportunityFilter = opportunityId || null;
        const createSelect = document.getElementById('activity-customer');
        const canSelectForCreate = Array.from(createSelect.options).some(option => option.value === customerId);
        if (canSelectForCreate) {
            createSelect.value = customerId;
            await loadOpportunityOptions(customerId, opportunityId);
        }
    }
    if (query.get('create') === 'true' && activityState.canWrite) {
        document.getElementById('activity-subject').focus();
    }
}

async function loadOpportunityOptions(customerId, selectedOpportunityId) {
    const select = document.getElementById('activity-opportunity');
    if (!customerId) {
        select.innerHTML = '<option value="">고객을 먼저 선택하세요.</option>';
        select.disabled = true;
        return;
    }
    select.disabled = true;
    select.innerHTML = '<option value="">영업기회를 불러오는 중입니다.</option>';
    try {
        const payload = await WorkBridge.request(`/api/v1/opportunities/options?customerId=${encodeURIComponent(customerId)}`);
        select.innerHTML = '<option value="">연결하지 않음</option>' + (payload.data || []).map(item =>
            `<option value="${item.opportunityId}">${WorkBridge.escapeHtml(item.opportunityNo)} · ${WorkBridge.escapeHtml(item.opportunityName)}</option>`
        ).join('');
        select.disabled = false;
        if (selectedOpportunityId && (payload.data || []).some(item => String(item.opportunityId) === String(selectedOpportunityId))) {
            select.value = String(selectedOpportunityId);
        }
    } catch (error) {
        select.innerHTML = '<option value="">영업기회 조회 실패</option>';
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    }
}

function employeeLabel(employee) {
    const department = employee.departmentName ? ` · ${employee.departmentName}` : '';
    return `${employee.employeeName} · ${employee.employeeNo}${department}`;
}

function isSalesOnly() {
    return WorkBridgeSales.hasRole(activityState.currentUser, 'SALES')
        && !WorkBridgeSales.hasRole(activityState.currentUser, 'ADMIN');
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
