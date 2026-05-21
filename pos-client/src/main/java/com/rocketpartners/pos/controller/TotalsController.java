package com.rocketpartners.pos.controller;

import com.rocketpartners.pos.event.PosEvent;
import com.rocketpartners.pos.event.PosEventDispatcher;
import com.rocketpartners.pos.event.PosEventListener;
import com.rocketpartners.pos.model.TransactionContext;
import com.rocketpartners.pos.model.TransactionStatus;
import com.rocketpartners.pos.view.model.TotalsRow;
import com.rocketpartners.pos.view.panels.TotalsPanel;

public class TotalsController implements PosEventListener {

    static final double TAX_RATE = 0.08;

    private final PosEventDispatcher dispatcher;
    private final TotalsPanel view;

    public TotalsController(PosEventDispatcher dispatcher, TotalsPanel view) {
        this.dispatcher = dispatcher;
        this.view       = view;

        attachListeners();
    }

    private void attachListeners() {
        view.addTotalListener(e -> total());
    }

    @Override
    public void onPosEvent(PosEvent event, Object payload) {
        if (event == PosEvent.ITEM_ADDED || event == PosEvent.ITEM_VOIDED || event == PosEvent.BASKET_VOIDED) {
            syncButtonState();
        } else if (event == PosEvent.TRANSACTION_RESET) {
            view.reset();
        }
    }

    public void total() {
        double subtotal  = TransactionContext.get().getLineItems().stream()
            .mapToDouble(li -> li.getProduct().getPrice() * li.getQuantity())
            .sum();
        double taxAmount = subtotal * TAX_RATE;
        double total     = subtotal + taxAmount;

        TransactionContext.get().setSubtotal(subtotal);
        TransactionContext.get().setTaxAmount(taxAmount);
        TransactionContext.get().setTotal(total);
        TransactionContext.get().setStatus(TransactionStatus.TOTALLED);

        view.refresh(new TotalsRow(
            String.format("$%.2f", subtotal),
            String.format("$%.2f", taxAmount),
            String.format("$%.2f", total)
        ));
        view.lock();
        dispatcher.dispatch(PosEvent.BASKET_TOTALLED, TransactionContext.get());
    }

    private void syncButtonState() {
        view.setTotalEnabled(TransactionContext.get().getStatus() == TransactionStatus.ACTIVE);
    }

    public TotalsPanel getView() {
        return view;
    }
}
