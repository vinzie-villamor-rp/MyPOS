package com.rocketpartners.pos.controller;

import com.rocketpartners.pos.event.PosEvent;
import com.rocketpartners.pos.event.PosEventDispatcher;
import com.rocketpartners.pos.event.PosEventListener;
import com.rocketpartners.pos.model.TenderType;
import com.rocketpartners.pos.model.TransactionContext;
import com.rocketpartners.pos.model.TransactionStatus;
import com.rocketpartners.pos.view.model.TenderRow;
import com.rocketpartners.pos.view.panels.TenderPanel;

public class TenderController implements PosEventListener {

    private final PosEventDispatcher dispatcher;
    private final TenderPanel view;

    public TenderController(PosEventDispatcher dispatcher, TenderPanel view) {
        this.dispatcher = dispatcher;
        this.view       = view;

        attachListeners();
    }

    private void attachListeners() {
        view.addCashListener(e -> tenderCash());
        view.addNextDollarListener(e -> tenderNextDollar());
        view.addDebitListener(e -> tenderNonCash(TenderType.DEBIT));
        view.addCreditListener(e -> tenderNonCash(TenderType.CREDIT));
        view.addCheckListener(e -> tenderNonCash(TenderType.CHECK));
        view.addMobileListener(e -> tenderNonCash(TenderType.MOBILE_PAYMENT));
        view.addGiftCardListener(e -> tenderNonCash(TenderType.GIFT_CARD));
    }

    @Override
    public void onPosEvent(PosEvent event, Object payload) {
        if (event == PosEvent.BASKET_TOTALLED) {
            view.setAllEnabled(true);
        } else if (event == PosEvent.TRANSACTION_RESET) {
            view.reset();
        }
    }

    private void tenderCash() {
        String input = view.getCashInput();
        double amountTendered;
        try {
            amountTendered = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            view.setCashInputVisible(true);
            return;
        }

        double total = TransactionContext.get().getTotal();
        if (amountTendered < total) {
            view.setCashInputVisible(true);
            return;
        }

        double changeDue = amountTendered - total;
        complete(TenderType.CASH, amountTendered, changeDue, true);
    }

    private void tenderNextDollar() {
        double total          = TransactionContext.get().getTotal();
        double amountTendered = Math.ceil(total);
        double changeDue      = amountTendered - total;
        complete(TenderType.CASH, amountTendered, changeDue, true);
    }

    private void tenderNonCash(TenderType type) {
        double total = TransactionContext.get().getTotal();
        complete(type, total, 0.0, false);
    }

    private void complete(TenderType type, double amountTendered, double changeDue, boolean showChange) {
        TransactionContext.get().setTenderType(type);
        TransactionContext.get().setAmountTendered(amountTendered);
        TransactionContext.get().setChangeDue(changeDue);
        TransactionContext.get().setStatus(TransactionStatus.TENDERED);

        view.setCashInputVisible(showChange);
        view.refresh(new TenderRow(
            String.format("$%.2f", amountTendered),
            String.format("$%.2f", changeDue)
        ));
        view.lock();
        dispatcher.dispatch(PosEvent.TRANSACTION_COMPLETE, TransactionContext.get());
    }

    public TenderPanel getView() {
        return view;
    }
}
