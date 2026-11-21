/**
 * METRON Reports & Goals Controller
 * Consolida Metas Financeiras, Avaliacao Semanal e Relatorio Mensal em uma unica pagina.
 */

let goalsCache = [];

document.addEventListener('DOMContentLoaded', async () => {
  if (!API.requireAuth()) return;
  renderNavbar('reports');

  setupTabs();
  setupGoalModals();

  // Seletor de mes padrao para o relatorio
  const repPicker = document.getElementById('reportMonthPicker');
  const now = new Date();
  repPicker.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
  repPicker.addEventListener('change', () => loadMonthlyReport());

  document.getElementById('btnExportPdf').addEventListener('click', () => exportPdf());

  // Cargas iniciais
  await loadGoals();
  await loadWeeklyData();
  await loadMonthlyReport();

  // Parametro ?tab=...
  const urlParams = new URLSearchParams(window.location.search);
  const tabParam = urlParams.get('tab');
  if (tabParam) {
    const map = {
      'goals': 'tab-goals',
      'weekly': 'tab-weekly',
      'monthly': 'tab-monthly'
    };
    if (map[tabParam]) switchTab(map[tabParam]);
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

  // Oculta/mostra botao de nova meta no cabecalho dependendo da aba
  const headerBtn = document.getElementById('goalsActionHeader');
  if (headerBtn) {
    headerBtn.style.display = targetId === 'tab-goals' ? 'block' : 'none';
  }
}

/* ==========================================================================
   ABA 1: Metas Financeiras
   ========================================================================== */
async function loadGoals() {
  const container = document.getElementById('goalsContainer');
  try {
    goalsCache = await API.getGoals();

    if (!goalsCache || goalsCache.length === 0) {
      container.innerHTML = `
        <div style="grid-column: 1 / -1;">
          <div class="empty-state">
            <div class="empty-title">Nenhuma meta financeira cadastrada</div>
            <div class="empty-desc">
              Comece agora mesmo a planejar seus sonhos. Clique no botao "+ Nova Meta de Economia" acima.
            </div>
          </div>
        </div>
      `;
      return;
    }

    container.innerHTML = goalsCache.map(g => {
      const isCompleted = g.status === 'CONCLUIDA' || g.progressPercentage >= 100;
      const pct = Number(g.progressPercentage || 0);

      return `
        <div class="card" style="display:flex; flex-direction:column; justify-content:space-between; margin-bottom:0;">
          <div>
            <div style="display:flex; justify-content:space-between; align-items:flex-start; gap:0.5rem; margin-bottom:0.75rem;">
              <div>
                <h3 style="font-size:1.25rem; font-weight:700; color:var(--color-navy); margin-bottom:0.25rem;">
                  ${g.title}
                </h3>
                ${g.category ? `<span class="badge-status AZUL">${g.category}</span>` : ''}
              </div>
              <span class="badge-status ${isCompleted ? 'VERDE' : 'AMARELO'}">
                ${isCompleted ? 'Concluida' : 'Em Andamento'}
              </span>
            </div>

            <div style="display:flex; justify-content:space-between; font-size:1.05rem; margin:1rem 0 0.5rem 0;">
              <div>
                <span style="font-size:0.85rem; color:var(--color-muted); display:block;">Acumulado:</span>
                <strong style="color:#047857; font-size:1.25rem;">${UI.formatCurrency(g.currentAmount)}</strong>
              </div>
              <div style="text-align:right;">
                <span style="font-size:0.85rem; color:var(--color-muted); display:block;">Objetivo:</span>
                <strong style="color:var(--color-navy); font-size:1.25rem;">${UI.formatCurrency(g.targetAmount)}</strong>
              </div>
            </div>

            <div class="spotlight-bar-bg" style="height:12px; margin:0.75rem 0;">
              <div class="spotlight-bar-fill" style="width: ${Math.min(pct, 100)}%; background:${isCompleted ? '#047857' : '#0d9488'};"></div>
            </div>

            <div style="display:flex; justify-content:space-between; font-size:0.9rem; color:var(--color-text-muted); margin-bottom:1rem;">
              <span><strong>${pct.toFixed(1)}%</strong> alcancado</span>
              <span>Faltam <strong>${UI.formatCurrency(g.remainingAmount)}</strong></span>
            </div>

            ${!isCompleted ? `
              <div style="background:#f8fafc; padding:0.75rem 1rem; border-radius:var(--radius-sm); border:1px solid var(--color-border); font-size:0.9rem; color:var(--color-slate); line-height:1.4; margin-bottom:1rem;">
                <strong>Poupanca recomendada:</strong> ${UI.formatCurrency(g.monthlySavingsNeeded)} por mes
                ${g.deadline ? `ate ${UI.formatDate(g.deadline)}` : ''}
              </div>
            ` : ''}
          </div>

          <div style="display:flex; justify-content:space-between; align-items:center; border-top:1px solid var(--color-border); padding-top:1rem; margin-top:0.5rem; gap:0.5rem; flex-wrap:wrap;">
            <div class="btn-group">
              <button class="btn btn-primary btn-sm" onclick="openDepositModal(${g.id})">
                + Guardar Dinheiro
              </button>
              <button class="btn btn-secondary btn-sm" onclick="editGoal(${g.id})">
                Editar
              </button>
            </div>
            <button class="btn btn-danger btn-sm" onclick="deleteGoal(${g.id})">
              Excluir
            </button>
          </div>
        </div>
      `;
    }).join('');

  } catch (err) {
    UI.showToast('Erro ao carregar metas: ' + err.message, 'error');
  }
}

function setupGoalModals() {
  const goalModal = document.getElementById('goalModal');
  document.getElementById('btnOpenGoalModal').addEventListener('click', () => {
    document.getElementById('goalModalTitle').textContent = 'Nova Meta de Economia';
    document.getElementById('goalForm').reset();
    document.getElementById('goalId').value = '';
    goalModal.classList.add('active');
  });

  const closeGoal = () => goalModal.classList.remove('active');
  document.getElementById('btnCloseGoalModal').addEventListener('click', closeGoal);
  document.getElementById('btnCancelGoal').addEventListener('click', closeGoal);

  document.getElementById('goalForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('goalId').value;

    const payload = {
      title: document.getElementById('goalTitle').value.trim(),
      targetAmount: parseFloat(document.getElementById('goalTarget').value),
      currentAmount: parseFloat(document.getElementById('goalCurrent').value) || 0,
      deadline: document.getElementById('goalDeadline').value || null,
      category: document.getElementById('goalCategory').value.trim() || null,
      notes: document.getElementById('goalNotes').value.trim() || null
    };

    try {
      if (id) {
        await API.updateGoal(id, payload);
        UI.showToast('Meta atualizada com sucesso!', 'success');
      } else {
        await API.createGoal(payload);
        UI.showToast('Meta criada com sucesso!', 'success');
      }
      closeGoal();
      await loadGoals();
    } catch (err) {
      UI.showToast(err.message, 'error');
    }
  });

  // Modal de Aporte
  const depModal = document.getElementById('depositModal');
  const closeDep = () => depModal.classList.remove('active');
  document.getElementById('btnCloseDepositModal').addEventListener('click', closeDep);
  document.getElementById('btnCancelDeposit').addEventListener('click', closeDep);

  document.getElementById('depositForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const goalId = document.getElementById('depositGoalId').value;
    const amount = parseFloat(document.getElementById('depositAmount').value);

    try {
      await API.depositGoal(goalId, amount);
      UI.showToast('Aporte registrado com sucesso!', 'success');
      closeDep();
      await loadGoals();
    } catch (err) {
      UI.showToast(err.message, 'error');
    }
  });
}

window.openDepositModal = function(id) {
  const goal = goalsCache.find(g => g.id === id);
  if (!goal) return;

  document.getElementById('depositGoalId').value = goal.id;
  document.getElementById('depositGoalTitle').textContent = `Meta: ${goal.title}`;
  document.getElementById('depositAmount').value = '';
  document.getElementById('depositModal').classList.add('active');
};

window.editGoal = function(id) {
  const goal = goalsCache.find(g => g.id === id);
  if (!goal) return;

  document.getElementById('goalModalTitle').textContent = 'Editar Meta';
  document.getElementById('goalId').value = goal.id;
  document.getElementById('goalTitle').value = goal.title;
  document.getElementById('goalTarget').value = goal.targetAmount;
  document.getElementById('goalCurrent').value = goal.currentAmount;
  document.getElementById('goalDeadline').value = goal.deadline || '';
  document.getElementById('goalCategory').value = goal.category || '';
  document.getElementById('goalNotes').value = goal.notes || '';

  document.getElementById('goalModal').classList.add('active');
};

window.deleteGoal = async function(id) {
  if (!confirm('Deseja realmente excluir esta meta financeira?')) return;
  try {
    await API.deleteGoal(id);
    UI.showToast('Meta excluida com sucesso!', 'success');
    await loadGoals();
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
};

/* ==========================================================================
   ABA 2: Avaliacao Semanal
   ========================================================================== */
async function loadWeeklyData() {
  if (!document.getElementById('dispWeeklyPeriod')) return;
  try {
    const d = await API.getWeeklyEvaluation();
    if (d.periodStart && d.periodEnd) {
      document.getElementById('dispWeeklyPeriod').textContent =
        `Auditoria do periodo: ${UI.formatDate(d.periodStart)} a ${UI.formatDate(d.periodEnd)}`;
    }

    document.getElementById('wkReceived').textContent = UI.formatCurrency(d.totalReceived);
    document.getElementById('wkSpent').textContent = UI.formatCurrency(d.totalSpent);
    document.getElementById('wkBalance').textContent = UI.formatCurrency(d.weekBalance);

    const growthSub = document.getElementById('wkGrowthSub');
    const growth = Number(d.growthPercentageVsPreviousWeek || 0);
    if (growth > 0) {
      growthSub.textContent = `Gastou ${growth.toFixed(1)}% a mais que na semana anterior`;
      growthSub.style.color = '#b91c1c';
    } else if (growth < 0) {
      growthSub.textContent = `Economizou ${Math.abs(growth).toFixed(1)}% em relacao a semana anterior`;
      growthSub.style.color = '#047857';
    } else {
      growthSub.textContent = 'Mesmo ritmo da semana anterior';
    }

    document.getElementById('wkTopCategory').textContent = d.topSpendingCategory || 'Nenhuma';
    document.getElementById('wkTopCategoryAmount').textContent = UI.formatCurrency(d.topSpendingCategoryAmount);

    document.getElementById('wkRecurringTotal').textContent = UI.formatCurrency(d.recurringExpensesTotal);
    const recList = document.getElementById('wkRecurringList');
    if (d.recurringExpensesList && d.recurringExpensesList.length > 0) {
      recList.innerHTML = d.recurringExpensesList.map(item => `<li>${item}</li>`).join('');
    } else {
      recList.innerHTML = '<li style="color:var(--color-muted);">Nenhuma assinatura ou despesa recorrente registrada nesta semana.</li>';
    }

    // Recomendacoes
    const recContainer = document.getElementById('wkRecommendationsContainer');
    if (d.recommendations && d.recommendations.length > 0) {
      recContainer.innerHTML = d.recommendations.map(r => `
        <div style="background:#f8fafc; border-left:4px solid var(--color-primary); padding:0.85rem 1.15rem; border-radius:var(--radius-sm); font-size:0.95rem; color:var(--color-navy); line-height:1.5;">
          ${r}
        </div>
      `).join('');
    } else {
      recContainer.innerHTML = '<p style="color:var(--color-muted);">Suas financas seguiram estaveis nesta semana. Continue registrando seus gastos.</p>';
    }
  } catch (err) {
    console.error('Erro na avaliacao semanal:', err);
  }
}

/* ==========================================================================
   ABA 3: Relatorio Mensal Completo
   ========================================================================== */
async function loadMonthlyReport() {
  const pickerVal = document.getElementById('reportMonthPicker').value;
  if (!pickerVal) return;

  const [yearStr, monthStr] = pickerVal.split('-');
  const year = parseInt(yearStr);
  const month = parseInt(monthStr);

  try {
    const d = await API.getMonthlyReport(year, month);

    document.getElementById('repIncome').textContent = UI.formatCurrency(d.totalIncome);
    document.getElementById('repExpense').textContent = UI.formatCurrency(d.totalExpenses);
    document.getElementById('repBalance').textContent = UI.formatCurrency(d.finalBalance);
    document.getElementById('repSavingsRate').textContent = UI.formatPercentage(d.savingsRatePercent);

    document.getElementById('repTopCatName').textContent = d.topSpendingCategory || 'Nenhuma';
    document.getElementById('repTopCatVal').textContent = `${UI.formatCurrency(d.topSpendingCategoryAmount)} (${UI.formatPercentage(d.topSpendingCategoryPercent)})`;

    const momEl = document.getElementById('repMoMDiff');
    const momDiff = Number(d.monthOverMonthExpenseDiffPercent || 0);
    momEl.textContent = (momDiff > 0 ? '+' : '') + momDiff.toFixed(1) + '%';
    momEl.style.color = momDiff > 0 ? '#b91c1c' : '#047857';

    document.getElementById('repGoalsStatus').textContent =
      `${d.achievedGoalsCount || 0} metas concluidas | ${d.pendingGoalsCount || 0} em andamento`;

    // Recomendacoes
    const recContainer = document.getElementById('repRecommendations');
    if (d.recommendationsForNextMonth && d.recommendationsForNextMonth.length > 0) {
      recContainer.innerHTML = d.recommendationsForNextMonth.map(r => `
        <div style="background:#f8fafc; border-left:4px solid var(--color-primary); padding:0.85rem 1.15rem; border-radius:var(--radius-sm); font-size:0.95rem; color:var(--color-navy); line-height:1.5;">
          ${r}
        </div>
      `).join('');
    } else {
      recContainer.innerHTML = '<p style="color:var(--color-muted);">Mantenha seus lancamentos em dia para gerar recomendacoes estrategicas para o proximo mes.</p>';
    }
  } catch (err) {
    console.error('Erro no relatorio mensal:', err);
  }
}

async function exportPdf() {
  const pickerVal = document.getElementById('reportMonthPicker').value;
  if (!pickerVal) return;

  const [yearStr, monthStr] = pickerVal.split('-');
  const year = parseInt(yearStr);
  const month = parseInt(monthStr);

  try {
    UI.showToast('Gerando documento PDF oficial...', 'info');
    await API.downloadMonthlyReportPdf(year, month);
    UI.showToast('Download do PDF concluido!', 'success');
  } catch (err) {
    UI.showToast('Erro ao exportar PDF: ' + err.message, 'error');
  }
}
