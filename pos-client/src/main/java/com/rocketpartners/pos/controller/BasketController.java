package com.rocketpartners.pos.controller;

import com.rocketpartners.pos.event.PosEvent;
import com.rocketpartners.pos.event.PosEventDispatcher;
import com.rocketpartners.pos.event.PosEventListener;
import com.rocketpartners.pos.model.LineItem;
import com.rocketpartners.pos.model.TransactionContext;
import com.rocketpartners.pos.model.TransactionStatus;
import com.rocketpartners.pos.pricebook.PricebookRepository;
import com.rocketpartners.pos.view.model.BasketRow;
import com.rocketpartners.pos.view.panels.BasketPanel;

import java.util.List;

public class BasketController implements PosEventListener {

    private final PricebookRepository pricebookRepository;
    private final PosEventDispatcher dispatcher;
    private final BasketPanel view;

    public BasketController(PricebookRepository pricebookRepository,
                            PosEventDispatcher dispatcher,
                            BasketPanel view) {
        this.pricebookRepository = pricebookRepository;
        this.dispatcher          = dispatcher;
        this.view                = view;

        attachListeners();
    }

    private void attachListeners() {
        BasketPanel.QUICK_ADD_ITEMS.forEach((label, upc) ->
            view.addQuickAddListener(upc, e -> addItem(upc))
        );

        view.addVoidLineListener(e -> {
            int selected = view.getSelectedRow();
            if (selected >= 0) voidLine(selected);
        });

        view.addVoidBasketListener(e -> voidBasket());

        view.addTableSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                view.setVoidLineEnabled(view.getSelectedRow() >= 0);
            }
        });
    }

    @Override
    public void onPosEvent(PosEvent event, Object payload) {
        if (event == PosEvent.BASKET_TOTALLED) {
            view.lock();
        } else if (event == PosEvent.TRANSACTION_RESET) {
            view.reset();
        }
    }

    public void addItem(String upc) {
        pricebookRepository.findByUpc(upc).ifPresentOrElse(
            product -> {
                List<LineItem> items = TransactionContext.get().getLineItems();
                items.stream()
                    .filter(li -> li.getProduct().getUpc().equals(upc))
                    .findFirst()
                    .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + 1),
                        () -> items.add(LineItem.builder().product(product).quantity(1).build())
                    );
                TransactionContext.get().setStatus(TransactionStatus.ACTIVE);
                view.refresh(toViewModels());
                dispatcher.dispatch(PosEvent.ITEM_ADDED, TransactionContext.get());
            },
            () -> view.showItemNotFound(upc)
        );
    }

    public void voidLine(int index) {
        List<LineItem> items = TransactionContext.get().getLineItems();
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            if (items.isEmpty()) TransactionContext.get().setStatus(TransactionStatus.IDLE);
            view.refresh(toViewModels());
            dispatcher.dispatch(PosEvent.ITEM_VOIDED, TransactionContext.get());
        }
    }

    public void voidBasket() {
        TransactionContext.get().getLineItems().clear();
        TransactionContext.get().setStatus(TransactionStatus.IDLE);
        view.refresh(toViewModels());
        dispatcher.dispatch(PosEvent.BASKET_VOIDED, TransactionContext.get());
    }

    private List<BasketRow> toViewModels() {
        return TransactionContext.get().getLineItems().stream()
            .map(item -> new BasketRow(
                item.getProduct().getName(),
                item.getQuantity(),
                String.format("$%.2f", item.getProduct().getPrice() * item.getQuantity())
            ))
            .toList();
    }

    public BasketPanel getView() {
        return view;
    }
}
