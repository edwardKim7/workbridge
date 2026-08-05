(function (global) {
    'use strict';

    const CUSTOMER_STATUS_LABELS = {
        PROSPECT: '잠재 고객',
        ACTIVE: '거래 중',
        INACTIVE: '비활성'
    };
    const OPPORTUNITY_STAGE_LABELS = {
        LEAD: '리드',
        QUALIFIED: '유효 기회',
        PROPOSAL: '제안',
        NEGOTIATION: '협상',
        WON: '수주',
        LOST: '실주'
    };
    const OPPORTUNITY_STATUS_LABELS = {
        OPEN: '진행 중',
        CLOSED: '종결'
    };
    const ACTIVITY_TYPE_LABELS = {
        VISIT: '방문',
        CALL: '통화',
        MEETING: '회의',
        EMAIL: '이메일',
        OTHER: '기타'
    };

    function customerStatusLabel(value) {
        return CUSTOMER_STATUS_LABELS[value] || value || '-';
    }

    function opportunityStageLabel(value) {
        return OPPORTUNITY_STAGE_LABELS[value] || value || '-';
    }

    function opportunityStatusLabel(value) {
        return OPPORTUNITY_STATUS_LABELS[value] || value || '-';
    }

    function activityTypeLabel(value) {
        return ACTIVITY_TYPE_LABELS[value] || value || '-';
    }

    function statusBadgeClass(value) {
        const normalized = String(value || '').toUpperCase();
        if (['ACTIVE', 'WON', 'OPEN', 'APPROVED'].includes(normalized)) return 'status-active';
        if (['PROSPECT', 'QUALIFIED', 'PROPOSAL', 'NEGOTIATION', 'LEAD'].includes(normalized)) return 'status-warning';
        if (['INACTIVE', 'LOST', 'CLOSED', 'REJECTED'].includes(normalized)) return 'status-inactive';
        return 'status-neutral';
    }

    function formatCurrency(value) {
        const number = Number(value || 0);
        return new Intl.NumberFormat('ko-KR', {
            style: 'currency',
            currency: 'KRW',
            maximumFractionDigits: 0
        }).format(Number.isFinite(number) ? number : 0);
    }

    function formatNumber(value) {
        const number = Number(value || 0);
        return new Intl.NumberFormat('ko-KR').format(Number.isFinite(number) ? number : 0);
    }

    function formatBusinessRegistrationNo(value) {
        const digits = String(value || '').replace(/[^0-9]/g, '');
        if (digits.length !== 10) return value || '-';
        return `${digits.slice(0, 3)}-${digits.slice(3, 5)}-${digits.slice(5)}`;
    }

    function formatDate(value) {
        if (!value) return '-';
        const date = new Date(`${value}T00:00:00`);
        if (Number.isNaN(date.getTime())) return value;
        return new Intl.DateTimeFormat('ko-KR', {
            year: 'numeric', month: '2-digit', day: '2-digit'
        }).format(date);
    }

    function toDateTimeLocal(value) {
        if (!value) return '';
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return '';
        const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
        return local.toISOString().slice(0, 16);
    }

    function currentDateTimeLocal() {
        return toDateTimeLocal(new Date().toISOString());
    }

    function findMenu(menus, menuCode) {
        for (const menu of menus || []) {
            if (menu.menuCode === menuCode) return menu;
            const child = findMenu(menu.children || [], menuCode);
            if (child) return child;
        }
        return null;
    }

    function hasRole(currentUser, roleCode) {
        return (currentUser?.roles || []).some(role => role.roleCode === roleCode);
    }

    function populateSelect(select, items, options = {}) {
        if (!select) return;
        const {
            placeholder = '선택해 주세요.',
            valueKey = 'employeeId',
            label = item => item.employeeName,
            selectedValue = null
        } = options;
        select.innerHTML = '';
        const emptyOption = document.createElement('option');
        emptyOption.value = '';
        emptyOption.textContent = placeholder;
        select.appendChild(emptyOption);
        for (const item of items || []) {
            const option = document.createElement('option');
            option.value = String(item[valueKey]);
            option.textContent = label(item);
            if (selectedValue !== null && String(selectedValue) === option.value) {
                option.selected = true;
            }
            select.appendChild(option);
        }
    }

    function setFormEnabled(form, enabled) {
        if (!form) return;
        form.querySelectorAll('input, select, textarea, button').forEach(element => {
            if (!element.dataset.keepEnabled) {
                element.disabled = !enabled;
            }
        });
        form.classList.toggle('read-only-form', !enabled);
    }

    function opportunityProbabilityHint(stage) {
        if (stage === 'WON') return '수주 단계는 100%만 허용됩니다.';
        if (stage === 'LOST') return '실주 단계는 0%만 허용됩니다.';
        return '진행 단계는 0~99% 범위입니다.';
    }

    global.WorkBridgeSales = {
        CUSTOMER_STATUS_LABELS,
        OPPORTUNITY_STAGE_LABELS,
        OPPORTUNITY_STATUS_LABELS,
        ACTIVITY_TYPE_LABELS,
        customerStatusLabel,
        opportunityStageLabel,
        opportunityStatusLabel,
        activityTypeLabel,
        statusBadgeClass,
        formatCurrency,
        formatNumber,
        formatBusinessRegistrationNo,
        formatDate,
        toDateTimeLocal,
        currentDateTimeLocal,
        findMenu,
        hasRole,
        populateSelect,
        setFormEnabled,
        opportunityProbabilityHint
    };
}(window));
