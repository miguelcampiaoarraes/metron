/**
 * METRON Planning & Purchase Simulator Controller
 * Simplicidade, organizacao e clareza nas decisoes financeiras.
 */

let lastSimulated = null;

document.addEventListener('DOMContentLoaded', async () => {
  if (!API.requireAuth()) return;
  renderNavbar('planning');

  setupTabs();

  // Configura seletor de mes padrao para o mes corrente
  const picker = document.getElementById('budgetMonthPicker');
  const now = new Date();
  picker.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;

  picker.addEventListener('change', () => loadBudget());

  // Salvar orcamento
  document.getElementById('budgetForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    await saveBudget();
  });

  // Simulador de compras
  setupSimulator();

  await loadBudget();

  // Se URL contem ?tab=simulator, abre direto
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.get('tab') === 'simulator') {
    switchTab('tab-simulator');
  }
});

/* ==========================================================================
   Controle das Abas
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
   Orcamento Mensal
   ========================================================================== */
async function loadBudget() {
  const pickerVal = document.getElementById('budgetMonthPicker').value;
  if (!pickerVal) return;

  const [yearStr, monthStr] = pickerVal.split('-');
  const year = parseInt(yearStr);
  const month = parseInt(monthStr);

  try {
    const b = await API.getBudget(year, month);
    populateBudgetForm(b);
    updateBudgetMetrics(b);
  } catch (err) {
    UI.showToast('Erro ao carregar orcamento: ' + err.message, 'error');
  }
}

function populateBudgetForm(b) {
  document.getElementById('budgetExpectedIncome').value = b.expectedIncome || '';
  document.getElementById('budgetExpectedExtra').value = b.expectedExtraIncome || '0.00';
  document.getElementById('budgetFixedExpenses').value = b.fixedExpensesBudget || '';
  document.getElementById('budgetVariableExpenses').value = b.variableExpensesBudget || '';
  document.getElementById('budgetSavingsTarget').value = b.savingsTarget || '0.00';
  document.getElementById('budgetSalaryDate').value = b.salaryExpectedDate || '';
}

function updateBudgetMetrics(b) {
  document.getElementById('dispTotalExpected').textContent = UI.formatCurrency(b.totalExpectedIncome);
  document.getElementById('dispTotalBudgeted').textContent = UI.formatCurrency(b.totalBudgetedExpenses);
  document.getElementById('dispDailyLimit').textContent = UI.formatCurrency(b.recommendedDailyLimit) + ' / dia';
  document.getElementById('dispWeeklyLimitSub').textContent = `Ou ${UI.formatCurrency(b.recommendedWeeklyLimit)} por semana`;

  // Barras de acompanhamento
  const expRealized = Number(b.realizedExpenses || 0);
  const expTotal = Number(b.totalBudgetedExpenses || 0);
  document.getElementById('txtExpenseComparison').textContent = `${UI.formatCurrency(expRealized)} / ${UI.formatCurrency(expTotal)}`;
  const expPct = expTotal > 0 ? Math.min((expRealized / expTotal) * 100, 100) : 0;
  document.getElementById('barExpenseRealized').style.width = expPct + '%';

  const incRealized = Number(b.realizedIncome || 0);
  const incTotal = Number(b.totalExpectedIncome || 0);
  document.getElementById('txtIncomeComparison').textContent = `${UI.formatCurrency(incRealized)} / ${UI.formatCurrency(incTotal)}`;
  const incPct = incTotal > 0 ? Math.min((incRealized / incTotal) * 100, 100) : 0;
  document.getElementById('barIncomeRealized').style.width = incPct + '%';
}

async function saveBudget() {
  const pickerVal = document.getElementById('budgetMonthPicker').value;
  const [yearStr, monthStr] = pickerVal.split('-');

  const payload = {
    year: parseInt(yearStr),
    month: parseInt(monthStr),
    expectedIncome: parseFloat(document.getElementById('budgetExpectedIncome').value) || 0,
    expectedExtraIncome: parseFloat(document.getElementById('budgetExpectedExtra').value) || 0,
    fixedExpensesBudget: parseFloat(document.getElementById('budgetFixedExpenses').value) || 0,
    variableExpensesBudget: parseFloat(document.getElementById('budgetVariableExpenses').value) || 0,
    savingsTarget: parseFloat(document.getElementById('budgetSavingsTarget').value) || 0,
    salaryExpectedDate: document.getElementById('budgetSalaryDate').value || null
  };

  try {
    await API.saveBudget(payload);
    UI.showToast('Orcamento salvo com sucesso!', 'success');
    await loadBudget();
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
}

/* ==========================================================================
   Simulador de Compras ("Posso Comprar?")
   ========================================================================== */
function setupSimulator() {
  const simForm = document.getElementById('simForm');
  simForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const desc = document.getElementById('simDesc').value.trim();
    const amount = parseFloat(document.getElementById('simAmount').value);
    const paymentMethod = document.getElementById('simPaymentMethod').value;

    try {
      const res = await API.simulatePurchase({
        description: desc,
        amount: amount,
        paymentMethod: paymentMethod
      });

      lastSimulated = { description: desc, amount: amount, paymentMethod: paymentMethod };
      renderSimulationResult(res);
    } catch (err) {
      UI.showToast('Erro ao simular compra: ' + err.message, 'error');
    }
  });

  // Registrar despesa simulada
  document.getElementById('btnRegisterSimulated').addEventListener('click', async () => {
    if (!lastSimulated) return;

    try {
      await API.createExpense({
        description: lastSimulated.description,
        amount: lastSimulated.amount,
        expenseDate: new Date().toISOString().split('T')[0],
        paymentMethod: lastSimulated.paymentMethod,
        paymentStatus: 'PAGO',
        notes: 'Compra avaliada no Simulador'
      });

      UI.showToast('Compra registrada com sucesso como despesa!', 'success');
      setTimeout(() => {
        window.location.href = '/transactions.html?tab=expenses';
      }, 700);
    } catch (err) {
      UI.showToast('Erro ao salvar despesa: ' + err.message, 'error');
    }
  });
}

function renderSimulationResult(res) {
  const container = document.getElementById('simResultContainer');
  container.style.display = 'block';

  const badge = document.getElementById('simVerdictBadge');
  if (res.verdict === 'APROVADO') {
    badge.className = 'badge-status VERDE';
    badge.textContent = 'Compra Aprovada';
  } else if (res.verdict === 'ATENCAO') {
    badge.className = 'badge-status AMARELO';
    badge.textContent = 'Atencao ao Limite';
  } else {
    badge.className = 'badge-status VERMELHO';
    badge.textContent = 'Desaconselhado no Momento';
  }

  document.getElementById('simSummaryMsg').textContent = res.summaryMessage;
  document.getElementById('simDetailMsg').textContent = res.detailedExplanation;

  // Impactos
  const dailyEl = document.getElementById('simDailyImpact');
  if (res.excessOverDailyLimit > 0) {
    dailyEl.textContent = `Ultrapassa em ${UI.formatCurrency(res.excessOverDailyLimit)}`;
    dailyEl.style.color = '#b91c1c';
  } else {
    dailyEl.textContent = `Sobra ${UI.formatCurrency(res.remainingDailyAfterPurchase)} hoje`;
    dailyEl.style.color = '#047857';
  }

  const savEl = document.getElementById('simSavingsImpact');
  if (res.savingsCompromised) {
    savEl.textContent = 'Meta Comprometida';
    savEl.style.color = '#b91c1c';
  } else {
    savEl.textContent = 'Meta Preservada';
    savEl.style.color = '#047857';
  }

  const pendEl = document.getElementById('simPendingImpact');
  if (res.pendingBillsAtRisk) {
    pendEl.textContent = 'Contas Futuras em Risco';
    pendEl.style.color = '#b91c1c';
  } else {
    pendEl.textContent = 'Contas Futuras Seguras';
    pendEl.style.color = '#047857';
  }

  container.scrollIntoView({ behavior: 'smooth' });
}
