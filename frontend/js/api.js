class ApiClient {
  constructor(baseUrl) {
    this.baseUrl = baseUrl;
  }

  getToken() {
    return localStorage.getItem('token');
  }

  async request(endpoint, options = {}) {
    const headers = { 'Content-Type': 'application/json', ...options.headers };
    const token = this.getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const response = await fetch(`${this.baseUrl}${endpoint}`, { ...options, headers });
    const data = await response.json();

    if (!response.ok) {
      const message = data.message || (data.data && typeof data.data === 'object'
        ? Object.values(data.data).join(', ')
        : 'Request failed');
      throw new Error(message);
    }

    return data;
  }

  get(endpoint) { return this.request(endpoint); }

  post(endpoint, body) {
    return this.request(endpoint, { method: 'POST', body: JSON.stringify(body) });
  }

  put(endpoint, body) {
    return this.request(endpoint, { method: 'PUT', body: JSON.stringify(body) });
  }

  patch(endpoint) {
    return this.request(endpoint, { method: 'PATCH' });
  }

  delete(endpoint) {
    return this.request(endpoint, { method: 'DELETE' });
  }
}

const api = new ApiClient(API_BASE_URL);
