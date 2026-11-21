/**
 * METRON API Client & Utility Library
 * "Não desperdice seu tempo e seu dinheiro."
 */

const API = {
  baseUrl: '/api',

  getToken() {
    return localStorage.getItem('metron_token');
  },

  getUser() {
    const u = localStorage.getItem('metron_user');
    return u ? JSON.parse(u) : null;
  },

  setSession(token, user) {
    localStorage.setItem('metron_token', token);
    localStorage.setItem('metron_user', JSON.stringify(user));
  },

  clearSession() {
    localStorage.removeItem('metron_token');
    localStorage.removeItem('metron_user');
  },

  requireAuth() {
    if (!this.getToken()) {
      window.location.href = '/login.html';
      return false;
    }
    return true;
  },

  async request(endpoint, options = {}) {
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers
    };

    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    try {
      const response = await fetch(`${this.baseUrl}${endpoint}`, {
        ...options,
        headers
      });

      if (response.status === 401) {
        if (endpoint === '/auth/login') {
          let errData = {};
          try { errData = await response.json(); } catch(e) {}
          throw new Error(errData.message || 'E-mail ou senha incorretos. Verifique seus dados.');
        }
        this.clearSession();
        window.location.href = '/login.html';
        throw new Error('Sessão expirada. Faça login novamente.');
      }

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        const message = errorData.message || `Erro ${response.status}: ${response.statusText}`;
        throw new Error(message);
      }

      // Check if response is PDF binary
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/pdf')) {
        return await response.blob();
      }

      if (response.status === 204) return null;
      return await response.json();
    } catch (err) {
      console.error(`API Error on ${endpoint}:`, err);
      throw err;
    }
  },

  // Auth
  login(email, password) {
    return this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    });
  },

  register(data) {
    return this.request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  resetPassword(email, newPassword) {
    return this.request('/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify({ email, newPassword })
    });
  },

  getProfile() {
    return this.request('/users/me');
  },

  updateProfile(data) {
    return this.request('/users/me', {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  // Dashboard & Engine
  getDashboardSummary(period = 'mes') {
    return this.request(`/dashboard/summary?period=${encodeURIComponent(period)}`);
  },

  getDailyLimit() {
    return this.request('/daily-limit');
  },

  simulatePurchase(data) {
    return this.request('/simulate/purchase', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // Expenses
  getExpenses(params = {}) {
    const q = new URLSearchParams(params).toString();
    return this.request(`/expenses?${q}`);
  },

  createExpense(data) {
    return this.request('/expenses', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  updateExpense(id, data) {
    return this.request(`/expenses/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  deleteExpense(id) {
    return this.request(`/expenses/${id}`, {
      method: 'DELETE'
    });
  },

  // Incomes
  getIncomes(params = {}) {
    const q = new URLSearchParams(params).toString();
    return this.request(`/incomes?${q}`);
  },

  createIncome(data) {
    return this.request('/incomes', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  updateIncome(id, data) {
    return this.request(`/incomes/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  deleteIncome(id) {
    return this.request(`/incomes/${id}`, {
      method: 'DELETE'
    });
  },

  // Categories
  getCategories(type) {
    const query = type ? `?type=${encodeURIComponent(type)}` : '';
    return this.request(`/categories${query}`);
  },

  createCategory(data) {
    return this.request('/categories', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  updateCategory(id, data) {
    return this.request(`/categories/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  deleteCategory(id) {
    return this.request(`/categories/${id}`, {
      method: 'DELETE'
    });
  },

  // Budget
  getBudget(year, month) {
    return this.request(`/budgets/${year}/${month}`);
  },

  saveBudget(data) {
    return this.request('/budgets', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // Goals
  getGoals() {
    return this.request('/goals');
  },

  createGoal(data) {
    return this.request('/goals', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  updateGoal(id, data) {
    return this.request(`/goals/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  depositGoal(id, amount) {
    return this.request(`/goals/${id}/deposit`, {
      method: 'POST',
      body: JSON.stringify({ amount })
    });
  },

  deleteGoal(id) {
    return this.request(`/goals/${id}`, {
      method: 'DELETE'
    });
  },

  // Reports
  getWeeklyEvaluation() {
    return this.request('/reports/weekly');
  },

  getMonthlyReport(year, month) {
    return this.request(`/reports/monthly/${year}/${month}`);
  },

  async downloadMonthlyReportPdf(year, month) {
    const blob = await this.request(`/reports/monthly/${year}/${month}/pdf`);
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `metron-relatorio-${month}-${year}.pdf`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    a.remove();
  },

  // Assistant
  askAssistant(question) {
    return this.request('/assistant/ask', {
      method: 'POST',
      body: JSON.stringify({ question })
    });
  },

  // Patterns
  getPatterns() {
    return this.request('/patterns/insights');
  },

  // Alerts
  getAlerts() {
    return this.request('/alerts');
  },

  getUnreadAlertsCount() {
    return this.request('/alerts/unread-count');
  },

  markAlertAsRead(id) {
    return this.request(`/alerts/${id}/read`, { method: 'PATCH' });
  },

  markAllAlertsAsRead() {
    return this.request('/alerts/read-all', { method: 'PATCH' });
  }
};

// UI Helpers
const UI = {
  formatCurrency(value) {
    if (value === null || value === undefined) return 'R$ 0,00';
    const num = Number(value);
    return num.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  },

  formatDate(dateStr) {
    if (!dateStr) return '-';
    const parts = dateStr.split('-');
    if (parts.length === 3) {
      return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }
    return dateStr;
  },

  formatPercentage(value) {
    if (value === null || value === undefined) return '0%';
    return Number(value).toFixed(1).replace('.', ',') + '%';
  },

  showToast(message, type = 'info') {
    let container = document.getElementById('toastContainer');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toastContainer';
      container.className = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  }
};
