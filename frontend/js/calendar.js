class BookingCalendar {
  constructor(containerId, options = {}) {
    this.container = document.getElementById(containerId);
    this.options = options;
    this.currentDate = new Date();
    this.selectedPickup = null;
    this.selectedReturn = null;
    this.availability = {};
    this.truckId = options.truckId || null;
    this.onDateSelect = options.onDateSelect || (() => {});
  }

  setTruckId(truckId) {
    this.truckId = truckId;
    this.selectedPickup = null;
    this.selectedReturn = null;
    this.render();
    this.loadAvailability();
  }

  async loadAvailability() {
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth() + 1;

    // Show calendar immediately while loading
    if (!this.container?.innerHTML) {
      this.render();
    }

    try {
      const res = await api.get(`/bookings/availability?year=${year}&month=${month}`);
      const data = res.data || [];
      this.offline = false;

      if (this.truckId) {
        const truckData = data.find(t => t.truckId === this.truckId);
        this.availability = truckData ? truckData.dateStatus : {};
      } else {
        // Merge: date is booked only if ALL trucks are unavailable
        this.availability = {};
        const allDates = new Set();
        data.forEach(t => Object.keys(t.dateStatus || {}).forEach(d => allDates.add(d)));

        allDates.forEach(date => {
          const statuses = data.map(t => t.dateStatus[date] || 'available');
          if (statuses.every(s => s === 'booked' || s === 'maintenance')) {
            this.availability[date] = 'booked';
          } else if (statuses.some(s => s === 'pending')) {
            this.availability[date] = 'pending';
          } else if (statuses.every(s => s === 'maintenance')) {
            this.availability[date] = 'maintenance';
          } else {
            this.availability[date] = 'available';
          }
        });
      }
    } catch (err) {
      console.warn('Availability API unavailable — showing all dates as available:', err.message);
      this.offline = true;
      this.availability = {};
    }

    this.render();
  }

  render() {
    if (!this.container) return;

    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();
    const monthNames = ['January','February','March','April','May','June',
      'July','August','September','October','November','December'];
    const dayNames = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'];

    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    let html = `
      <div class="calendar-header">
        <button class="btn btn-sm btn-outline" id="cal-prev">&larr;</button>
        <h3>${monthNames[month]} ${year}</h3>
        <button class="btn btn-sm btn-outline" id="cal-next">&rarr;</button>
      </div>
      <div class="calendar-grid">
        ${dayNames.map(d => `<div class="calendar-day-name">${d}</div>`).join('')}
    `;

    for (let i = 0; i < firstDay; i++) {
      html += '<div class="calendar-day empty"></div>';
    }

    for (let day = 1; day <= daysInMonth; day++) {
      const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
      const dateObj = new Date(year, month, day);
      const status = this.availability[dateStr] || 'available';
      let classes = ['calendar-day', status];

      if (dateObj < today) classes.push('past');
      if (this.selectedPickup === dateStr) classes.push('selected');
      if (this.selectedReturn === dateStr) classes.push('selected');
      if (this.selectedPickup && this.selectedReturn && dateStr > this.selectedPickup && dateStr < this.selectedReturn) {
        classes.push('in-range');
      }

      const clickable = status !== 'booked' && status !== 'maintenance' && dateObj >= today;
      html += `<div class="${classes.join(' ')}" data-date="${dateStr}" ${clickable ? '' : 'style="cursor:not-allowed"'}>${day}</div>`;
    }

    html += `</div>
      <div class="calendar-legend">
        <div class="legend-item"><span class="legend-dot" style="background:var(--success)"></span> Available</div>
        <div class="legend-item"><span class="legend-dot" style="background:var(--danger)"></span> Booked</div>
        <div class="legend-item"><span class="legend-dot" style="background:var(--warning)"></span> Pending</div>
      </div>
      ${this.offline ? '<p style="font-size:0.8rem;color:var(--warning);margin-top:0.75rem;text-align:center">Live availability unavailable — start the backend for real-time booking data.</p>' : ''}`;

    this.container.innerHTML = html;

    this.container.querySelector('#cal-prev')?.addEventListener('click', () => {
      this.currentDate.setMonth(this.currentDate.getMonth() - 1);
      this.loadAvailability();
    });

    this.container.querySelector('#cal-next')?.addEventListener('click', () => {
      this.currentDate.setMonth(this.currentDate.getMonth() + 1);
      this.loadAvailability();
    });

    this.container.querySelectorAll('.calendar-day[data-date]').forEach(el => {
      el.addEventListener('click', () => this.handleDateClick(el.dataset.date, el));
    });
  }

  handleDateClick(dateStr, el) {
    if (el.classList.contains('booked') || el.classList.contains('maintenance') || el.classList.contains('past')) {
      Utils.toast('This date is not available', 'error');
      return;
    }

    if (!this.selectedPickup || (this.selectedPickup && this.selectedReturn)) {
      this.selectedPickup = dateStr;
      this.selectedReturn = null;
    } else if (dateStr > this.selectedPickup) {
      // Check range for conflicts
      if (this.hasConflictInRange(this.selectedPickup, dateStr)) {
        Utils.toast('Selected range includes unavailable dates', 'error');
        return;
      }
      this.selectedReturn = dateStr;
    } else {
      this.selectedPickup = dateStr;
      this.selectedReturn = null;
    }

    this.render();
    this.onDateSelect(this.selectedPickup, this.selectedReturn);
  }

  hasConflictInRange(start, end) {
    const startDate = new Date(start);
    const endDate = new Date(end);
    for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
      const ds = d.toISOString().split('T')[0];
      const status = this.availability[ds];
      if (status === 'booked' || status === 'maintenance') return true;
    }
    return false;
  }

  getSelectedDates() {
    return { pickup: this.selectedPickup, return: this.selectedReturn };
  }
}
