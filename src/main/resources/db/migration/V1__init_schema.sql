-- V1__init_schema.sql: Initial Schema for METRON

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    monthly_income NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    current_bank_balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    savings_goal_monthly NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    salary_day INT NOT NULL DEFAULT 5,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(80) NOT NULL,
    type VARCHAR(20) NOT NULL,
    color VARCHAR(20) NOT NULL,
    icon VARCHAR(50) NOT NULL,
    budget_limit NUMERIC(15, 2) DEFAULT 0.00,
    is_system_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    description VARCHAR(200) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    expense_date DATE NOT NULL,
    payment_method VARCHAR(40) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE incomes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    description VARCHAR(200) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    receipt_date DATE NOT NULL,
    income_type VARCHAR(40) NOT NULL,
    receipt_status VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE budgets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    budget_month INT NOT NULL,
    budget_year INT NOT NULL,
    expected_income NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    expected_extra_income NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    fixed_expenses_budget NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    variable_expenses_budget NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    savings_target NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    salary_expected_date DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_user_month_year UNIQUE (user_id, budget_month, budget_year)
);

CREATE TABLE financial_goals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    target_amount NUMERIC(15, 2) NOT NULL,
    current_amount NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    deadline DATE,
    category VARCHAR(60),
    status VARCHAR(20) NOT NULL DEFAULT 'EM_ANDAMENTO',
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

-- Indexes for performance
CREATE INDEX idx_expenses_user_date ON expenses(user_id, expense_date);
CREATE INDEX idx_expenses_user_status ON expenses(user_id, payment_status);
CREATE INDEX idx_incomes_user_date ON incomes(user_id, receipt_date);
CREATE INDEX idx_incomes_user_status ON incomes(user_id, receipt_status);
CREATE INDEX idx_budgets_user_period ON budgets(user_id, budget_year, budget_month);
CREATE INDEX idx_goals_user ON financial_goals(user_id);
CREATE INDEX idx_categories_user ON categories(user_id);

-- Default system categories (user_id IS NULL)
INSERT INTO categories (user_id, name, type, color, icon, budget_limit, is_system_default, created_at) VALUES
(NULL, 'Alimentação', 'EXPENSE', '#E53E3E', 'utensils', 0.00, TRUE, CURRENT_TIMESTAMP),
(NULL, 'Transporte', 'EXPENSE', '#DD6B20', 'car', 0.00, TRUE, CURRENT_TIMESTAMP),
(NULL, 'Moradia', 'EXPENSE', '#3182CE', 'home', 0.00, TRUE, CURRENT_TIMESTAMP),
(NULL, 'Saúde', 'EXPENSE', '#38A169', 'heart-pulse', 0.00, TRUE, CURRENT_TIMESTAMP),
(NULL, 'Educação', 'EXPENSE', '#805AD5', 'graduation-cap', 0.00, TRUE, CURRENT_TIMESTAMP),
(NULL, 'Roupas', 'EXPENSE', '#D53F8C', 'shirt', 0.00, TRUE, CURRENT_TIMESTAMP),
(NULL, 'Lazer', 'EXPENSE', '#319795', 'gamepad', 0.00, TRUE, CURRENT_TIMESTAMP),
(NULL, 'Assinaturas', 'EXPENSE', '#4A5568', 'repeat', 0.00, TRUE, CURRENT_TIMESTAMP),
(NULL, 'Compras pessoais', 'EXPENSE', '#D69E2E', 'shopping-bag', 0.00, TRUE, CURRENT_TIMESTAMP),
(NULL, 'Contas e serviços', 'EXPENSE', '#C53030', 'file-invoice-dollar', 0.00, TRUE, CURRENT_TIMESTAMP),
(NULL, 'Outros', 'BOTH', '#718096', 'asterisk', 0.00, TRUE, CURRENT_TIMESTAMP),
(NULL, 'Salário', 'INCOME', '#38A169', 'money-bill-wave', 0.00, TRUE, CURRENT_TIMESTAMP),
(NULL, 'Benefício', 'INCOME', '#3182CE', 'award', 0.00, TRUE, CURRENT_TIMESTAMP),
(NULL, 'Renda extra', 'INCOME', '#805AD5', 'coins', 0.00, TRUE, CURRENT_TIMESTAMP),
(NULL, 'Investimentos', 'INCOME', '#D69E2E', 'chart-line', 0.00, TRUE, CURRENT_TIMESTAMP);
