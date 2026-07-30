let calendar;
let trucks = [];
let lastBooking = null;

document.addEventListener('DOMContentLoaded', async () => {
  initCalendar();
  initForm();
  await loadTrucks();
});

async function loadTrucks() {
  const offlineNotice = document.getElementById('booking-offline-notice');

  try {
    Utils.showLoader();
    const res = await api.get('/trucks');
    trucks = res.data?.length ? res.data : FALLBACK_TRUCKS;
    renderTruckSelection();
  } catch (err) {
    console.warn('Trucks API unavailable, using local data:', err.message);
    trucks = FALLBACK_TRUCKS;
    renderTruckSelection();
    if (offlineNotice) offlineNotice.style.display = 'block';
  } finally {
    Utils.hideLoader();
  }
}

function renderTruckSelection() {
  const container = document.getElementById('truck-selection');
  if (!container) return;

  container.innerHTML = trucks.map(t => `
    <label class="card truck-option" style="cursor:pointer;display:flex;align-items:center;gap:1rem;padding:1rem">
      <input type="radio" name="truckId" value="${t.id}">
      <div>
        <strong>${t.truckNumber}</strong> — ${t.registration}
        <span class="status-badge ${Utils.statusClass(t.status)}" style="margin-left:0.5rem">${Utils.formatStatus(t.status)}</span>
        <p style="font-size:0.85rem;color:var(--text-muted);margin-top:0.25rem">${t.capacity}</p>
      </div>
    </label>
  `).join('');

  container.querySelectorAll('input[name="truckId"]').forEach(radio => {
    radio.addEventListener('change', (e) => {
      calendar.setTruckId(parseInt(e.target.value, 10));
    });
  });
}

function initCalendar() {
  calendar = new BookingCalendar('booking-calendar', {
    onDateSelect: (pickup, returnDate) => {
      if (pickup) document.getElementById('pickupDate').value = pickup;
      if (returnDate) document.getElementById('returnDate').value = returnDate;
    }
  });
  calendar.loadAvailability();
}

function initForm() {
  const form = document.getElementById('booking-form');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const pickupDate = document.getElementById('pickupDate').value;
    const returnDate = document.getElementById('returnDate').value;

    if (!pickupDate || !returnDate) {
      Utils.toast('Please select pickup and return dates on the calendar', 'error');
      return;
    }

    if (new Date(pickupDate) < new Date(new Date().toDateString())) {
      Utils.toast('Pickup date cannot be in the past', 'error');
      return;
    }

    if (returnDate <= pickupDate) {
      Utils.toast('Return date must be after pickup date', 'error');
      return;
    }

    const truckRadio = document.querySelector('input[name="truckId"]:checked');

    const payload = {
      fullName: document.getElementById('fullName').value.trim(),
      phone: document.getElementById('phone').value.trim(),
      email: document.getElementById('email').value.trim(),
      pickupAddress: document.getElementById('pickupAddress').value.trim(),
      deliveryAddress: document.getElementById('deliveryAddress').value.trim(),
      pickupDate,
      returnDate,
      cargoDescription: document.getElementById('cargoDescription').value.trim(),
      weight: parseFloat(document.getElementById('weight').value) || null,
      specialInstructions: document.getElementById('specialInstructions').value.trim(),
      truckId: truckRadio ? parseInt(truckRadio.value, 10) : null
    };

    try {
      Utils.showLoader();
      const res = await api.post('/bookings', payload);
      showConfirmation(res.data);
      form.reset();
      calendar.selectedPickup = null;
      calendar.selectedReturn = null;
      calendar.loadAvailability();
    } catch (err) {
      Utils.toast(err.message || 'Could not submit booking. Is the backend running?', 'error');
    } finally {
      Utils.hideLoader();
    }
  });
}

function showConfirmation(booking) {
  lastBooking = booking;
  const section = document.getElementById('confirmation-section');
  const details = document.getElementById('confirmation-details');

  details.innerHTML = `
    <div class="reference-number">${booking.bookingReference}</div>
    <div class="booking-details" id="print-area">
      <div class="detail-row"><span>Customer</span><span>${booking.customerName}</span></div>
      <div class="detail-row"><span>Truck</span><span>${booking.truckNumber} (${booking.truckRegistration})</span></div>
      <div class="detail-row"><span>Pickup Date</span><span>${Utils.formatDate(booking.pickupDate)}</span></div>
      <div class="detail-row"><span>Return Date</span><span>${Utils.formatDate(booking.returnDate)}</span></div>
      <div class="detail-row"><span>Pickup Address</span><span>${booking.pickupAddress}</span></div>
      <div class="detail-row"><span>Delivery Address</span><span>${booking.deliveryAddress}</span></div>
      ${booking.totalAmount ? `<div class="detail-row"><span>Estimated Total</span><span>R ${Number(booking.totalAmount).toLocaleString('en-ZA', { minimumFractionDigits: 2 })}</span></div>` : ''}
      <div class="detail-row"><span>Status</span><span class="status-badge ${Utils.statusClass(booking.status)}">${Utils.formatStatus(booking.status)}</span></div>
    </div>
    <div style="display:flex;gap:1rem;justify-content:center;margin-top:1.5rem;flex-wrap:wrap">
      <button class="btn btn-primary" onclick="BookingPdf.download(lastBooking)">Download PDF</button>
      <button class="btn btn-outline" onclick="Utils.printElement('print-area')">Print Booking</button>
      <a href="my-bookings.html" class="btn btn-outline">View My Bookings</a>
      <button class="btn btn-ghost" onclick="document.getElementById('confirmation-section').style.display='none'">Close</button>
    </div>
  `;

  section.style.display = 'block';
  section.scrollIntoView({ behavior: 'smooth' });
  Utils.toast('Booking submitted successfully!', 'success');
}
