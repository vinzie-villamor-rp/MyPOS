package com.rocketpartners.pos.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class Transaction {

    @Builder.Default
    private String transactionId = UUID.randomUUID().toString();

    @Builder.Default
    private List<LineItem> lineItems = new ArrayList<>();

    @Builder.Default
    private TransactionStatus status = TransactionStatus.IDLE;

    private double subtotal;
    private double taxAmount;
    private double total;
    private TenderType tenderType;
    private double amountTendered;
    private double changeDue;
    private LocalDateTime timestamp;
}
