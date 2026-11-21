package com.metron.service;

import com.metron.dto.CategoryDto;
import com.metron.exception.BusinessValidationException;
import com.metron.exception.ResourceNotFoundException;
import com.metron.model.entity.Category;
import com.metron.model.entity.User;
import com.metron.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDto> findAllAvailable(User user) {
        return findAllAvailable(user, null);
    }

    public List<CategoryDto> findAllAvailable(User user, String type) {
        return categoryRepository.findAllAvailableForUser(user.getId())
                .stream()
                .filter(c -> {
                    if (type == null || type.isBlank()) return true;
                    if ("EXPENSE".equalsIgnoreCase(type)) {
                        return c.getType() == com.metron.model.entity.CategoryType.EXPENSE || c.getType() == com.metron.model.entity.CategoryType.BOTH;
                    }
                    if ("INCOME".equalsIgnoreCase(type)) {
                        return c.getType() == com.metron.model.entity.CategoryType.INCOME || c.getType() == com.metron.model.entity.CategoryType.BOTH;
                    }
                    return true;
                })
                .map(CategoryDto::new)
                .collect(Collectors.toList());
    }

    public List<CategoryDto> findUserCategories(User user) {
        return categoryRepository.findByUserId(user.getId())
                .stream()
                .map(CategoryDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDto create(User user, CategoryDto dto) {
        Category category = new Category();
        category.setUser(user);
        category.setName(dto.getName().trim());
        category.setType(dto.getType());
        category.setColor(dto.getColor() != null && !dto.getColor().isBlank() ? dto.getColor() : "#4A5568");
        category.setIcon(dto.getIcon() != null && !dto.getIcon().isBlank() ? dto.getIcon() : "tag");
        category.setBudgetLimit(dto.getBudgetLimit() != null ? dto.getBudgetLimit() : BigDecimal.ZERO);
        category.setSystemDefault(false);

        Category saved = categoryRepository.save(category);
        return new CategoryDto(saved);
    }

    @Transactional
    public CategoryDto update(User user, Long id, CategoryDto dto) {
        Category category = categoryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria personalizada não encontrada ou acesso não autorizado."));

        if (category.isSystemDefault()) {
            throw new BusinessValidationException("Categorias padrão do sistema não podem ser alteradas.");
        }

        category.setName(dto.getName().trim());
        category.setType(dto.getType());
        if (dto.getColor() != null && !dto.getColor().isBlank()) {
            category.setColor(dto.getColor());
        }
        if (dto.getIcon() != null && !dto.getIcon().isBlank()) {
            category.setIcon(dto.getIcon());
        }
        if (dto.getBudgetLimit() != null) {
            category.setBudgetLimit(dto.getBudgetLimit());
        }

        Category saved = categoryRepository.save(category);
        return new CategoryDto(saved);
    }

    @Transactional
    public void delete(User user, Long id) {
        Category category = categoryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada ou acesso não autorizado."));

        if (category.isSystemDefault()) {
            throw new BusinessValidationException("Categorias padrão do sistema não podem ser excluídas.");
        }

        categoryRepository.delete(category);
    }
}
