const Auth = {
  saveSession(data) {
    localStorage.setItem('token', data.token);
    localStorage.setItem('username', data.username);
    localStorage.setItem('role', data.role);
    if (data.userId) localStorage.setItem('userId', data.userId);
    if (data.customerId) localStorage.setItem('customerId', data.customerId);
  },

  clearSession() {
    ['token', 'username', 'role', 'userId', 'customerId'].forEach(k => localStorage.removeItem(k));
  },

  isLoggedIn() { return !!localStorage.getItem('token'); },

  isAdmin() { return localStorage.getItem('role') === 'ADMIN'; },

  requireAdmin() {
    if (!this.isLoggedIn() || !this.isAdmin()) {
      window.location.href = 'login.html';
      return false;
    }
    return true;
  },

  logout() {
    this.clearSession();
    window.location.href = 'index.html';
  }
};
