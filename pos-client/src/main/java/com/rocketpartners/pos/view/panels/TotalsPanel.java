package com.rocketpartners.pos.view.panels;

import com.rocketpartners.pos.view.model.TotalsRow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class TotalsPanel extends JPanel {

    private final JLabel subtotalValue  = new JLabel("$0.00");
    private final JLabel taxValue       = new JLabel("$0.00");
    private final JLabel totalValue     = new JLabel("$0.00");
    private final JButton totalButton   = new JButton("Total");

    public TotalsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createTitledBorder("Totals"));

        JPanel grid = new JPanel(new GridLayout(3, 2, 4, 4));
        grid.add(new JLabel("Subtotal:"));  grid.add(subtotalValue);
        grid.add(new JLabel("Tax:"));       grid.add(taxValue);
        grid.add(new JLabel("Total:"));     grid.add(totalValue);
        add(grid, BorderLayout.CENTER);

        totalButton.setEnabled(false);
        add(totalButton, BorderLayout.SOUTH);
    }

    public void addTotalListener(ActionListener listener) {
        totalButton.addActionListener(listener);
    }

    public void setTotalEnabled(boolean enabled) {
        totalButton.setEnabled(enabled);
    }

    public void refresh(TotalsRow row) {
        subtotalValue.setText(row.subtotal());
        taxValue.setText(row.taxAmount());
        totalValue.setText(row.total());
    }

    public void lock() {
        totalButton.setEnabled(false);
    }

    public void reset() {
        refresh(new com.rocketpartners.pos.view.model.TotalsRow("$0.00", "$0.00", "$0.00"));
        totalButton.setEnabled(false);
    }
}
