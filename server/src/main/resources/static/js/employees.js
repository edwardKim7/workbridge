'use strict';

const employeeState = {
    page: 0,
    size: 10,
    departments: [],
    roles: [],
    selected: null,
    canWrite: document.body.dataset.canWrite === 'true'
};

const employeeElements = {};

document.addEventListener('DOMContentLoaded', async () => {
    bindEmployeeElements();
    bindEmployeeEvents();
    try {
        await loadEmployeeReferences();
        resetEmployeeForm();
        await loadEmployees();
    } catch (error) {
        handleEmployeeError(error);
    }
});

function bindEmployeeElements() {
    const ids = [
        'employee-keyword', 'employee-department-filter', 'employee-active-filter',
        'employee-search-button', 'employee-table-body', 'employee-result-count',
        'employee-pagination', 'new-employee-button', 'employee-form', 'employee-id',
        'employee-version', 'employee-no', 'employee-login-id', 'employee-password',
        'employee-name', 'employee-email', 'employee-department', 'employee-active',
        'employee-role-options', 'employee-save-button', 'employee-role-save-button',
        'employee-form-reset', 'employee-form-title', 'employee-form-eyebrow',
        'password-field', 'create-active-field', 'employee-account-actions',
        'employee-account-state', 'employee-state-button', 'employee-unlock-button',
        'employee-form-message'
    ];
    ids.forEach(id => employeeElements[id] = document.getElementById(id));
}

function bindEmployeeEvents() {
    employeeElements['employee-search-button'].addEventListener('click', () => {
        employeeState.page = 0;
        loadEmployees().catch(handleEmployeeError);
    });
    employeeElements['employee-keyword'].addEventListener('keydown', event => {
        if (event.key === 'Enter') {
            event.preventDefault();
            employeeState.page = 0;
            loadEmployees().catch(handleEmployeeError);
        }
    });
    employeeElements['employee-table-body'].addEventListener('click', event => {
        const row = event.target.closest('tr[data-employee-id]');
        if (row) {
            selectEmployee(Number(row.dataset.employeeId)).catch(handleEmployeeError);
        }
    });
    employeeElements['new-employee-button'].addEventListener('click', resetEmployeeForm);
    employeeElements['employee-form-reset'].addEventListener('click', resetEmployeeForm);
    employeeElements['employee-form'].addEventListener('submit', event => {
        event.preventDefault();
        saveEmployee().catch(handleEmployeeError);
    });
    employeeElements['employee-role-save-button'].addEventListener('click', () => {
        saveEmployeeRoles().catch(handleEmployeeError);
    });
    employeeElements['employee-state-button'].addEventListener('click', () => {
        changeEmployeeState().catch(handleEmployeeError);
    });
    employeeElements['employee-unlock-button'].addEventListener('click', () => {
        unlockEmployee().catch(handleEmployeeError);
    });
}

async function loadEmployeeReferences() {
    const [departmentsPayload, rolesPayload] = await Promise.all([
        WorkBridge.request('/api/v1/departments/options'),
        WorkBridge.request('/api/v1/roles')
    ]);
    employeeState.departments = departmentsPayload.data;
    employeeState.roles = rolesPayload.data.filter(item => item.active);
    renderEmployeeReferenceOptions();
}

function renderEmployeeReferenceOptions() {
    const departmentOptions = employeeState.departments
        .map(department => `<option value="${department.departmentId}">${WorkBridge.escapeHtml(department.departmentCode)} · ${WorkBridge.escapeHtml(department.departmentName)}</option>`)
        .join('');
    employeeElements['employee-department-filter'].innerHTML = `<option value="">전체 부서</option>${departmentOptions}`;
    employeeElements['employee-department'].innerHTML = `<option value="">부서를 선택하세요</option>${departmentOptions}`;
    employeeElements['employee-role-options'].innerHTML = employeeState.roles
        .map(role => `
            <label class="role-option">
                <input type="checkbox" name="employee-role" value="${role.roleId}" ${employeeState.canWrite ? '' : 'disabled'}>
                <span><strong>${WorkBridge.escapeHtml(role.roleName)}</strong><small>${WorkBridge.escapeHtml(role.roleCode)}</small></span>
            </label>
        `).join('');
}

async function loadEmployees() {
    const params = new URLSearchParams({
        page: String(employeeState.page),
        size: String(employeeState.size)
    });
    const keyword = employeeElements['employee-keyword'].value.trim();
    const departmentId = employeeElements['employee-department-filter'].value;
    const active = employeeElements['employee-active-filter'].value;
    if (keyword) params.set('keyword', keyword);
    if (departmentId) params.set('departmentId', departmentId);
    if (active) params.set('active', active);

    const payload = await WorkBridge.request(`/api/v1/employees?${params}`);
    renderEmployees(payload.data);
}

function renderEmployees(pageData) {
    employeeElements['employee-result-count'].textContent = `${pageData.totalElements}건`;
    if (!pageData.items.length) {
        employeeElements['employee-table-body'].innerHTML = '<tr><td colspan="6" class="table-empty">조회된 직원이 없습니다.</td></tr>';
    } else {
        employeeElements['employee-table-body'].innerHTML = pageData.items.map(employee => {
            const locked = isEmployeeLocked(employee);
            const status = !employee.active
                ? '<span class="status-badge status-inactive">비활성</span>'
                : locked
                    ? '<span class="status-badge status-warning">잠금</span>'
                    : '<span class="status-badge status-active">활성</span>';
            const roles = employee.roles.length
                ? employee.roles.map(role => `<span class="mini-chip">${WorkBridge.escapeHtml(role.roleCode)}</span>`).join(' ')
                : '<span class="muted-text">역할 없음</span>';
            return `
                <tr data-employee-id="${employee.employeeId}" class="clickable-row ${employeeState.selected?.employeeId === employee.employeeId ? 'selected' : ''}">
                    <td><strong>${WorkBridge.escapeHtml(employee.employeeNo)}</strong><small>${WorkBridge.escapeHtml(employee.loginId)}</small></td>
                    <td>${WorkBridge.escapeHtml(employee.employeeName)}<small>${WorkBridge.escapeHtml(employee.email)}</small></td>
                    <td>${WorkBridge.escapeHtml(employee.departmentName)}</td>
                    <td>${roles}</td>
                    <td>${status}</td>
                    <td>${WorkBridge.escapeHtml(WorkBridge.formatDateTime(employee.lastLoginAt))}</td>
                </tr>`;
        }).join('');
    }
    WorkBridge.renderPagination(
        employeeElements['employee-pagination'],
        pageData.page,
        pageData.totalPages,
        targetPage => {
            employeeState.page = targetPage;
            loadEmployees().catch(handleEmployeeError);
        }
    );
}

async function selectEmployee(employeeId) {
    const payload = await WorkBridge.request(`/api/v1/employees/${employeeId}`);
    employeeState.selected = payload.data;
    populateEmployeeForm(payload.data);
    await loadEmployees();
}

function populateEmployeeForm(employee) {
    employeeElements['employee-id'].value = employee.employeeId;
    employeeElements['employee-version'].value = employee.version;
    employeeElements['employee-no'].value = employee.employeeNo;
    employeeElements['employee-login-id'].value = employee.loginId;
    employeeElements['employee-name'].value = employee.employeeName;
    employeeElements['employee-email'].value = employee.email;
    employeeElements['employee-department'].value = String(employee.departmentId);
    employeeElements['employee-no'].disabled = true;
    employeeElements['employee-login-id'].disabled = true;
    employeeElements['password-field'].classList.add('hidden');
    employeeElements['create-active-field'].classList.add('hidden');
    employeeElements['employee-form-title'].textContent = '직원 상세·수정';
    employeeElements['employee-form-eyebrow'].textContent = 'EMPLOYEE DETAIL';
    employeeElements['employee-save-button'].textContent = '기본정보 저장';
    employeeElements['employee-role-save-button'].classList.remove('hidden');
    employeeElements['employee-account-actions'].classList.remove('hidden');
    setCheckedRoleIds(employee.roles.map(role => role.roleId));
    updateAccountActionState(employee);
    WorkBridge.setFormMessage(employeeElements['employee-form-message'], '수정 시 최신 version을 조건으로 반영합니다.');
}

function resetEmployeeForm() {
    employeeState.selected = null;
    employeeElements['employee-form'].reset();
    employeeElements['employee-id'].value = '';
    employeeElements['employee-version'].value = '0';
    employeeElements['employee-no'].disabled = false;
    employeeElements['employee-login-id'].disabled = false;
    employeeElements['password-field'].classList.remove('hidden');
    employeeElements['create-active-field'].classList.remove('hidden');
    employeeElements['employee-active'].checked = true;
    employeeElements['employee-form-title'].textContent = '직원 등록';
    employeeElements['employee-form-eyebrow'].textContent = 'NEW EMPLOYEE';
    employeeElements['employee-save-button'].textContent = '직원 등록';
    employeeElements['employee-role-save-button'].classList.add('hidden');
    employeeElements['employee-account-actions'].classList.add('hidden');
    setCheckedRoleIds([]);
    WorkBridge.setFormMessage(employeeElements['employee-form-message'], '초기 비밀번호는 BCrypt 해시로만 저장합니다.');
    loadEmployees().catch(() => {});
}

async function saveEmployee() {
    ensureEmployeeWritable();
    if (!employeeElements['employee-form'].reportValidity()) {
        return;
    }
    const employeeId = employeeElements['employee-id'].value;
    if (!employeeId) {
        const roleIds = selectedRoleIds();
        if (!roleIds.length) {
            WorkBridge.setFormMessage(employeeElements['employee-form-message'], '역할을 하나 이상 선택해 주세요.', 'danger');
            return;
        }
        const payload = await WorkBridge.request('/api/v1/employees', {
            method: 'POST',
            body: {
                employeeNo: employeeElements['employee-no'].value.trim(),
                loginId: employeeElements['employee-login-id'].value.trim(),
                password: employeeElements['employee-password'].value,
                employeeName: employeeElements['employee-name'].value.trim(),
                email: employeeElements['employee-email'].value.trim(),
                departmentId: Number(employeeElements['employee-department'].value),
                active: employeeElements['employee-active'].checked,
                roleIds
            }
        });
        WorkBridge.showToast(payload.message);
        employeeState.selected = payload.data;
        populateEmployeeForm(payload.data);
    } else {
        const payload = await WorkBridge.request(`/api/v1/employees/${employeeId}`, {
            method: 'PUT',
            body: {
                employeeName: employeeElements['employee-name'].value.trim(),
                email: employeeElements['employee-email'].value.trim(),
                departmentId: Number(employeeElements['employee-department'].value),
                version: Number(employeeElements['employee-version'].value)
            }
        });
        WorkBridge.showToast(payload.message);
        employeeState.selected = payload.data;
        populateEmployeeForm(payload.data);
    }
    await loadEmployees();
}

async function saveEmployeeRoles() {
    ensureEmployeeWritable();
    const employee = requireSelectedEmployee();
    const roleIds = selectedRoleIds();
    if (!roleIds.length) {
        WorkBridge.setFormMessage(employeeElements['employee-form-message'], '역할을 하나 이상 선택해 주세요.', 'danger');
        return;
    }
    const payload = await WorkBridge.request(`/api/v1/employees/${employee.employeeId}/roles`, {
        method: 'PUT',
        body: { roleIds, version: Number(employeeElements['employee-version'].value) }
    });
    WorkBridge.showToast(payload.message);
    employeeState.selected = payload.data;
    populateEmployeeForm(payload.data);
    await loadEmployees();
}

async function changeEmployeeState() {
    ensureEmployeeWritable();
    const employee = requireSelectedEmployee();
    const action = employee.active ? 'deactivate' : 'activate';
    const payload = await WorkBridge.request(`/api/v1/employees/${employee.employeeId}/${action}`, {
        method: 'POST',
        body: { version: Number(employeeElements['employee-version'].value) }
    });
    WorkBridge.showToast(payload.message);
    employeeState.selected = payload.data;
    populateEmployeeForm(payload.data);
    await loadEmployees();
}

async function unlockEmployee() {
    ensureEmployeeWritable();
    const employee = requireSelectedEmployee();
    const payload = await WorkBridge.request(`/api/v1/employees/${employee.employeeId}/unlock`, {
        method: 'POST',
        body: { version: Number(employeeElements['employee-version'].value) }
    });
    WorkBridge.showToast(payload.message);
    employeeState.selected = payload.data;
    populateEmployeeForm(payload.data);
    await loadEmployees();
}

function updateAccountActionState(employee) {
    const locked = isEmployeeLocked(employee);
    employeeElements['employee-account-state'].textContent = !employee.active
        ? '현재 비활성 계정입니다. 신규 로그인이 차단됩니다.'
        : locked
            ? `${WorkBridge.formatDateTime(employee.lockedUntil)}까지 로그인 잠금 상태입니다.`
            : `활성 계정 · 로그인 실패 ${employee.loginFailCount}회`;
    employeeElements['employee-state-button'].textContent = employee.active ? '비활성화' : '활성화';
    employeeElements['employee-state-button'].classList.toggle('button-warning', employee.active);
    employeeElements['employee-unlock-button'].disabled = !employeeState.canWrite || (!locked && employee.loginFailCount === 0);
}

function selectedRoleIds() {
    return [...document.querySelectorAll('input[name="employee-role"]:checked')]
        .map(input => Number(input.value));
}

function setCheckedRoleIds(roleIds) {
    const selected = new Set(roleIds.map(Number));
    document.querySelectorAll('input[name="employee-role"]').forEach(input => {
        input.checked = selected.has(Number(input.value));
    });
}

function isEmployeeLocked(employee) {
    return employee.lockedUntil && new Date(employee.lockedUntil).getTime() > Date.now();
}

function ensureEmployeeWritable() {
    if (!employeeState.canWrite) {
        throw new Error('조회 권한만 있어 변경할 수 없습니다.');
    }
}

function requireSelectedEmployee() {
    if (!employeeState.selected) {
        throw new Error('직원을 먼저 선택해 주세요.');
    }
    return employeeState.selected;
}

function handleEmployeeError(error) {
    const message = WorkBridge.errorMessage(error);
    WorkBridge.setFormMessage(employeeElements['employee-form-message'], message, 'danger');
    WorkBridge.showToast(message, 'danger', error.traceId);
}
