'use strict';

const departmentState = {
    page: 0,
    size: 10,
    items: [],
    referenceItems: [],
    selected: null,
    canWrite: document.body.dataset.canWrite === 'true'
};

const departmentElements = {};

document.addEventListener('DOMContentLoaded', async () => {
    bindDepartmentElements();
    bindDepartmentEvents();
    resetDepartmentForm();
    try {
        await loadDepartmentReferences();
        await loadDepartments();
    } catch (error) {
        handleDepartmentError(error);
    }
});

function bindDepartmentElements() {
    [
        'department-keyword', 'department-active-filter', 'department-search-button',
        'department-table-body', 'department-result-count', 'department-pagination',
        'new-department-button', 'department-form', 'department-id', 'department-version',
        'department-code', 'department-name', 'parent-department', 'department-active',
        'department-save-button', 'department-form-reset', 'department-form-title',
        'department-form-eyebrow', 'department-form-message'
    ].forEach(id => departmentElements[id] = document.getElementById(id));
}

function bindDepartmentEvents() {
    departmentElements['department-search-button'].addEventListener('click', () => {
        departmentState.page = 0;
        loadDepartments().catch(handleDepartmentError);
    });
    departmentElements['department-keyword'].addEventListener('keydown', event => {
        if (event.key === 'Enter') {
            event.preventDefault();
            departmentState.page = 0;
            loadDepartments().catch(handleDepartmentError);
        }
    });
    departmentElements['department-table-body'].addEventListener('click', event => {
        const row = event.target.closest('tr[data-department-id]');
        if (row) {
            selectDepartment(Number(row.dataset.departmentId)).catch(handleDepartmentError);
        }
    });
    departmentElements['new-department-button'].addEventListener('click', resetDepartmentForm);
    departmentElements['department-form-reset'].addEventListener('click', resetDepartmentForm);
    departmentElements['department-form'].addEventListener('submit', event => {
        event.preventDefault();
        saveDepartment().catch(handleDepartmentError);
    });
}

async function loadDepartmentReferences() {
    const payload = await WorkBridge.request('/api/v1/departments/options');
    departmentState.referenceItems = payload.data;
    renderParentDepartmentOptions();
}

async function loadDepartments() {
    setDepartmentLoading(true);
    const params = new URLSearchParams({
        page: String(departmentState.page),
        size: String(departmentState.size)
    });
    const keyword = departmentElements['department-keyword'].value.trim();
    const active = departmentElements['department-active-filter'].value;
    if (keyword) params.set('keyword', keyword);
    if (active) params.set('active', active);

    try {
        const payload = await WorkBridge.request(`/api/v1/departments?${params}`);
        const page = payload.data;
        departmentState.items = page.items;
        if (page.totalPages > 0 && departmentState.page >= page.totalPages) {
            departmentState.page = page.totalPages - 1;
            await loadDepartments();
            return;
        }
        renderDepartmentTable();
        departmentElements['department-result-count'].textContent = `${page.totalElements}건`;
        WorkBridge.renderPagination(
            departmentElements['department-pagination'],
            page.page,
            page.totalPages,
            nextPage => {
                departmentState.page = nextPage;
                loadDepartments().catch(handleDepartmentError);
            }
        );
    } finally {
        setDepartmentLoading(false);
    }
}

function renderDepartmentTable() {
    const body = departmentElements['department-table-body'];
    if (!departmentState.items.length) {
        body.innerHTML = '<tr><td colspan="5" class="table-empty">조회된 부서가 없습니다.</td></tr>';
        return;
    }
    body.innerHTML = departmentState.items.map(item => {
        const selected = departmentState.selected?.departmentId === item.departmentId ? ' selected' : '';
        const statusClass = item.active ? 'status-active' : 'status-inactive';
        const statusLabel = item.active ? '활성' : '비활성';
        return `
            <tr class="clickable-row${selected}" data-department-id="${item.departmentId}">
                <td><strong>${WorkBridge.escapeHtml(item.departmentCode)}</strong></td>
                <td>${WorkBridge.escapeHtml(item.departmentName)}</td>
                <td>${WorkBridge.escapeHtml(item.parentDepartmentName || '최상위')}</td>
                <td><span class="status-badge ${statusClass}">${statusLabel}</span></td>
                <td>v${item.version}</td>
            </tr>`;
    }).join('');
}

async function selectDepartment(departmentId) {
    const payload = await WorkBridge.request(`/api/v1/departments/${departmentId}`);
    departmentState.selected = payload.data;
    const item = departmentState.selected;

    departmentElements['department-id'].value = item.departmentId;
    departmentElements['department-version'].value = item.version;
    departmentElements['department-code'].value = item.departmentCode;
    departmentElements['department-code'].readOnly = true;
    departmentElements['department-name'].value = item.departmentName;
    departmentElements['department-active'].checked = item.active;
    departmentElements['department-form-eyebrow'].textContent = 'EDIT DEPARTMENT';
    departmentElements['department-form-title'].textContent = '부서 수정';
    departmentElements['department-save-button'].textContent = '부서 수정';
    renderParentDepartmentOptions(item.parentDepartmentId, item.departmentId);
    WorkBridge.setFormMessage(departmentElements['department-form-message'], `현재 버전 v${item.version}`);
    renderDepartmentTable();
}

function resetDepartmentForm() {
    departmentState.selected = null;
    departmentElements['department-form'].reset();
    departmentElements['department-id'].value = '';
    departmentElements['department-version'].value = '0';
    departmentElements['department-code'].readOnly = false;
    departmentElements['department-active'].checked = true;
    departmentElements['department-form-eyebrow'].textContent = 'NEW DEPARTMENT';
    departmentElements['department-form-title'].textContent = '부서 등록';
    departmentElements['department-save-button'].textContent = '부서 등록';
    renderParentDepartmentOptions();
    WorkBridge.setFormMessage(
        departmentElements['department-form-message'],
        departmentState.canWrite ? '' : '읽기 권한만 있어 등록·수정할 수 없습니다.'
    );
    renderDepartmentTable();
}

function renderParentDepartmentOptions(selectedId = null, excludedId = null) {
    const options = ['<option value="">최상위 부서</option>'];
    departmentState.referenceItems
        .filter(item => item.active && item.departmentId !== excludedId)
        .sort((a, b) => a.departmentName.localeCompare(b.departmentName, 'ko'))
        .forEach(item => {
            const selected = item.departmentId === selectedId ? ' selected' : '';
            options.push(`<option value="${item.departmentId}"${selected}>${WorkBridge.escapeHtml(item.departmentName)} (${WorkBridge.escapeHtml(item.departmentCode)})</option>`);
        });
    departmentElements['parent-department'].innerHTML = options.join('');
}

async function saveDepartment() {
    ensureDepartmentWritable();
    if (!departmentElements['department-form'].reportValidity()) {
        return;
    }

    const selectedId = Number(departmentElements['department-id'].value || 0);
    const parentValue = departmentElements['parent-department'].value;
    const common = {
        departmentName: departmentElements['department-name'].value.trim(),
        parentDepartmentId: parentValue ? Number(parentValue) : null,
        active: departmentElements['department-active'].checked
    };

    let payload;
    if (selectedId) {
        payload = await WorkBridge.request(`/api/v1/departments/${selectedId}`, {
            method: 'PUT',
            body: {
                ...common,
                version: Number(departmentElements['department-version'].value)
            }
        });
    } else {
        payload = await WorkBridge.request('/api/v1/departments', {
            method: 'POST',
            body: {
                departmentCode: departmentElements['department-code'].value.trim().toUpperCase(),
                ...common
            }
        });
    }

    WorkBridge.showToast(payload.message, 'success', payload.traceId);
    departmentState.selected = payload.data;
    await loadDepartmentReferences();
    await loadDepartments();
    await selectDepartment(payload.data.departmentId);
}

function ensureDepartmentWritable() {
    if (!departmentState.canWrite) {
        throw new Error('부서 등록·수정 권한이 없습니다.');
    }
}

function setDepartmentLoading(loading) {
    departmentElements['department-search-button'].disabled = loading;
    if (loading && !departmentState.items.length) {
        departmentElements['department-table-body'].innerHTML = '<tr><td colspan="5" class="table-empty">부서 정보를 불러오는 중입니다.</td></tr>';
    }
}

function handleDepartmentError(error) {
    const message = WorkBridge.errorMessage(error);
    WorkBridge.setFormMessage(departmentElements['department-form-message'], message, 'danger');
    WorkBridge.showToast(message, 'danger', error.traceId);
    if (error.status === 409 && departmentState.selected) {
        selectDepartment(departmentState.selected.departmentId).catch(() => {});
    }
}
