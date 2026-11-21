/**
 * METRON Navigation Bar
 * Exatamente 3 Páginas Principais:
 * 1. Administração Geral (/dashboard.html)
 * 2. Metas e Relatórios (/reports.html)
 * 3. Assistente Financeiro (/assistant.html)
 */

function renderNavbar(activePage = '') {
  const user = API.getUser();
  const userName = user ? user.name.split(' ')[0] : 'Minha Conta';

  const navHtml = `
    <nav class="metron-navbar" aria-label="Navegação Principal METRON">
      <div class="brand-wrapper">
        <a href="/dashboard.html" class="brand-link" title="METRON — Gestão Financeira">
          <img src="/assets/logo-white.svg" alt="METRON" class="brand-logo" />
        </a>
        <span class="brand-slogan">Não desperdice seu tempo e seu dinheiro.</span>
      </div>

      <button class="mobile-nav-toggle" id="mobileNavToggle" aria-label="Abrir menu de navegação" aria-expanded="false">
        <span class="hamburger-bar"></span>
        <span class="hamburger-bar"></span>
        <span class="hamburger-bar"></span>
      </button>

      <ul class="nav-links" id="navLinks">
        <li>
          <a href="/dashboard.html" class="nav-link ${activePage === 'dashboard' ? 'active' : ''}">
            Administração
          </a>
        </li>
        <li>
          <a href="/reports.html" class="nav-link ${activePage === 'reports' ? 'active' : ''}">
            Metas e Relatórios
          </a>
        </li>
        <li>
          <a href="/assistant.html" class="nav-link ${activePage === 'assistant' ? 'active' : ''}">
            Assistente
          </a>
        </li>
      </ul>

      <div class="nav-user">
        <a href="/settings.html" class="user-badge ${activePage === 'settings' ? 'active' : ''}" title="Configurações e Segurança">
          ${userName}
        </a>
        <button class="btn-logout" id="btnLogout" title="Encerrar sessão com segurança">Sair</button>
      </div>
    </nav>
  `;

  const container = document.getElementById('navbarContainer');
  if (container) {
    container.innerHTML = navHtml;
  }

  // Mobile menu toggle
  const toggleBtn = document.getElementById('mobileNavToggle');
  const navLinks = document.getElementById('navLinks');
  if (toggleBtn && navLinks) {
    toggleBtn.addEventListener('click', () => {
      const isOpen = navLinks.classList.toggle('open');
      toggleBtn.setAttribute('aria-expanded', isOpen);
    });
  }

  // Logout handler
  const btnLogout = document.getElementById('btnLogout');
  if (btnLogout) {
    btnLogout.addEventListener('click', () => {
      if (confirm('Deseja realmente sair da sua conta METRON?')) {
        API.clearSession();
        window.location.href = '/login.html';
      }
    });
  }
}
