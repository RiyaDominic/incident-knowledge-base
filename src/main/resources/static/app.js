const state = {
  tokens: {
    accessToken: localStorage.getItem('accessToken') || '',
    refreshToken: localStorage.getItem('refreshToken') || ''
  },
  selectedIncident: null,
  dashboardChart: null,
  applicationChart: null
};

const api = async (url, options = {}) => {
  const headers = new Headers(options.headers || {});
  if (!headers.has('Content-Type') && options.body && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }
  if (state.tokens.accessToken) {
    headers.set('Authorization', `Bearer ${state.tokens.accessToken}`);
  }
  const response = await fetch(url, { ...options, headers });
  if (response.status === 401 && state.tokens.refreshToken && !url.startsWith('/api/auth/')) {
    const refreshed = await refreshTokens();
    if (refreshed) return api(url, options);
  }
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Request failed: ${response.status}`);
  }
  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) return response.json();
  return response.text();
};

const showAuthStatus = (message, error = false) => {
  const el = document.getElementById('authStatus');
  el.textContent = message;
  el.classList.toggle('text-danger', error);
  el.classList.toggle('text-success', !error);
};

const persistTokens = (response) => {
  state.tokens.accessToken = response.accessToken;
  state.tokens.refreshToken = response.refreshToken;
  localStorage.setItem('accessToken', response.accessToken);
  localStorage.setItem('refreshToken', response.refreshToken);
};

const setAuthedView = (authed) => {
  document.getElementById('authSection').classList.toggle('d-none', authed);
  document.getElementById('appSection').classList.toggle('d-none', !authed);
  document.getElementById('reportIncidentBtn').classList.toggle('d-none', !authed);
  document.getElementById('refreshDashboardBtn').classList.toggle('d-none', !authed);
  document.getElementById('logoutBtn').classList.toggle('d-none', !authed);
};

const refreshTokens = async () => {
  try {
    const result = await fetch('/api/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: state.tokens.refreshToken })
    });
    if (!result.ok) return false;
    const json = await result.json();
    persistTokens(json);
    return true;
  } catch {
    return false;
  }
};

const formatDate = (value) => value ? new Date(value).toLocaleString() : '-';

const severityBadge = (value) => {
  const map = {
    LOW: 'bg-secondary',
    MEDIUM: 'bg-info text-dark',
    HIGH: 'bg-warning text-dark',
    CRITICAL: 'bg-danger'
  };
  return `<span class="badge ${map[value] || 'bg-secondary'}">${value || '-'}</span>`;
};

const statusBadge = (value) => {
  const map = {
    OPEN: 'bg-danger',
    INVESTIGATING: 'bg-warning text-dark',
    RESOLVED: 'bg-success'
  };
  return `<span class="badge ${map[value] || 'bg-secondary'}">${value || '-'}</span>`;
};

const loadDashboard = async () => {
  const dashboard = await api('/api/dashboard');
  document.getElementById('totalCount').textContent = dashboard.totalIncidents;
  document.getElementById('resolvedCount').textContent = dashboard.resolvedIncidents;
  document.getElementById('openCount').textContent = dashboard.openIncidents;
  document.getElementById('criticalCount').textContent = dashboard.criticalIncidents;
  renderCharts(dashboard);
};

const renderCharts = (dashboard) => {
  if (state.dashboardChart) state.dashboardChart.destroy();
  if (state.applicationChart) state.applicationChart.destroy();
  state.dashboardChart = new Chart(document.getElementById('severityChart'), {
    type: 'doughnut',
    data: {
      labels: dashboard.incidentsBySeverity.map(item => item.label),
      datasets: [{ data: dashboard.incidentsBySeverity.map(item => item.value), backgroundColor: ['#5f6caf', '#42d6c7', '#ffb86b', '#ff6b81'] }]
    },
    options: { plugins: { legend: { labels: { color: '#e8edf7' } } } }
  });
  state.applicationChart = new Chart(document.getElementById('applicationChart'), {
    type: 'bar',
    data: {
      labels: dashboard.applicationFailureCounts.map(item => item.label),
      datasets: [{ label: 'Incidents', data: dashboard.applicationFailureCounts.map(item => item.value), backgroundColor: '#4f8cff' }]
    },
    options: {
      plugins: { legend: { display: false } },
      scales: { x: { ticks: { color: '#93a4bf' } }, y: { ticks: { color: '#93a4bf' }, beginAtZero: true } }
    }
  });
};

const loadIncidents = async () => {
  const params = new URLSearchParams();
  const query = document.getElementById('queryInput').value.trim();
  const applicationName = document.getElementById('applicationFilter').value.trim();
  const severity = document.getElementById('severityFilter').value;
  const status = document.getElementById('statusFilter').value;
  if (query) params.set('query', query);
  if (applicationName) params.set('applicationName', applicationName);
  if (severity) params.set('severity', severity);
  if (status) params.set('status', status);
  params.set('page', '0');
  params.set('size', '20');
  const page = await api(`/api/incidents?${params.toString()}`);
  renderIncidents(page.content || []);
};

const renderIncidents = (incidents) => {
  const body = document.getElementById('incidentTableBody');
  if (!incidents.length) {
    body.innerHTML = `<tr><td colspan="7" class="text-center text-secondary py-4">No incidents found</td></tr>`;
    return;
  }
  body.innerHTML = incidents.map(item => `
    <tr data-incident-id="${item.incident?.incidentId || item.incidentId}">
      <td>${item.incident?.incidentId || item.incidentId}</td>
      <td>${item.incident?.title || item.title}</td>
      <td>${item.incident?.applicationName || item.applicationName}</td>
      <td>${severityBadge(item.incident?.severity || item.severity)}</td>
      <td>${statusBadge(item.incident?.status || item.status)}</td>
      <td>${formatDate(item.incident?.createdDate || item.createdDate)}</td>
      <td><button class="btn btn-sm btn-outline-light view-incident-btn" data-incident-id="${item.incident?.incidentId || item.incidentId}">View</button></td>
    </tr>
  `).join('');
  document.querySelectorAll('.view-incident-btn').forEach(btn => btn.addEventListener('click', () => loadIncident(btn.dataset.incidentId)));
  body.querySelectorAll('tr[data-incident-id]').forEach(row => row.addEventListener('click', () => loadIncident(row.dataset.incidentId)));
};

const loadIncident = async (incidentId) => {
  const incident = await api(`/api/incidents/${incidentId}`);
  state.selectedIncident = incident;
  document.getElementById('incidentDetail').innerHTML = `
    <div class="detail-grid">
      <div class="detail-card"><div class="detail-label">ID</div><div class="detail-value">${incident.incidentId}</div></div>
      <div class="detail-card"><div class="detail-label">Engineer</div><div class="detail-value">${incident.engineerName || incident.engineerId || '-'}</div></div>
      <div class="detail-card"><div class="detail-label">Title</div><div class="detail-value">${incident.title}</div></div>
      <div class="detail-card"><div class="detail-label">Application</div><div class="detail-value">${incident.applicationName}</div></div>
      <div class="detail-card"><div class="detail-label">Environment</div><div class="detail-value">${incident.environment}</div></div>
      <div class="detail-card"><div class="detail-label">Severity / Priority</div><div class="detail-value">${severityBadge(incident.severity)} ${statusBadge(incident.status)}</div></div>
      <div class="detail-card"><div class="detail-label">Error Message</div><div class="detail-value">${incident.errorMessage || '-'}</div></div>
      <div class="detail-card"><div class="detail-label">Root Cause</div><div class="detail-value">${incident.rootCause || '-'}</div></div>
      <div class="detail-card"><div class="detail-label">Solution</div><div class="detail-value">${incident.solution || '-'}</div></div>
      <div class="detail-card"><div class="detail-label">Tags</div><div class="detail-value">${(incident.tags || []).join(', ') || '-'}</div></div>
      <div class="detail-card"><div class="detail-label">Affected Services</div><div class="detail-value">${(incident.affectedServices || []).join(', ') || '-'}</div></div>
      <div class="detail-card"><div class="detail-label">Resolved</div><div class="detail-value">${formatDate(incident.resolvedDate)}</div></div>
    </div>
  `;
  document.getElementById('logContent').textContent = await loadText(`/api/incidents/${incidentId}/log`);
  await loadComments(incidentId);
};

const loadText = async (url) => {
  try { return await api(url); } catch { return 'No log content available.'; }
};

const loadComments = async (incidentId) => {
  const comments = await api(`/api/incidents/${incidentId}/comments`);
  document.getElementById('commentThread').innerHTML = comments.length
    ? comments.map(comment => `
        <div class="comment-item">
          <div class="comment-meta">${comment.userName} · ${formatDate(comment.timestamp)}</div>
          <div>${comment.message}</div>
        </div>
      `).join('')
    : '<div class="text-secondary">No comments yet.</div>';
};

const wireActions = () => {
  document.getElementById('loginBtn').addEventListener('click', async () => {
    try {
      const response = await api('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({
          email: document.getElementById('loginEmail').value,
          password: document.getElementById('loginPassword').value
        })
      });
      persistTokens(response);
      showAuthStatus('Login successful');
      setAuthedView(true);
      await bootstrapApp();
    } catch (error) {
      showAuthStatus(error.message, true);
    }
  });

  document.getElementById('registerBtn').addEventListener('click', async () => {
    try {
      const response = await api('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify({
          name: document.getElementById('registerName').value,
          email: document.getElementById('registerEmail').value,
          password: document.getElementById('registerPassword').value,
          role: document.getElementById('registerRole').value
        })
      });
      persistTokens(response);
      showAuthStatus('Registration successful');
      setAuthedView(true);
      await bootstrapApp();
    } catch (error) {
      showAuthStatus(error.message, true);
    }
  });

  document.getElementById('searchBtn').addEventListener('click', loadIncidents);
  document.getElementById('refreshDashboardBtn').addEventListener('click', bootstrapApp);
  document.getElementById('commentBtn').addEventListener('click', async () => {
    if (!state.selectedIncident) return;
    await api(`/api/incidents/${state.selectedIncident.incidentId}/comments`, {
      method: 'POST',
      body: JSON.stringify({ message: document.getElementById('commentMessage').value })
    });
    document.getElementById('commentMessage').value = '';
    await loadComments(state.selectedIncident.incidentId);
  });

  document.getElementById('copyIncidentJsonBtn').addEventListener('click', async () => {
    if (!state.selectedIncident) return;
    await navigator.clipboard.writeText(JSON.stringify(state.selectedIncident, null, 2));
  });

  document.getElementById('incidentForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const submitBtn = document.getElementById('modalSubmitBtn');
    const statusMsg = document.getElementById('modalStatusMsg');
    submitBtn.disabled = true;
    statusMsg.textContent = 'Submitting incident...';
    statusMsg.className = 'status-banner text-info mt-2';

    try {
      const tagsInput = document.getElementById('modalTags').value.trim();
      const servicesInput = document.getElementById('modalServices').value.trim();

      const payload = {
        title: document.getElementById('modalTitle').value.trim(),
        applicationName: document.getElementById('modalApp').value.trim(),
        environment: document.getElementById('modalEnv').value,
        severity: document.getElementById('modalSeverity').value,
        priority: document.getElementById('modalPriority').value,
        status: document.getElementById('modalStatus').value,
        errorMessage: document.getElementById('modalError').value.trim(),
        tags: tagsInput ? tagsInput.split(',').map(t => t.trim()) : [],
        affectedServices: servicesInput ? servicesInput.split(',').map(s => s.trim()) : []
      };

      const logContent = document.getElementById('modalLogs').value.trim();
      const incident = await api('/api/incidents', {
        method: 'POST',
        body: JSON.stringify(payload)
      });

      if (logContent) {
        statusMsg.textContent = 'Attaching raw logs...';
        await api(`/api/incidents/${incident.incidentId}/log`, {
          method: 'POST',
          headers: { 'Content-Type': 'text/plain' },
          body: logContent
        });
      }

      statusMsg.textContent = 'Incident reported successfully!';
      statusMsg.className = 'status-banner text-success mt-2';
      document.getElementById('incidentForm').reset();
      
      // Close Bootstrap Modal
      const modalEl = document.getElementById('incidentModal');
      const modal = bootstrap.Modal.getInstance(modalEl);
      if (modal) {
        setTimeout(() => {
          modal.hide();
          statusMsg.textContent = '';
        }, 1000);
      }

      await bootstrapApp();
    } catch (error) {
      statusMsg.textContent = 'Error: ' + error.message;
      statusMsg.className = 'status-banner text-danger mt-2';
    } finally {
      submitBtn.disabled = false;
    }
  });

  document.getElementById('logoutBtn').addEventListener('click', () => {
    state.tokens.accessToken = '';
    state.tokens.refreshToken = '';
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    state.selectedIncident = null;
    setAuthedView(false);
  });
};

const bootstrapApp = async () => {
  await loadDashboard();
  await loadIncidents();
};

const init = async () => {
  wireActions();
  if (state.tokens.accessToken) {
    setAuthedView(true);
    try {
      await bootstrapApp();
      return;
    } catch {
      if (state.tokens.refreshToken && await refreshTokens()) {
        await bootstrapApp();
        return;
      }
    }
  }
  setAuthedView(false);
};

document.addEventListener('DOMContentLoaded', init);
