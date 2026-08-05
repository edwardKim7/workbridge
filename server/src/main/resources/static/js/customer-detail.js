'use strict';

const customerDetailState = {
    customerId: Number(document.body.dataset.customerId),
    canWriteCustomer: document.body.dataset.canWriteCustomer === 'true',
    canReadOpportunity: document.body.dataset.canReadOpportunity === 'true',
    canWriteOpportunity: document.body.dataset.canWriteOpportunity === 'true',
    canReadActivity: document.body.dataset.canReadActivity === 'true',
    canWriteActivity: document.body.dataset.canWriteActivity === 'true',
    currentUser: null,
    customer: null,
    salesEmployees: [],
    opportunityOptions: [],
    opportunityPage: 0,
    activityPage: 0,
    pageSize: 5
};

document.addEventListener('DOMContentLoaded', initializeCustomerDetail);

async function initializeCustomerDetail() {
    bindCustomerDetailEvents();
    try {
        const [employeePayload, mePayload, customerPayload] = await Promise.all([
            WorkBridge.request('/api/v1/sales/employees/options'),
            WorkBridge.request('/api/v1/auth/me'),
            WorkBridge.request(`/api/v1/customers/${customerDetailState.customerId}`)
        ]);
        customerDetailState.salesEmployees = employeePayload.data || [];
        customerDetailState.currentUser = mePayload.data;
        customerDetailState.customer = customerPayload.data;
        renderCustomerDetail();
        resetOpportunityCreateForm();
        resetActivityCreateForm();
        configureCustomerDetailPermissions();

        const loaders = [];
        if (customerDetailState.canReadOpportunity) {
            loaders.push(loadOpportunityOptions(), loadCustomerOpportunities(), loadOpenOpportunitySummary());
        } else {
            hideSection('customer-opportunity-body', '영업기회 조회 권한이 없습니다.', 5);
        }
        if (customerDetailState.canReadActivity) {
            loaders.push(loadCustomerActivities());
        } else {
            const list = document.getElementById('customer-activity-list');
            if (list) list.innerHTML = '<p class="table-empty">영업활동 조회 권한이 없습니다.</p>';
            document.getElementById('customer-activity-count').textContent = '-';
        }
        await Promise.all(loaders);
    } catch (error) {
        showCustomerDetailError(error);
    }
}

function bindCustomerDetailEvents() {
    document.getElementById('customer-update-form')?.addEventListener('submit', updateCustomer);
    document.getElementById('customer-detail-reload')?.addEventListener('click', reloadCustomerDetail);
    document.getElementById('detail-customer-status')?.addEventListener('change', event => {
        if (event.target.value === 'INACTIVE') {
            document.getElementById('detail-customer-active').checked = false;
        }
    });
    document.getElementById('detail-customer-active')?.addEventListener('change', event => {
        if (!event.target.checked) {
            document.getElementById('detail-customer-status').value = 'INACTIVE';
        } else if (document.getElementById('detail-customer-status').value === 'INACTIVE') {
            document.getElementById('detail-customer-status').value = 'PROSPECT';
        }
    });
    document.getElementById('customer-opportunity-create-form')?.addEventListener('submit', createCustomerOpportunity);
    document.getElementById('customer-opportunity-stage')?.addEventListener('change', updateOpportunityStageRule);
    document.getElementById('customer-activity-create-form')?.addEventListener('submit', createCustomerActivity);
}

function renderCustomerDetail() {
    const customer = customerDetailState.customer;
    if (!customer) return;

    document.getElementById('customer-detail-title').textContent = customer.customerName;
    document.getElementById('customer-detail-subtitle').textContent =
        `${customer.formattedBusinessRegistrationNo || customer.businessRegistrationNo} · 담당 ${customer.assignedSalesName || '-'}`;

    document.getElementById('customer-summary-business-no').textContent =
        customer.formattedBusinessRegistrationNo || customer.businessRegistrationNo;
    document.getElementById('customer-summary-status').textContent = WorkBridgeSales.customerStatusLabel(customer.status);
    document.getElementById('customer-summary-active').textContent = customer.active ? '업무 처리 가능' : '미사용 고객';
    document.getElementById('customer-summary-owner').textContent = customer.assignedSalesName || '-';
    document.getElementById('customer-summary-owner-no').textContent = customer.assignedSalesEmployeeNo || '사번 미등록';
    document.getElementById('customer-version-badge').textContent = `v${customer.version}`;

    document.getElementById('detail-business-no').value = customer.formattedBusinessRegistrationNo || customer.businessRegistrationNo;
    document.getElementById('detail-customer-name').value = customer.customerName;
    document.getElementById('detail-representative-name').value = customer.representativeName || '';
    document.getElementById('detail-contact-name').value = customer.contactName || '';
    document.getElementById('detail-contact-phone').value = customer.contactPhone || '';
    document.getElementById('detail-contact-email').value = customer.contactEmail || '';
    document.getElementById('detail-established-date').value = customer.establishedDate || '';
    document.getElementById('detail-customer-status').value = customer.status;
    document.getElementById('detail-customer-active').checked = customer.active;
    document.getElementById('customer-created-at').textContent = WorkBridge.formatDateTime(customer.createdAt);
    document.getElementById('customer-updated-at').textContent = WorkBridge.formatDateTime(customer.updatedAt);

    populateEmployeeSelect(
        document.getElementById('detail-assigned-sales'),
        customer.assignedSalesId,
        customer.assignedSalesName,
        customer.assignedSalesEmployeeNo
    );
    populateEmployeeSelect(
        document.getElementById('customer-opportunity-owner'),
        customer.assignedSalesId,
        customer.assignedSalesName,
        customer.assignedSalesEmployeeNo
    );
    populateEmployeeSelect(
        document.getElementById('customer-activity-employee'),
        customer.assignedSalesId,
        customer.assignedSalesName,
        customer.assignedSalesEmployeeNo
    );
}

function configureCustomerDetailPermissions() {
    const customer = customerDetailState.customer;
    const canModifyCustomer = customerDetailState.canWriteCustomer && canModifyAssigned(customer.assignedSalesId);
    setFormEditable(document.getElementById('customer-update-form'), canModifyCustomer, ['customer-detail-reload']);
    if (isSalesOnly()) {
        document.getElementById('detail-assigned-sales').disabled = true;
    }
    if (!canModifyCustomer) {
        WorkBridge.setFormMessage(
            document.getElementById('customer-update-message'),
            '조회는 가능하지만 담당 영업사원 또는 관리자가 아니므로 고객 정보를 수정할 수 없습니다.'
        );
    }

    const customerAcceptsWork = customer.active && customer.status !== 'INACTIVE';
    const canCreateOpportunity = customerDetailState.canReadOpportunity
        && customerDetailState.canWriteOpportunity
        && customerAcceptsWork
        && canModifyAssigned(customer.assignedSalesId);
    setFormEditable(document.getElementById('customer-opportunity-create-form'), canCreateOpportunity);
    if (isSalesOnly()) {
        document.getElementById('customer-opportunity-owner').disabled = true;
    }
    if (!canCreateOpportunity) {
        WorkBridge.setFormMessage(
            document.getElementById('customer-opportunity-message'),
            customerAcceptsWork
                ? '이 고객에 영업기회를 등록할 권한이 없습니다.'
                : '미사용 또는 비활성 고객에는 영업기회를 등록할 수 없습니다.'
        );
    }

    const canCreateActivity = customerDetailState.canReadActivity
        && customerDetailState.canWriteActivity
        && customerAcceptsWork
        && canModifyAssigned(customer.assignedSalesId);
    setFormEditable(document.getElementById('customer-activity-create-form'), canCreateActivity);
    if (isSalesOnly()) {
        document.getElementById('customer-activity-employee').disabled = true;
    }
    if (!canCreateActivity) {
        WorkBridge.setFormMessage(
            document.getElementById('customer-activity-message'),
            customerAcceptsWork
                ? '이 고객에 영업활동을 등록할 권한이 없습니다.'
                : '미사용 또는 비활성 고객에는 영업활동을 등록할 수 없습니다.'
        );
    }
}

async function reloadCustomerDetail() {
    const button = document.getElementById('customer-detail-reload');
    setBusy(button, true, '다시 불러오기');
    try {
        const payload = await WorkBridge.request(`/api/v1/customers/${customerDetailState.customerId}`);
        customerDetailState.customer = payload.data;
        renderCustomerDetail();
        configureCustomerDetailPermissions();
        WorkBridge.setFormMessage(document.getElementById('customer-update-message'), '최신 정보를 불러왔습니다.', 'success');
    } catch (error) {
        showCustomerDetailError(error);
    } finally {
        setBusy(button, false, '다시 불러오기');
    }
}

async function updateCustomer(event) {
    event.preventDefault();
    const form = event.currentTarget;
    if (!form.reportValidity()) return;
    const customer = customerDetailState.customer;
    if (!customerDetailState.canWriteCustomer || !canModifyAssigned(customer.assignedSalesId)) return;

    const message = document.getElementById('customer-update-message');
    const button = form.querySelector('button[type="submit"]');
    setBusy(button, true, '변경사항 저장');
    WorkBridge.setFormMessage(message, '고객 정보를 저장하는 중입니다.');

    const requestBody = {
        businessRegistrationNo: document.getElementById('detail-business-no').value.trim(),
        customerName: document.getElementById('detail-customer-name').value.trim(),
        representativeName: emptyToNull('detail-representative-name'),
        contactName: emptyToNull('detail-contact-name'),
        contactPhone: emptyToNull('detail-contact-phone'),
        contactEmail: emptyToNull('detail-contact-email'),
        establishedDate: document.getElementById('detail-established-date').value || null,
        assignedSalesId: numberOrNull(document.getElementById('detail-assigned-sales').value),
        status: document.getElementById('detail-customer-status').value,
        active: document.getElementById('detail-customer-active').checked,
        version: customer.version
    };

    try {
        const payload = await WorkBridge.request(`/api/v1/customers/${customer.customerId}`, {
            method: 'PUT',
            body: requestBody
        });
        customerDetailState.customer = payload.data;
        renderCustomerDetail();
        configureCustomerDetailPermissions();
        WorkBridge.setFormMessage(message, payload.message, 'success');
        WorkBridge.showToast(payload.message, 'success', payload.traceId);
    } catch (error) {
        WorkBridge.setFormMessage(message, WorkBridge.errorMessage(error), 'danger');
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    } finally {
        setBusy(button, false, '변경사항 저장');
    }
}

async function loadCustomerOpportunities() {
    const body = document.getElementById('customer-opportunity-body');
    body.innerHTML = '<tr><td colspan="5" class="table-empty">영업기회를 조회하는 중입니다.</td></tr>';
    try {
        const query = WorkBridge.buildQuery({
            customerId: customerDetailState.customerId,
            page: customerDetailState.opportunityPage,
            size: customerDetailState.pageSize
        });
        const payload = await WorkBridge.request(`/api/v1/opportunities${query}`);
        renderCustomerOpportunityRows(payload.data.items || []);
        document.getElementById('customer-opportunity-count').textContent =
            `${Number(payload.data.totalElements || 0).toLocaleString('ko-KR')}건`;
        WorkBridge.renderPagination(
            document.getElementById('customer-opportunity-pagination'),
            payload.data.page,
            payload.data.totalPages,
            page => {
                customerDetailState.opportunityPage = page;
                loadCustomerOpportunities();
            }
        );
    } catch (error) {
        body.innerHTML = `<tr><td colspan="5" class="table-empty">${WorkBridge.escapeHtml(WorkBridge.errorMessage(error))}</td></tr>`;
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    }
}

function renderCustomerOpportunityRows(items) {
    const body = document.getElementById('customer-opportunity-body');
    if (!items.length) {
        body.innerHTML = '<tr><td colspan="5" class="table-empty">등록된 영업기회가 없습니다.</td></tr>';
        return;
    }
    body.innerHTML = items.map(item => `
        <tr class="clickable-row" data-opportunity-id="${item.opportunityId}" tabindex="0">
            <td><strong class="table-primary">${WorkBridge.escapeHtml(item.opportunityName)}</strong><small class="table-secondary">${WorkBridge.escapeHtml(item.opportunityNo)}</small></td>
            <td><span class="status-badge ${WorkBridgeSales.statusBadgeClass(item.stage)}">${WorkBridge.escapeHtml(WorkBridgeSales.opportunityStageLabel(item.stage))}</span></td>
            <td>${WorkBridge.formatCurrency(item.expectedRevenue)}</td>
            <td>${WorkBridge.formatDate(item.expectedCloseDate)}</td>
            <td><strong class="table-primary">${WorkBridge.escapeHtml(item.ownerEmployeeName || '-')}</strong><small class="table-secondary">${WorkBridge.escapeHtml(item.ownerEmployeeNo || '-')}</small></td>
        </tr>
    `).join('');
    body.querySelectorAll('[data-opportunity-id]').forEach(row => {
        const move = () => {
            window.location.href = `${WorkBridge.contextPath}/web/opportunities/${row.dataset.opportunityId}`;
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

async function loadOpenOpportunitySummary() {
    try {
        const query = WorkBridge.buildQuery({
            customerId: customerDetailState.customerId,
            status: 'OPEN',
            page: 0,
            size: 100
        });
        const payload = await WorkBridge.request(`/api/v1/opportunities${query}`);
        const items = payload.data.items || [];
        const total = Number(payload.data.totalElements || 0);
        const visiblePipeline = items.reduce((sum, item) => sum + Number(item.expectedRevenue || 0), 0);
        document.getElementById('customer-summary-open-opportunities').textContent = total.toLocaleString('ko-KR');
        document.getElementById('customer-summary-pipeline').textContent = total <= items.length
            ? `진행 파이프라인 ${WorkBridge.formatCurrency(visiblePipeline)}`
            : `상위 ${items.length}건 ${WorkBridge.formatCurrency(visiblePipeline)}`;
    } catch (error) {
        document.getElementById('customer-summary-open-opportunities').textContent = '-';
        document.getElementById('customer-summary-pipeline').textContent = '파이프라인 조회 실패';
    }
}

async function loadOpportunityOptions() {
    try {
        const payload = await WorkBridge.request(
            `/api/v1/opportunities/options?customerId=${encodeURIComponent(customerDetailState.customerId)}`
        );
        customerDetailState.opportunityOptions = payload.data || [];
        const select = document.getElementById('customer-activity-opportunity');
        select.innerHTML = '<option value="">고객 공통 활동</option>' + customerDetailState.opportunityOptions.map(item =>
            `<option value="${item.opportunityId}">${WorkBridge.escapeHtml(item.opportunityNo)} · ${WorkBridge.escapeHtml(item.opportunityName)}</option>`
        ).join('');
    } catch (error) {
        document.getElementById('customer-activity-opportunity').innerHTML = '<option value="">영업기회 조회 실패</option>';
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    }
}

async function createCustomerOpportunity(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const customer = customerDetailState.customer;
    if (!form.reportValidity() || !customerDetailState.canWriteOpportunity || !canModifyAssigned(customer.assignedSalesId)) return;

    const message = document.getElementById('customer-opportunity-message');
    const button = form.querySelector('button[type="submit"]');
    setBusy(button, true, '영업기회 등록');
    WorkBridge.setFormMessage(message, '영업기회를 저장하는 중입니다.');
    const requestBody = {
        customerId: customer.customerId,
        opportunityName: document.getElementById('customer-opportunity-name').value.trim(),
        stage: document.getElementById('customer-opportunity-stage').value,
        expectedRevenue: Number(document.getElementById('customer-opportunity-revenue').value),
        expectedCloseDate: document.getElementById('customer-opportunity-close-date').value,
        probability: Number(document.getElementById('customer-opportunity-probability').value),
        ownerEmployeeId: numberOrNull(document.getElementById('customer-opportunity-owner').value),
        description: emptyToNull('customer-opportunity-description')
    };
    try {
        const payload = await WorkBridge.request('/api/v1/opportunities', { method: 'POST', body: requestBody });
        WorkBridge.setFormMessage(message, payload.message, 'success');
        WorkBridge.showToast(payload.message, 'success', payload.traceId);
        customerDetailState.opportunityPage = 0;
        resetOpportunityCreateForm();
        await Promise.all([loadCustomerOpportunities(), loadOpportunityOptions(), loadOpenOpportunitySummary()]);
    } catch (error) {
        WorkBridge.setFormMessage(message, WorkBridge.errorMessage(error), 'danger');
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    } finally {
        setBusy(button, false, '영업기회 등록');
    }
}

function resetOpportunityCreateForm() {
    const form = document.getElementById('customer-opportunity-create-form');
    if (!form) return;
    form.reset();
    document.getElementById('customer-opportunity-stage').value = 'LEAD';
    document.getElementById('customer-opportunity-probability').value = '10';
    document.getElementById('customer-opportunity-close-date').value = dateAfterDays(30);
    document.getElementById('customer-opportunity-owner').value = String(customerDetailState.customer.assignedSalesId);
    updateOpportunityStageRule();
}

function updateOpportunityStageRule() {
    const stage = document.getElementById('customer-opportunity-stage').value;
    const probability = document.getElementById('customer-opportunity-probability');
    if (stage === 'WON') {
        probability.value = '100';
        probability.disabled = true;
    } else if (stage === 'LOST') {
        probability.value = '0';
        probability.disabled = true;
    } else {
        probability.disabled = false;
        if (Number(probability.value) < 0 || Number(probability.value) > 99) {
            probability.value = stage === 'NEGOTIATION' ? '70' : stage === 'PROPOSAL' ? '50' : stage === 'QUALIFIED' ? '30' : '10';
        }
    }
    document.getElementById('customer-opportunity-probability-hint').textContent =
        WorkBridgeSales.opportunityProbabilityHint(stage);
}

async function loadCustomerActivities() {
    const list = document.getElementById('customer-activity-list');
    list.innerHTML = '<p class="table-empty">영업활동을 조회하는 중입니다.</p>';
    try {
        const query = WorkBridge.buildQuery({
            customerId: customerDetailState.customerId,
            page: customerDetailState.activityPage,
            size: customerDetailState.pageSize
        });
        const payload = await WorkBridge.request(`/api/v1/activities${query}`);
        renderCustomerActivities(payload.data.items || []);
        document.getElementById('customer-activity-count').textContent =
            `${Number(payload.data.totalElements || 0).toLocaleString('ko-KR')}건`;
        WorkBridge.renderPagination(
            document.getElementById('customer-activity-pagination'),
            payload.data.page,
            payload.data.totalPages,
            page => {
                customerDetailState.activityPage = page;
                loadCustomerActivities();
            }
        );
    } catch (error) {
        list.innerHTML = `<p class="table-empty">${WorkBridge.escapeHtml(WorkBridge.errorMessage(error))}</p>`;
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    }
}

function renderCustomerActivities(items) {
    const list = document.getElementById('customer-activity-list');
    if (!items.length) {
        list.innerHTML = '<p class="table-empty">등록된 영업활동이 없습니다.</p>';
        return;
    }
    list.innerHTML = items.map(item => `
        <article class="activity-timeline-item">
            <span class="activity-type-marker">${WorkBridge.escapeHtml(WorkBridgeSales.activityTypeLabel(item.activityType).slice(0, 1))}</span>
            <div>
                <div class="timeline-heading"><strong>${WorkBridge.escapeHtml(item.subject)}</strong><time>${WorkBridge.formatDateTime(item.activityAt)}</time></div>
                <p>${WorkBridge.escapeHtml(item.content || '상세 내용 없음')}</p>
                <small>${WorkBridge.escapeHtml(item.employeeName || '-')}${item.opportunityName ? ` · ${WorkBridge.escapeHtml(item.opportunityName)}` : ''}${item.location ? ` · ${WorkBridge.escapeHtml(item.location)}` : ''}</small>
            </div>
        </article>
    `).join('');
}

async function createCustomerActivity(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const customer = customerDetailState.customer;
    if (!form.reportValidity() || !customerDetailState.canWriteActivity || !canModifyAssigned(customer.assignedSalesId)) return;

    const message = document.getElementById('customer-activity-message');
    const button = form.querySelector('button[type="submit"]');
    setBusy(button, true, '활동 저장');
    WorkBridge.setFormMessage(message, '영업활동을 저장하는 중입니다.');
    const requestBody = {
        customerId: customer.customerId,
        opportunityId: numberOrNull(document.getElementById('customer-activity-opportunity').value),
        employeeId: numberOrNull(document.getElementById('customer-activity-employee').value),
        activityType: document.getElementById('customer-activity-type').value,
        subject: document.getElementById('customer-activity-subject').value.trim(),
        activityAt: document.getElementById('customer-activity-at').value,
        location: emptyToNull('customer-activity-location'),
        content: emptyToNull('customer-activity-content')
    };
    try {
        const payload = await WorkBridge.request('/api/v1/activities', { method: 'POST', body: requestBody });
        WorkBridge.setFormMessage(message, payload.message, 'success');
        WorkBridge.showToast(payload.message, 'success', payload.traceId);
        customerDetailState.activityPage = 0;
        resetActivityCreateForm();
        await loadCustomerActivities();
    } catch (error) {
        WorkBridge.setFormMessage(message, WorkBridge.errorMessage(error), 'danger');
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    } finally {
        setBusy(button, false, '활동 저장');
    }
}

function resetActivityCreateForm() {
    const form = document.getElementById('customer-activity-create-form');
    if (!form) return;
    form.reset();
    document.getElementById('customer-activity-type').value = 'VISIT';
    document.getElementById('customer-activity-at').value = WorkBridge.toLocalDateTimeInput();
    document.getElementById('customer-activity-employee').value = String(customerDetailState.customer.assignedSalesId);
    document.getElementById('customer-activity-opportunity').value = '';
}

function populateEmployeeSelect(select, selectedId, fallbackName, fallbackNo) {
    if (!select) return;
    WorkBridgeSales.populateSelect(select, customerDetailState.salesEmployees, {
        placeholder: '담당자를 선택하세요.',
        label: employee => `${employee.employeeName} · ${employee.employeeNo}${employee.departmentName ? ` · ${employee.departmentName}` : ''}`,
        selectedValue: selectedId
    });
    if (selectedId != null && !customerDetailState.salesEmployees.some(item => String(item.employeeId) === String(selectedId))) {
        const option = document.createElement('option');
        option.value = String(selectedId);
        option.textContent = `${fallbackName || '현재 담당자'} · ${fallbackNo || selectedId} (비활성)`;
        option.selected = true;
        select.appendChild(option);
    }
}

function canModifyAssigned(ownerEmployeeId) {
    if (hasRole('ADMIN')) return true;
    return hasRole('SALES') && String(customerDetailState.currentUser?.employeeId) === String(ownerEmployeeId);
}

function hasRole(roleCode) {
    return WorkBridgeSales.hasRole(customerDetailState.currentUser, roleCode);
}

function isSalesOnly() {
    return hasRole('SALES') && !hasRole('ADMIN');
}

function setFormEditable(form, enabled, keepEnabledIds = []) {
    if (!form) return;
    form.querySelectorAll('input, select, textarea, button').forEach(element => {
        element.disabled = !enabled && !keepEnabledIds.includes(element.id);
    });
    form.classList.toggle('read-only-form', !enabled);
}

function hideSection(bodyId, message, colspan) {
    const body = document.getElementById(bodyId);
    if (body) body.innerHTML = `<tr><td colspan="${colspan}" class="table-empty">${WorkBridge.escapeHtml(message)}</td></tr>`;
    document.getElementById('customer-opportunity-count').textContent = '-';
    const form = document.getElementById('customer-opportunity-create-form');
    if (form) form.classList.add('hidden');
}

function showCustomerDetailError(error) {
    const message = WorkBridge.errorMessage(error);
    WorkBridge.showToast(message, 'danger', error.traceId);
    WorkBridge.setFormMessage(document.getElementById('customer-update-message'), message, 'danger');
}

function emptyToNull(elementId) {
    const value = document.getElementById(elementId).value.trim();
    return value || null;
}

function numberOrNull(value) {
    return value === '' || value === null || value === undefined ? null : Number(value);
}

function dateAfterDays(days) {
    const date = new Date();
    date.setDate(date.getDate() + days);
    return WorkBridge.toLocalDateTimeInput(date).slice(0, 10);
}

function setBusy(button, busy, normalLabel) {
    if (!button) return;
    button.disabled = busy;
    button.textContent = busy ? '처리 중…' : normalLabel;
}
