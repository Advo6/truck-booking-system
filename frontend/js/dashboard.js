let currentSection = 'overview';
let allBookings = [];
let adminCalendar;
let lastViewedBooking = null;

document.addEventListener('DOMContentLoaded', () => {
  if (!Auth.requireAdmin()) return;
  initDashboard();
});

async function initDashboard() {
  document.getElementById('admin-username').textContent = localStorage.getItem('username') || 'Admin';
  initSidebar();
  initBookingFilters();
  await loadDashboardStats();
  await loadBookings();
  await loadTrucks();
  await loadCustomers();
  initAdminCalendar();
}

function initSidebar() {
  document.querySelectorAll('.sidebar-nav a').forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      const section = link.dataset.section;
      showSection(section);
      document.querySelectorAll('.sidebar-nav a').forEach(l => l.classList.remove('active'));
      link.classList.add('active');
    });
  });

  document.getElementById('logout-btn')?.addEventListener('click', () => Auth.logout());
}

function showSection(section) {
  currentSection = section;
  document.querySelectorAll('.dash-section').forEach(s => s.style.display = 'none');
  const el = document.getElementById(`section-${section}`);
  if (el) el.style.display = 'block';
}

function initBookingFilters() {
  document.getElementById('booking-search')?.addEventListener('input', () => {
    clearTimeout(window.searchTimeout);
    window.searchTimeout = setTimeout(() => loadBookings(), 300);
  });

  document.getElementById('booking-filter')?.addEventListener('change', () => loadBookings());
  document.getElementById('booking-date-from')?.addEventListener('change', () => loadBookings());
  document.getElementById('booking-date-to')?.addEventListener('change', () => loadBookings());

  document.getElementById('booking-clear-dates')?.addEventListener('click', () => {
    document.getElementById('booking-date-from').value = '';
    document.getElementById('booking-date-to').value = '';
    loadBookings();
  });
}

function formatRevenue(amount) {
  const n = Number(amount || 0);
  return 'R ' + n.toLocaleString('en-ZA', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

async function loadDashboardStats() {
  try {
    const res = await api.get('/admin/dashboard');
    const stats = res.data;
    document.getElementById('stat-total').textContent = stats.totalBookings;
    document.getElementById('stat-today').textContent = stats.todayBookings;
    document.getElementById('stat-available').textContent = stats.availableTrucks;
    document.getElementById('stat-booked').textContent = stats.bookedTrucks;
    document.getElementById('stat-pending').textContent = stats.pendingBookings;
    document.getElementById('stat-revenue').textContent = formatRevenue(stats.totalRevenue);
  } catch (err) {
    Utils.toast('Failed to load stats', 'error');
  }
}

async function loadBookings() {
  try {
    const search = document.getElementById('booking-search')?.value || '';
    const status = document.getElementById('booking-filter')?.value || '';
    const dateFrom = document.getElementById('booking-date-from')?.value || '';
    const dateTo = document.getElementById('booking-date-to')?.value || '';

    const params = [];
    if (search) params.push(`search=${encodeURIComponent(search)}`);
    if (status) params.push(`status=${status}`);
    if (dateFrom) params.push(`dateFrom=${dateFrom}`);
    if (dateTo) params.push(`dateTo=${dateTo}`);

    let endpoint = '/admin/bookings';
    if (params.length) endpoint += '?' + params.join('&');

    const res = await api.get(endpoint);
    allBookings = res.data || [];
    renderBookingsTable();
  } catch (err) {
    Utils.toast('Failed to load bookings', 'error');
  }
}

function renderBookingsTable() {
  const tbody = document.getElementById('bookings-tbody');
  if (!tbody) return;

  tbody.innerHTML = allBookings.map(b => `
    <tr>
      <td>${b.bookingReference}</td>
      <td>${b.customerName}</td>
      <td>${b.truckNumber}</td>
      <td>${Utils.formatDate(b.pickupDate)}</td>
      <td>${Utils.formatDate(b.returnDate)}</td>
      <td><span class="status-badge ${Utils.statusClass(b.status)}">${Utils.formatStatus(b.status)}</span></td>
      <td class="action-btns">
        ${b.status === 'PENDING' ? `<button class="btn btn-sm btn-primary" onclick="updateBookingStatus(${b.id}, 'APPROVED')">Approve</button>
        <button class="btn btn-sm btn-outline" onclick="updateBookingStatus(${b.id}, 'REJECTED')">Reject</button>` : ''}
        ${b.status !== 'CANCELLED' ? `<button class="btn btn-sm btn-outline" onclick="updateBookingStatus(${b.id}, 'CANCELLED')">Cancel</button>` : ''}
        <button class="btn btn-sm btn-ghost" onclick="viewBookingDetails(${b.id})">View</button>
      </td>
    </tr>
  `).join('') || '<tr><td colspan="7" style="text-align:center">No bookings found</td></tr>';
}

async function updateBookingStatus(id, status) {
  try {
    await api.patch(`/admin/bookings/${id}/status?status=${status}`);
    Utils.toast(`Booking ${status.toLowerCase()}`, 'success');
    await loadBookings();
    await loadDashboardStats();
    initAdminCalendar();
  } catch (err) {
    Utils.toast(err.message, 'error');
  }
}

function viewBookingDetails(id) {
  const b = allBookings.find(x => x.id === id);
  if (!b) return;
  lastViewedBooking = b;

  Utils.showModal('Booking Details', `
    <div class="booking-details">
      <div class="detail-row"><span>Reference</span><span>${b.bookingReference}</span></div>
      <div class="detail-row"><span>Customer</span><span>${b.customerName}</span></div>
      <div class="detail-row"><span>Phone</span><span>${b.customerPhone}</span></div>
      <div class="detail-row"><span>Email</span><span>${b.customerEmail}</span></div>
      <div class="detail-row"><span>Truck</span><span>${b.truckNumber}</span></div>
      <div class="detail-row"><span>Pickup</span><span>${Utils.formatDate(b.pickupDate)}</span></div>
      <div class="detail-row"><span>Return</span><span>${Utils.formatDate(b.returnDate)}</span></div>
      <div class="detail-row"><span>From</span><span>${b.pickupAddress}</span></div>
      <div class="detail-row"><span>To</span><span>${b.deliveryAddress}</span></div>
      <div class="detail-row"><span>Cargo</span><span>${b.cargoDescription || 'N/A'}</span></div>
      <div class="detail-row"><span>Weight</span><span>${b.weight ? b.weight + ' kg' : 'N/A'}</span></div>
      ${b.totalAmount ? `<div class="detail-row"><span>Revenue</span><span>${formatRevenue(b.totalAmount)}</span></div>` : ''}
      <div class="detail-row"><span>Status</span><span class="status-badge ${Utils.statusClass(b.status)}">${Utils.formatStatus(b.status)}</span></div>
    </div>
  `, [`<button class="btn btn-primary btn-sm" onclick="BookingPdf.download(lastViewedBooking)">Download PDF</button>`]);
}

async function loadTrucks() {
  try {
    const res = await api.get('/trucks');
    const trucks = res.data || [];
    const grid = document.getElementById('admin-trucks-grid');
    if (!grid) return;

    grid.innerHTML = trucks.map(t => `
      <div class="card">
        <h4>${t.truckNumber}</h4>
        <p style="color:var(--text-muted);font-size:0.9rem">${t.registration} · ${t.capacity}</p>
        <span class="status-badge ${Utils.statusClass(t.status)}">${Utils.formatStatus(t.status)}</span>
        <div style="margin-top:1rem;display:flex;gap:0.5rem;flex-wrap:wrap">
          <select class="form-control" style="width:auto" onchange="updateTruckStatus(${t.id}, this.value)">
            <option value="">Change Status</option>
            <option value="AVAILABLE">Available</option>
            <option value="BOOKED">Booked</option>
            <option value="MAINTENANCE">Maintenance</option>
          </select>
          <button class="btn btn-sm btn-outline" onclick="editTruck(${t.id})">Edit</button>
          <button class="btn btn-sm btn-outline" style="color:var(--danger);border-color:var(--danger)" onclick="deleteTruck(${t.id}, '${t.truckNumber}')">Delete</button>
        </div>
      </div>
    `).join('');
  } catch (err) {
    Utils.toast('Failed to load trucks', 'error');
  }
}

function showAddTruckModal() {
  Utils.showModal('Add New Truck', `
    <form id="add-truck-form">
      <div class="form-group"><label>Truck Number</label><input class="form-control" id="at-number" required placeholder="Truck 3"></div>
      <div class="form-group"><label>Registration</label><input class="form-control" id="at-reg" required placeholder="CA 000-000 GP"></div>
      <div class="form-group"><label>Capacity</label><input class="form-control" id="at-cap" required placeholder="34 Tonnes"></div>
      <div class="form-group"><label>Description</label><textarea class="form-control" id="at-desc"></textarea></div>
      <div class="form-group"><label>Image Path</label><input class="form-control" id="at-img" placeholder="images/truck3.svg"></div>
      <div class="form-group"><label>Status</label>
        <select class="form-control" id="at-status">
          <option value="AVAILABLE">Available</option>
          <option value="BOOKED">Booked</option>
          <option value="MAINTENANCE">Maintenance</option>
        </select>
      </div>
    </form>
  `, [`<button class="btn btn-primary" onclick="saveNewTruck()">Add Truck</button>`]);
}

async function saveNewTruck() {
  try {
    await api.post('/admin/trucks', {
      truckNumber: document.getElementById('at-number').value,
      registration: document.getElementById('at-reg').value,
      capacity: document.getElementById('at-cap').value,
      description: document.getElementById('at-desc').value,
      image: document.getElementById('at-img').value,
      status: document.getElementById('at-status').value
    });
    Utils.closeModal();
    Utils.toast('Truck added', 'success');
    await loadTrucks();
    await loadDashboardStats();
  } catch (err) {
    Utils.toast(err.message, 'error');
  }
}

async function deleteTruck(id, name) {
  if (!confirm(`Delete ${name}? This cannot be undone.`)) return;
  try {
    await api.delete(`/admin/trucks/${id}`);
    Utils.toast('Truck deleted', 'success');
    await loadTrucks();
    await loadDashboardStats();
  } catch (err) {
    Utils.toast(err.message, 'error');
  }
}

async function updateTruckStatus(id, status) {
  if (!status) return;
  try {
    const res = await api.get(`/trucks/${id}`);
    const truck = res.data;
    await api.put(`/admin/trucks/${id}`, { ...truck, status });
    Utils.toast('Truck status updated', 'success');
    await loadTrucks();
    await loadDashboardStats();
  } catch (err) {
    Utils.toast(err.message, 'error');
  }
}

function editTruck(id) {
  api.get(`/trucks/${id}`).then(res => {
    const t = res.data;
    Utils.showModal('Edit Truck', `
      <form id="edit-truck-form">
        <div class="form-group"><label>Truck Number</label><input class="form-control" id="et-number" value="${t.truckNumber}"></div>
        <div class="form-group"><label>Registration</label><input class="form-control" id="et-reg" value="${t.registration}"></div>
        <div class="form-group"><label>Capacity</label><input class="form-control" id="et-cap" value="${t.capacity}"></div>
        <div class="form-group"><label>Description</label><textarea class="form-control" id="et-desc">${t.description || ''}</textarea></div>
        <div class="form-group"><label>Image URL</label><input class="form-control" id="et-img" value="${t.image || ''}"></div>
      </form>
    `, [`<button class="btn btn-primary" onclick="saveTruckEdit(${id})">Save</button>`]);
  });
}

async function saveTruckEdit(id) {
  try {
    await api.put(`/admin/trucks/${id}`, {
      truckNumber: document.getElementById('et-number').value,
      registration: document.getElementById('et-reg').value,
      capacity: document.getElementById('et-cap').value,
      description: document.getElementById('et-desc').value,
      image: document.getElementById('et-img').value
    });
    Utils.closeModal();
    Utils.toast('Truck updated', 'success');
    await loadTrucks();
  } catch (err) {
    Utils.toast(err.message, 'error');
  }
}

async function loadCustomers() {
  try {
    const res = await api.get('/admin/customers');
    const customers = res.data || [];
    const tbody = document.getElementById('customers-tbody');
    if (!tbody) return;

    tbody.innerHTML = customers.map(c => `
      <tr>
        <td>${c.name}</td>
        <td>${c.phone}</td>
        <td>${c.email}</td>
        <td>${c.bookingCount}</td>
        <td><button class="btn btn-sm btn-outline" onclick="viewCustomerBookings(${c.id})">History</button></td>
      </tr>
    `).join('') || '<tr><td colspan="5" style="text-align:center">No customers</td></tr>';
  } catch (err) {
    Utils.toast('Failed to load customers', 'error');
  }
}

async function viewCustomerBookings(customerId) {
  try {
    const res = await api.get(`/admin/customers/${customerId}/bookings`);
    const bookings = res.data || [];
    Utils.showModal('Booking History', bookings.length ? bookings.map(b => `
      <div class="detail-row"><span>${b.bookingReference}</span><span>${Utils.formatDate(b.pickupDate)} — ${Utils.formatStatus(b.status)}</span></div>
    `).join('') : '<p>No bookings found</p>');
  } catch (err) {
    Utils.toast(err.message, 'error');
  }
}

function initAdminCalendar() {
  const container = document.getElementById('admin-calendar');
  if (!container) return;

  adminCalendar = new BookingCalendar('admin-calendar', {
    onDateSelect: (pickup) => {
      if (pickup) {
        const dayBookings = allBookings.filter(b =>
          pickup >= b.pickupDate && pickup <= b.returnDate
        );
        if (dayBookings.length) {
          viewBookingDetails(dayBookings[0].id);
        }
      }
    }
  });
  adminCalendar.loadAvailability();
}

document.getElementById('customer-search')?.addEventListener('input', async (e) => {
  const q = e.target.value;
  if (!q) { await loadCustomers(); return; }
  try {
    const res = await api.get(`/admin/customers?search=${encodeURIComponent(q)}`);
    const customers = res.data || [];
    const tbody = document.getElementById('customers-tbody');
    tbody.innerHTML = customers.map(c => `
      <tr><td>${c.name}</td><td>${c.phone}</td><td>${c.email}</td><td>${c.bookingCount}</td>
      <td><button class="btn btn-sm btn-outline" onclick="viewCustomerBookings(${c.id})">History</button></td></tr>
    `).join('');
  } catch (err) { Utils.toast(err.message, 'error'); }
});
