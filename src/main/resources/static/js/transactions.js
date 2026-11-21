/**
 * METRON Transactions Controller
 * Gerencia Extrato Geral, Despesas, Receitas e Categorias em uma unica pagina.
 */

let categoriesCache = [];
let allExpensesList = [];
let allIncomesList = [];

document.addEventListener('DOMContentLoaded', async () => {
  if (!API.requireAuth()) return;
  renderNavbar('transactions');

  setupTabs();
  setupModals();

  await loadCategories();
  await loadAllData();

  // Verifica se veio com parametro na URL (?tab=expenses etc)
  const urlParams = new URLSearchParams(window.location.search);
  const tabParam = urlParams.get('tab');
  if (tabParam) {
    const tabMap = {
      'all': 'tab-all',
      'expenses': 'tab-expenses',
      'incomes': 'tab-incomes',
      'categories': 'tab-categories'
    };
    const targetTab = tabMap[tabParam];
    if (targetTab) {
      switchTab(targetTab);
    }
  }
});

/* ==========================================================================
   Controle das Abas Internas
   ========================================================================== */
function setupTabs() {
  const tabs = document.querySelectorAll('.tab-btn');
  tabs.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetId = btn.getAttribute('data-tab');
      switchTab(targetId);
    });
  });
}

function switchTab(targetId) {
  document.querySelectorAll('.tab-btn').forEach(b => {
    const isActive = b.getAttribute('data-tab') === targetId;
    b.classList.toggle('active', isActive);
    b.setAttribute('aria-selected', isActive);
  });

  document.querySelectorAll('.tab-panel').forEach(panel => {
    panel.classList.toggle('active', panel.id === targetId);
  });
}

/* ==========================================================================
   Carga de Dados
   ========================================================================== */
async function loadCategories() {
  try {
    categoriesCache = await API.getCategories();
    renderCategoriesTable();

    // Popula selects de categorias
    const selFilter = document.getElementById('expCategoryFilter');
    const selModal = document.getElementById('expenseCategory');

    selFilter.innerHTML = '<option value="">Todas as Categorias</option>';
    selModal.innerHTML = '<option value="">Sem categoria especifica</option>';

    categoriesCache.forEach(c => {
      const opt1 = document.createElement('option');
      opt1.value = c.id;
      opt1.textContent = c.name;
      selFilter.appendChild(opt1);

      const opt2 = document.createElement('option');
      opt2.value = c.id;
      opt2.textContent = c.name;
      selModal.appendChild(opt2);
    });
  } catch (err) {
    console.error('Erro ao carregar categorias:', err);
  }
}

async function loadAllData() {
  try {
    const [exp, inc] = await Promise.all([
      API.getExpenses(),
      API.getIncomes()
    ]);
    allExpensesList = exp || [];
    allIncomesList = inc || [];

    renderAllTransactions();
    renderExpensesTable();
    renderIncomesTable();
  } catch (err) {
    UI.showToast('Erro ao carregar movimentacoes: ' + err.message, 'error');
  }
}

/* ==========================================================================
   Renderizacao das Tabelas
   ========================================================================== */
function renderAllTransactions() {
  const tbody = document.getElementById('allTableBody');
  const search = (document.getElementById('allSearch').value || '').trim().toLowerCase();
  const start = document.getElementById('allStartDate').value;
  const end = document.getElementById('allEndDate').value;

  let combined = [];

  allExpensesList.forEach(e => {
    combined.push({
      id: e.id,
      type: 'EXPENSE',
      typeLabel: 'Despesa',
      date: e.expenseDate,
      description: e.description,
      category: e.category ? e.category.name : 'Geral',
      amount: -Number(e.amount),
      status: e.paymentStatus
    });
  });

  allIncomesList.forEach(i => {
    combined.push({
      id: i.id,
      type: 'INCOME',
      typeLabel: 'Receita',
      date: i.receiptDate,
      description: i.description,
      category: formatIncomeType(i.incomeType),
      amount: Number(i.amount),
      status: i.receiptStatus
    });
  });

  // Filtros
  if (search) {
    combined = combined.filter(t => t.description.toLowerCase().includes(search));
  }
  if (start) {
    combined = combined.filter(t => t.date >= start);
  }
  if (end) {
    combined = combined.filter(t => t.date <= end);
  }

  // Ordenar decrescente por data
  combined.sort((a, b) => new Date(b.date) - new Date(a.date));

  if (combined.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="6">
          <div class="empty-state">
            <div class="empty-title">Nenhuma movimentacao encontrada</div>
            <div class="empty-desc">Nao encontramos registros para o periodo ou busca selecionada.</div>
          </div>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = combined.map(item => {
    const isExpense = item.type === 'EXPENSE';
    const sign = isExpense ? '- ' : '+ ';
    const valColor = isExpense ? '#b91c1c' : '#047857';

    return `
      <tr>
        <td><strong>${UI.formatDate(item.date)}</strong></td>
        <td>
          <span class="badge-status ${isExpense ? 'VERMELHO' : 'VERDE'}">
            ${item.typeLabel}
          </span>
        </td>
        <td><strong style="color:var(--color-navy);">${item.description}</strong></td>
        <td>${item.category}</td>
        <td style="text-align:right; font-weight:700; color:${valColor}; font-size:1.05rem;">
          ${sign}${UI.formatCurrency(Math.abs(item.amount))}
        </td>
        <td style="text-align:right;">
          <div class="btn-group" style="justify-content:flex-end;">
            <button class="btn btn-secondary btn-sm" onclick="${isExpense ? `editExpense(${item.id})` : `editIncome(${item.id})`}">Editar</button>
            <button class="btn btn-danger btn-sm" onclick="${isExpense ? `deleteExpense(${item.id})` : `deleteIncome(${item.id})`}">Excluir</button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function renderExpensesTable() {
  const tbody = document.getElementById('expensesTableBody');
  const search = (document.getElementById('expSearch').value || '').trim().toLowerCase();
  const catFilter = document.getElementById('expCategoryFilter').value;

  let list = allExpensesList;
  if (search) {
    list = list.filter(e => e.description.toLowerCase().includes(search));
  }
  if (catFilter) {
    list = list.filter(e => e.category && e.category.id == catFilter);
  }

  if (!list || list.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7">
          <div class="empty-state">
            <div class="empty-title">Nenhuma despesa cadastrada</div>
            <div class="empty-desc">Clique no botao "+ Nova Despesa" acima para registrar seus gastos.</div>
          </div>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = list.map(e => {
    const catName = e.category ? e.category.name : 'Geral';
    const isPaid = e.paymentStatus === 'PAGO';

    return `
      <tr>
        <td><strong>${UI.formatDate(e.expenseDate)}</strong></td>
        <td>
          <strong style="color:var(--color-navy);">${e.description}</strong>
          ${e.recurring ? '<span style="display:block; font-size:0.8rem; color:var(--color-muted);">Despesa Recorrente</span>' : ''}
        </td>
        <td>${catName}</td>
        <td>${formatPaymentMethod(e.paymentMethod)}</td>
        <td>
          <span class="badge-status ${isPaid ? 'VERDE' : 'AMARELO'}">
            ${isPaid ? 'Pago' : 'Pendente'}
          </span>
        </td>
        <td style="text-align:right; font-weight:700; color:#b91c1c; font-size:1.05rem;">
          ${UI.formatCurrency(e.amount)}
        </td>
        <td style="text-align:right;">
          <div class="btn-group" style="justify-content:flex-end;">
            <button class="btn btn-secondary btn-sm" onclick="editExpense(${e.id})">Editar</button>
            <button class="btn btn-danger btn-sm" onclick="deleteExpense(${e.id})">Excluir</button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function renderIncomesTable() {
  const tbody = document.getElementById('incomesTableBody');
  const search = (document.getElementById('incSearch').value || '').trim().toLowerCase();

  let list = allIncomesList;
  if (search) {
    list = list.filter(i => i.description.toLowerCase().includes(search));
  }

  if (!list || list.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="6">
          <div class="empty-state">
            <div class="empty-title">Nenhuma receita cadastrada</div>
            <div class="empty-desc">Clique no botao "+ Nova Receita" acima para registrar seu salario ou rendas extras.</div>
          </div>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = list.map(i => {
    const isReceived = i.receiptStatus === 'RECEBIDO';

    return `
      <tr>
        <td><strong>${UI.formatDate(i.receiptDate)}</strong></td>
        <td><strong style="color:var(--color-navy);">${i.description}</strong></td>
        <td>${formatIncomeType(i.incomeType)}</td>
        <td>
          <span class="badge-status ${isReceived ? 'VERDE' : 'AMARELO'}">
            ${isReceived ? 'Recebido' : 'Previsto'}
          </span>
        </td>
        <td style="text-align:right; font-weight:700; color:#047857; font-size:1.05rem;">
          + ${UI.formatCurrency(i.amount)}
        </td>
        <td style="text-align:right;">
          <div class="btn-group" style="justify-content:flex-end;">
            <button class="btn btn-secondary btn-sm" onclick="editIncome(${i.id})">Editar</button>
            <button class="btn btn-danger btn-sm" onclick="deleteIncome(${i.id})">Excluir</button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function renderCategoriesTable() {
  const tbody = document.getElementById('categoriesTableBody');
  if (!categoriesCache || categoriesCache.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">Nenhuma categoria.</td></tr>';
    return;
  }

  tbody.innerHTML = categoriesCache.map(c => `
    <tr>
      <td><strong>${c.name}</strong></td>
      <td>${c.type === 'EXPENSE' ? 'Despesa' : c.type === 'INCOME' ? 'Receita' : 'Ambos'}</td>
      <td>${c.budgetLimit > 0 ? UI.formatCurrency(c.budgetLimit) : '<span style="color:var(--color-text-light);">Sem limite fixado</span>'}</td>
      <td>
        <span class="badge-status ${c.systemDefault ? 'AZUL' : 'AMARELO'}">
          ${c.systemDefault ? 'Padrao' : 'Personalizada'}
        </span>
      </td>
      <td style="text-align:right;">
        ${!c.systemDefault ? `
          <div class="btn-group" style="justify-content:flex-end;">
            <button class="btn btn-secondary btn-sm" onclick="editCategory(${c.id})">Editar</button>
            <button class="btn btn-danger btn-sm" onclick="deleteCategory(${c.id})">Excluir</button>
          </div>
        ` : '<span style="color:var(--color-text-light); font-size:0.85rem;">Fixa do Sistema</span>'}
      </td>
    </tr>
  `).join('');
}

/* ==========================================================================
   Modais e Formularios
   ========================================================================== */
function setupModals() {
  // Despesa
  const expModal = document.getElementById('expenseModal');
  document.getElementById('btnOpenExpenseModal').addEventListener('click', () => {
    document.getElementById('expenseModalTitle').textContent = 'Cadastrar Despesa';
    document.getElementById('expenseForm').reset();
    document.getElementById('expenseId').value = '';
    document.getElementById('expenseDate').value = new Date().toISOString().split('T')[0];
    expModal.classList.add('active');
  });

  const closeExp = () => expModal.classList.remove('active');
  document.getElementById('btnCloseExpenseModal').addEventListener('click', closeExp);
  document.getElementById('btnCancelExpense').addEventListener('click', closeExp);

  document.getElementById('expenseForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('expenseId').value;
    const catVal = document.getElementById('expenseCategory').value;

    const payload = {
      description: document.getElementById('expenseDesc').value.trim(),
      amount: parseFloat(document.getElementById('expenseAmount').value),
      expenseDate: document.getElementById('expenseDate').value,
      categoryId: catVal ? parseInt(catVal) : null,
      paymentMethod: document.getElementById('expenseMethod').value,
      paymentStatus: document.getElementById('expenseStatus').value,
      recurring: document.getElementById('expenseRecurring').value === 'true',
      notes: document.getElementById('expenseNotes').value.trim()
    };

    try {
      if (id) {
        await API.updateExpense(id, payload);
        UI.showToast('Despesa atualizada com sucesso!', 'success');
      } else {
        await API.createExpense(payload);
        UI.showToast('Despesa cadastrada com sucesso!', 'success');
      }
      closeExp();
      await loadAllData();
    } catch (err) {
      UI.showToast(err.message, 'error');
    }
  });

  // Receita
  const incModal = document.getElementById('incomeModal');
  document.getElementById('btnOpenIncomeModal').addEventListener('click', () => {
    document.getElementById('incomeModalTitle').textContent = 'Cadastrar Receita';
    document.getElementById('incomeForm').reset();
    document.getElementById('incomeId').value = '';
    document.getElementById('incomeDate').value = new Date().toISOString().split('T')[0];
    incModal.classList.add('active');
  });

  const closeInc = () => incModal.classList.remove('active');
  document.getElementById('btnCloseIncomeModal').addEventListener('click', closeInc);
  document.getElementById('btnCancelIncome').addEventListener('click', closeInc);

  document.getElementById('incomeForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('incomeId').value;

    const payload = {
      description: document.getElementById('incomeDesc').value.trim(),
      amount: parseFloat(document.getElementById('incomeAmount').value),
      receiptDate: document.getElementById('incomeDate').value,
      incomeType: document.getElementById('incomeType').value,
      receiptStatus: document.getElementById('incomeStatus').value,
      notes: document.getElementById('incomeNotes').value.trim()
    };

    try {
      if (id) {
        await API.updateIncome(id, payload);
        UI.showToast('Receita atualizada com sucesso!', 'success');
      } else {
        await API.createIncome(payload);
        UI.showToast('Receita cadastrada com sucesso!', 'success');
      }
      closeInc();
      await loadAllData();
    } catch (err) {
      UI.showToast(err.message, 'error');
    }
  });

  // Categoria
  const catModal = document.getElementById('categoryModal');
  document.getElementById('btnNewCategory').addEventListener('click', () => {
    document.getElementById('catModalTitle').textContent = 'Nova Categoria';
    document.getElementById('categoryForm').reset();
    document.getElementById('catId').value = '';
    catModal.classList.add('active');
  });

  const closeCat = () => catModal.classList.remove('active');
  document.getElementById('btnCloseCatModal').addEventListener('click', closeCat);
  document.getElementById('btnCancelCat').addEventListener('click', closeCat);

  document.getElementById('categoryForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('catId').value;
    const limitVal = parseFloat(document.getElementById('catLimit').value);

    const payload = {
      name: document.getElementById('catName').value.trim(),
      type: document.getElementById('catType').value,
      budgetLimit: !isNaN(limitVal) && limitVal > 0 ? limitVal : 0
    };

    try {
      if (id) {
        await API.updateCategory(id, payload);
        UI.showToast('Categoria atualizada com sucesso!', 'success');
      } else {
        await API.createCategory(payload);
        UI.showToast('Categoria criada com sucesso!', 'success');
      }
      closeCat();
      await loadCategories();
    } catch (err) {
      UI.showToast(err.message, 'error');
    }
  });

  // Filtros listeners
  document.getElementById('btnFilterAll').addEventListener('click', renderAllTransactions);
  document.getElementById('btnClearFilterAll').addEventListener('click', () => {
    document.getElementById('allSearch').value = '';
    document.getElementById('allStartDate').value = '';
    document.getElementById('allEndDate').value = '';
    renderAllTransactions();
  });

  document.getElementById('btnFilterExpenses').addEventListener('click', renderExpensesTable);
  document.getElementById('btnClearFilterExpenses').addEventListener('click', () => {
    document.getElementById('expSearch').value = '';
    document.getElementById('expCategoryFilter').value = '';
    renderExpensesTable();
  });

  document.getElementById('btnFilterIncomes').addEventListener('click', renderIncomesTable);
  document.getElementById('btnClearFilterIncomes').addEventListener('click', () => {
    document.getElementById('incSearch').value = '';
    renderIncomesTable();
  });
}

/* ==========================================================================
   Acoes Globais (Editar / Excluir)
   ========================================================================== */
window.editExpense = function(id) {
  const item = allExpensesList.find(e => e.id === id);
  if (!item) return;

  document.getElementById('expenseModalTitle').textContent = 'Editar Despesa';
  document.getElementById('expenseId').value = item.id;
  document.getElementById('expenseDesc').value = item.description;
  document.getElementById('expenseAmount').value = item.amount;
  document.getElementById('expenseDate').value = item.expenseDate;
  document.getElementById('expenseCategory').value = item.category ? item.category.id : '';
  document.getElementById('expenseMethod').value = item.paymentMethod;
  document.getElementById('expenseStatus').value = item.paymentStatus;
  document.getElementById('expenseRecurring').value = item.recurring ? 'true' : 'false';
  document.getElementById('expenseNotes').value = item.notes || '';

  document.getElementById('expenseModal').classList.add('active');
};

window.deleteExpense = async function(id) {
  if (!confirm('Deseja realmente excluir esta despesa? Esta acao nao pode ser desfeita.')) return;
  try {
    await API.deleteExpense(id);
    UI.showToast('Despesa excluida com sucesso!', 'success');
    await loadAllData();
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
};

window.editIncome = function(id) {
  const item = allIncomesList.find(i => i.id === id);
  if (!item) return;

  document.getElementById('incomeModalTitle').textContent = 'Editar Receita';
  document.getElementById('incomeId').value = item.id;
  document.getElementById('incomeDesc').value = item.description;
  document.getElementById('incomeAmount').value = item.amount;
  document.getElementById('incomeDate').value = item.receiptDate;
  document.getElementById('incomeType').value = item.incomeType;
  document.getElementById('incomeStatus').value = item.receiptStatus;
  document.getElementById('incomeNotes').value = item.notes || '';

  document.getElementById('incomeModal').classList.add('active');
};

window.deleteIncome = async function(id) {
  if (!confirm('Deseja realmente excluir esta receita?')) return;
  try {
    await API.deleteIncome(id);
    UI.showToast('Receita excluida com sucesso!', 'success');
    await loadAllData();
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
};

window.editCategory = function(id) {
  const cat = categoriesCache.find(c => c.id === id);
  if (!cat) return;

  document.getElementById('catModalTitle').textContent = 'Editar Categoria';
  document.getElementById('catId').value = cat.id;
  document.getElementById('catName').value = cat.name;
  document.getElementById('catType').value = cat.type;
  document.getElementById('catLimit').value = cat.budgetLimit || '';

  document.getElementById('categoryModal').classList.add('active');
};

window.deleteCategory = async function(id) {
  if (!confirm('Deseja realmente excluir esta categoria personalizada?')) return;
  try {
    await API.deleteCategory(id);
    UI.showToast('Categoria excluida com sucesso!', 'success');
    await loadCategories();
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
};

/* ==========================================================================
   Formatadores Simples
   ========================================================================== */
function formatPaymentMethod(m) {
  const map = {
    'PIX': 'PIX',
    'CARTAO_CREDITO': 'Cartao de Credito',
    'CARTAO_DEBITO': 'Cartao de Debito',
    'BOLETO': 'Boleto',
    'DINHEIRO': 'Dinheiro',
    'TRANSFERENCIA': 'TED / Transferencia'
  };
  return map[m] || m;
}

function formatIncomeType(t) {
  const map = {
    'SALARIO': 'Salario',
    'FREELANCE': 'Freelance',
    'RENDA_EXTRA': 'Renda Extra',
    'COMISSAO': 'Comissao',
    'BENEFICIO': 'Beneficio',
    'INVESTIMENTO': 'Investimento',
    'OUTROS': 'Outros'
  };
  return map[t] || t;
}
