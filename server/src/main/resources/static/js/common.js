(function (global) {
    'use strict';

    const body = document.body;
    const contextPath = body ? (body.dataset.contextPath || '') : '';
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';

    async function request(path, options = {}) {
        const method = (options.method || 'GET').toUpperCase();
        const headers = new Headers(options.headers || {});
        headers.set('Accept', 'application/json');

        let bodyValue = options.body;
        if (bodyValue !== undefined && bodyValue !== null && !(bodyValue instanceof FormData)) {
            headers.set('Content-Type', 'application/json');
            bodyValue = JSON.stringify(bodyValue);
        }
        if (!['GET', 'HEAD', 'OPTIONS'].includes(method) && csrfToken) {
            headers.set(csrfHeader, csrfToken);
        }

        const response = await fetch(contextPath + path, {
            method,
            headers,
            body: bodyValue,
            credentials: 'same-origin'
        });

        const text = await response.text();
        let payload = null;
        if (text) {
            try {
                payload = JSON.parse(text);
            } catch (error) {
                payload = null;
            }
        }

        if (!response.ok) {
            if (response.status === 401) {
                global.location.href = contextPath + '/login';
            }
            const apiError = new Error(payload?.message || `요청 처리에 실패했습니다. (${response.status})`);
            apiError.status = response.status;
            apiError.code = payload?.code || 'HTTP_ERROR';
            apiError.fieldErrors = payload?.fieldErrors || [];
            apiError.traceId = payload?.traceId || response.headers.get('X-Trace-Id');
            throw apiError;
        }
        return payload;
    }

    function escapeHtml(value) {
        return String(value ?? '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#039;');
    }

    function formatDateTime(value) {
        if (!value) {
            return '-';
        }
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return value;
        }
        return new Intl.DateTimeFormat('ko-KR', {
            year: 'numeric', month: '2-digit', day: '2-digit',
            hour: '2-digit', minute: '2-digit', hour12: false
        }).format(date);
    }

    function formatDate(value) {
        if (!value) {
            return '-';
        }
        const date = new Date(`${value}T00:00:00`);
        if (Number.isNaN(date.getTime())) {
            return value;
        }
        return new Intl.DateTimeFormat('ko-KR', {
            year: 'numeric', month: '2-digit', day: '2-digit'
        }).format(date);
    }

    function formatCurrency(value) {
        const amount = Number(value ?? 0);
        if (!Number.isFinite(amount)) {
            return '-';
        }
        return new Intl.NumberFormat('ko-KR', {
            style: 'currency', currency: 'KRW', maximumFractionDigits: 0
        }).format(amount);
    }

    function toLocalDateTimeInput(value = new Date()) {
        const date = value instanceof Date ? value : new Date(value);
        if (Number.isNaN(date.getTime())) {
            return '';
        }
        const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
        return local.toISOString().slice(0, 16);
    }

    function buildQuery(parameters) {
        const query = new URLSearchParams();
        Object.entries(parameters).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                query.set(key, String(value));
            }
        });
        const text = query.toString();
        return text ? `?${text}` : '';
    }

    function showToast(message, type = 'success', traceId = '') {
        const region = document.getElementById('toast-region');
        if (!region) {
            return;
        }
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        const trace = traceId ? `<small>Trace ID: ${escapeHtml(traceId)}</small>` : '';
        toast.innerHTML = `<strong>${escapeHtml(message)}</strong>${trace}`;
        region.appendChild(toast);
        global.setTimeout(() => toast.classList.add('show'), 10);
        global.setTimeout(() => {
            toast.classList.remove('show');
            global.setTimeout(() => toast.remove(), 180);
        }, 4200);
    }

    function errorMessage(error) {
        if (error?.fieldErrors?.length) {
            return error.fieldErrors.map(item => `${item.field}: ${item.message}`).join(' / ');
        }
        return error?.message || '요청 처리 중 오류가 발생했습니다.';
    }

    function setFormMessage(element, message, type = '') {
        if (!element) {
            return;
        }
        element.textContent = message || '';
        element.className = `form-message ${type ? `form-message-${type}` : ''}`;
    }

    function renderPagination(container, page, totalPages, onMove) {
        container.innerHTML = '';
        if (totalPages <= 1) {
            return;
        }
        const createButton = (label, targetPage, disabled, active = false) => {
            const button = document.createElement('button');
            button.type = 'button';
            button.textContent = label;
            button.disabled = disabled;
            button.className = `page-button${active ? ' active' : ''}`;
            button.addEventListener('click', () => onMove(targetPage));
            return button;
        };
        container.appendChild(createButton('이전', Math.max(0, page - 1), page === 0));
        const start = Math.max(0, page - 2);
        const end = Math.min(totalPages - 1, start + 4);
        for (let index = start; index <= end; index += 1) {
            container.appendChild(createButton(String(index + 1), index, false, index === page));
        }
        container.appendChild(createButton('다음', Math.min(totalPages - 1, page + 1), page >= totalPages - 1));
    }

    function booleanParam(value) {
        if (value === 'true') return true;
        if (value === 'false') return false;
        return null;
    }

    global.WorkBridge = {
        contextPath,
        request,
        escapeHtml,
        formatDate,
        formatDateTime,
        formatCurrency,
        toLocalDateTimeInput,
        buildQuery,
        showToast,
        errorMessage,
        setFormMessage,
        renderPagination,
        booleanParam
    };
}(window));
