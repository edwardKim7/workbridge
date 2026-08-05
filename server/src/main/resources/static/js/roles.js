'use strict';

const roleState = {
    roles: [],
    selectedRole: null,
    permissions: [],
    canWrite: document.body.dataset.canWrite === 'true'
};

const roleElements = {};

document.addEventListener('DOMContentLoaded', async () => {
    bindRoleElements();
    bindRoleEvents();
    try {
        const payload = await WorkBridge.request('/api/v1/roles');
        roleState.roles = payload.data;
        renderRoleList();
        if (roleState.roles.length) {
            await selectRole(roleState.roles[0].roleId);
        }
    } catch (error) {
        handleRoleError(error);
    }
});

function bindRoleElements() {
    [
        'role-permission-save', 'role-select-list', 'selected-role-code',
        'selected-role-name', 'selected-role-description', 'selected-role-version',
        'permission-table-body', 'role-form-message'
    ].forEach(id => roleElements[id] = document.getElementById(id));
}

function bindRoleEvents() {
    roleElements['role-select-list'].addEventListener('click', event => {
        const button = event.target.closest('button[data-role-id]');
        if (button) {
            selectRole(Number(button.dataset.roleId)).catch(handleRoleError);
        }
    });
    roleElements['permission-table-body'].addEventListener('change', event => {
        const input = event.target.closest('input[type="checkbox"][data-permission]');
        if (!input) return;
        const row = input.closest('tr[data-menu-id]');
        synchronizePermissionRow(row, input.dataset.permission);
    });
    roleElements['role-permission-save'].addEventListener('click', () => {
        saveRolePermissions().catch(handleRoleError);
    });
}

function renderRoleList() {
    if (!roleState.roles.length) {
        roleElements['role-select-list'].innerHTML = '<p class="table-empty">조회된 역할이 없습니다.</p>';
        return;
    }
    roleElements['role-select-list'].innerHTML = roleState.roles.map(role => {
        const selected = roleState.selectedRole?.roleId === role.roleId ? ' selected' : '';
        return `
            <button type="button" class="role-select-item${selected}" data-role-id="${role.roleId}">
                <span><strong>${WorkBridge.escapeHtml(role.roleName)}</strong><small>${WorkBridge.escapeHtml(role.roleCode)}</small></span>
                <em>v${role.version}</em>
            </button>`;
    }).join('');
}

async function selectRole(roleId) {
    const payload = await WorkBridge.request(`/api/v1/roles/${roleId}/menus`);
    roleState.selectedRole = payload.data.role;
    roleState.permissions = payload.data.permissions;
    renderRoleList();
    renderSelectedRole();
    renderPermissionTable();
    WorkBridge.setFormMessage(
        roleElements['role-form-message'],
        roleState.canWrite ? '' : '읽기 권한만 있어 메뉴 권한을 변경할 수 없습니다.'
    );
}

function renderSelectedRole() {
    const role = roleState.selectedRole;
    roleElements['selected-role-code'].textContent = role.roleCode;
    roleElements['selected-role-name'].textContent = role.roleName;
    roleElements['selected-role-description'].textContent = role.description || '역할 설명이 등록되어 있지 않습니다.';
    roleElements['selected-role-version'].textContent = `v${role.version}`;
}

function renderPermissionTable() {
    if (!roleState.permissions.length) {
        roleElements['permission-table-body'].innerHTML = '<tr><td colspan="4" class="table-empty">등록된 메뉴가 없습니다.</td></tr>';
        return;
    }

    roleElements['permission-table-body'].innerHTML = roleState.permissions.map(permission => {
        const isChild = permission.parentMenuId !== null;
        const protectedPermission = permission.menuCode === 'DASHBOARD'
            || (roleState.selectedRole.roleCode === 'ADMIN' && permission.menuCode === 'EMPLOYEE_ROLE');
        const disabled = (!roleState.canWrite || protectedPermission) ? ' disabled' : '';
        const readChecked = permission.canRead ? ' checked' : '';
        const writeChecked = permission.canWrite ? ' checked' : '';
        const path = permission.menuPath || '-';
        const indentation = isChild ? '<span class="tree-connector">└</span>' : '';
        const protectedLabel = protectedPermission ? '<span class="locked-label">보호</span>' : '';
        return `
            <tr data-menu-id="${permission.menuId}" data-menu-code="${WorkBridge.escapeHtml(permission.menuCode)}">
                <td><div class="menu-name-cell ${isChild ? 'child' : 'root'}">${indentation}<span><strong>${WorkBridge.escapeHtml(permission.menuName)}</strong><small>${WorkBridge.escapeHtml(permission.menuCode)}</small></span>${protectedLabel}</div></td>
                <td><code>${WorkBridge.escapeHtml(path)}</code></td>
                <td><label class="switch-check"><input type="checkbox" data-permission="read"${readChecked}${disabled}><span></span></label></td>
                <td><label class="switch-check"><input type="checkbox" data-permission="write"${writeChecked}${disabled}><span></span></label></td>
            </tr>`;
    }).join('');
}

function synchronizePermissionRow(row, changedPermission) {
    const read = row.querySelector('input[data-permission="read"]');
    const write = row.querySelector('input[data-permission="write"]');
    if (changedPermission === 'write' && write.checked) {
        read.checked = true;
    }
    if (changedPermission === 'read' && !read.checked) {
        write.checked = false;
    }
}

async function saveRolePermissions() {
    if (!roleState.canWrite) {
        throw new Error('역할·메뉴 권한 변경 권한이 없습니다.');
    }
    if (!roleState.selectedRole) {
        throw new Error('변경할 역할을 선택해 주세요.');
    }

    const permissions = Array.from(roleElements['permission-table-body'].querySelectorAll('tr[data-menu-id]'))
        .map(row => ({
            menuId: Number(row.dataset.menuId),
            canRead: row.querySelector('input[data-permission="read"]').checked,
            canWrite: row.querySelector('input[data-permission="write"]').checked
        }));

    const payload = await WorkBridge.request(`/api/v1/roles/${roleState.selectedRole.roleId}/menus`, {
        method: 'PUT',
        body: {
            permissions,
            version: roleState.selectedRole.version
        }
    });
    WorkBridge.showToast(payload.message, 'success', payload.traceId);
    roleState.selectedRole = payload.data.role;
    roleState.permissions = payload.data.permissions;
    roleState.roles = roleState.roles.map(role => role.roleId === roleState.selectedRole.roleId
        ? roleState.selectedRole
        : role);
    renderRoleList();
    renderSelectedRole();
    renderPermissionTable();
    WorkBridge.setFormMessage(roleElements['role-form-message'], '권한 변경이 저장되었습니다.', 'success');
}

function handleRoleError(error) {
    const message = WorkBridge.errorMessage(error);
    WorkBridge.setFormMessage(roleElements['role-form-message'], message, 'danger');
    WorkBridge.showToast(message, 'danger', error.traceId);
    if (error.status === 409 && roleState.selectedRole) {
        selectRole(roleState.selectedRole.roleId).catch(() => {});
    }
}
