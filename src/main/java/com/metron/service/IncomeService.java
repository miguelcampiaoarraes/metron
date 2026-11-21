package com.metron.service;

import com.metron.dto.IncomeRequest;
import com.metron.dto.IncomeResponse;
import com.metron.exception.ResourceNotFoundException;
import com.metron.model.entity.Category;
import com.metron.model.entity.Income;
import com.metron.model.entity.User;
import com.metron.repository.CategoryRepository;
import com.metron.repository.IncomeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;

    public IncomeService(IncomeRepository incomeRepository,
                         CategoryRepository categoryRepository) {
        this.incomeRepository = incomeRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<IncomeResponse> findIncomes(User user, LocalDate startDate, LocalDate endDate,
                                            Long categoryId, String search, String sortBy) {
        List<Income> list;
        if (startDate != null && endDate != null) {
            list = incomeRepository.findByUserIdAndReceiptDateBetweenOrderByReceiptDateDesc(user.getId(), startDate, endDate);
        } else {
            list = incomeRepository.findByUserIdOrderByReceiptDateDesc(user.getId());
        }

        if (categoryId != null) {
            list = list.stream()
                    .filter(i -> i.getCategory() != null && i.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.isBlank()) {
            String lower = search.toLowerCase().trim();
            list = list.stream()
                    .filter(i -> i.getDescription().toLowerCase().contains(lower) ||
                            (i.getNotes() != null && i.getNotes().toLowerCase().contains(lower)))
                    .collect(Collectors.toList());
        }

        if ("amount_desc".equalsIgnoreCase(sortBy)) {
            list.sort(Comparator.comparing(Income::getAmount).reversed());
        } else if ("amount_asc".equalsIgnoreCase(sortBy)) {
            list.sort(Comparator.comparing(Income::getAmount));
        } else if ("date_asc".equalsIgnoreCase(sortBy)) {
            list.sort(Comparator.comparing(Income::getReceiptDate));
        } else {
            list.sort(Comparator.comparing(Income::getReceiptDate).reversed());
        }

        return list.stream().map(IncomeResponse::new).collect(Collectors.toList());
    }

    public IncomeResponse findById(User user, Long id) {
        Income income = incomeRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Receita não encontrada ou acesso não autorizado."));
        return new IncomeResponse(income);
    }

    @Transactional
    public IncomeResponse create(User user, IncomeRequest request) {
        Income income = new Income();
        income.setUser(user);
        income.setDescription(request.getDescription().trim());
        income.setAmount(request.getAmount());
        income.setReceiptDate(request.getReceiptDate());
        income.setIncomeType(request.getIncomeType());
        income.setReceiptStatus(request.getReceiptStatus());
        income.setNotes(request.getNotes());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId()).orElse(null);
            income.setCategory(category);
        }

        Income saved = incomeRepository.save(income);
        return new IncomeResponse(saved);
    }

    @Transactional
    public IncomeResponse update(User user, Long id, IncomeRequest request) {
        Income income = incomeRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Receita não encontrada ou acesso não autorizado."));

        income.setDescription(request.getDescription().trim());
        income.setAmount(request.getAmount());
        income.setReceiptDate(request.getReceiptDate());
        income.setIncomeType(request.getIncomeType());
        income.setReceiptStatus(request.getReceiptStatus());
        income.setNotes(request.getNotes());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId()).orElse(null);
            income.setCategory(category);
        } else {
            income.setCategory(null);
        }

        Income saved = incomeRepository.save(income);
        return new IncomeResponse(saved);
    }

    @Transactional
    public void delete(User user, Long id) {
        Income income = incomeRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Receita não encontrada ou acesso não autorizado."));
        incomeRepository.delete(income);
    }
}
