package com.metron.service;

import com.metron.dto.ExpenseRequest;
import com.metron.dto.ExpenseResponse;
import com.metron.exception.ResourceNotFoundException;
import com.metron.model.entity.Category;
import com.metron.model.entity.Expense;
import com.metron.model.entity.User;
import com.metron.repository.CategoryRepository;
import com.metron.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ExpenseResponse> findExpenses(User user, LocalDate startDate, LocalDate endDate,
                                              Long categoryId, String search, String sortBy) {
        List<Expense> list;
        if (startDate != null && endDate != null) {
            list = expenseRepository.findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(user.getId(), startDate, endDate);
        } else {
            list = expenseRepository.findByUserIdOrderByExpenseDateDesc(user.getId());
        }

        // Filter by category
        if (categoryId != null) {
            list = list.stream()
                    .filter(e -> e.getCategory() != null && e.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        // Filter by search text
        if (search != null && !search.isBlank()) {
            String lower = search.toLowerCase().trim();
            list = list.stream()
                    .filter(e -> e.getDescription().toLowerCase().contains(lower) ||
                            (e.getNotes() != null && e.getNotes().toLowerCase().contains(lower)))
                    .collect(Collectors.toList());
        }

        // Sort
        if ("amount_desc".equalsIgnoreCase(sortBy)) {
            list.sort(Comparator.comparing(Expense::getAmount).reversed());
        } else if ("amount_asc".equalsIgnoreCase(sortBy)) {
            list.sort(Comparator.comparing(Expense::getAmount));
        } else if ("date_asc".equalsIgnoreCase(sortBy)) {
            list.sort(Comparator.comparing(Expense::getExpenseDate));
        } else {
            list.sort(Comparator.comparing(Expense::getExpenseDate).reversed());
        }

        return list.stream().map(ExpenseResponse::new).collect(Collectors.toList());
    }

    public ExpenseResponse findById(User user, Long id) {
        Expense expense = expenseRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada ou acesso não autorizado."));
        return new ExpenseResponse(expense);
    }

    @Transactional
    public ExpenseResponse create(User user, ExpenseRequest request) {
        Expense expense = new Expense();
        expense.setUser(user);
        expense.setDescription(request.getDescription().trim());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setPaymentStatus(request.getPaymentStatus());
        expense.setRecurring(request.isRecurring());
        expense.setNotes(request.getNotes());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElse(null);
            expense.setCategory(category);
        }

        Expense saved = expenseRepository.save(expense);
        return new ExpenseResponse(saved);
    }

    @Transactional
    public ExpenseResponse update(User user, Long id, ExpenseRequest request) {
        Expense expense = expenseRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada ou acesso não autorizado."));

        expense.setDescription(request.getDescription().trim());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setPaymentStatus(request.getPaymentStatus());
        expense.setRecurring(request.isRecurring());
        expense.setNotes(request.getNotes());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId()).orElse(null);
            expense.setCategory(category);
        } else {
            expense.setCategory(null);
        }

        Expense saved = expenseRepository.save(expense);
        return new ExpenseResponse(saved);
    }

    @Transactional
    public void delete(User user, Long id) {
        Expense expense = expenseRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada ou acesso não autorizado."));
        expenseRepository.delete(expense);
    }
}
