(() => {
  'use strict';

  const views = [...document.querySelectorAll('.view')];
  const breadcrumb = document.getElementById('breadcrumb');
  const menuButton = document.getElementById('menuButton');
  const mainNav = document.getElementById('mainNav');
  const toast = document.getElementById('toast');
  const memoryStore = {};

  const storage = {
    get(key) {
      try { return localStorage.getItem(key); }
      catch (e) { return Object.prototype.hasOwnProperty.call(memoryStore, key) ? memoryStore[key] : null; }
    },
    set(key, value) {
      try { localStorage.setItem(key, value); }
      catch (e) { memoryStore[key] = String(value); }
    }
  };

  const state = {
    service: null,
    professional: null,
    date: null,
    time: null,
    selectedAppointmentId: null,
    reprogramAppointmentId: null
  };

  const services = [
    { id: 'srv1', name: 'Corte de cabello', icon: '✂️', duration: '45 min', price: '$35.000' },
    { id: 'srv2', name: 'Manicure', icon: '💅', duration: '50 min', price: '$45.000' },
    { id: 'srv3', name: 'Masaje relajante', icon: '✦', duration: '60 min', price: '$70.000' }
  ];

  const professionals = [
    { id: 'pro1', name: 'Laura Poveda', initials: 'LP', specialty: 'Disponible ahora' },
    { id: 'pro2', name: 'Andrés Angarita', initials: 'AA', specialty: 'Disponible esta semana' },
    { id: 'pro3', name: 'Cualquiera disponible', initials: 'CD', specialty: 'Primera opción libre' }
  ];

  const timeSlots = ['08:00', '09:30', '11:00', '14:00', '15:30', '17:00'];

  function localDateISO(offsetDays = 0) {
    const date = new Date();
    date.setHours(12, 0, 0, 0);
    date.setDate(date.getDate() + offsetDays);
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  function seedAppointments() {
    if (!storage.get('citanova_appointments')) {
      const seed = [
        { id: 'CN-1049', client: 'Cliente demo', service: 'Manicure', professional: 'Andrés Angarita', date: localDateISO(1), time: '13:00', status: 'Pendiente' },
        { id: 'CN-1048', client: 'Cliente demo', service: 'Corte de cabello', professional: 'Laura Poveda', date: localDateISO(-2), time: '10:00', status: 'Inasistencia' }
      ];
      storage.set('citanova_appointments', JSON.stringify(seed));
    }
  }

  function getAppointments() {
    seedAppointments();
    try { return JSON.parse(storage.get('citanova_appointments') || '[]'); }
    catch (e) { return []; }
  }

  function saveAppointments(items) {
    storage.set('citanova_appointments', JSON.stringify(items));
  }

  function getCurrentClientName() {
    try {
      const user = JSON.parse(storage.get('citanova_user') || 'null');
      return [user?.name, user?.last].filter(Boolean).join(' ') || 'Fredy Castañeda';
    } catch (e) { return 'Fredy Castañeda'; }
  }

  function formatDate(value) {
    if (!value) return '-';
    const [y, m, d] = value.split('-').map(Number);
    return new Intl.DateTimeFormat('es-CO', { day: '2-digit', month: 'long', year: 'numeric' }).format(new Date(y, m - 1, d));
  }

  function monthShort(value) {
    const [y, m, d] = value.split('-').map(Number);
    return {
      day: String(d).padStart(2, '0'),
      month: new Intl.DateTimeFormat('es-CO', { month: 'short' }).format(new Date(y, m - 1, d)).replace('.', '')
    };
  }

  function statusClass(status) {
    if (status === 'Cancelada') return 'status-cancelled';
    if (status === 'Pendiente') return 'status-pending';
    if (status === 'Inasistencia') return 'status-noshow';
    return 'status-confirmed';
  }

  function showToast(message) {
    toast.textContent = message;
    toast.classList.add('show');
    clearTimeout(showToast.t);
    showToast.t = setTimeout(() => toast.classList.remove('show'), 2600);
  }

  function showView(id, pushHash = true) {
    const target = document.getElementById(`view-${id}`) || document.getElementById('view-p01');
    views.forEach(v => v.classList.toggle('active', v === target));
    breadcrumb.textContent = `CitaNOVA › ${target.dataset.title || 'Inicio'}`;
    if (pushHash && location.hash !== `#${id}`) {
      try { history.pushState(null, '', `#${id}`); } catch (e) {}
    }
    mainNav.classList.remove('open');
    menuButton.setAttribute('aria-expanded', 'false');
    document.getElementById('main-content').focus({ preventScroll: true });

    if (id === 'p04') renderDashboard();
    if (id === 'p05') renderServices();
    if (id === 'p06') renderProfessionals();
    if (id === 'p07') renderSchedule();
    if (id === 'p08') renderAppointments('upcoming');
    if (id === 'p09') renderAppointmentDetail();
    if (id === 'p10') renderProfessionalPanel();
    if (id === 'p11') renderAdminPanel();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  document.addEventListener('click', e => {
    const go = e.target.closest('[data-go]');
    if (go) { e.preventDefault(); showView(go.dataset.go); }
    const stateBtn = e.target.closest('[data-state]');
    if (stateBtn) { e.preventDefault(); configureState(stateBtn.dataset.state); showView('p12'); }
  });

  menuButton.addEventListener('click', () => {
    const open = mainNav.classList.toggle('open');
    menuButton.setAttribute('aria-expanded', String(open));
  });
  window.addEventListener('popstate', () => showView((location.hash || '#p01').slice(1), false));

  const loginForm = document.getElementById('loginForm');
  loginForm.addEventListener('submit', e => {
    e.preventDefault();
    loginForm.classList.add('was-validated');
    if (!loginForm.checkValidity()) return;
    const role = document.getElementById('loginRole').value;
    storage.set('citanova_role', role);
    showView(role === 'cliente' ? 'p04' : role === 'profesional' ? 'p10' : 'p11');
    showToast('Sesión de demostración iniciada.');
  });

  const registerForm = document.getElementById('registerForm');
  registerForm.addEventListener('submit', e => {
    e.preventDefault();
    registerForm.classList.add('was-validated');
    if (!registerForm.checkValidity()) return;
    const user = {
      name: document.getElementById('regNames').value.trim(),
      last: document.getElementById('regLastnames').value.trim(),
      email: document.getElementById('regEmail').value.trim()
    };
    storage.set('citanova_user', JSON.stringify(user));
    configureState('success', 'Cuenta creada', 'Tu registro se completó correctamente. Ya puedes iniciar sesión.', 'Ir a iniciar sesión', 'p02');
    showView('p12');
  });

  function renderServices() {
    const wrap = document.getElementById('serviceOptions');
    wrap.innerHTML = services.map(s => `<button type="button" class="selection-card ${state.service?.id === s.id ? 'selected' : ''}" data-service-id="${s.id}" aria-pressed="${state.service?.id === s.id}"><span class="select-check">✓</span><div class="service-icon" aria-hidden="true">${s.icon}</div><h3>${s.name}</h3><p>${s.duration} - ${s.price}</p></button>`).join('');
    wrap.querySelectorAll('[data-service-id]').forEach(btn => btn.addEventListener('click', () => {
      state.service = services.find(s => s.id === btn.dataset.serviceId);
      state.professional = null;
      renderServices();
    }));
    document.getElementById('serviceContinue').disabled = !state.service;
  }
  document.getElementById('serviceContinue').addEventListener('click', () => showView('p06'));

  function renderProfessionals() {
    document.getElementById('selectedServiceLabel').textContent = state.service?.name || 'Sin seleccionar';
    const mirror = document.getElementById('chosenServiceMirror');
    mirror.textContent = state.service ? `${state.service.name} · ${state.service.duration} · ${state.service.price}` : 'Continúa con la selección del profesional.';
    const wrap = document.getElementById('professionalOptions');
    wrap.innerHTML = professionals.map(p => `<button type="button" class="selection-card ${state.professional?.id === p.id ? 'selected' : ''}" data-professional-id="${p.id}" aria-pressed="${state.professional?.id === p.id}"><span class="select-check">✓</span><div class="professional-avatar">${p.initials}</div><h3>${p.name}</h3><p>${p.specialty}</p></button>`).join('');
    wrap.querySelectorAll('[data-professional-id]').forEach(btn => btn.addEventListener('click', () => {
      state.professional = professionals.find(p => p.id === btn.dataset.professionalId);
      renderProfessionals();
    }));
    document.getElementById('professionalContinue').disabled = !state.professional;
  }
  document.getElementById('professionalContinue').addEventListener('click', () => showView('p07'));

  function renderCalendar(selectedDate) {
    const value = selectedDate || localDateISO(1);
    const [year, month, day] = value.split('-').map(Number);
    const first = new Date(year, month - 1, 1);
    const daysInMonth = new Date(year, month, 0).getDate();
    const mondayOffset = (first.getDay() + 6) % 7;
    const title = new Intl.DateTimeFormat('es-CO', { month: 'long', year: 'numeric' }).format(first);
    document.getElementById('calendarTitle').textContent = title.charAt(0).toUpperCase() + title.slice(1);

    const labels = ['L', 'M', 'M', 'J', 'V', 'S', 'D'].map(label => `<span>${label}</span>`).join('');
    const blanks = Array.from({ length: mondayOffset }, () => '<i aria-hidden="true"></i>').join('');
    const minDate = document.getElementById('appointmentDate').min;
    const days = Array.from({ length: daysInMonth }, (_, index) => {
      const n = index + 1;
      const iso = `${year}-${String(month).padStart(2, '0')}-${String(n).padStart(2, '0')}`;
      const selected = n === day ? ' selected-day' : '';
      const disabled = minDate && iso < minDate ? ' disabled' : '';
      return `<button type="button" class="calendar-day${selected}" data-calendar-date="${iso}" aria-pressed="${n === day}"${disabled}>${n}</button>`;
    }).join('');
    const grid = document.getElementById('calendarGrid');
    grid.innerHTML = labels + blanks + days;
    grid.querySelectorAll('[data-calendar-date]').forEach(button => button.addEventListener('click', () => {
      state.date = button.dataset.calendarDate;
      document.getElementById('appointmentDate').value = state.date;
      renderSchedule();
    }));
  }

  function renderSchedule() {
    document.getElementById('summaryService').textContent = state.service?.name || '-';
    document.getElementById('summaryProfessional').textContent = state.professional?.name || '-';
    const dateInput = document.getElementById('appointmentDate');
    const today = localDateISO();
    dateInput.min = today;
    if (!state.date || state.date < today) state.date = localDateISO(1);
    dateInput.value = state.date;
    renderCalendar(state.date);
    document.getElementById('summaryDate').textContent = formatDate(state.date);
    document.getElementById('summaryTime').textContent = state.time || '-';

    const wrap = document.getElementById('timeOptions');
    wrap.innerHTML = timeSlots.map(t => `<button type="button" class="time-option ${state.time === t ? 'selected' : ''}" data-time="${t}" aria-pressed="${state.time === t}">${t}</button>`).join('');
    wrap.querySelectorAll('[data-time]').forEach(btn => btn.addEventListener('click', () => {
      state.time = btn.dataset.time;
      renderSchedule();
    }));
    document.getElementById('confirmAppointment').disabled = !(state.service && state.professional && state.date && state.time);
  }

  document.getElementById('appointmentDate').addEventListener('change', e => {
    state.date = e.target.value;
    renderSchedule();
  });

  function nextAppointmentId(items) {
    const max = items.reduce((value, item) => {
      const match = String(item.id || '').match(/(\d+)$/);
      return match ? Math.max(value, Number(match[1])) : value;
    }, 1049);
    return `CN-${String(max + 1).padStart(4, '0')}`;
  }

  document.getElementById('confirmAppointment').addEventListener('click', () => {
    if (!(state.service && state.professional && state.date && state.time)) return;
    const items = getAppointments();

    if (state.reprogramAppointmentId) {
      const id = state.reprogramAppointmentId;
      const updated = items.map(item => item.id === id ? {
        ...item,
        service: state.service.name,
        professional: state.professional.name,
        date: state.date,
        time: state.time,
        status: 'Confirmada'
      } : item);
      saveAppointments(updated);
      state.selectedAppointmentId = id;
      state.reprogramAppointmentId = null;
      configureState('success', 'Cita reprogramada', `La cita ${id} fue actualizada para el ${formatDate(state.date)} a las ${state.time}.`, 'Ver mis citas', 'p08');
    } else {
      const id = nextAppointmentId(items);
      items.unshift({
        id,
        client: getCurrentClientName(),
        service: state.service.name,
        professional: state.professional.name,
        date: state.date,
        time: state.time,
        status: 'Confirmada'
      });
      saveAppointments(items);
      state.selectedAppointmentId = id;
      configureState('success', 'Cita confirmada', `Tu cita ${id} quedó agendada para el ${formatDate(state.date)} a las ${state.time}.`, 'Ver mis citas', 'p08');
    }
    showView('p12');
  });

  function renderAppointments(filter = 'all') {
    document.querySelectorAll('[data-appointment-filter]').forEach(b => b.classList.toggle('active', b.dataset.appointmentFilter === filter));
    let items = getAppointments();
    if (filter === 'cancelled') items = items.filter(i => i.status === 'Cancelada');
    if (filter === 'upcoming') items = items.filter(i => i.status !== 'Cancelada' && i.status !== 'Inasistencia' && i.date >= localDateISO());
    document.getElementById('appointmentsList').innerHTML = items.length ? items.map(a => {
      const md = monthShort(a.date);
      return `<article class="appointment-card"><div class="appt-main"><div class="appt-date"><strong>${md.day}</strong><span>${md.month}</span></div><div class="appt-copy"><h3>${a.service}</h3><p>${a.professional} · ${a.time}</p><span class="status ${statusClass(a.status)}">${a.status}</span></div></div><div class="appt-actions"><button type="button" class="btn btn-ghost" data-open-appointment="${a.id}">Ver detalle</button></div></article>`;
    }).join('') : '<div class="panel"><p>No hay citas para este filtro.</p></div>';
    document.querySelectorAll('[data-open-appointment]').forEach(btn => btn.addEventListener('click', () => {
      state.selectedAppointmentId = btn.dataset.openAppointment;
      showView('p09');
    }));
  }
  document.querySelectorAll('[data-appointment-filter]').forEach(btn => btn.addEventListener('click', () => renderAppointments(btn.dataset.appointmentFilter)));

  function selectedAppointment() {
    const items = getAppointments();
    return items.find(i => i.id === state.selectedAppointmentId) || items[0];
  }

  function renderAppointmentDetail() {
    const a = selectedAppointment();
    if (!a) {
      document.getElementById('appointmentDetail').innerHTML = '<p>No hay cita seleccionada.</p>';
      return;
    }
    state.selectedAppointmentId = a.id;
    document.getElementById('appointmentDetail').innerHTML = `<dl><div><dt>Código</dt><dd>${a.id}</dd></div><div><dt>Servicio</dt><dd>${a.service}</dd></div><div><dt>Profesional</dt><dd>${a.professional}</dd></div><div><dt>Fecha</dt><dd>${formatDate(a.date)}</dd></div><div><dt>Hora</dt><dd>${a.time}</dd></div><div><dt>Estado</dt><dd><span class="status ${statusClass(a.status)}">${a.status}</span></dd></div></dl>`;
    document.getElementById('cancelBtn').disabled = a.status === 'Cancelada' || a.status === 'Inasistencia';
    document.getElementById('reprogramBtn').disabled = a.status === 'Cancelada' || a.status === 'Inasistencia';
  }

  document.getElementById('reprogramBtn').addEventListener('click', () => {
    const a = selectedAppointment();
    if (!a || a.status === 'Cancelada' || a.status === 'Inasistencia') return;
    state.reprogramAppointmentId = a.id;
    state.service = services.find(s => s.name === a.service) || services[0];
    state.professional = professionals.find(p => p.name === a.professional) || professionals[0];
    state.date = a.date >= localDateISO() ? a.date : localDateISO(1);
    state.time = a.time || null;
    showView('p07');
    showToast(`Reprogramando ${a.id}. La confirmación actualizará la cita existente.`);
  });

  document.getElementById('cancelBtn').addEventListener('click', () => {
    const a = selectedAppointment();
    if (!a || a.status === 'Cancelada') return;
    const items = getAppointments().map(i => i.id === a.id ? { ...i, status: 'Cancelada' } : i);
    saveAppointments(items);
    configureState('warning', 'Cita cancelada', `La cita ${a.id} fue marcada como cancelada.`, 'Volver a mis citas', 'p08');
    showView('p12');
  });

  function renderDashboard() {
    let user = null;
    try { user = JSON.parse(storage.get('citanova_user') || 'null'); } catch (e) {}
    document.getElementById('clientName').textContent = (user?.name || 'FREDY').toUpperCase();
    const items = getAppointments().filter(i => i.status !== 'Cancelada' && i.status !== 'Inasistencia' && i.date >= localDateISO());
    document.getElementById('metricUpcoming').textContent = items.length;
    const a = items.sort((x, y) => `${x.date} ${x.time}`.localeCompare(`${y.date} ${y.time}`))[0];
    document.getElementById('dashboardAppointment').innerHTML = a ? `<div class="appointment-line"><span>${formatDate(a.date)}</span><strong>${a.time}</strong></div><div class="appointment-line"><span>Profesional</span><strong>${a.professional}</strong></div>` : '<p>No tienes próximas citas.</p>';
  }

  function renderProfessionalPanel() {
    const items = getAppointments();
    const active = items.filter(i => i.status !== 'Cancelada' && i.status !== 'Inasistencia');
    const upcoming = active.filter(i => i.date >= localDateISO());
    const cancelled = items.filter(i => i.status === 'Cancelada');
    document.getElementById('proActiveCount').textContent = active.length;
    document.getElementById('proUpcomingCount').textContent = upcoming.length;
    document.getElementById('proCancelledCount').textContent = cancelled.length;

    const rows = upcoming.sort((a, b) => `${a.date} ${a.time}`.localeCompare(`${b.date} ${b.time}`)).slice(0, 5);
    document.getElementById('professionalAppointmentsBody').innerHTML = rows.length ? rows.map(a => `<tr><td>${formatDate(a.date)}<br><small>${a.time}</small></td><td>${a.client || 'Cliente'}</td><td>${a.service}</td><td><span class="status ${statusClass(a.status)}">${a.status}</span></td><td><button class="table-action" type="button" data-professional-open="${a.id}">Ver cita</button></td></tr>`).join('') : '<tr><td colspan="5">No hay próximas citas registradas.</td></tr>';
    document.querySelectorAll('[data-professional-open]').forEach(btn => btn.addEventListener('click', () => {
      state.selectedAppointmentId = btn.dataset.professionalOpen;
      showView('p09');
    }));
  }

  function renderAdminPanel() {
    const items = getAppointments();
    document.getElementById('adminTotalCount').textContent = items.length;
    document.getElementById('adminConfirmedCount').textContent = items.filter(i => i.status === 'Confirmada').length;
    document.getElementById('adminCancelledCount').textContent = items.filter(i => i.status === 'Cancelada').length;
    document.getElementById('adminNoShowCount').textContent = items.filter(i => i.status === 'Inasistencia').length;
  }

  function configureState(type = 'info', title, message, buttonLabel = 'Continuar', go = 'p01') {
    const defaults = {
      success: ['✓', 'Operación completada', 'La acción se procesó correctamente.'],
      warning: ['!', 'Atención', 'La operación fue registrada.'],
      info: ['i', 'Información', 'Esta función se presenta como parte del prototipo funcional.']
    };
    const [icon, defTitle, defMsg] = defaults[type] || defaults.info;
    const iconEl = document.getElementById('stateIcon');
    iconEl.textContent = icon;
    iconEl.style.background = type === 'warning' ? 'var(--warning-bg)' : type === 'info' ? '#E9F0F7' : 'var(--success-bg)';
    iconEl.style.color = type === 'warning' ? 'var(--warning)' : type === 'info' ? 'var(--navy)' : 'var(--success)';
    document.getElementById('stateTitle').textContent = title || defTitle;
    document.getElementById('stateMessage').textContent = message || defMsg;
    const current = selectedAppointment();
    document.getElementById('stateService').textContent = current?.service || '-';
    document.getElementById('stateAppointmentCode').textContent = current?.id || '-';
    document.getElementById('stateAppointmentTime').textContent = current?.time || '-';
    const btn = document.getElementById('statePrimaryBtn');
    btn.textContent = buttonLabel;
    btn.dataset.target = go;
  }
  document.getElementById('statePrimaryBtn').addEventListener('click', e => showView(e.currentTarget.dataset.target || 'p01'));

  seedAppointments();
  const initial = (location.hash || '#p01').slice(1);
  showView(initial, false);
})();
