/**
 * METRON — Central de Administração Geral
 * Simplificada, Direta e 100% Focada em Custos Reais, Poupança e Ganhos Extras.
 */

document.addEventListener('DOMContentLoaded', async () => {
  if (!API.requireAuth()) return;
  renderNavbar('dashboard');

  setupModals();
  setupQuickForms();

  // Preencher data padrão de hoje no formulário de ganho extra
  const extraDateInput = document.getElementById('extraDate');
  if (extraDateInput) {
    extraDateInput.value = new Date().toISOString().split('T')[0];
  }

  await loadAllData();
});

let allCategories = [];
let allExpenses = [];
let allIncomes = [];
let currentUserProfile = null;

function isFixedExpense(e) {
  if (!e) return false;
  return e.isRecurring === true || e.recurring === true;
}

function getExpenseCategories() {
  return (allCategories || []).filter(c => c.type !== 'INCOME');
}

async function loadAllData() {
  try {
    // 1. Carregar perfil do usuário
    currentUserProfile = await API.getProfile().catch(() => API.getUser());

    // 2. Carregar Indicadores e Dados
    const [dailyLimit, categories, expenses, incomes] = await Promise.all([
      API.getDailyLimit().catch(() => null),
      API.getCategories('EXPENSE').catch(() => []),
      API.getExpenses().catch(() => []),
      API.getIncomes().catch(() => [])
    ]);

    allCategories = categories || [];
    allExpenses = expenses || [];
    allIncomes = incomes || [];

    // 3. Atualizar Dropdowns (Apenas categorias de CUSTO)
    populateCategoryDropdowns();

    // 4. Renderizar Hero Banner Executivo
    renderHeroBanner(currentUserProfile, dailyLimit);

    // 5. Renderizar Tabelas
    renderTables();

  } catch (err) {
    UI.showToast('Erro ao atualizar dados: ' + err.message, 'error');
  }
}

/* ==========================================================================
   Dropdowns de Categorias de CUSTO
   ========================================================================== */
function populateCategoryDropdowns() {
  const expenseCats = getExpenseCategories();

  // Dropdown de Contas Fixas
  const fixedSel = document.getElementById('fixedCategory');
  if (fixedSel) {
    fixedSel.innerHTML = '<option value="">Selecione a categoria...</option>';
    expenseCats.forEach(cat => {
      const opt = document.createElement('option');
      opt.value = cat.id;
      opt.textContent = cat.name;
      fixedSel.appendChild(opt);
    });
  }

  // Dropdown de Gastos Diários
  const dailySel = document.getElementById('dailyCategory');
  if (dailySel) {
    dailySel.innerHTML = '<option value="">Selecione a categoria...</option>';
    expenseCats.forEach(cat => {
      const opt = document.createElement('option');
      opt.value = cat.id;
      opt.textContent = cat.name;
      dailySel.appendChild(opt);
    });
  }

  // Dropdown do Modal de Edição de Despesa
  const modalSel = document.getElementById('expenseCategory');
  if (modalSel) {
    modalSel.innerHTML = '<option value="">Selecione a categoria...</option>';
    expenseCats.forEach(cat => {
      const opt = document.createElement('option');
      opt.value = cat.id;
      opt.textContent = cat.name;
      modalSel.appendChild(opt);
    });
  }
}

/* ==========================================================================
   1. Hero Banner: Visão Executiva do Mês
   ========================================================================== */
function renderHeroBanner(user, dailyLimit) {
  const salary = Number(user?.monthlyIncome || 0);
  const salaryDay = user?.salaryDay || 5;
  const savingsGoal = Number(user?.savingsGoalMonthly || 0);

  // Filtrar ganhos extras do mês atual
  const currentMonth = new Date().toISOString().slice(0, 7);
  const currentMonthIncomes = allIncomes.filter(i => !i.receiptDate || i.receiptDate.startsWith(currentMonth));
  const extraTotal = currentMonthIncomes.reduce((acc, curr) => acc + Number(curr.amount || 0), 0);

  // Filtrar contas fixas
  const fixedList = allExpenses.filter(e => isFixedExpense(e));
  const fixedTotal = fixedList.reduce((acc, curr) => acc + Number(curr.amount || 0), 0);

  // Sobra Livre = Salário + Ganhos Extras - Contas Fixas - Guardar no Mês
  const freeBalance = Math.max(0, salary + extraTotal - fixedTotal - savingsGoal);

  // Dias restantes até o próximo salário
  let daysLeft = 15;
  if (dailyLimit && dailyLimit.daysUntilNextSalary !== undefined) {
    daysLeft = dailyLimit.daysUntilNextSalary;
  } else {
    const today = new Date().getDate();
    daysLeft = salaryDay > today ? salaryDay - today : (30 - today + salaryDay);
  }
  if (daysLeft < 1) daysLeft = 1;

  // Limite diário seguro
  let dailySafe = 0;
  if (dailyLimit && Number(dailyLimit.recommendedDailyBudget || 0) > 0) {
    dailySafe = Number(dailyLimit.recommendedDailyBudget);
  } else if (freeBalance > 0 && daysLeft > 0) {
    dailySafe = freeBalance / daysLeft;
  }

  // Preencher elementos
  const salEl = document.getElementById('displaySalary');
  if (salEl) salEl.textContent = UI.formatCurrency(salary);

  const salDayEl = document.getElementById('displaySalaryDay');
  if (salDayEl) salDayEl.textContent = `Pagamento todo dia ${salaryDay}`;

  const extraEl = document.getElementById('displayExtraTotal');
  if (extraEl) extraEl.textContent = `+ ${UI.formatCurrency(extraTotal)}`;

  const extraCountEl = document.getElementById('displayExtraCount');
  if (extraCountEl) {
    extraCountEl.textContent = `${currentMonthIncomes.length} extra${currentMonthIncomes.length === 1 ? '' : 's'} no mês`;
  }

  const fixedEl = document.getElementById('displayFixedTotal');
  if (fixedEl) fixedEl.textContent = `- ${UI.formatCurrency(fixedTotal)}`;

  const fixedCountEl = document.getElementById('displayFixedCount');
  if (fixedCountEl) {
    fixedCountEl.textContent = `${fixedList.length} conta${fixedList.length === 1 ? '' : 's'} cadastrada${fixedList.length === 1 ? '' : 's'}`;
  }

  const savingsEl = document.getElementById('displaySavingsGoal');
  if (savingsEl) savingsEl.textContent = `- ${UI.formatCurrency(savingsGoal)}`;

  const dailyEl = document.getElementById('displayDailySafe');
  if (dailyEl) dailyEl.textContent = `${UI.formatCurrency(dailySafe)} / dia`;

  const daysLeftEl = document.getElementById('displayDailyDaysLeft');
  if (daysLeftEl) daysLeftEl.textContent = `Ritmo seguro (${daysLeft} dias restantes)`;

  // Pré-popular campos do modal de salário
  const cfgInc = document.getElementById('cfgMonthlyIncome');
  if (cfgInc) cfgInc.value = salary > 0 ? salary.toFixed(2) : '';
  const cfgSav = document.getElementById('cfgSavingsGoal');
  if (cfgSav) cfgSav.value = savingsGoal > 0 ? savingsGoal.toFixed(2) : '';
  const cfgDay = document.getElementById('cfgSalaryDay');
  if (cfgDay) cfgDay.value = salaryDay;
  const cfgBal = document.getElementById('cfgBankBalance');
  if (cfgBal && user?.currentBankBalance !== undefined && user?.currentBankBalance !== null) {
    cfgBal.value = Number(user.currentBankBalance).toFixed(2);
  }

  // Mensagem de boas-vindas
  const welcomeTitle = document.getElementById('welcomeUserTitle');
  if (welcomeTitle && user?.name) {
    welcomeTitle.textContent = `Administração Financeira — Olá, ${user.name.split(' ')[0]}`;
  }

  // Título com mês atual
  const periodTitle = document.getElementById('heroPeriodTitle');
  if (periodTitle) {
    const meses = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];
    const now = new Date();
    periodTitle.textContent = `Meu Planejamento — ${meses[now.getMonth()]} de ${now.getFullYear()}`;
  }
}

/* ==========================================================================
   2, 3 e 4. Renderização das Tabelas (Contas Fixas, Ganhos Extras e Gastos Diários)
   ========================================================================== */
function renderTables() {
  const currentMonth = new Date().toISOString().slice(0, 7);

  // 1. Contas Fixas
  const fixedList = allExpenses.filter(e => isFixedExpense(e));
  fixedList.sort((a, b) => new Date(b.expenseDate) - new Date(a.expenseDate));
  const fixedTotal = fixedList.reduce((acc, curr) => acc + Number(curr.amount || 0), 0);

  const fixedSecTotal = document.getElementById('fixedSectionTotal');
  if (fixedSecTotal) fixedSecTotal.textContent = `Total em Contas: ${UI.formatCurrency(fixedTotal)}`;

  const fixedBody = document.getElementById('fixedExpensesBody');
  if (fixedBody) {
    if (fixedList.length === 0) {
      fixedBody.innerHTML = `
        <tr>
          <td colspan="6" style="text-align:center; padding:2.5rem; color:var(--color-muted);">
            Nenhuma conta fixa cadastrada. Use o formulário acima para adicionar aluguel, internet, luz, parcelas, etc.
          </td>
        </tr>
      `;
    } else {
      fixedBody.innerHTML = fixedList.map(item => {
        const catName = item.category ? item.category.name : 'Geral';
        const catColor = (item.category && item.category.color) ? item.category.color : '#0d9488';
        const isPaid = item.paymentStatus === 'PAGO';

        let dayLabel = 'Mensal';
        if (item.expenseDate) {
          const parts = item.expenseDate.split('-');
          dayLabel = `Dia ${parseInt(parts[2], 10)}`;
        }

        return `
          <tr>
            <td style="font-weight:700; color:var(--color-navy);">${dayLabel}</td>
            <td>
              <div style="font-weight:700; color:var(--color-navy);">${item.description}</div>
              ${item.notes ? `<div style="font-size:0.82rem; color:var(--color-muted);">${item.notes}</div>` : ''}
            </td>
            <td>
              <span class="cat-tag" style="font-size:0.85rem;">
                <span class="cat-color-dot" style="background:${catColor};"></span>
                ${catName}
              </span>
            </td>
            <td class="tnum" style="text-align:right; font-weight:800; font-size:1.05rem; color:#b91c1c;">
              ${UI.formatCurrency(item.amount)}
            </td>
            <td style="text-align:center;">
              <button class="status-toggle-btn ${isPaid ? 'paid' : 'pending'}" onclick="togglePaymentStatus(${item.id})" title="Clique para alternar situação">
                ${isPaid ? '✓ Pago' : '○ Pendente'}
              </button>
            </td>
            <td style="text-align:right; white-space:nowrap;">
              <button class="btn btn-secondary btn-sm" onclick="editTransaction('expense', ${item.id})" style="padding:0.3rem 0.65rem; font-size:0.85rem;">
                Editar
              </button>
              <button class="btn btn-secondary btn-sm" onclick="deleteTransaction('expense', ${item.id})" style="padding:0.3rem 0.65rem; font-size:0.85rem; color:#dc2626; border-color:#fca5a5;">
                Excluir
              </button>
            </td>
          </tr>
        `;
      }).join('');
    }
  }

  // 2. Ganhos Extras no Mês
  const extraList = allIncomes.filter(i => !i.receiptDate || i.receiptDate.startsWith(currentMonth));
  extraList.sort((a, b) => new Date(b.receiptDate || '') - new Date(a.receiptDate || ''));
  const extraTotal = extraList.reduce((acc, curr) => acc + Number(curr.amount || 0), 0);

  const extraSecTotal = document.getElementById('extraSectionTotal');
  if (extraSecTotal) extraSecTotal.textContent = `Total de Extras: ${UI.formatCurrency(extraTotal)}`;

  const extraBody = document.getElementById('extraIncomeBody');
  if (extraBody) {
    if (extraList.length === 0) {
      extraBody.innerHTML = `
        <tr>
          <td colspan="5" style="text-align:center; padding:2.5rem; color:var(--color-muted);">
            Nenhum ganho extra registrado neste mês. Use o formulário acima se receber qualquer dinheiro além do salário.
          </td>
        </tr>
      `;
    } else {
      extraBody.innerHTML = extraList.map(item => {
        const isReceived = item.receiptStatus === 'RECEBIDO';
        const badgeBg = isReceived ? '#ecfdf5' : '#fffbeb';
        const badgeColor = isReceived ? '#047857' : '#b45309';
        const statusText = isReceived ? '✓ Recebido' : '○ A Receber';

        return `
          <tr>
            <td style="white-space:nowrap; font-weight:600;">${UI.formatDate(item.receiptDate)}</td>
            <td>
              <div style="font-weight:700; color:var(--color-navy);">${item.description}</div>
            </td>
            <td>
              <span style="display:inline-block; font-size:0.82rem; font-weight:700; background:${badgeBg}; color:${badgeColor}; padding:0.2rem 0.65rem; border-radius:var(--radius-full);">
                ${statusText}
              </span>
            </td>
            <td class="tnum" style="text-align:right; font-weight:800; font-size:1.05rem; color:#047857;">
              + ${UI.formatCurrency(item.amount)}
            </td>
            <td style="text-align:right; white-space:nowrap;">
              <button class="btn btn-secondary btn-sm" onclick="deleteExtraIncome(${item.id})" style="padding:0.3rem 0.65rem; font-size:0.85rem; color:#dc2626; border-color:#fca5a5;">
                Excluir
              </button>
            </td>
          </tr>
        `;
      }).join('');
    }
  }

  // 3. Gastos do Dia a Dia
  const dailyList = allExpenses.filter(e => !isFixedExpense(e));
  dailyList.sort((a, b) => new Date(b.expenseDate) - new Date(a.expenseDate));
  const dailyTotal = dailyList.reduce((acc, curr) => acc + Number(curr.amount || 0), 0);

  const dailySecTotal = document.getElementById('dailySectionTotal');
  if (dailySecTotal) dailySecTotal.textContent = `Total Gasto no Dia a Dia: ${UI.formatCurrency(dailyTotal)}`;

  const dailyBody = document.getElementById('dailyExpensesBody');
  if (dailyBody) {
    if (dailyList.length === 0) {
      dailyBody.innerHTML = `
        <tr>
          <td colspan="6" style="text-align:center; padding:2.5rem; color:var(--color-muted);">
            Nenhum gasto do dia a dia anotado este mês.
          </td>
        </tr>
      `;
    } else {
      dailyBody.innerHTML = dailyList.map(item => {
        const catName = item.category ? item.category.name : 'Geral';
        const catColor = (item.category && item.category.color) ? item.category.color : '#0d9488';

        return `
          <tr>
            <td style="white-space:nowrap; font-weight:600;">${UI.formatDate(item.expenseDate)}</td>
            <td>
              <div style="font-weight:700; color:var(--color-navy);">${item.description}</div>
              ${item.notes ? `<div style="font-size:0.82rem; color:var(--color-muted);">${item.notes}</div>` : ''}
            </td>
            <td>
              <span class="cat-tag" style="font-size:0.85rem;">
                <span class="cat-color-dot" style="background:${catColor};"></span>
                ${catName}
              </span>
            </td>
            <td style="font-size:0.88rem; color:var(--color-muted); font-weight:500;">
              ${item.paymentMethod || 'PIX'}
            </td>
            <td class="tnum" style="text-align:right; font-weight:800; font-size:1.05rem; color:#b91c1c;">
              - ${UI.formatCurrency(item.amount)}
            </td>
            <td style="text-align:right; white-space:nowrap;">
              <button class="btn btn-secondary btn-sm" onclick="editTransaction('expense', ${item.id})" style="padding:0.3rem 0.65rem; font-size:0.85rem;">
                Editar
              </button>
              <button class="btn btn-secondary btn-sm" onclick="deleteTransaction('expense', ${item.id})" style="padding:0.3rem 0.65rem; font-size:0.85rem; color:#dc2626; border-color:#fca5a5;">
                Excluir
              </button>
            </td>
          </tr>
        `;
      }).join('');
    }
  }
}

/* ==========================================================================
   Formulários Rápidos Diretos na Tela
   ========================================================================== */
function setupQuickForms() {
  // 1. Formulário de Conta Fixa
  const fixedForm = document.getElementById('quickFixedForm');
  if (fixedForm) {
    fixedForm.addEventListener('submit', async (e) => {
      e.preventDefault();

      const desc = document.getElementById('fixedDesc').value.trim();
      const amount = parseFloat(document.getElementById('fixedAmount').value);
      const catId = document.getElementById('fixedCategory').value || null;
      const day = parseInt(document.getElementById('fixedDay').value, 10);
      const duration = document.getElementById('fixedDuration').value;

      if (!desc || isNaN(amount) || amount <= 0 || !catId || isNaN(day)) {
        UI.showToast('Por favor, preencha todos os campos da conta fixa.', 'error');
        return;
      }

      const now = new Date();
      const validDay = Math.min(Math.max(1, day), 31);
      const dateStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(validDay).padStart(2, '0')}`;

      let notes = 'Conta Fixa Mensal';
      if (duration !== 'forever') {
        notes = `Conta parcelada por ${duration} meses`;
      }

      const payload = {
        description: desc,
        amount: amount,
        expenseDate: dateStr,
        categoryId: catId,
        paymentMethod: 'BOLETO',
        paymentStatus: 'PENDENTE',
        isRecurring: true,
        recurring: true,
        notes: notes
      };

      try {
        await API.createExpense(payload);
        UI.showToast('Conta fixa cadastrada com sucesso!', 'success');
        fixedForm.reset();
        await loadAllData();
      } catch (err) {
        UI.showToast(err.message, 'error');
      }
    });
  }

  // 2. Formulário de Ganhos Extras
  const extraForm = document.getElementById('quickExtraForm');
  if (extraForm) {
    extraForm.addEventListener('submit', async (e) => {
      e.preventDefault();

      const desc = document.getElementById('extraDesc').value.trim();
      const amount = parseFloat(document.getElementById('extraAmount').value);
      const dateStr = document.getElementById('extraDate').value || new Date().toISOString().split('T')[0];
      const status = document.getElementById('extraStatus').value;

      if (!desc || isNaN(amount) || amount <= 0) {
        UI.showToast('Por favor, preencha a origem e o valor do ganho extra.', 'error');
        return;
      }

      // ReceiptStatus enum aceita: RECEBIDO ou PREVISTO
      const receiptSt = (status === 'PREVISTO') ? 'PREVISTO' : 'RECEBIDO';
      const payload = {
        description: desc,
        amount: amount,
        receiptDate: dateStr,
        incomeType: 'RENDA_EXTRA',
        receiptStatus: receiptSt,
        notes: ''
      };

      try {
        await API.createIncome(payload);
        UI.showToast('Ganho extra registrado com sucesso!', 'success');
        extraForm.reset();
        document.getElementById('extraDate').value = new Date().toISOString().split('T')[0];
        await loadAllData();
      } catch (err) {
        UI.showToast(err.message, 'error');
      }
    });
  }

  // 3. Formulário de Gasto Diário Direto
  const dailyForm = document.getElementById('quickDailyForm');
  if (dailyForm) {
    dailyForm.addEventListener('submit', async (e) => {
      e.preventDefault();

      const desc = document.getElementById('dailyDesc').value.trim();
      const amount = parseFloat(document.getElementById('dailyAmount').value);
      const catId = document.getElementById('dailyCategory').value || null;
      const method = document.getElementById('dailyMethod').value;

      if (!desc || isNaN(amount) || amount <= 0 || !catId) {
        UI.showToast('Por favor, preencha a descrição, valor e categoria.', 'error');
        return;
      }

      const today = new Date().toISOString().split('T')[0];
      const payload = {
        description: desc,
        amount: amount,
        expenseDate: today,
        categoryId: catId,
        paymentMethod: method,
        paymentStatus: 'PAGO',
        isRecurring: false,
        recurring: false,
        notes: ''
      };

      try {
        await API.createExpense(payload);
        UI.showToast('Gasto registrado com sucesso!', 'success');
        dailyForm.reset();
        await loadAllData();
      } catch (err) {
        UI.showToast(err.message, 'error');
      }
    });
  }
}

/* ==========================================================================
   Modais
   ========================================================================== */
function setupModals() {
  // Modal de Salário e Poupança
  const salModal = document.getElementById('salaryModal');
  const btnOpenSal = document.getElementById('btnOpenSalaryModal');
  const btnHeroOpenSal = document.getElementById('btnHeroOpenSalaryModal');
  const btnCloseSal = document.getElementById('btnCloseSalaryModal');
  const btnCancelSal = document.getElementById('btnCancelSalary');
  const salForm = document.getElementById('salaryForm');

  const openSal = () => salModal.classList.add('active');
  const closeSal = () => salModal.classList.remove('active');

  if (btnOpenSal) btnOpenSal.addEventListener('click', openSal);
  if (btnHeroOpenSal) btnHeroOpenSal.addEventListener('click', openSal);
  if (btnCloseSal) btnCloseSal.addEventListener('click', closeSal);
  if (btnCancelSal) btnCancelSal.addEventListener('click', closeSal);

  if (salForm) {
    salForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const user = API.getUser() || {};
      const payload = {
        name: user.name || 'Usuário METRON',
        monthlyIncome: parseFloat(document.getElementById('cfgMonthlyIncome').value) || 0,
        savingsGoalMonthly: parseFloat(document.getElementById('cfgSavingsGoal').value) || 0,
        salaryDay: parseInt(document.getElementById('cfgSalaryDay').value, 10) || 5,
        currentBankBalance: parseFloat(document.getElementById('cfgBankBalance').value) || 0
      };

      try {
        const updated = await API.updateProfile(payload);
        API.setSession(API.getToken(), updated);
        UI.showToast('Salário e poupança atualizados com sucesso!', 'success');
        closeSal();
        await loadAllData();
      } catch (err) {
        UI.showToast(err.message, 'error');
      }
    });
  }

  // Modal de Edição de Despesa
  const expModal = document.getElementById('expenseModal');
  const btnCloseExp = document.getElementById('btnCloseExpenseModal');
  const btnCancelExp = document.getElementById('btnCancelExpense');
  const expForm = document.getElementById('expenseForm');

  if (btnCloseExp) btnCloseExp.addEventListener('click', () => expModal.classList.remove('active'));
  if (btnCancelExp) btnCancelExp.addEventListener('click', () => expModal.classList.remove('active'));

  if (expForm) {
    expForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const id = document.getElementById('expenseId').value;
      const isRec = document.getElementById('expenseRecurring').checked;
      const payload = {
        description: document.getElementById('expenseDesc').value.trim(),
        amount: parseFloat(document.getElementById('expenseAmount').value),
        expenseDate: document.getElementById('expenseDate').value,
        categoryId: document.getElementById('expenseCategory').value || null,
        paymentMethod: document.getElementById('expenseMethod').value,
        paymentStatus: document.getElementById('expenseStatus').value,
        isRecurring: isRec,
        recurring: isRec,
        notes: ''
      };

      try {
        await API.updateExpense(id, payload);
        UI.showToast('Registro atualizado com sucesso!', 'success');
        expModal.classList.remove('active');
        await loadAllData();
      } catch (err) {
        UI.showToast(err.message, 'error');
      }
    });
  }
}

/* ==========================================================================
   Ações Globais
   ========================================================================== */
window.togglePaymentStatus = async function(id) {
  const item = allExpenses.find(e => e.id === id);
  if (!item) return;

  const newStatus = item.paymentStatus === 'PAGO' ? 'PENDENTE' : 'PAGO';
  const isRec = isFixedExpense(item);
  const payload = {
    description: item.description,
    amount: item.amount,
    expenseDate: item.expenseDate,
    categoryId: item.category ? item.category.id : null,
    paymentMethod: item.paymentMethod || 'BOLETO',
    paymentStatus: newStatus,
    isRecurring: isRec,
    recurring: isRec,
    notes: item.notes || ''
  };

  try {
    await API.updateExpense(id, payload);
    UI.showToast(`Conta marcada como ${newStatus === 'PAGO' ? 'Paga' : 'Pendente'}!`, 'success');
    await loadAllData();
  } catch (err) {
    UI.showToast('Erro ao atualizar: ' + err.message, 'error');
  }
};

window.deleteExtraIncome = async function(id) {
  if (!confirm('Deseja realmente excluir este ganho extra?')) return;

  try {
    await API.deleteIncome(id);
    UI.showToast('Ganho extra excluído com sucesso!', 'success');
    await loadAllData();
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
};

window.editTransaction = function(kind, id) {
  const item = allExpenses.find(e => e.id === id);
  if (!item) return;

  document.getElementById('expenseId').value = item.id;
  document.getElementById('expenseDesc').value = item.description;
  document.getElementById('expenseAmount').value = item.amount;
  document.getElementById('expenseDate').value = item.expenseDate;
  document.getElementById('expenseCategory').value = item.category ? item.category.id : '';
  document.getElementById('expenseMethod').value = item.paymentMethod || 'PIX';
  document.getElementById('expenseStatus').value = item.paymentStatus || 'PAGO';
  document.getElementById('expenseRecurring').checked = isFixedExpense(item);
  document.getElementById('expenseModalTitle').textContent = 'Editar Gasto / Conta';

  document.getElementById('expenseModal').classList.add('active');
};

window.deleteTransaction = async function(kind, id) {
  if (!confirm('Deseja realmente excluir este registro? Esta ação não pode ser desfeita.')) return;

  try {
    await API.deleteExpense(id);
    UI.showToast('Registro excluído com sucesso!', 'success');
    await loadAllData();
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
};
