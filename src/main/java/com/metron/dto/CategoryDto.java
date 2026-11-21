package com.metron.dto;

import com.metron.model.entity.Category;
import com.metron.model.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CategoryDto {

    private Long id;

    @NotBlank(message = "O nome da categoria é obrigatório.")
    private String name;

    @NotNull(message = "O tipo da categoria é obrigatório.")
    private CategoryType type;

    private String color = "#4A5568";
    private String icon = "tag";
    private BigDecimal budgetLimit = BigDecimal.ZERO;
    private boolean isSystemDefault = false;

    public CategoryDto() {}

    public CategoryDto(Category category) {
        if (category != null) {
            this.id = category.getId();
            this.name = category.getName();
            this.type = category.getType();
            this.color = category.getColor();
            this.icon = category.getIcon();
            this.budgetLimit = category.getBudgetLimit();
            this.isSystemDefault = category.isSystemDefault();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public BigDecimal getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(BigDecimal budgetLimit) { this.budgetLimit = budgetLimit; }

    public boolean isSystemDefault() { return isSystemDefault; }
    public void setSystemDefault(boolean systemDefault) { isSystemDefault = systemDefault; }
}
