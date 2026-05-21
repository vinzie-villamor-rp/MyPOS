package com.rocketpartners.pos.model;

import java.time.LocalDateTime;
import java.util.List;

public record Receipt(
    String transactionId,
    List<LineItem> lineItems,
    double subtotal,
    double taxAmount,
    double total,
    TenderType tenderType,
    double amountTendered,
    double changeDue,
    LocalDateTime timestamp
) {
    public static Receipt from(Transaction tx) {
        return new Receipt(
            tx.getTransactionId(),
            List.copyOf(tx.getLineItems()),
            tx.getSubtotal(),
            tx.getTaxAmount(),
            tx.getTotal(),
            tx.getTenderType(),
            tx.getAmountTendered(),
            tx.getChangeDue(),
            LocalDateTime.now()
        );
    }
}
