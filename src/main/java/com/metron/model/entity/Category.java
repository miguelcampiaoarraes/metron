package com.metron.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryType type;

    @Column(nullable = false, length = 20)
    private String color;

    @Column(nullable = false, length = 50)
    private String icon;

    @Column(name = "budget_limit", precision = 15, scale = 2)
    private BigDecimal budgetLimit = BigDecimal.ZERO;

    @Column(name = "is_system_default", nullable = false)
    private boolean isSystemDefault = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.budgetLimit == null) {
            this.budgetLimit = BigDecimal.ZERO;
        }
    }

    public Category() {}

    public Category(User user, String name, CategoryType type, String color, String icon, BigDecimal budgetLimit, boolean isSystemDefault) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.color = color;
        this.icon = icon;
        this.budgetLimit = budgetLimit != null ? budgetLimit : BigDecimal.ZERO;
        this.isSystemDefault = isSystemDefault;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public BigDecimal getBudgetLimit() {
        return budgetLimit;
    }

    public void setBudgetLimit(BigDecimal budgetLimit) {
        this.budgetLimit = budgetLimit;
    }

    public boolean isSystemDefault() {
        return isSystemDefault;
    }

    public void setSystemDefault(boolean systemDefault) {
        isSystemDefault = systemDefault;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
