package com.rocketpartners.pos.view.panels;

import com.rocketpartners.pos.event.PosEvent;
import com.rocketpartners.pos.event.PosEventDispatcher;
import com.rocketpartners.pos.event.PosEventListener;
import com.rocketpartners.pos.model.Receipt;
import com.rocketpartners.pos.model.Transaction;
import com.rocketpartners.pos.model.TransactionContext;

import javax.swing.*;
import java.awt.*;

public class ReceiptDialog extends JDialog implements PosEventListener {

    private final PosEventDispatcher dispatcher;

    public ReceiptDialog(Frame owner, PosEventDispatcher dispatcher) {
        super(owner, "Receipt", true);
        this.dispatcher = dispatcher;
        setSize(400, 500);
        setLocationRelativeTo(owner);
    }

    @Override
    public void onPosEvent(PosEvent event, Object payload) {
        if (event == PosEvent.TRANSACTION_COMPLETE) {
            Receipt receipt = Receipt.from((Transaction) payload);
            printToConsole(receipt);
            showReceipt(receipt);
        }
    }

    private void showReceipt(Receipt receipt) {
        getContentPane().removeAll();
        setLayout(new BorderLayout(8, 8));

        JTextArea receiptText = new JTextArea(formatReceipt(receipt));
        receiptText.setEditable(false);
        receiptText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        add(new JScrollPane(receiptText), BorderLayout.CENTER);

        JButton newTransactionButton = new JButton("New Transaction");
        newTransactionButton.addActionListener(e -> {
            dispose();
            TransactionContext.reset();
            dispatcher.dispatch(PosEvent.TRANSACTION_RESET);
        });
        add(newTransactionButton, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }

    private String formatReceipt(Receipt receipt) {
        StringBuilder sb = new StringBuilder();
        sb.append("========== RECEIPT ==========\n");
        sb.append(String.format("ID: %s%n", receipt.transactionId()));
        sb.append(String.format("Time: %s%n", receipt.timestamp()));
        sb.append("-----------------------------\n");
        receipt.lineItems().forEach(li ->
            sb.append(String.format("%-20s x%d  $%.2f%n",
                li.getProduct().getName(),
                li.getQuantity(),
                li.getProduct().getPrice() * li.getQuantity()))
        );
        sb.append("-----------------------------\n");
        sb.append(String.format("Subtotal:  $%.2f%n", receipt.subtotal()));
        sb.append(String.format("Tax:       $%.2f%n", receipt.taxAmount()));
        sb.append(String.format("Total:     $%.2f%n", receipt.total()));
        sb.append("-----------------------------\n");
        sb.append(String.format("Tender:    %s%n", receipt.tenderType()));
        sb.append(String.format("Tendered:  $%.2f%n", receipt.amountTendered()));
        sb.append(String.format("Change:    $%.2f%n", receipt.changeDue()));
        sb.append("=============================\n");
        return sb.toString();
    }

    private void printToConsole(Receipt receipt) {
        System.out.println(formatReceipt(receipt));
    }
}
