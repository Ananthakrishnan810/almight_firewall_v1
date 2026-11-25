/**
 * NetGuard WAF & SOC v1.0 - Frontend Controller
 */

const DEFAULT_SETTINGS = {
  threatUrl: 'http://localhost:5000',
  cassandraProxy: '/cassandra-proxy',
  pollSec: 15,
  kafkaBroker: 'localhost:9092',
  retention: '7d',
  defaultPolicy: 'ALLOW',
  webhook: ''
};

function loadSettings() {
  try {
    const saved = localStorage.getItem('netguard_settings');
    return saved ? { ...DEFAULT_SETTINGS, ...JSON.parse(saved) } : { ...DEFAULT_SETTINGS };
  } catch {
    return { ...DEFAULT_SETTINGS };
  }
}

const state = {
  settings: loadSettings(),
  activeTab: 'tab-overview',
  timeframe: '24h',
  quickSearch: '',
  eventsPage: 1,
  eventsPageSize: 20,
  logs: [],
  rules: [],
  threatRules: [],
  actuatorMetrics: [],
  httpCount: 0,
  services: {
    waf: { up: true, ms: 0 },
    threat: { up: false, ms: 0 },
    cassandra: { up: false, ms: 0 }
  },
  currentUser: {
    authenticated: false,
    username: 'viewer',
    fullName: 'Demo Viewer',
    role: 'VIEWER',
    isAdmin: false
  },
  pollTimer: null
};

document.addEventListener('DOMContentLoaded', () => {
  initAuth();
  initNavigation();
  initTopbarControls();
  initRules();
  initThreatEngine();
  initSimulator();
  initEventsTab();
  initTelemetryTab();
  initAuditTab();
  initSyslogTab();
  initSettingsTab();
  startUtcClock();

  if (sessionStorage.getItem('netguard_auth') === '1') {
    fetch('/auth/me', { credentials: 'same-origin' })
      .then(r => r.ok ? r.json() : null)
      .then(user => {
        if (user && user.username) {
          state.currentUser = user;
        }
        enterDashboard(true);
      })
      .catch(() => enterDashboard(true));
  }

  window.addEventListener('resize', () => {
    if (state.activeTab === 'tab-overview') renderOverviewCharts();
    if (state.activeTab === 'tab-telemetry') renderTelemetryChart();
  });
});

/* ========================================================================= */
/* 1. AUTHENTICATION                                                         */
/* ========================================================================= */
function initAuth() {
  const loginForm = document.getElementById('loginForm');
  const btnQuickDemo = document.getElementById('btnQuickDemo');
  const btnSignOut = document.getElementById('btnSignOut');
  const loginErr = document.getElementById('loginError');

  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (loginErr) loginErr.style.display = 'none';
      const username = document.getElementById('username').value.trim();
      const password = document.getElementById('password').value;

      try {
        const res = await fetch('/auth/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'same-origin',
          body: JSON.stringify({ username, password })
        });
        const data = await res.json();
        if (!res.ok) {
          if (loginErr) {
            loginErr.textContent = data.error || 'Invalid credentials';
            loginErr.style.display = 'block';
          }
          return;
        }
        state.currentUser = data;
        enterDashboard(false);
      } catch (err) {
        if (loginErr) {
          loginErr.textContent = `Login failed: ${err.message}`;
          loginErr.style.display = 'block';
        }
      }
    });
  }
  if (btnQuickDemo) {
    btnQuickDemo.addEventListener('click', async () => {
      await fetch('/auth/logout', { method: 'POST', credentials: 'same-origin' }).catch(() => {});
      state.currentUser = {
        authenticated: false,
        username: 'viewer',
        fullName: 'Demo Viewer',
        role: 'VIEWER',
        isAdmin: false
      };
      enterDashboard(false);
    });
  }
  if (btnSignOut) {
    btnSignOut.addEventListener('click', async () => {
      await fetch('/auth/logout', { method: 'POST', credentials: 'same-origin' }).catch(() => {});
      sessionStorage.removeItem('netguard_auth');
      state.currentUser = {
        authenticated: false,
        username: 'viewer',
        fullName: 'Demo Viewer',
        role: 'VIEWER',
        isAdmin: false
      };
      if (state.pollTimer) clearInterval(state.pollTimer);
      document.getElementById('appDashboard').style.display = 'none';
      document.getElementById('loginSection').style.display = 'flex';
      showToast('Signed out of NetGuard console', 'info');
    });
  }
}

function enterDashboard(silent) {
  sessionStorage.setItem('netguard_auth', '1');
  document.getElementById('loginSection').style.display = 'none';
  document.getElementById('appDashboard').style.display = 'flex';
  applyUserRoleUI();
  if (!silent) {
    const label = state.currentUser.fullName || state.currentUser.username;
    showToast(`Signed in as ${label} (${state.currentUser.role})`, 'success');
  }
  refreshAllData();
  restartPolling();
}

function applyUserRoleUI() {
  const user = state.currentUser || {};
  const displayName = user.fullName || user.username || 'Demo Viewer';
  const roleLabel = user.role || 'VIEWER';
  const initials = displayName.split(/\s+/).map(p => p[0]).join('').slice(0, 2).toUpperCase() || 'NG';

  setText('sidebarAvatar', initials);
  setText('sidebarUserName', displayName);
  setText('sidebarUserRole', roleLabel);

  const lockBox = document.getElementById('createUserAdminLock');
  const adminBox = document.getElementById('createUserAdminContent');
  if (lockBox && adminBox) {
    const isAdmin = Boolean(user.isAdmin);
    lockBox.style.display = isAdmin ? 'none' : 'block';
    adminBox.style.display = isAdmin ? 'block' : 'none';
  }
}

function restartPolling() {
  if (state.pollTimer) clearInterval(state.pollTimer);
  const ms = Math.max(5, Number(state.settings.pollSec) || 15) * 1000;
  state.pollTimer = setInterval(() => {
    checkServiceHealth();
    loadCassandraLogs();
    loadActuatorTelemetry();
  }, ms);
}

/* ========================================================================= */
/* 2. NAVIGATION & TOPBAR                                                    */
/* ========================================================================= */
function initNavigation() {
  document.querySelectorAll('.sidebar-nav .nav-item').forEach(item => {
    item.addEventListener('click', () => {
      switchTab(item.getAttribute('data-tab'));
    });
  });
}

function switchTab(tabId) {
  state.activeTab = tabId;
  document.querySelectorAll('.sidebar-nav .nav-item').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('data-tab') === tabId);
  });
  document.querySelectorAll('.tab-panel').forEach(panel => {
    panel.classList.toggle('active', panel.id === tabId);
  });

  if (tabId === 'tab-overview') renderOverview();
  if (tabId === 'tab-events') renderEventsTable();
  if (tabId === 'tab-telemetry') {
    loadActuatorTelemetry().then(renderTelemetryView);
  }
  if (tabId === 'tab-rules') loadRules();
  if (tabId === 'tab-threats') loadThreatRules();
  if (tabId === 'tab-logs') renderAuditLogs();
  if (tabId === 'tab-syslog') renderRawSyslog();
}

function initTopbarControls() {
  const btnRefresh = document.getElementById('btnRefreshAll');
  if (btnRefresh) {
    btnRefresh.addEventListener('click', () => {
      refreshAllData();
      showToast('Telemetry refreshed', 'info');
    });
  }

  document.querySelectorAll('#timeframeGroup .tf-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('#timeframeGroup .tf-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      state.timeframe = btn.getAttribute('data-tf');
      state.eventsPage = 1;
      renderAllDataViews();
    });
  });

  const qSearch = document.getElementById('globalQuickSearch');
  if (qSearch) {
    qSearch.addEventListener('input', () => {
      state.quickSearch = qSearch.value.trim().toLowerCase();
      state.eventsPage = 1;
      renderAllDataViews();
    });
    qSearch.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' && state.activeTab === 'tab-overview' && state.quickSearch) {
        switchTab('tab-events');
      }
    });
  }

  document.addEventListener('keydown', (e) => {
    if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
      e.preventDefault();
      qSearch?.focus();
    }
  });
}

function startUtcClock() {
  const el = document.getElementById('utcClock');
  const tick = () => {
    if (!el) return;
    const now = new Date();
    const hh = String(now.getUTCHours()).padStart(2, '0');
    const mm = String(now.getUTCMinutes()).padStart(2, '0');
    const ss = String(now.getUTCSeconds()).padStart(2, '0');
    el.textContent = `${hh}:${mm}:${ss} UTC`;
  };
  tick();
  setInterval(tick, 1000);
}

function refreshAllData() {
  checkServiceHealth();
  loadCassandraLogs();
  loadRules();
  loadThreatRules();
  loadActuatorTelemetry();
}

/* ========================================================================= */
/* 3. HEALTH, ACTUATOR & TICKER                                              */
/* ========================================================================= */
async function probeEndpoint(url, options) {
  const t0 = performance.now();
  try {
    const res = await fetch(url, options);
    const ms = Math.max(1, Math.round(performance.now() - t0));
    return { ok: res.ok, ms, res };
  } catch {
    return { ok: false, ms: 0, res: null };
  }
}

async function checkServiceHealth() {
  const waf = await probeEndpoint('/actuator/health');
  state.services.waf = { up: waf.ok, ms: waf.ms };

  const threat = await probeEndpoint(`${state.settings.threatUrl}/health`);
  state.services.threat = { up: threat.ok, ms: threat.ms };

  const cass = await probeEndpoint(`${state.settings.cassandraProxy}/health`);
  let cassUp = cass.ok;
  if (cass.res && cass.ok) {
    const txt = await cass.res.text();
    cassUp = !txt.includes('UNAVAILABLE');
  }
  state.services.cassandra = { up: cassUp, ms: cass.ms };

  updateMeshRow('meshWaf', 'latWaf', state.services.waf);
  updateMeshRow('meshThreat', 'latThreat', state.services.threat);
  updateMeshRow('meshCassandra', 'latCassandra', state.services.cassandra);

  const tickerWaf = document.getElementById('tickerWaf');
  if (tickerWaf) {
    tickerWaf.textContent = state.services.waf.up ? `ONLINE (${state.services.waf.ms}ms)` : 'OFFLINE';
  }
}

function updateMeshRow(badgeId, latId, info) {
  const b = document.getElementById(badgeId);
  const l = document.getElementById(latId);
  if (b) {
    b.className = info.up ? 'badge-online' : 'badge-offline';
    b.textContent = info.up ? '• ONLINE' : '• OFFLINE';
  }
  if (l) {
    l.textContent = info.up ? `${info.ms}ms` : '--ms';
  }
}

async function loadActuatorTelemetry() {
  const targets = [
    { name: 'http.server.requests', category: 'HTTP Server', component: 'Tomcat :7080', label: 'HTTP Requests Handled' },
    { name: 'jvm.memory.used', category: 'JVM Memory', component: 'JVM Heap', label: 'JVM Memory Used' },
    { name: 'process.cpu.usage', category: 'WAF Engine', component: 'almight_firewall_v1', label: 'Process CPU Usage' },
    { name: 'hikaricp.connections.active', category: 'Database Pool', component: 'MySQL HikariCP', label: 'Active DB Connections' },
    { name: 'disk.free', category: 'Microservice', component: 'Host Storage', label: 'Free Disk Space' }
  ];

  const collected = [];
  const nowStr = formatTimeUtc(Date.now());

  await Promise.all(targets.map(async (t) => {
    try {
      const res = await fetch(`/actuator/metrics/${t.name}`);
      if (!res.ok) return;
      const data = await res.json();
      const m = data.measurements && data.measurements[0];
      if (!m) return;
      let formatted = String(m.value);
      if (t.name === 'http.server.requests') {
        state.httpCount = Math.round(m.value);
        formatted = `${Math.round(m.value)} requests`;
      } else if (t.name === 'jvm.memory.used' || t.name === 'disk.free') {
        const mb = m.value / (1024 * 1024);
        formatted = mb > 1024 ? `${(mb / 1024).toFixed(2)} GB` : `${mb.toFixed(1)} MB`;
      } else if (t.name === 'process.cpu.usage') {
        formatted = `${(m.value * 100).toFixed(2)}%`;
      }
      collected.push({
        time: nowStr,
        category: t.category,
        component: t.component,
        detail: t.label,
        value: formatted
      });
    } catch {
      // Ignore individual metric fetch errors
    }
  }));

  state.actuatorMetrics = collected;
  const kpiInternal = document.getElementById('kpiInternal');
  if (kpiInternal) kpiInternal.textContent = formatNumber(state.httpCount || state.logs.length);
  if (state.activeTab === 'tab-telemetry') renderTelemetryView();
}

/* ========================================================================= */
/* 4. LOG DATA & TIMEFRAME FILTERING                                         */
/* ========================================================================= */
function normalizeMs(ts) {
  const n = Number(ts || 0);
  if (!n) return 0;
  return n < 1e12 ? n * 1000 : n;
}

function getWindowMs(tf) {
  const map = {
    '15m': 15 * 60 * 1000,
    '1h': 60 * 60 * 1000,
    '24h': 24 * 60 * 60 * 1000,
    '7d': 7 * 24 * 60 * 60 * 1000,
    '30d': 30 * 24 * 60 * 60 * 1000
  };
  return map[tf] || map['24h'];
}

function getWindowedLogs() {
  const now = Date.now();
  const cutoff = now - getWindowMs(state.timeframe);
  return state.logs.filter(l => {
    const ms = normalizeMs(l.actionTime);
    return !ms || ms >= cutoff;
  });
}

async function loadCassandraLogs() {
  try {
    let res = await fetch(`${state.settings.cassandraProxy}/getAllLogs`);
    if (!res.ok) {
      res = await fetch('http://localhost:7081/cassandra-log/getAllLogs');
    }
    const rawText = await res.text();
    const logs = rawText ? JSON.parse(rawText) : [];
    state.logs = (Array.isArray(logs) ? logs : []).sort(
      (a, b) => normalizeMs(b.actionTime) - normalizeMs(a.actionTime)
    );

    const tickerDb = document.getElementById('tickerDb');
    if (tickerDb) tickerDb.textContent = `${formatNumber(state.logs.length)} idx`;
    const tickerRet = document.getElementById('tickerRetention');
    if (tickerRet) tickerRet.textContent = state.settings.retention || '7d';

    renderAllDataViews();
  } catch (e) {
    console.warn('Error loading logs:', e.message);
  }
}

function renderAllDataViews() {
  renderOverview();
  if (state.activeTab === 'tab-events') renderEventsTable();
  if (state.activeTab === 'tab-telemetry') renderTelemetryView();
  if (state.activeTab === 'tab-rules') renderRulesTable();
  if (state.activeTab === 'tab-logs') renderAuditLogs();
  if (state.activeTab === 'tab-syslog') renderRawSyslog();
}

/* ========================================================================= */
/* 5. OVERVIEW KPIs, CHARTS & RECENT STREAM                                  */
/* ========================================================================= */
function renderOverview() {
  const logs = getWindowedLogs();
  const total = logs.length;
  let allowed = 0;
  let blocked = 0;
  const clients = new Set();
  const threatIps = new Set();

  logs.forEach(l => {
    if (l.ip) clients.add(l.ip);
    const isBlock = (l.action || '').toUpperCase() === 'BLOCK';
    if (isBlock) {
      blocked++;
      if (l.ip) threatIps.add(l.ip);
    } else {
      allowed++;
    }
  });

  const allowPct = total ? ((allowed / total) * 100).toFixed(1) : '0.0';
  const blockPct = total ? ((blocked / total) * 100).toFixed(1) : '0.0';

  setText('overviewSubtitle', `Firewall & security analytics for the last ${state.timeframe} window`);
  setText('kpiClients', formatNumber(clients.size));
  setText('kpiTotalEvents', formatNumber(total));
  setText('kpiAllowed', formatNumber(allowed));
  setText('kpiAllowedPct', `${allowPct}% accept/allow`);
  setText('kpiBlocked', formatNumber(blocked));
  setText('kpiBlockedPct', `${blockPct}% block/deny`);
  setText('kpiInternal', formatNumber(state.httpCount || total));
  setText('kpiThreatIps', formatNumber(threatIps.size));

  // Threat level indicator & syslog msg/s ticker
  const badge = document.getElementById('threatLevelBadge');
  const txt = document.getElementById('threatLevelText');
  if (badge && txt) {
    const elevated = blocked > 0;
    badge.className = `threat-pill ${elevated ? 'elevated' : 'normal'}`;
    badge.querySelector('.dot-sm').className = `dot-sm ${elevated ? 'red' : 'green'}`;
    txt.textContent = elevated ? 'THREAT LEVEL ELEVATED' : 'THREAT LEVEL NORMAL';
  }
  const winSec = getWindowMs(state.timeframe) / 1000;
  const rate = total > 0 ? Math.max(0.1, (total / Math.min(winSec, 3600)) * 60).toFixed(1) : '0.0';
  setText('tickerRate', `${rate} msg/min`);

  renderOverviewCharts(logs);
  renderRecentLogs(logs);
}

function buildTimeBuckets(logs, bucketCount = 24) {
  const now = Date.now();
  const span = getWindowMs(state.timeframe);
  const start = now - span;
  const step = span / bucketCount;

  const buckets = Array.from({ length: bucketCount }, (_, i) => {
    const t = new Date(start + i * step);
    const label = state.timeframe === '7d' || state.timeframe === '30d'
      ? `${t.getMonth() + 1}/${t.getDate()}`
      : `${String(t.getHours()).padStart(2, '0')}:${String(t.getMinutes()).padStart(2, '0')}`;
    return { label, allow: 0, block: 0, sqli: 0, xss: 0, path: 0, clean: 0 };
  });

  logs.forEach(l => {
    const ms = normalizeMs(l.actionTime);
    if (!ms || ms < start) return;
    const idx = Math.min(bucketCount - 1, Math.max(0, Math.floor((ms - start) / step)));
    const b = buckets[idx];
    const isBlock = (l.action || '').toUpperCase() === 'BLOCK';
    if (isBlock) b.block++; else b.allow++;

    const th = (l.threat_type || 'NONE').toUpperCase();
    if (th.includes('SQL')) b.sqli++;
    else if (th.includes('XSS')) b.xss++;
    else if (th.includes('PATH')) b.path++;
    else b.clean++;
  });

  return buckets;
}

function renderOverviewCharts(logs = getWindowedLogs()) {
  const buckets = buildTimeBuckets(logs, 28);
  const bucketMins = Math.max(1, Math.round((getWindowMs(state.timeframe) / 60000) / 28));
  const bucketText = bucketMins >= 60 ? `per ${Math.round(bucketMins / 60)}h` : `per ${bucketMins}m`;
  setText('chartEventsBucketLabel', bucketText);
  setText('chartThreatBucketLabel', bucketText);

  drawStackedBarChart('chartFirewallEvents', buckets, [
    { key: 'allow', color: '#10b981' },
    { key: 'block', color: '#ef4444' }
  ]);

  drawStackedBarChart('chartThreatSeries', buckets, [
    { key: 'clean', color: '#38bdf8' },
    { key: 'path', color: '#8b5cf6' },
    { key: 'xss', color: '#f59e0b' },
    { key: 'sqli', color: '#ef4444' }
  ]);

  // Donuts
  const blockCount = logs.filter(l => (l.action || '').toUpperCase() === 'BLOCK').length;
  const allowCount = logs.length - blockCount;
  drawDonut('donutActions', 'legendActions', 'donutActionsTotal', [
    { label: 'BLOCK', value: blockCount, color: '#ef4444' },
    { label: 'ALLOW', value: allowCount, color: '#10b981' }
  ]);

  const methodsMap = {};
  logs.forEach(l => {
    const m = (l.method || 'GET').toUpperCase();
    methodsMap[m] = (methodsMap[m] || 0) + 1;
  });
  const methodColors = { GET: '#38bdf8', POST: '#10b981', PUT: '#f59e0b', DELETE: '#ef4444', OTHER: '#8b5cf6' };
  const methodItems = Object.entries(methodsMap)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 5)
    .map(([k, v]) => ({ label: k, value: v, color: methodColors[k] || '#8b5cf6' }));
  drawDonut('donutMethods', 'legendMethods', 'donutMethodsTotal', methodItems);

  const threatMap = {};
  logs.forEach(l => {
    const t = l.threat_type || 'NONE';
    threatMap[t] = (threatMap[t] || 0) + 1;
  });
  const threatColors = {
    SQL_INJECTION: '#ef4444',
    XSS: '#f59e0b',
    PATH_TRAVERSAL: '#8b5cf6',
    IP_BLACKLISTED: '#f97316',
    NONE: '#10b981'
  };
  const threatItems = Object.entries(threatMap)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 5)
    .map(([k, v]) => ({ label: k === 'NONE' ? 'Clean' : k, value: v, color: threatColors[k] || '#38bdf8' }));
  drawDonut('donutThreats', 'legendThreats', 'donutThreatsTotal', threatItems);
}

function drawStackedBarChart(canvasId, buckets, series) {
  const canvas = document.getElementById(canvasId);
  if (!canvas || !canvas.parentElement) return;
  const rect = canvas.parentElement.getBoundingClientRect();
  if (!rect.width || !rect.height) return;

  const dpr = window.devicePixelRatio || 1;
  canvas.width = rect.width * dpr;
  canvas.height = rect.height * dpr;
  const ctx = canvas.getContext('2d');
  ctx.scale(dpr, dpr);

  const w = rect.width;
  const h = rect.height;
  const padL = 32;
  const padR = 10;
  const padT = 8;
  const padB = 22;
  const plotW = w - padL - padR;
  const plotH = h - padT - padB;

  ctx.clearRect(0, 0, w, h);

  let maxVal = 4;
  buckets.forEach(b => {
    const sum = series.reduce((acc, s) => acc + (b[s.key] || 0), 0);
    if (sum > maxVal) maxVal = sum;
  });
  maxVal = Math.ceil(maxVal / 4) * 4;

  // Horizontal grid + Y labels
  ctx.font = '10px "JetBrains Mono", monospace';
  ctx.fillStyle = '#55657e';
  ctx.strokeStyle = '#182336';
  ctx.lineWidth = 1;
  for (let i = 0; i <= 4; i++) {
    const y = padT + plotH - (plotH * i) / 4;
    ctx.beginPath();
    ctx.moveTo(padL, y);
    ctx.lineTo(w - padR, y);
    ctx.stroke();
    ctx.textAlign = 'right';
    ctx.fillText(String(Math.round((maxVal * i) / 4)), padL - 6, y + 3);
  }

  // Bars
  const slotW = plotW / buckets.length;
  const barW = Math.max(3, Math.floor(slotW * 0.65));

  buckets.forEach((b, i) => {
    const x = padL + i * slotW + (slotW - barW) / 2;
    let curY = padT + plotH;
    series.forEach(s => {
      const val = b[s.key] || 0;
      if (!val) return;
      const bh = (val / maxVal) * plotH;
      curY -= bh;
      ctx.fillStyle = s.color;
      ctx.fillRect(x, curY, barW, bh);
    });

    if (i % 4 === 0 || i === buckets.length - 1) {
      ctx.fillStyle = '#55657e';
      ctx.textAlign = 'center';
      ctx.fillText(b.label, x + barW / 2, h - 6);
    }
  });
}

function drawDonut(canvasId, legendId, totalId, items) {
  const canvas = document.getElementById(canvasId);
  const legend = document.getElementById(legendId);
  const totalEl = document.getElementById(totalId);
  if (!canvas || !legend || !totalEl) return;

  const total = items.reduce((a, b) => a + b.value, 0);
  totalEl.textContent = formatNumber(total);

  const size = 118;
  const dpr = window.devicePixelRatio || 1;
  canvas.width = size * dpr;
  canvas.height = size * dpr;
  const ctx = canvas.getContext('2d');
  ctx.scale(dpr, dpr);
  ctx.clearRect(0, 0, size, size);

  const cx = size / 2;
  const cy = size / 2;
  const radius = 46;
  const lineWidth = 14;

  if (total === 0) {
    ctx.beginPath();
    ctx.arc(cx, cy, radius, 0, Math.PI * 2);
    ctx.strokeStyle = '#182336';
    ctx.lineWidth = lineWidth;
    ctx.stroke();
    legend.innerHTML = `<div class="dleg-item"><span class="dleg-left">No events in window</span></div>`;
    return;
  }

  let angle = -Math.PI / 2;
  items.forEach(it => {
    if (!it.value) return;
    const slice = (it.value / total) * Math.PI * 2;
    ctx.beginPath();
    ctx.arc(cx, cy, radius, angle, angle + slice);
    ctx.strokeStyle = it.color;
    ctx.lineWidth = lineWidth;
    ctx.stroke();
    angle += slice;
  });

  legend.innerHTML = items.map(it => {
    const pct = total ? ((it.value / total) * 100).toFixed(1) : '0.0';
    return `
      <div class="dleg-item">
        <span class="dleg-left"><i class="dot-sm" style="background:${it.color}"></i>${escapeHtml(it.label)}</span>
        <span class="dleg-right">${formatNumber(it.value)}<span class="dleg-pct">${pct}%</span></span>
      </div>
    `;
  }).join('');
}

function renderRecentLogs(logs = getWindowedLogs()) {
  const tbody = document.getElementById('recentLogsBody');
  if (!tbody) return;

  const filtered = applyQuickSearch(logs).slice(0, 8);
  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="empty-cell">No interceptions recorded in this window.</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(l => {
    const isBlock = (l.action || '').toUpperCase() === 'BLOCK';
    const th = l.threat_type || 'NONE';
    return `
      <tr>
        <td class="mono">${formatTimeShort(l.actionTime)}</td>
        <td class="mono">${escapeHtml(l.ip || '127.0.0.1')}</td>
        <td><span class="pill-badge pill-neutral">${escapeHtml(l.method || 'GET')}</span></td>
        <td class="mono">${escapeHtml(l.uri || '/')}${l.query ? '?' + escapeHtml(l.query) : ''}</td>
        <td><span class="pill-badge ${th !== 'NONE' ? 'pill-danger' : 'pill-neutral'}">${escapeHtml(th)}</span></td>
        <td><span class="pill-badge ${isBlock ? 'pill-block' : 'pill-allow'}">${escapeHtml(l.action || 'ALLOW')}</span></td>
      </tr>
    `;
  }).join('');
}

/* ========================================================================= */
/* 6. FIREWALL EVENTS TAB                                                    */
/* ========================================================================= */
function initEventsTab() {
  ['eventsSearch', 'eventsActionFilter', 'eventsThreatFilter'].forEach(id => {
    const el = document.getElementById(id);
    if (el) {
      el.addEventListener('input', () => {
        state.eventsPage = 1;
        renderEventsTable();
      });
    }
  });

  document.getElementById('btnEventsPrev')?.addEventListener('click', () => {
    if (state.eventsPage > 1) {
      state.eventsPage--;
      renderEventsTable();
    }
  });
  document.getElementById('btnEventsNext')?.addEventListener('click', () => {
    state.eventsPage++;
    renderEventsTable();
  });
  document.getElementById('btnExportEventsCsv')?.addEventListener('click', exportEventsCsv);
}

function getFilteredEvents() {
  const q = (document.getElementById('eventsSearch')?.value || '').trim().toLowerCase();
  const act = document.getElementById('eventsActionFilter')?.value || '';
  const thr = document.getElementById('eventsThreatFilter')?.value || '';

  return applyQuickSearch(getWindowedLogs()).filter(l => {
    if (act && (l.action || '').toUpperCase() !== act) return false;
    if (thr && (l.threat_type || 'NONE').toUpperCase() !== thr) return false;
    if (q) {
      const hay = `${l.ip || ''} ${l.uri || ''} ${l.query || ''} ${l.method || ''} ${l.threat_type || ''}`.toLowerCase();
      if (!hay.includes(q)) return false;
    }
    return true;
  });
}

function renderEventsTable() {
  const tbody = document.getElementById('eventsTableBody');
  if (!tbody) return;

  const filtered = getFilteredEvents();
  const totalPages = Math.max(1, Math.ceil(filtered.length / state.eventsPageSize));
  if (state.eventsPage > totalPages) state.eventsPage = totalPages;

  setText('eventsSubtitle', `${formatNumber(filtered.length)} interceptions in the last ${state.timeframe} window`);
  setText('eventsPageInfo', `Page ${state.eventsPage} / ${totalPages}`);

  const start = (state.eventsPage - 1) * state.eventsPageSize;
  const pageItems = filtered.slice(start, start + state.eventsPageSize);

  if (pageItems.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" class="empty-cell">No firewall events match the current filter.</td></tr>`;
    return;
  }

  tbody.innerHTML = pageItems.map(l => {
    const isBlock = (l.action || '').toUpperCase() === 'BLOCK';
    const th = l.threat_type || 'NONE';
    const service = deriveServiceName(l.uri);
    const statusCode = isBlock ? '403' : '200';
    return `
      <tr>
        <td class="mono">${formatTimeShort(l.actionTime)}</td>
        <td class="mono">${escapeHtml(l.ip || '127.0.0.1')}</td>
        <td><span class="pill-badge pill-neutral">${escapeHtml(l.method || 'GET')}</span></td>
        <td class="mono" style="color:var(--text-muted)">${escapeHtml(service)}</td>
        <td class="mono">${escapeHtml(l.uri || '/')}${l.query ? '?' + escapeHtml(l.query) : ''}</td>
        <td><span class="pill-badge ${th !== 'NONE' ? 'pill-danger' : 'pill-neutral'}">${escapeHtml(th)}</span></td>
        <td><span class="pill-badge ${isBlock ? 'pill-block' : 'pill-allow'}">${escapeHtml(l.action || 'ALLOW')}</span></td>
        <td class="mono" style="color:${isBlock ? '#f87171' : 'var(--text-muted)'}">${statusCode}</td>
      </tr>
    `;
  }).join('');
}

function exportEventsCsv() {
  const rows = getFilteredEvents();
  const header = ['timestamp', 'ip', 'method', 'service', 'uri', 'query', 'threat_type', 'action', 'status'];
  const csv = [header.join(',')].concat(rows.map(l => {
    const isBlock = (l.action || '').toUpperCase() === 'BLOCK';
    return [
      new Date(normalizeMs(l.actionTime)).toISOString(),
      l.ip || '',
      l.method || '',
      deriveServiceName(l.uri),
      l.uri || '',
      l.query || '',
      l.threat_type || 'NONE',
      l.action || 'ALLOW',
      isBlock ? '403' : '200'
    ].map(v => `"${String(v).replace(/"/g, '""')}"`).join(',');
  })).join('\n');

  downloadFile(`netguard-events-${state.timeframe}.csv`, csv, 'text/csv');
}

/* ========================================================================= */
/* 7. INTERNAL TELEMETRY TAB                                                 */
/* ========================================================================= */
function initTelemetryTab() {
  document.getElementById('telemetrySearch')?.addEventListener('input', renderTelemetryTable);
  document.getElementById('telemetryCategoryFilter')?.addEventListener('change', renderTelemetryTable);
}

function renderTelemetryView() {
  renderTelemetryChart();
  renderTelemetryTable();
}

function renderTelemetryChart() {
  const logs = getWindowedLogs();
  const buckets = buildTimeBuckets(logs, 28);
  drawStackedBarChart('chartTelemetrySeries', buckets, [
    { key: 'allow', color: '#10b981' },
    { key: 'path', color: '#8b5cf6' },
    { key: 'xss', color: '#f59e0b' },
    { key: 'sqli', color: '#ef4444' }
  ]);
}

function renderTelemetryTable() {
  const tbody = document.getElementById('telemetryTableBody');
  if (!tbody) return;

  const q = (document.getElementById('telemetrySearch')?.value || '').trim().toLowerCase();
  const cat = document.getElementById('telemetryCategoryFilter')?.value || '';

  const serviceRows = [
    {
      time: formatTimeUtc(Date.now()),
      category: 'Microservice',
      component: 'almight_firewall_v1 (:7080)',
      detail: 'Spring Boot WAF Engine Health',
      value: state.services.waf.up ? `UP (${state.services.waf.ms}ms)` : 'DOWN'
    },
    {
      time: formatTimeUtc(Date.now()),
      category: 'Microservice',
      component: 'threat_detection (:5000)',
      detail: `FastAPI Inspection (${state.threatRules.length} rules)`,
      value: state.services.threat.up ? `UP (${state.services.threat.ms}ms)` : 'DOWN'
    },
    {
      time: formatTimeUtc(Date.now()),
      category: 'Microservice',
      component: 'almighty_cassandra_log (:7081)',
      detail: `Cassandra Audit (${state.logs.length} records)`,
      value: state.services.cassandra.up ? `UP (${state.services.cassandra.ms}ms)` : 'DOWN'
    }
  ];

  const rows = [...serviceRows, ...state.actuatorMetrics].filter(r => {
    if (cat && r.category !== cat) return false;
    if (q && !`${r.category} ${r.component} ${r.detail} ${r.value}`.toLowerCase().includes(q)) return false;
    return true;
  });

  if (rows.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" class="empty-cell">No telemetry entries match filter.</td></tr>`;
    return;
  }

  tbody.innerHTML = rows.map(r => `
    <tr>
      <td class="mono">${escapeHtml(r.time)}</td>
      <td><span class="pill-badge pill-info">${escapeHtml(r.category)}</span></td>
      <td class="mono">${escapeHtml(r.component)}</td>
      <td>${escapeHtml(r.detail)}</td>
      <td class="mono">${escapeHtml(r.value)}</td>
    </tr>
  `).join('');
}

/* ========================================================================= */
/* 8. FIREWALL RULES CRUD                                                    */
/* ========================================================================= */
function initRules() {
  const modal = document.getElementById('addRuleModal');
  const form = document.getElementById('addRuleForm');

  document.getElementById('btnOpenAddRuleModal')?.addEventListener('click', () => {
    document.getElementById('ruleModalTitle').textContent = 'Create New Firewall Rule';
    document.getElementById('editRuleId').value = '';
    form.reset();
    modal.style.display = 'flex';
  });
  document.getElementById('btnCloseModal')?.addEventListener('click', () => modal.style.display = 'none');
  document.getElementById('btnCancelModal')?.addEventListener('click', () => modal.style.display = 'none');

  form?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const editId = document.getElementById('editRuleId').value;
    const payload = {
      ruleId: editId ? Number(editId) : null,
      ruleName: document.getElementById('ruleName').value.trim(),
      pattern: document.getElementById('rulePattern').value.trim(),
      rulePattern: document.getElementById('rulePattern').value.trim(),
      field: document.getElementById('ruleField').value,
      action: document.getElementById('ruleAction').value,
      status: document.getElementById('ruleStatus').value
    };

    try {
      const endpoint = editId ? '/rule/ruleUpdate' : '/rule/ruleInsert';
      const res = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (res.ok) {
        showToast(editId ? 'Rule updated' : 'Rule saved', 'success');
        modal.style.display = 'none';
        loadRules();
      } else {
        showToast('Failed to save rule', 'error');
      }
    } catch (err) {
      showToast(`Error: ${err.message}`, 'error');
    }
  });
}

async function loadRules() {
  try {
    const res = await fetch('/rule/getAllRules');
    if (!res.ok) throw new Error('HTTP ' + res.status);
    const text = await res.text();
    const rules = text ? JSON.parse(text) : [];
    state.rules = Array.isArray(rules) ? rules : [];
    renderRulesTable();
  } catch (e) {
    const tbody = document.getElementById('rulesTableBody');
    if (tbody) tbody.innerHTML = `<tr><td colspan="7" class="empty-cell">Error loading rules: ${escapeHtml(e.message)}</td></tr>`;
  }
}

function renderRulesTable() {
  const tbody = document.getElementById('rulesTableBody');
  if (!tbody) return;

  const rules = state.rules.filter(r => {
    if (!state.quickSearch) return true;
    return `${r.ruleName || ''} ${r.pattern || ''} ${r.field || ''}`.toLowerCase().includes(state.quickSearch);
  });

  if (rules.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="empty-cell">No custom firewall rules configured. Click "+ Add New Rule" to create one.</td></tr>`;
    return;
  }

  tbody.innerHTML = rules.map(r => {
    const id = r.id;
    const hexId = Number(id).toString(16).padStart(6, '0');
    const pat = r.pattern || '';
    const name = r.ruleName || pat;
    const action = (r.action || 'BLOCK').toUpperCase();
    const isActive = (r.status || 'active').toLowerCase() === 'active';
    const hits = countRuleHits(pat);

    return `
      <tr>
        <td class="mono" style="color:var(--text-dim)">${hexId}</td>
        <td>
          <strong>${escapeHtml(name)}</strong>
          <span class="rule-sub-pattern">${escapeHtml(pat)}</span>
        </td>
        <td class="mono">${escapeHtml(r.field || 'path')}</td>
        <td><span class="pill-badge ${action === 'BLOCK' ? 'pill-block' : 'pill-allow'}">${action}</span></td>
        <td class="mono">${hits}</td>
        <td>
          <label class="chk-inline">
            <input type="checkbox" ${isActive ? 'checked' : ''} onchange="toggleRuleStatus(${id}, this.checked)">
            <span>${isActive ? 'ACTIVE' : 'DISABLED'}</span>
          </label>
        </td>
        <td class="text-right">
          <button class="icon-op-btn" title="Edit Rule" onclick="openEditRule(${id})">&#9998;</button>
          <button class="icon-op-btn danger" title="Delete Rule" onclick="deleteRule(${id})">&#128465;</button>
        </td>
      </tr>
    `;
  }).join('');
}

function countRuleHits(pattern) {
  if (!pattern) return 0;
  try {
    const re = new RegExp(pattern, 'i');
    return state.logs.filter(l => re.test(`${l.uri || ''}${l.query ? '?' + l.query : ''}`)).length;
  } catch {
    return state.logs.filter(l => `${l.uri || ''}${l.query || ''}`.includes(pattern)).length;
  }
}

window.openEditRule = function (id) {
  const r = state.rules.find(x => x.id === id);
  if (!r) return;
  document.getElementById('ruleModalTitle').textContent = 'Edit Firewall Rule';
  document.getElementById('editRuleId').value = r.id;
  document.getElementById('ruleName').value = r.ruleName || r.pattern || '';
  document.getElementById('rulePattern').value = r.pattern || '';
  document.getElementById('ruleField').value = r.field || 'path';
  document.getElementById('ruleAction').value = (r.action || 'BLOCK').toUpperCase();
  document.getElementById('ruleStatus').value = (r.status || 'active').toLowerCase();
  document.getElementById('addRuleModal').style.display = 'flex';
};

window.toggleRuleStatus = async function (id, checked) {
  try {
    await fetch('/rule/ruleUpdate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ruleId: id, status: checked ? 'active' : 'inactive' })
    });
    showToast(`Rule ${checked ? 'enabled' : 'disabled'}`, 'info');
    loadRules();
  } catch (e) {
    showToast('Failed to toggle rule', 'error');
  }
};

window.deleteRule = async function (id) {
  if (!confirm('Delete this firewall rule?')) return;
  try {
    const res = await fetch('/rule/delete', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify([{ ruleId: id }])
    });
    if (res.ok) {
      showToast('Firewall rule deleted', 'success');
      loadRules();
    }
  } catch (e) {
    showToast('Error deleting rule', 'error');
  }
};

/* ========================================================================= */
/* 9. THREAT ENGINE TAB                                                      */
/* ========================================================================= */
function initThreatEngine() {
  document.getElementById('btnTestPayload')?.addEventListener('click', classifyCurrentPayload);
  document.getElementById('btnReloadThreatRules')?.addEventListener('click', async () => {
    try {
      const res = await fetch(`${state.settings.threatUrl}/reload-rules`, { method: 'POST' });
      const data = await res.json();
      showToast(`Hot reloaded ${data.rules_loaded || 0} rules`, 'success');
      loadThreatRules();
    } catch {
      showToast('Failed to reach threat_detection :5000', 'error');
    }
  });
  document.getElementById('threatPatternFilter')?.addEventListener('input', renderThreatRulesTable);
}

async function classifyCurrentPayload() {
  const input = document.getElementById('threatPayloadInput')?.value.trim() || '';
  const box = document.getElementById('testClassificationResult');
  if (!box) return;

  let uri = input;
  let query = '';
  let body = '';
  if (input.startsWith('{')) {
    uri = '/api/inspect';
    body = input;
  } else if (input.includes('?')) {
    const idx = input.indexOf('?');
    uri = input.slice(0, idx);
    query = input.slice(idx + 1);
  }

  try {
    const res = await fetch(`${state.settings.threatUrl}/detect`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ uri, query, body })
    });
    const data = await res.json();
    const th = data.threat_type || 'NONE';
    const isThreat = th !== 'NONE';
    box.style.display = 'block';
    box.className = `classify-result ${isThreat ? 'pill-danger' : 'pill-allow'}`;
    box.textContent = isThreat ? `THREAT DETECTED: ${th} (Action: BLOCK)` : 'CLEAN PAYLOAD: NONE (Action: ALLOW)';
  } catch {
    showToast('Could not connect to FastAPI threat engine', 'error');
  }
}

async function loadThreatRules() {
  try {
    const res = await fetch(`${state.settings.threatUrl}/rules`);
    const data = await res.json();
    state.threatRules = Array.isArray(data.rules) ? data.rules : [];
    setText('threatRuleCount', state.threatRules.length);
    setText('tickerThreatRules', `THREAT ML ${state.threatRules.length} RULES`);
    renderThreatRulesTable();
  } catch {
    const tbody = document.getElementById('threatRulesBody');
    if (tbody) tbody.innerHTML = `<tr><td colspan="5" class="empty-cell">Threat Engine (:5000) unreachable.</td></tr>`;
  }
}

function renderThreatRulesTable() {
  const tbody = document.getElementById('threatRulesBody');
  if (!tbody) return;
  const filter = (document.getElementById('threatPatternFilter')?.value || '').trim().toLowerCase();

  const list = state.threatRules.filter(r => {
    if (!filter) return true;
    return `${r.threat_type || ''} ${r.pattern || ''} ${r.field || ''}`.toLowerCase().includes(filter);
  });

  if (list.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" class="empty-cell">No matching inspection rules.</td></tr>`;
    return;
  }

  tbody.innerHTML = list.map((r, idx) => {
    const th = (r.threat_type || 'UNKNOWN').toUpperCase();
    const cls = th.includes('SQL') ? 'pill-danger' : th.includes('XSS') ? 'pill-warn' : th.includes('PATH') ? 'pill-purple' : 'pill-info';
    return `
      <tr>
        <td><span class="pill-badge ${cls}">${escapeHtml(th)}</span></td>
        <td class="mono">${escapeHtml(r.field || 'query')}</td>
        <td><span class="pill-badge pill-neutral">${escapeHtml(r.pattern_type || 'REGEX')}</span></td>
        <td class="mono">${escapeHtml(r.pattern || '')}</td>
        <td class="text-right">
          <button class="btn-outline btn-xs" onclick="testSingleThreatRule(${idx})">Test</button>
        </td>
      </tr>
    `;
  }).join('');
}

window.testSingleThreatRule = function (idx) {
  const r = state.threatRules[idx];
  if (!r) return;
  const th = (r.threat_type || '').toUpperCase();
  let sample = '/user?query=test';
  if (th.includes('SQL')) sample = "/user?query=UNION SELECT * FROM users--";
  else if (th.includes('XSS')) sample = "/user?query=<script>alert(1)</script>";
  else if (th.includes('PATH')) sample = "/user?query=../../../etc/passwd";
  document.getElementById('threatPayloadInput').value = sample;
  classifyCurrentPayload();
};

/* ========================================================================= */
/* 10. ATTACK SIMULATOR TAB & OVERVIEW QUICK ATTACKS                         */
/* ========================================================================= */
function initSimulator() {
  document.getElementById('btnLaunchFullSimulation')?.addEventListener('click', () => {
    const url = document.getElementById('simTargetUrl').value.trim();
    const repeat = document.getElementById('simRepeatCount').value;
    const vector = document.getElementById('simVectorSelect').value;
    runSimulationSuite(url, repeat, vector);
  });

  document.getElementById('btnClearSimConsole')?.addEventListener('click', () => {
    document.getElementById('simTerminal').innerHTML =
      `<div class="term-line muted">[System Ready] Click 'Launch Simulation Suite' to begin testing.</div>`;
    document.getElementById('simInlineStats').style.display = 'none';
  });
}

async function runSimulationSuite(url, repeat, vector) {
  const term = document.getElementById('simTerminal');
  const stats = document.getElementById('simInlineStats');
  const btn = document.getElementById('btnLaunchFullSimulation');

  btn.disabled = true;
  term.innerHTML = `<div class="term-line info">==> Running ${vector} simulation (${repeat}x) against ${escapeHtml(url)}...</div>`;

  try {
    const res = await fetch(`/simulate/run?url=${encodeURIComponent(url)}&repeat=${repeat}&vector=${encodeURIComponent(vector)}`);
    const data = await res.json();

    stats.style.display = 'flex';
    setText('simTotalSent', data.totalRequests || 0);
    setText('simBlocked', data.blockedCount || 0);
    setText('simAllowed', data.allowedCount || 0);
    const rate = data.totalRequests ? Math.round((data.blockedCount / data.totalRequests) * 100) : 0;
    setText('simBlockRate', `${rate}%`);

    (data.results || []).forEach(r => {
      const cls = r.responseCode === 403 ? 'block' : 'allow';
      const label = r.responseCode === 403 ? 'BLOCKED 403' : `ALLOWED ${r.responseCode}`;
      const div = document.createElement('div');
      div.className = `term-line ${cls}`;
      const detail = r.errorMessage ? `Error: ${r.errorMessage}` : (r.url || 'OK');
      div.textContent = `[${label}] ${r.name || 'Request'} -> ${detail}`;
      term.appendChild(div);
    });
    term.scrollTop = term.scrollHeight;
    showToast(`Simulation finished (${data.totalRequests} requests)`, 'success');
    setTimeout(refreshAllData, 900);
  } catch (e) {
    term.innerHTML += `<div class="term-line block">[ERROR] ${escapeHtml(e.message)}</div>`;
  } finally {
    btn.disabled = false;
  }
}

window.executeQuickAttack = async function (attackType) {
  const box = document.getElementById('quickSimResult');
  let url = '/user?query=test';
  let body = '{}';
  if (attackType === 'SQL Injection') url = '/user?query=id=1%20OR%201=1';
  else if (attackType === 'XSS Attack') url = "/user?query=%3Cscript%3Ealert('XSS')%3C%2Fscript%3E";
  else if (attackType === 'Path Traversal') url = '/user?query=..%2F..%2F..%2Fetc%2Fpasswd';
  else { url = '/user'; body = JSON.stringify({ username: 'secops_test' }); }

  box.style.display = 'block';
  box.style.color = 'var(--cyan)';
  box.textContent = `Sending ${attackType} -> ${url}...`;

  try {
    const res = await fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body });
    const txt = await res.text();
    const blocked = res.status === 403;
    box.style.color = blocked ? '#f87171' : 'var(--green)';
    box.textContent = `[HTTP ${res.status}] ${attackType} -> ${blocked ? 'BLOCKED' : 'ALLOWED'} (${txt.slice(0, 140)})`;
    setTimeout(loadCassandraLogs, 700);
  } catch (e) {
    box.style.color = '#f87171';
    box.textContent = `Error: ${e.message}`;
  }
};

/* ========================================================================= */
/* 11. AUDIT LOGS & RAW SYSLOG                                               */
/* ========================================================================= */
function initAuditTab() {
  document.getElementById('auditSearch')?.addEventListener('input', renderAuditLogs);
  document.getElementById('auditSeverityFilter')?.addEventListener('change', renderAuditLogs);
}

function renderAuditLogs() {
  const tbody = document.getElementById('auditLogsBody');
  if (!tbody) return;

  const q = (document.getElementById('auditSearch')?.value || '').trim().toLowerCase();
  const sev = document.getElementById('auditSeverityFilter')?.value || '';

  const rows = applyQuickSearch(getWindowedLogs()).map(l => {
    const isBlock = (l.action || '').toUpperCase() === 'BLOCK';
    const severity = isBlock ? 'ERR' : 'INFO';
    const statusCode = isBlock ? '403' : '200';
    const th = l.threat_type || 'NONE';
    const message = `${l.action || 'ALLOW'} ${th} ${l.ip || '127.0.0.1'} ${l.method || 'GET'} ${l.uri || '/'}${l.query ? '?' + l.query : ''} -> ${statusCode}`;
    return {
      ts: formatTimeMonthDay(l.actionTime),
      host: 'waf-gateway-01',
      program: 'almight_firewall_v1',
      severity,
      message
    };
  }).filter(r => {
    if (sev && r.severity !== sev) return false;
    if (q && !`${r.host} ${r.program} ${r.message}`.toLowerCase().includes(q)) return false;
    return true;
  });

  if (rows.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" class="empty-cell">No audit logs matching filter.</td></tr>`;
    return;
  }

  tbody.innerHTML = rows.slice(0, 120).map(r => `
    <tr>
      <td class="mono">${escapeHtml(r.ts)}</td>
      <td class="mono" style="color:var(--text-muted)">${r.host}</td>
      <td class="mono" style="color:var(--text-muted)">${r.program}</td>
      <td><span class="pill-badge ${r.severity === 'ERR' ? 'pill-danger' : 'pill-info'}">${r.severity}</span></td>
      <td class="mono">${escapeHtml(r.message)}</td>
    </tr>
  `).join('');
}

function initSyslogTab() {
  document.getElementById('syslogFilterInput')?.addEventListener('input', renderRawSyslog);
  document.getElementById('btnExportSyslog')?.addEventListener('click', () => {
    const lines = buildSyslogLines().map(x => x.text).join('\n');
    downloadFile('netguard-syslog.log', lines, 'text/plain');
  });
}

function buildSyslogLines() {
  const filter = (document.getElementById('syslogFilterInput')?.value || '').trim().toLowerCase();
  return applyQuickSearch(getWindowedLogs()).slice(0, 300).map(l => {
    const isBlock = (l.action || '').toUpperCase() === 'BLOCK';
    const sev = isBlock ? 'ERR' : 'INFO';
    const code = isBlock ? '403' : '200';
    const th = l.threat_type || 'NONE';
    const text = `${formatTimeMonthDay(l.actionTime)} waf-gateway-01 almight_firewall_v1[7080]: ${sev} ${l.action || 'ALLOW'} ${th} ${l.ip || '127.0.0.1'} ${l.method || 'GET'} ${l.uri || '/'}${l.query ? '?' + l.query : ''} -> ${code}`;
    return { cls: isBlock ? 'err' : 'info', text };
  }).filter(item => !filter || item.text.toLowerCase().includes(filter));
}

function renderRawSyslog() {
  const term = document.getElementById('syslogTerminal');
  const streaming = document.getElementById('syslogStreaming')?.checked;
  if (!term || streaming === false) return;

  const lines = buildSyslogLines();
  setText('syslogLineCount', `${lines.length} lines`);

  if (lines.length === 0) {
    term.innerHTML = `<div class="syslog-line info">No syslog lines match filter.</div>`;
    return;
  }

  term.innerHTML = lines.map(l => `<div class="syslog-line ${l.cls}">${escapeHtml(l.text)}</div>`).join('');
  if (document.getElementById('syslogAutoScroll')?.checked) {
    term.scrollTop = term.scrollHeight;
  }
}

/* ========================================================================= */
/* 12. SETTINGS TAB                                                          */
/* ========================================================================= */
function initSettingsTab() {
  document.getElementById('setThreatUrl').value = state.settings.threatUrl;
  document.getElementById('setCassandraUrl').value = state.settings.cassandraProxy;
  document.getElementById('setPollSec').value = String(state.settings.pollSec);
  document.getElementById('setKafkaBroker').value = state.settings.kafkaBroker;
  document.getElementById('setRetention').value = state.settings.retention;
  document.getElementById('setDefaultPolicy').value = state.settings.defaultPolicy;
  document.getElementById('setWebhook').value = state.settings.webhook || '';

  document.getElementById('settingsForm')?.addEventListener('submit', (e) => {
    e.preventDefault();
    state.settings = {
      threatUrl: document.getElementById('setThreatUrl').value.trim() || DEFAULT_SETTINGS.threatUrl,
      cassandraProxy: document.getElementById('setCassandraUrl').value.trim() || DEFAULT_SETTINGS.cassandraProxy,
      pollSec: Number(document.getElementById('setPollSec').value) || 15,
      kafkaBroker: document.getElementById('setKafkaBroker').value.trim() || DEFAULT_SETTINGS.kafkaBroker,
      retention: document.getElementById('setRetention').value || '7d',
      defaultPolicy: document.getElementById('setDefaultPolicy').value || 'ALLOW',
      webhook: document.getElementById('setWebhook').value.trim()
    };
    localStorage.setItem('netguard_settings', JSON.stringify(state.settings));
    setText('tickerRetention', state.settings.retention);
    restartPolling();
    refreshAllData();
    showToast('Settings saved', 'success');
  });

  document.getElementById('createUserForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const statusEl = document.getElementById('createUserStatus');
    const payload = {
      fullName: document.getElementById('newUserFullName').value.trim(),
      email: document.getElementById('newUserEmail').value.trim(),
      phone: document.getElementById('newUserPhone').value.trim(),
      department: document.getElementById('newUserDepartment').value,
      role: document.getElementById('newUserRole').value,
      status: document.getElementById('newUserStatus').value,
      username: document.getElementById('newUsername').value.trim(),
      password: document.getElementById('newUserPassword').value
    };
    if (!payload.username || !payload.password) return;

    try {
      const res = await fetch('/users/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(payload)
      });
      const msg = await res.text();
      const ok = res.ok && msg.toLowerCase().includes('success');
      if (statusEl) {
        statusEl.textContent = msg;
        statusEl.style.color = ok ? 'var(--green)' : '#f87171';
      }
      showToast(msg || (ok ? 'User created' : 'Failed to create user'), ok ? 'success' : 'error');
      if (ok) {
        e.target.reset();
      }
    } catch (err) {
      if (statusEl) {
        statusEl.textContent = `Error: ${err.message}`;
        statusEl.style.color = '#f87171';
      }
      showToast(`Error creating user: ${err.message}`, 'error');
    }
  });
}

/* ========================================================================= */
/* 13. HELPERS                                                               */
/* ========================================================================= */
function applyQuickSearch(logs) {
  if (!state.quickSearch) return logs;
  return logs.filter(l => {
    const hay = `${l.ip || ''} ${l.uri || ''} ${l.query || ''} ${l.method || ''} ${l.threat_type || ''} ${l.action || ''}`.toLowerCase();
    return hay.includes(state.quickSearch);
  });
}

function deriveServiceName(uri) {
  const u = (uri || '/').split('?')[0];
  if (u.startsWith('/auth')) return 'auth-service';
  if (u.startsWith('/user')) return 'user-api';
  if (u.startsWith('/rule')) return 'rule-engine';
  if (u.startsWith('/simulate')) return 'simulator';
  return 'waf-gateway';
}

function formatTimeShort(ts) {
  const ms = normalizeMs(ts);
  if (!ms) return '--:--:--';
  const d = new Date(ms);
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`;
}

function formatTimeUtc(ms) {
  const d = new Date(ms);
  return `${String(d.getUTCHours()).padStart(2, '0')}:${String(d.getUTCMinutes()).padStart(2, '0')}:${String(d.getUTCSeconds()).padStart(2, '0')}`;
}

function formatTimeMonthDay(ts) {
  const ms = normalizeMs(ts);
  if (!ms) return '--- -- --:--:--';
  const d = new Date(ms);
  const mon = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'][d.getMonth()];
  const day = String(d.getDate()).padStart(2, '0');
  return `${mon} ${day}, ${formatTimeShort(ms)}`;
}

function formatNumber(n) {
  return Number(n || 0).toLocaleString();
}

function setText(id, val) {
  const el = document.getElementById(id);
  if (el) el.textContent = String(val);
}

function downloadFile(filename, content, mime) {
  const blob = new Blob([content], { type: mime });
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = filename;
  a.click();
  URL.revokeObjectURL(a.href);
}

function showToast(msg, type = 'info') {
  const c = document.getElementById('toastContainer');
  if (!c) return;
  const el = document.createElement('div');
  el.className = `toast toast-${type}`;
  el.textContent = msg;
  c.appendChild(el);
  setTimeout(() => el.remove(), 3500);
}

function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
