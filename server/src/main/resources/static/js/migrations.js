'use strict';

const migrationState = {
  page: 0,
  size: 20,
};

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('migration-form').addEventListener('submit', uploadMigrationFile);
  loadMigrations();
});

async function uploadMigrationFile(event) {
  event.preventDefault();

  const file = document.getElementById('migration-file').files[0];
  if (!file) {
    return;
  }

  const submitButton = document.getElementById('migration-submit');
  submitButton.disabled = true;

  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await WorkBridge.request('/api/v1/migrations/customers', {
      method: 'POST',
      body: formData,
    });

    WorkBridge.showToast(response.message);
    renderMigrationErrors(response.data);
    await loadMigrations();
  } catch (error) {
    WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
  } finally {
    submitButton.disabled = false;
  }
}

async function loadMigrations() {
  try {
    const query = WorkBridge.buildQuery({
      page: migrationState.page,
      size: migrationState.size,
    });
    const response = await WorkBridge.request(`/api/v1/migrations${query}`);
    const page = response.data;

    document.getElementById('migration-count').textContent = `총 ${page.totalElements}건`;
    document.getElementById('migration-body').innerHTML = page.items.length
      ? page.items.map(renderMigrationRow).join('')
      : '<tr><td colspan="8" class="table-empty">이관 이력이 없습니다.</td></tr>';

    document.querySelectorAll('[data-batch]').forEach((button) => {
      button.addEventListener('click', () => loadMigrationDetail(button.dataset.batch));
    });

    WorkBridge.renderPagination(
      document.getElementById('migration-pagination'),
      page.page,
      page.totalPages,
      (nextPage) => {
        migrationState.page = nextPage;
        loadMigrations();
      },
    );
  } catch (error) {
    WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
  }
}

function renderMigrationRow(migration) {
  const statusClass = migration.status === 'COMPLETED'
    ? 'status-success'
    : migration.status === 'FAILED'
      ? 'status-danger'
      : 'status-warning';

  return `
    <tr>
      <td>
        <span class="table-primary">${migration.batchNo}</span>
        <small>${WorkBridge.formatDateTime(migration.startedAt)}</small>
      </td>
      <td>${WorkBridge.escapeHtml(migration.originalFileName)}</td>
      <td><span class="status-badge ${statusClass}">${migration.status}</span></td>
      <td>${migration.totalCount}</td>
      <td>${migration.successCount}</td>
      <td>${migration.failureCount}</td>
      <td>${WorkBridge.escapeHtml(migration.executorName)}</td>
      <td>
        <button class="button button-secondary-light" data-batch="${migration.batchId}">
          보기
        </button>
      </td>
    </tr>
  `;
}

async function loadMigrationDetail(batchId) {
  try {
    const response = await WorkBridge.request(`/api/v1/migrations/${batchId}`);
    renderMigrationErrors(response.data);
  } catch (error) {
    WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
  }
}

function renderMigrationErrors(detail) {
  const panel = document.getElementById('migration-errors-panel');
  panel.classList.remove('hidden');

  document.getElementById('migration-error-download').href =
    `${WorkBridge.contextPath}/api/v1/migrations/${detail.batch.batchId}/errors.csv`;
  document.getElementById('migration-errors').innerHTML = detail.errors.length
    ? detail.errors.map(renderMigrationErrorRow).join('')
    : '<tr><td colspan="5" class="table-empty">오류가 없습니다.</td></tr>';

  panel.scrollIntoView({ behavior: 'smooth' });
}

function renderMigrationErrorRow(error) {
  return `
    <tr>
      <td>${error.rowNo}</td>
      <td>${WorkBridge.escapeHtml(error.fieldName || '-')}</td>
      <td>${WorkBridge.escapeHtml(error.errorCode)}</td>
      <td>${WorkBridge.escapeHtml(error.errorMessage)}</td>
      <td>${WorkBridge.escapeHtml(error.rawValue || '-')}</td>
    </tr>
  `;
}
