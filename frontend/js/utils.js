const Utils = {
  showLoader() {
    let overlay = document.getElementById('loader-overlay');
    if (!overlay) {
      overlay = document.createElement('div');
      overlay.id = 'loader-overlay';
      overlay.className = 'loader-overlay';
      overlay.innerHTML = '<div class="spinner"></div>';
      document.body.appendChild(overlay);
    }
    overlay.style.display = 'flex';
  },

  hideLoader() {
    const overlay = document.getElementById('loader-overlay');
    if (overlay) overlay.style.display = 'none';
  },

  toast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
      container = document.createElement('div');
      container.className = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  },

  showModal(title, content, actions = []) {
    let overlay = document.getElementById('modal-overlay');
    if (!overlay) {
      overlay = document.createElement('div');
      overlay.id = 'modal-overlay';
      overlay.className = 'modal-overlay';
      document.body.appendChild(overlay);
    }

    overlay.innerHTML = `
      <div class="modal">
        <div class="modal-header">
          <h3>${title}</h3>
          <button class="modal-close" onclick="Utils.closeModal()">&times;</button>
        </div>
        <div class="modal-body">${content}</div>
        ${actions.length ? `<div class="modal-actions" style="display:flex;gap:0.5rem;margin-top:1.5rem;justify-content:flex-end">${actions.join('')}</div>` : ''}
      </div>`;

    overlay.classList.add('active');
    overlay.addEventListener('click', (e) => { if (e.target === overlay) Utils.closeModal(); });
  },

  closeModal() {
    const overlay = document.getElementById('modal-overlay');
    if (overlay) overlay.classList.remove('active');
  },

  formatDate(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr + 'T00:00:00');
    return d.toLocaleDateString('en-ZA', { day: 'numeric', month: 'long', year: 'numeric' });
  },

  formatStatus(status) {
    const map = {
      AVAILABLE: 'Available', BOOKED: 'Booked', MAINTENANCE: 'Maintenance',
      PENDING: 'Pending Approval', APPROVED: 'Approved', REJECTED: 'Rejected',
      CANCELLED: 'Cancelled', COMPLETED: 'Completed'
    };
    return map[status] || status;
  },

  statusClass(status) {
    const map = {
      AVAILABLE: 'status-available', BOOKED: 'status-booked', MAINTENANCE: 'status-maintenance',
      PENDING: 'status-pending', APPROVED: 'status-available', REJECTED: 'status-booked',
      CANCELLED: 'status-maintenance', COMPLETED: 'status-available'
    };
    return map[status] || 'status-pending';
  },

  animateCounters() {
    document.querySelectorAll('[data-count]').forEach(el => {
      const target = parseInt(el.dataset.count, 10);
      const duration = 2000;
      const start = performance.now();

      const update = (now) => {
        const progress = Math.min((now - start) / duration, 1);
        el.textContent = Math.floor(progress * target);
        if (progress < 1) requestAnimationFrame(update);
        else el.textContent = target;
      };
      requestAnimationFrame(update);
    });
  },

  initScrollAnimations() {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('visible');
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.1 });

    document.querySelectorAll('.scroll-animate').forEach(el => observer.observe(el));
  },

  initRipple() {
    document.addEventListener('click', (e) => {
      const btn = e.target.closest('.btn');
      if (!btn) return;
      const ripple = document.createElement('span');
      ripple.className = 'ripple';
      const rect = btn.getBoundingClientRect();
      const size = Math.max(rect.width, rect.height);
      ripple.style.width = ripple.style.height = size + 'px';
      ripple.style.left = (e.clientX - rect.left - size / 2) + 'px';
      ripple.style.top = (e.clientY - rect.top - size / 2) + 'px';
      btn.appendChild(ripple);
      setTimeout(() => ripple.remove(), 600);
    });
  },

  initTheme() {
    const saved = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', saved);

    document.querySelectorAll('.theme-toggle').forEach(btn => {
      btn.textContent = saved === 'dark' ? '☀️' : '🌙';
      btn.addEventListener('click', () => {
        const current = document.documentElement.getAttribute('data-theme');
        const next = current === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', next);
        localStorage.setItem('theme', next);
        btn.textContent = next === 'dark' ? '☀️' : '🌙';
      });
    });
  },

  initBackToTop() {
    const btn = document.querySelector('.fab-top');
    if (!btn) return;
    window.addEventListener('scroll', () => {
      btn.classList.toggle('visible', window.scrollY > 400);
    });
    btn.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));
  },

  initNavbar() {
    const navbar = document.querySelector('.navbar');
    if (navbar) {
      window.addEventListener('scroll', () => navbar.classList.toggle('scrolled', window.scrollY > 50));
    }

    const hamburger = document.querySelector('.hamburger');
    const navLinks = document.querySelector('.nav-links');
    if (hamburger && navLinks) {
      hamburger.addEventListener('click', () => navLinks.classList.toggle('open'));
    }

    const currentPage = window.location.pathname.split('/').pop() || 'index.html';
    document.querySelectorAll('.nav-links a').forEach(link => {
      if (link.getAttribute('href') === currentPage) link.classList.add('active');
    });
  },

  printElement(elementId) {
    const content = document.getElementById(elementId);
    if (!content) return;
    const win = window.open('', '_blank');
    win.document.write(`<html><head><title>Booking Confirmation</title>
      <style>body{font-family:Poppins,sans-serif;padding:2rem;color:#0B3C5D}
      h2{color:#0B3C5D} .detail-row{display:flex;justify-content:space-between;padding:0.5rem 0;border-bottom:1px solid #eee}</style></head><body>${content.innerHTML}</body></html>`);
    win.document.close();
    win.print();
  }
};

document.addEventListener('DOMContentLoaded', () => {
  Utils.initTheme();
  Utils.initNavbar();
  Utils.initRipple();
  Utils.initScrollAnimations();
  Utils.initBackToTop();
});
