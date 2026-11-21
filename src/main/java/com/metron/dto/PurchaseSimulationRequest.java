package com.metron.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PurchaseSimulationRequest {

    @NotBlank(message = "A descrição da compra pretendida é obrigatória.")
    private String description;

    @NotNull(message = "O valor da compra é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
    private BigDecimal amount;

    private Long categoryId;
    private String paymentMethod = "PIX";

    public PurchaseSimulationRequest() {}

    public PurchaseSimulationRequest(String description, BigDecimal amount) {
        this.description = description;
        this.amount = amount;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
