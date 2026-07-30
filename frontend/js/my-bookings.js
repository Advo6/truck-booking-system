let cachedBookings = [];

document.addEventListener('DOMContentLoaded', async () => {
  updateNavAuth();

  if (Auth.isLoggedIn() && !Auth.isAdmin()) {
    document.getElementById('guest-lookup').style.display = 'none';
    document.getElementById('logged-in-section').style.display = 'block';
    await loadMyBookings();
  } else if (Auth.isLoggedIn() && Auth.isAdmin()) {
    window.location.href = 'dashboard.html';
  }
});

function updateNavAuth() {
  const link = document.getElementById('nav-auth-link');
  if (Auth.isLoggedIn()) {
    link.textContent = 'Logout';
    link.href = '#';
    link.onclick = (e) => { e.preventDefault(); Auth.logout(); };
  }
}

document.getElementById('lookup-form')?.addEventListener('submit', async (e) => {
  e.preventDefault();
  const email = document.getElementById('lookup-email').value.trim();
  try {
    Utils.showLoader();
    const res = await api.get(`/bookings/history?email=${encodeURIComponent(email)}`);
    renderBookings(res.data || []);
  } catch (err) {
    Utils.toast(err.message || 'No bookings found', 'error');
    renderBookings([]);
  } finally {
    Utils.hideLoader();
  }
});

async function loadMyBookings() {
  try {
    Utils.showLoader();
    const res = await api.get('/bookings/my');
    const bookings = res.data || [];
    if (bookings.length) {
      document.getElementById('user-email').textContent = bookings[0].customerEmail;
    }
    renderBookings(bookings);
  } catch (err) {
    Utils.toast(err.message, 'error');
    renderBookings([]);
  } finally {
    Utils.hideLoader();
  }
}

function renderBookings(bookings) {
  cachedBookings = bookings;
  const list = document.getElementById('bookings-list');
  const empty = document.getElementById('no-bookings');

  if (!bookings.length) {
    list.innerHTML = '';
    empty.style.display = 'block';
    return;
  }

  empty.style.display = 'none';
  list.innerHTML = bookings.map((b, i) => `
    <div class="card" style="margin-bottom:1rem">
      <div style="display:flex;justify-content:space-between;align-items:flex-start;flex-wrap:wrap;gap:1rem">
        <div>
          <h3 style="color:var(--primary)">${b.bookingReference}</h3>
          <p style="color:var(--text-muted);font-size:0.9rem">${b.truckNumber} · ${Utils.formatDate(b.pickupDate)} → ${Utils.formatDate(b.returnDate)}</p>
        </div>
        <span class="status-badge ${Utils.statusClass(b.status)}">${Utils.formatStatus(b.status)}</span>
      </div>
      <div class="booking-details" style="margin:1rem 0">
        <div class="detail-row"><span>Pickup</span><span>${b.pickupAddress}</span></div>
        <div class="detail-row"><span>Delivery</span><span>${b.deliveryAddress}</span></div>
        ${b.totalAmount ? `<div class="detail-row"><span>Estimated Total</span><span>R ${Number(b.totalAmount).toLocaleString('en-ZA', { minimumFractionDigits: 2 })}</span></div>` : ''}
      </div>
      <div style="display:flex;gap:0.5rem;flex-wrap:wrap">
        <button class="btn btn-sm btn-primary" onclick="BookingPdf.download(cachedBookings[${i}])">Download PDF</button>
        <button class="btn btn-sm btn-outline" onclick="Utils.printElement('booking-print-${i}')">Print</button>
      </div>
      <div id="booking-print-${i}" style="display:none">
        <h3>${b.bookingReference}</h3>
        <div class="detail-row"><span>Truck</span><span>${b.truckNumber}</span></div>
        <div class="detail-row"><span>Pickup</span><span>${Utils.formatDate(b.pickupDate)}</span></div>
        <div class="detail-row"><span>Return</span><span>${Utils.formatDate(b.returnDate)}</span></div>
        <div class="detail-row"><span>Status</span><span>${Utils.formatStatus(b.status)}</span></div>
      </div>
    </div>
  `).join('');
}
