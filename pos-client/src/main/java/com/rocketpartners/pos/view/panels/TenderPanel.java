package com.rocketpartners.pos.view.panels;

import com.rocketpartners.pos.view.model.TenderRow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class TenderPanel extends JPanel {

    private final JButton cashButton         = new JButton("Pay Cash");
    private final JButton nextDollarButton   = new JButton("Next Dollar");
    private final JButton debitButton        = new JButton("Pay Debit");
    private final JButton creditButton       = new JButton("Pay Credit");
    private final JButton checkButton        = new JButton("Pay Check");
    private final JButton mobileButton       = new JButton("Pay Mobile");
    private final JButton giftCardButton     = new JButton("Pay Gift Card");

    private final JTextField cashInputField  = new JTextField(8);
    private final JLabel changeDueLabel      = new JLabel("Change: $0.00");

    public TenderPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createTitledBorder("Tender"));

        JPanel buttonGrid = new JPanel(new GridLayout(4, 2, 4, 4));
        buttonGrid.add(cashButton);
        buttonGrid.add(nextDollarButton);
        buttonGrid.add(debitButton);
        buttonGrid.add(creditButton);
        buttonGrid.add(checkButton);
        buttonGrid.add(mobileButton);
        buttonGrid.add(giftCardButton);
        add(buttonGrid, BorderLayout.CENTER);

        JPanel cashPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        cashPanel.add(new JLabel("Cash Tendered:"));
        cashPanel.add(cashInputField);
        cashPanel.add(changeDueLabel);
        add(cashPanel, BorderLayout.SOUTH);

        setAllEnabled(false);
        setCashInputVisible(false);
    }

    // --- Listener registration ---

    public void addCashListener(ActionListener listener) {
        cashButton.addActionListener(listener);
    }

    public void addNextDollarListener(ActionListener listener) {
        nextDollarButton.addActionListener(listener);
    }

    public void addDebitListener(ActionListener listener) {
        debitButton.addActionListener(listener);
    }

    public void addCreditListener(ActionListener listener) {
        creditButton.addActionListener(listener);
    }

    public void addCheckListener(ActionListener listener) {
        checkButton.addActionListener(listener);
    }

    public void addMobileListener(ActionListener listener) {
        mobileButton.addActionListener(listener);
    }

    public void addGiftCardListener(ActionListener listener) {
        giftCardButton.addActionListener(listener);
    }

    // --- View state ---

    public String getCashInput() {
        return cashInputField.getText().trim();
    }

    public void setCashInputVisible(boolean visible) {
        cashInputField.setVisible(visible);
        changeDueLabel.setVisible(visible);
    }

    public void refresh(TenderRow row) {
        changeDueLabel.setText("Change: " + row.changeDue());
    }

    public void setAllEnabled(boolean enabled) {
        cashButton.setEnabled(enabled);
        nextDollarButton.setEnabled(enabled);
        debitButton.setEnabled(enabled);
        creditButton.setEnabled(enabled);
        checkButton.setEnabled(enabled);
        mobileButton.setEnabled(enabled);
        giftCardButton.setEnabled(enabled);
    }

    public void lock() {
        setAllEnabled(false);
        cashInputField.setEnabled(false);
    }
}
