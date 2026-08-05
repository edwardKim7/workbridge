'use strict';

const opportunityDetailState = {
    opportunityId: Number(document.body.dataset.opportunityId),
    canWriteOpportunity: document.body.dataset.canWriteOpportunity === 'true',
    canReadCustomer: document.body.dataset.canReadCustomer === 'true',
    canReadActivity: document.body.dataset.canReadActivity === 'true',
    canWriteActivity: document.body.dataset.canWriteActivity === 'true',
    currentUser: null,
    opportunity: null,
    salesEmployees: [],
    activityPage: 0,
    pageSize: 5
};

document.addEventListener('DOMContentLoaded', initializeOpportunityDetail);

async function initializeOpportunityDetail() {
    bindOpportunityDetailEvents();
    try {
        const [employeePayload, mePayload, opportunityPayload] = await Promise.all([
            WorkBridge.request('/api/v1/sales/employees/options'),
            WorkBridge.request('/api/v1/auth/me'),
            WorkBridge.request(`/api/v1/opportunities/${opportunityDetailState.opportunityId}`)
        ]);
        opportunityDetailState.salesEmployees = employeePayload.data || [];
        opportunityDetailState.currentUser = mePayload.data;
        opportunityDetailState.opportunity = opportunityPayload.data;

        renderOpportunityDetail();
        configureOpportunityPermissions();
        resetOpportunityActivityForm();
        if (opportunityDetailState.canReadActivity) {
            await loadOpportunityActivities();
        } else {
            document.getElementById('opportunity-activity-list').innerHTML =
                '<p class="table-empty">영업활동 조회 권한이 없습니다.</p>';
            document.getElementById('opportunity-activity-count').textContent = '-';
            document.getElementById('opportunity-activity-create-form').classList.add('hidden');
        }
    } catch (error) {
        showOpportunityDetailError(error);
    }
}

function bindOpportunityDetailEvents() {
    document.getElementById('opportunity-update-form')?.addEventListener('submit', updateOpportunity);
    document.getElementById('opportunity-detail-reload')?.addEventListener('click', reloadOpportunityDetail);
    document.getElementById('detail-opportunity-stage')?.addEventListener('change', () => updateStageRule(true));
    document.getElementById('opportunity-activity-create-form')?.addEventListener('submit', createOpportunityActivity);
}

function renderOpportunityDetail() {
    const opportunity = opportunityDetailState.opportunity;
    if (!opportunity) return;

    document.getElementById('opportunity-detail-title').textContent = opportunity.opportunityName;
    document.getElementById('opportunity-detail-subtitle').textContent =
        `${opportunity.customerName} · 담당 ${opportunity.ownerEmployeeName || '-'}`;
    document.getElementById('opportunity-summary-no').textContent = opportunity.opportunityNo;
    document.getElementById('opportunity-summary-status').textContent =
        WorkBridgeSales.opportunityStatusLabel(opportunity.status);
    document.getElementById('opportunity-summary-stage').textContent =
        WorkBridgeSales.opportunityStageLabel(opportunity.stage);
    document.getElementById('opportunity-summary-probability').textContent = `수주확률 ${opportunity.probability}%`;
    document.getElementById('opportunity-summary-revenue').textContent =
        WorkBridge.formatCurrency(opportunity.expectedRevenue);
    document.getElementById('opportunity-summary-close').textContent =
        WorkBridge.formatDate(opportunity.expectedCloseDate);
    document.getElementById('opportunity-summary-owner').textContent =
        `${opportunity.ownerEmployeeName || '-'} · ${opportunity.ownerEmployeeNo || '-'}`;
    document.getElementById('opportunity-version-badge').textContent = `v${opportunity.version}`;

    document.getElementById('detail-opportunity-no').value = opportunity.opportunityNo;
    document.getElementById('detail-opportunity-customer').value =
        `${opportunity.customerName} · ${opportunity.formattedBusinessRegistrationNo || opportunity.businessRegistrationNo}`;
    document.getElementById('detail-opportunity-name').value = opportunity.opportunityName;
    document.getElementById('detail-opportunity-stage').value = opportunity.stage;
    document.getElementById('detail-opportunity-probability').value = opportunity.probability;
    document.getElementById('detail-opportunity-close-date').value = opportunity.expectedCloseDate || '';
    document.getElementById('detail-opportunity-revenue').value = opportunity.expectedRevenue;
    document.getElementById('detail-opportunity-description').value = opportunity.description || '';
    document.getElementById('opportunity-customer-business-no').textContent =
        opportunity.formattedBusinessRegistrationNo || opportunity.businessRegistrationNo;
    document.getElementById('opportunity-created-at').textContent = WorkBridge.formatDateTime(opportunity.createdAt);
    document.getElementById('opportunity-updated-at').textContent = WorkBridge.formatDateTime(opportunity.updatedAt);
    const customerLink = document.getElementById('opportunity-customer-link');
    if (opportunityDetailState.canReadCustomer) {
        customerLink.href = `${WorkBridge.contextPath}/web/customers/${opportunity.customerId}`;
        customerLink.classList.remove('hidden');
    } else {
        customerLink.removeAttribute('href');
        customerLink.classList.add('hidden');
    }

    populateEmployeeSelect(
        document.getElementById('detail-opportunity-owner'),
        opportunity.ownerEmployeeId,
        opportunity.ownerEmployeeName,
        opportunity.ownerEmployeeNo
    );
    populateEmployeeSelect(
        document.getElementById('opportunity-activity-employee'),
        opportunity.ownerEmployeeId,
        opportunity.ownerEmployeeName,
        opportunity.ownerEmployeeNo
    );
    updateStageRule(false);
}

function configureOpportunityPermissions() {
    const opportunity = opportunityDetailState.opportunity;
    const customerAcceptsWork = opportunity.customerActive
        && opportunity.customerStatus !== 'INACTIVE';
    const canModifyOpportunity = opportunityDetailState.canWriteOpportunity
        && customerAcceptsWork
        && canModifyAssigned(opportunity.ownerEmployeeId);
    setFormEditable(document.getElementById('opportunity-update-form'), canModifyOpportunity, [
        'detail-opportunity-no',
        'detail-opportunity-customer',
        'opportunity-detail-reload'
    ]);
    document.getElementById('detail-opportunity-no').readOnly = true;
    document.getElementById('detail-opportunity-customer').readOnly = true;
    if (isSalesOnly()) {
        document.getElementById('detail-opportunity-owner').disabled = true;
    }
    if (opportunity.status === 'CLOSED') {
        document.getElementById('detail-opportunity-stage').disabled = true;
    }
    if (!canModifyOpportunity) {
        WorkBridge.setFormMessage(
            document.getElementById('opportunity-update-message'),
            customerAcceptsWork
                ? '조회는 가능하지만 담당 영업사원 또는 관리자가 아니므로 영업기회를 수정할 수 없습니다.'
                : '비활성 고객의 기존 영업기회는 이력 보존을 위해 조회만 가능합니다.'
        );
    }
    const canCreateActivity = opportunityDetailState.canReadActivity
        && opportunityDetailState.canWriteActivity
        && customerAcceptsWork
        && canModifyAssigned(opportunity.customerAssignedSalesId)
        && canModifyAssigned(opportunity.ownerEmployeeId);
    setFormEditable(document.getElementById('opportunity-activity-create-form'), canCreateActivity);
    if (isSalesOnly()) {
        document.getElementById('opportunity-activity-employee').disabled = true;
    }
    if (!canCreateActivity) {
        WorkBridge.setFormMessage(
            document.getElementById('opportunity-activity-message'),
            customerAcceptsWork
                ? '이 영업기회에 활동을 등록할 권한이 없습니다.'
                : '미사용 또는 비활성 고객에는 영업활동을 등록할 수 없습니다.'
        );
    }
}

async function reloadOpportunityDetail() {
    const button = document.getElementById('opportunity-detail-reload');
    setBusy(button, true, '다시 불러오기');
    try {
        const opportunityPayload = await WorkBridge.request(
            `/api/v1/opportunities/${opportunityDetailState.opportunityId}`
        );
        opportunityDetailState.opportunity = opportunityPayload.data;
        renderOpportunityDetail();
        configureOpportunityPermissions();
        WorkBridge.setFormMessage(document.getElementById('opportunity-update-message'), '최신 정보를 불러왔습니다.', 'success');
    } catch (error) {
        showOpportunityDetailError(error);
    } finally {
        setBusy(button, false, '다시 불러오기');
    }
}

async function updateOpportunity(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const opportunity = opportunityDetailState.opportunity;
    if (!form.reportValidity()
        || !opportunityDetailState.canWriteOpportunity
        || !opportunity.customerActive
        || opportunity.customerStatus === 'INACTIVE'
        || !canModifyAssigned(opportunity.ownerEmployeeId)) {
        return;
    }

    const message = document.getElementById('opportunity-update-message');
    const button = form.querySelector('button[type="submit"]');
    setBusy(button, true, '변경사항 저장');
    WorkBridge.setFormMessage(message, '영업기회를 저장하는 중입니다.');
    const requestBody = {
        opportunityName: document.getElementById('detail-opportunity-name').value.trim(),
        stage: document.getElementById('detail-opportunity-stage').value,
        expectedRevenue: Number(document.getElementById('detail-opportunity-revenue').value),
        expectedCloseDate: document.getElementById('detail-opportunity-close-date').value,
        probability: Number(document.getElementById('detail-opportunity-probability').value),
        ownerEmployeeId: numberOrNull(document.getElementById('detail-opportunity-owner').value),
        description: emptyToNull('detail-opportunity-description'),
        version: opportunity.version
    };

    try {
        const payload = await WorkBridge.request(`/api/v1/opportunities/${opportunity.opportunityId}`, {
            method: 'PUT',
            body: requestBody
        });
        opportunityDetailState.opportunity = payload.data;
        renderOpportunityDetail();
        configureOpportunityPermissions();
        WorkBridge.setFormMessage(message, payload.message, 'success');
        WorkBridge.showToast(payload.message, 'success', payload.traceId);
    } catch (error) {
        WorkBridge.setFormMessage(message, WorkBridge.errorMessage(error), 'danger');
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    } finally {
        setBusy(button, false, '변경사항 저장');
    }
}

function updateStageRule(adjustProbability) {
    const stage = document.getElementById('detail-opportunity-stage').value;
    const probability = document.getElementById('detail-opportunity-probability');
    if (stage === 'WON') {
        if (adjustProbability) probability.value = '100';
        probability.max = '100';
    } else if (stage === 'LOST') {
        if (adjustProbability) probability.value = '0';
        probability.max = '100';
    } else {
        probability.max = '99';
        const current = Number(probability.value);
        if (adjustProbability && (!Number.isFinite(current) || current < 0 || current > 99)) {
            probability.value = stage === 'NEGOTIATION' ? '70' : stage === 'PROPOSAL' ? '50' : stage === 'QUALIFIED' ? '30' : '10';
        }
    }
    document.getElementById('detail-opportunity-probability-hint').textContent =
        opportunityDetailState.opportunity?.status === 'CLOSED'
            ? `${WorkBridgeSales.opportunityProbabilityHint(stage)} 종결 단계는 다른 단계로 변경할 수 없습니다.`
            : WorkBridgeSales.opportunityProbabilityHint(stage);
}

async function loadOpportunityActivities() {
    const list = document.getElementById('opportunity-activity-list');
    list.innerHTML = '<p class="table-empty">영업활동을 조회하는 중입니다.</p>';
    try {
        const query = WorkBridge.buildQuery({
            opportunityId: opportunityDetailState.opportunityId,
            page: opportunityDetailState.activityPage,
            size: opportunityDetailState.pageSize
        });
        const payload = await WorkBridge.request(`/api/v1/activities${query}`);
        renderOpportunityActivities(payload.data.items || []);
        document.getElementById('opportunity-activity-count').textContent =
            `${Number(payload.data.totalElements || 0).toLocaleString('ko-KR')}건`;
        WorkBridge.renderPagination(
            document.getElementById('opportunity-activity-pagination'),
            payload.data.page,
            payload.data.totalPages,
            page => {
                opportunityDetailState.activityPage = page;
                loadOpportunityActivities();
            }
        );
    } catch (error) {
        list.innerHTML = `<p class="table-empty">${WorkBridge.escapeHtml(WorkBridge.errorMessage(error))}</p>`;
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    }
}

function renderOpportunityActivities(items) {
    const list = document.getElementById('opportunity-activity-list');
    if (!items.length) {
        list.innerHTML = '<p class="table-empty">연결된 영업활동이 없습니다.</p>';
        return;
    }
    list.innerHTML = items.map(item => `
        <article class="activity-timeline-item">
            <span class="activity-type-marker">${WorkBridge.escapeHtml(WorkBridgeSales.activityTypeLabel(item.activityType).slice(0, 1))}</span>
            <div>
                <div class="timeline-heading"><strong>${WorkBridge.escapeHtml(item.subject)}</strong><time>${WorkBridge.formatDateTime(item.activityAt)}</time></div>
                <p>${WorkBridge.escapeHtml(item.content || '상세 내용 없음')}</p>
                <small>${WorkBridge.escapeHtml(item.employeeName || '-')}${item.location ? ` · ${WorkBridge.escapeHtml(item.location)}` : ''}</small>
            </div>
        </article>
    `).join('');
}

async function createOpportunityActivity(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const opportunity = opportunityDetailState.opportunity;
    if (!form.reportValidity()
        || !opportunityDetailState.canWriteActivity
        || !opportunity.customerActive
        || opportunity.customerStatus === 'INACTIVE'
        || !canModifyAssigned(opportunity.customerAssignedSalesId)
        || !canModifyAssigned(opportunity.ownerEmployeeId)) {
        return;
    }

    const message = document.getElementById('opportunity-activity-message');
    const button = form.querySelector('button[type="submit"]');
    setBusy(button, true, '활동 저장');
    WorkBridge.setFormMessage(message, '영업활동을 저장하는 중입니다.');
    const requestBody = {
        customerId: opportunity.customerId,
        opportunityId: opportunity.opportunityId,
        employeeId: numberOrNull(document.getElementById('opportunity-activity-employee').value),
        activityType: document.getElementById('opportunity-activity-type').value,
        subject: document.getElementById('opportunity-activity-subject').value.trim(),
        activityAt: document.getElementById('opportunity-activity-at').value,
        location: emptyToNull('opportunity-activity-location'),
        content: emptyToNull('opportunity-activity-content')
    };

    try {
        const payload = await WorkBridge.request('/api/v1/activities', { method: 'POST', body: requestBody });
        WorkBridge.setFormMessage(message, payload.message, 'success');
        WorkBridge.showToast(payload.message, 'success', payload.traceId);
        opportunityDetailState.activityPage = 0;
        resetOpportunityActivityForm();
        await loadOpportunityActivities();
    } catch (error) {
        WorkBridge.setFormMessage(message, WorkBridge.errorMessage(error), 'danger');
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    } finally {
        setBusy(button, false, '활동 저장');
    }
}

function resetOpportunityActivityForm() {
    const form = document.getElementById('opportunity-activity-create-form');
    if (!form || !opportunityDetailState.opportunity) return;
    form.reset();
    document.getElementById('opportunity-activity-type').value = 'VISIT';
    document.getElementById('opportunity-activity-at').value = WorkBridge.toLocalDateTimeInput();
    document.getElementById('opportunity-activity-employee').value =
        String(opportunityDetailState.opportunity.ownerEmployeeId);
}

function populateEmployeeSelect(select, selectedId, fallbackName, fallbackNo) {
    if (!select) return;
    WorkBridgeSales.populateSelect(select, opportunityDetailState.salesEmployees, {
        placeholder: '담당자를 선택하세요.',
        label: employee => `${employee.employeeName} · ${employee.employeeNo}${employee.departmentName ? ` · ${employee.departmentName}` : ''}`,
        selectedValue: selectedId
    });
    if (selectedId != null && !opportunityDetailState.salesEmployees.some(item => String(item.employeeId) === String(selectedId))) {
        const option = document.createElement('option');
        option.value = String(selectedId);
        option.textContent = `${fallbackName || '현재 담당자'} · ${fallbackNo || selectedId} (비활성)`;
        option.selected = true;
        select.appendChild(option);
    }
}

function canModifyAssigned(ownerEmployeeId) {
    if (hasRole('ADMIN')) return true;
    return hasRole('SALES') && String(opportunityDetailState.currentUser?.employeeId) === String(ownerEmployeeId);
}

function hasRole(roleCode) {
    return WorkBridgeSales.hasRole(opportunityDetailState.currentUser, roleCode);
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

function showOpportunityDetailError(error) {
    const message = WorkBridge.errorMessage(error);
    WorkBridge.showToast(message, 'danger', error.traceId);
    WorkBridge.setFormMessage(document.getElementById('opportunity-update-message'), message, 'danger');
}

function emptyToNull(elementId) {
    const value = document.getElementById(elementId).value.trim();
    return value || null;
}

function numberOrNull(value) {
    return value === '' || value === null || value === undefined ? null : Number(value);
}

function setBusy(button, busy, normalLabel) {
    if (!button) return;
    button.disabled = busy;
    button.textContent = busy ? '처리 중…' : normalLabel;
}
