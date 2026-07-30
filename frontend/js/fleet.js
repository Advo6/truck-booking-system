document.addEventListener('DOMContentLoaded', async () => {
  const offlineNotice = document.getElementById('fleet-offline-notice');

  try {
    const res = await api.get('/trucks');
    const trucks = res.data?.length ? res.data : FALLBACK_TRUCKS;
    renderFleet(trucks);
  } catch (err) {
    console.warn('Fleet API unavailable, using local data:', err.message);
    if (offlineNotice) offlineNotice.style.display = 'block';
    renderFleet(FALLBACK_TRUCKS);
  }
});

function renderFleet(trucks) {
  const grid = document.getElementById('fleet-grid');
  const loading = document.getElementById('fleet-loading');
  if (!grid) return;

  if (loading) loading.style.display = 'none';

  if (!trucks || trucks.length === 0) {
    grid.innerHTML = '<p style="text-align:center;color:var(--text-muted);grid-column:1/-1">No trucks available at the moment.</p>';
    return;
  }

  grid.innerHTML = trucks.map((t, i) => `
    <div class="card fleet-card scroll-animate visible">
      <img src="${TruckImages.src(i, t.image)}" alt="${t.truckNumber}" ${TruckImages.onerrorAttr(i, t.image)}>
      <h3>${t.truckNumber}</h3>
      <p style="color:var(--text-muted);margin-bottom:0.5rem"><strong>Registration:</strong> ${t.registration}</p>
      <p style="color:var(--text-muted);margin-bottom:0.5rem"><strong>Capacity:</strong> ${t.capacity}</p>
      <p style="margin-bottom:1rem">${t.description || ''}</p>
      <span class="status-badge ${Utils.statusClass(t.status)}">${Utils.formatStatus(t.status)}</span>
      <a href="booking.html?truck=${t.id}" class="btn btn-primary btn-sm" style="margin-top:1rem;display:inline-flex">Book This Truck</a>
    </div>
  `).join('');

  Utils.initScrollAnimations();
}
